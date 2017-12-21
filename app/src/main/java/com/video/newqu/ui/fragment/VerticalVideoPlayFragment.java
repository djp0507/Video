package com.video.newqu.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.google.gson.Gson;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.VerticalPagerAdapter;
import com.video.newqu.base.BaseVideoPlayFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.TopicVideoList;
import com.video.newqu.bean.VideoEventMessage;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentVideoPlayListBinding;
import com.video.newqu.event.VerticalPlayMessageEvent;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.contract.SlideVideoPlayerContract;
import com.video.newqu.ui.pager.VerticalPlayeViewPager;
import com.video.newqu.ui.presenter.SlideVideoPlayerPresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.widget.VerticalViewPager;
import com.xinqu.videoplayer.full.WindowVideoPlayer;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * TinyHung@Outlook.com
 * 2017/12/5.
 * 视频播放竖直滑动列表
 */

public class VerticalVideoPlayFragment extends BaseVideoPlayFragment<FragmentVideoPlayListBinding> implements SlideVideoPlayerContract.View {

    public static final String TAG=VerticalVideoPlayFragment.class.getSimpleName();
    private int mItemPoistion;
    private int mPage;
    private String mDataJson;
    private SlideVideoPlayerPresenter mSlideVideoPlayerPresenter;//数据加载
    private int listSize;
    private PlayListVerticalPagerAdapter mVerticalPagerAdapter;
    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList;
    private Map<Integer,VerticalPlayeViewPager> playerViews=new HashMap<>();//存放界面View的集合
    private int mFragmentType;
    private String mAuthorID;
    private WeakReference<VerticalVideoPlayActivity> mActivityWeakReference;
    private String mTopic;
    private Handler mHandler;
    private boolean first=true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        VerticalVideoPlayActivity playActivity= (VerticalVideoPlayActivity) context;
        mActivityWeakReference = new WeakReference<>(playActivity);
    }

    public static VerticalVideoPlayFragment newInstance(String dataJson, int fragmentType, String authorID, int itemPistion, int page,String topic) {
        VerticalVideoPlayFragment fragment=new VerticalVideoPlayFragment();
        Bundle bundle=new Bundle();
        bundle.putInt(Constant.KEY_FRAGMENT_TYPE,fragmentType);
        bundle.putString(Constant.KEY_JSON,dataJson);
        bundle.putString(Constant.KEY_TOPIC,topic);
        bundle.putString(Constant.KEY_AUTHOE_ID,authorID);
        bundle.putInt(Constant.KEY_POISTION,itemPistion);
        bundle.putInt(Constant.KEY_PAGE,page);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if(null!=arguments){
            mDataJson = arguments.getString(Constant.KEY_JSON);
            mTopic = arguments.getString(Constant.KEY_TOPIC);
            mAuthorID = arguments.getString(Constant.KEY_AUTHOE_ID);
            mItemPoistion = arguments.getInt(Constant.KEY_POISTION,0);
            mPage = arguments.getInt(Constant.KEY_PAGE,0);
            mFragmentType = arguments.getInt(Constant.KEY_FRAGMENT_TYPE,2);
        }
    }

    @Override
    protected void initViews() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_video_play_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSlideVideoPlayerPresenter = new SlideVideoPlayerPresenter(getActivity());
        mSlideVideoPlayerPresenter.attachView(this);
        if(!TextUtils.isEmpty(mDataJson)){
            try{
                mListsBeanList = new Gson().fromJson(mDataJson, FollowVideoList.class).getData().getLists();
                if(null!= mListsBeanList && mListsBeanList.size()>0){
                    listSize= mListsBeanList.size();//实时记录当前已经加载的视频数量，用来加载更多判断
                    //初始化界面适配器
                    mVerticalPagerAdapter = new PlayListVerticalPagerAdapter();
                    bindingView.verticalViewPager.setOnPageChangeListener(new VerticalViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }

                        @Override
                        public void onPageSelected(int position) {
                            Logger.d(TAG,"position="+position);
                            releaseDanmaku(mItemPoistion);//清空上一个播放器中的弹幕和资源
                            mItemPoistion=position;
                            //释放VideoPlayer
                            WindowVideoPlayer.releaseAllVideos();
                            sendUpdataUserDataMessage(mItemPoistion);
                            if(null!=mHandler){
                                mHandler.removeMessages(0);//创建新的任务前，取消所有的定时任务
                            }
                            waitPlayVideo(500,mItemPoistion);//创建延时播放任务
                            //加载更多
                            if(mItemPoistion>=(listSize-1)){
                                loadVideoList();
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {

                        }
                    });

                    bindingView.verticalViewPager.setOffscreenPageLimit(1);
                    bindingView.verticalViewPager.setAdapter(mVerticalPagerAdapter);
                    bindingView.verticalViewPager.setCurrentItem(mItemPoistion);
                    waitPlayVideo(800,mItemPoistion);
                }
            }catch (Exception e){
                ToastUtils.shoCenterToast("数据异常："+e.getMessage());
            }
        }else{
            ToastUtils.shoCenterToast("播放失败!");
        }
    }


    /**
     * 设置延缓任务
     * @param misTime 需要延缓多久
     * @param waitPoistion 延缓播放视频的目标Poistion
     */
    private void waitPlayVideo(long misTime,int waitPoistion) {
        if(null==mHandler) mHandler=new Handler();
        mHandler.postAtTime(new PlayVideoRunnable(waitPoistion), SystemClock.uptimeMillis()+misTime);//设置延缓任务
    }

    /**
     * 这个Runnable用来执行延缓任务，waitPlayPoistin是记录要执行的延缓任务，只有当前显示的viewPager cureenItem与当时提交的cureenItem相等才允许播放
     */
    private class PlayVideoRunnable implements Runnable{
        private final int waitPlayPoistin;

        public PlayVideoRunnable(int waitPoistion){
            this.waitPlayPoistin=waitPoistion;
        }

        @Override
        public void run() {
            if(this.waitPlayPoistin!=mItemPoistion){
                return;
            }
            if(first){
                first=false;
                sendUpdataUserDataMessage(waitPlayPoistin);
            }
            //WIFI下自动播放
            if (1 == Utils.getNetworkType() && ConfigSet.getInstance().isWifiAuthPlayer()) {
                playerVideo(waitPlayPoistin);
            //用户允许了移动网络下自动播放
            } else if (2 == Utils.getNetworkType() && ConfigSet.getInstance().isMobilePlayer()) {
                playerVideo(waitPlayPoistin);
            }else{
                //有网络但用户未开启自动播放
                if(Utils.isCheckNetwork()){
                    playerVideo(waitPlayPoistin);
                }
            }
        }
    }


    /**
     * 这个方法由最外面的Activity调用
     * @param flag true：onResume(); false:onPause();
     */
    public void onChildResume(boolean flag){
        if(flag){
            onChildononResume(mItemPoistion);
        }else{
            onChildonPause(mItemPoistion);
        }
    }
    /**
     * 播放视频
     */
    private void playerVideo(int itemPoistion) {
        if(-1!=itemPoistion&&null!=playerViews&&playerViews.size()>0){
            Iterator<Map.Entry<Integer, VerticalPlayeViewPager>> iterator = playerViews.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, VerticalPlayeViewPager> next = iterator.next();
                if(itemPoistion==next.getKey()){
                    VerticalPlayeViewPager playerTempPager = next.getValue();
                    if(null!=playerTempPager){
                        SharedPreferencesUtil.getInstance().putInt(Constant.GRADE_PLAYER_VIDEO_COUNT,(SharedPreferencesUtil.getInstance().getInt(Constant.GRADE_PLAYER_VIDEO_COUNT)+1));
                        playerTempPager.onPlaye();
                    }
                }
            }
        }
    }

    /**
     * 销毁弹幕资源
     */
    private void releaseDanmaku(int itemPoistion) {
        if(-1!=itemPoistion&&null!=playerViews&&playerViews.size()>0){
            Iterator<Map.Entry<Integer, VerticalPlayeViewPager>> iterator = playerViews.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, VerticalPlayeViewPager> next = iterator.next();
                if(itemPoistion==next.getKey()){
                    VerticalPlayeViewPager playerTempPager = next.getValue();
                    if(null!=playerTempPager){
                        playerTempPager.releaseDanmaku();
                    }
                }
            }
        }
    }

    /**
     * 生命周期onChildDestroyView
     * @param itemPoistion
     */
    private void onChildDestroyView(int itemPoistion) {
        if(-1!=itemPoistion&&null!=playerViews&&playerViews.size()>0){
            Iterator<Map.Entry<Integer, VerticalPlayeViewPager>> iterator = playerViews.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, VerticalPlayeViewPager> next = iterator.next();
                if(itemPoistion==next.getKey()){
                    VerticalPlayeViewPager playerTempPager = next.getValue();
                    if(null!=playerTempPager){
                        playerTempPager.onChildDestroy();
                    }
                }
            }
        }
    }

    /**
     * 生命周期onChildononResume
     * @param itemPoistion
     */
    private void onChildononResume(int itemPoistion) {
        if(-1!=itemPoistion&&null!=playerViews&&playerViews.size()>0){
            Iterator<Map.Entry<Integer, VerticalPlayeViewPager>> iterator = playerViews.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, VerticalPlayeViewPager> next = iterator.next();
                if(itemPoistion==next.getKey()){
                    VerticalPlayeViewPager playerTempPager = next.getValue();
                    if(null!=playerTempPager){
                        playerTempPager.onChildResume();
                    }
                }
            }
        }
    }

    /**
     * 生命周期onChildonPause
     * @param itemPoistion
     */
    private void onChildonPause(int itemPoistion) {
        if(-1!=itemPoistion&&null!=playerViews&&playerViews.size()>0){
            Iterator<Map.Entry<Integer, VerticalPlayeViewPager>> iterator = playerViews.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Integer, VerticalPlayeViewPager> next = iterator.next();
                if(itemPoistion==next.getKey()){
                    VerticalPlayeViewPager playerTempPager = next.getValue();
                    if(null!=playerTempPager){
                        playerTempPager.onChildonPause();
                    }
                }
            }
        }
    }


    /**
     * 及时发送消息给用户信息界面，更新用户ID，和还原所有数据位初始状态
     * @param itemPoistion
     */
    private void sendUpdataUserDataMessage(int itemPoistion) {
        if(null!=mListsBeanList&&mListsBeanList.size()>0){
            FollowVideoList.DataBean.ListsBean listsBean = mListsBeanList.get(itemPoistion);
            if(null!=listsBean){
                VerticalPlayMessageEvent messageEvent=new VerticalPlayMessageEvent();
                messageEvent.setAuthorID(listsBean.getUser_id());
                messageEvent.setPoistion(itemPoistion);
                messageEvent.setUserCover(listsBean.getLogo());
                messageEvent.setUserName(listsBean.getNickname());
                EventBus.getDefault().post(messageEvent);
            }
        }
    }

    /**
     * 加载更多视频列表数据
     */
    private void loadVideoList() {
        if(null!=mSlideVideoPlayerPresenter&&!mSlideVideoPlayerPresenter.isLoading()){
            mPage++;
            switch (mFragmentType) {
                //关注
                case Constant.FRAGMENT_TYPE_FOLLOW:
                    mSlideVideoPlayerPresenter.getFollowUserVideoList(mAuthorID,mPage);
                    break;
                //热门
                case Constant.FRAGMENT_TYPE_HOT:
                    mSlideVideoPlayerPresenter.getHotVideoList(mAuthorID,mPage);
                    break;
                //发布的作品
                case Constant.FRAGMENT_TYPE_WORKS:
                    mSlideVideoPlayerPresenter.getUserUpLoadVideoList(mAuthorID, VideoApplication.getLoginUserID(),mPage);
                    break;
                //收藏
                case Constant.FRAGMENT_TYPE_LIKE:
                    mSlideVideoPlayerPresenter.getLikeVideoList(mAuthorID,mPage);
                    break;
                //用户中心
                case Constant.FRAGMENT_TYPE_AUTHOE_CORE:
                    mSlideVideoPlayerPresenter.getUserUpLoadVideoList(mAuthorID,VideoApplication.getLoginUserID(),mPage);
                    break;
                //话题下的列表
                case Constant.FRAGMENT_TYPE_TOPIC_LIST:
                    mSlideVideoPlayerPresenter.getTopicVideoList(mAuthorID,mTopic,mPage+"");
                    break;
                //首页的话题
                case Constant.FRAGMENT_TYPE_HOME_TOPIC:
                    mSlideVideoPlayerPresenter.getTopicVideoList(mAuthorID,mTopic,mPage+"");
                    break;
            }
        }
    }

    /**
     * 订阅刷新源数据的Event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VideoEventMessage event) {
        //刷新
        if(null!=event&&TextUtils.equals(Constant.EVENT_VIDEO_PLAY_PAGE_UPDATA,event.getMessage())){
            FollowVideoList.DataBean.ListsBean mewListsBean = event.getListsBean();
            if(null!=mewListsBean){
                if(null!=mListsBeanList&&mListsBeanList.size()>0){
                    int poistion = event.getPoistion();
                    mListsBeanList.remove(poistion);
                    mListsBeanList.add(poistion,mewListsBean);
                }
            }
        //删除某个元素,后重新初始化界面
        }else if(null!=event&&TextUtils.equals(Constant.EVENT_TOPIC_VIDEO_PLAY_PAGE_DETELE,event.getMessage())){
            FollowVideoList.DataBean.ListsBean listsBean = event.getListsBean();
            if(null!=listsBean){
                if(null!=mListsBeanList&&mListsBeanList.size()>0){
                    if(null!=mVerticalPagerAdapter){
                        mListsBeanList.remove(event.getPoistion());
                        mVerticalPagerAdapter.notifyDataSetChanged();
                        bindingView.verticalViewPager.setAdapter(null);
                        bindingView.verticalViewPager.setAdapter(mVerticalPagerAdapter);
                        if(null!=mListsBeanList&&mListsBeanList.size()>0){
                            listSize=mListsBeanList.size();
                            mItemPoistion=0;
                            sendUpdataUserDataMessage(mItemPoistion);
                        }else{
                            if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()){
                                mActivityWeakReference.get().onBackPressed();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()){
            if(0==mActivityWeakReference.get().getCureenItem()){
                WindowVideoPlayer.goOnPlayOnResume();
            }
        }
        onChildononResume(mItemPoistion);
    }


    @Override
    public void onPause() {
        super.onPause();
        onChildonPause(mItemPoistion);
        if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()){
            if(0==mActivityWeakReference.get().getCureenItem()){
                WindowVideoPlayer.goOnPlayOnPause();
            }
        }
    }


    @Override
    public void onDestroyView() {
        if(null!=mSlideVideoPlayerPresenter){
            mSlideVideoPlayerPresenter.detachView();
        }
        onChildDestroyView(mItemPoistion);
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if(null!=mListsBeanList){
            //将观看的结果发送至跳转者(订阅方),订阅方需要刷新观看进度
            ChangingViewEvent changingViewEvent=new ChangingViewEvent();
            changingViewEvent.setFragmentType(mFragmentType);
            changingViewEvent.setPage(mPage);
            changingViewEvent.setPoistion(mItemPoistion);
            changingViewEvent.setListsBeanList(mListsBeanList);
            EventBus.getDefault().post(changingViewEvent);
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(null!=playerViews){
            playerViews.clear();
            playerViews=null;
        }
        if(null!=mHandler){
            mHandler.removeMessages(0);
            mHandler=null;
        }
        WindowVideoPlayer.releaseAllVideos();
        mSlideVideoPlayerPresenter=null;
        mVerticalPagerAdapter=null;
        if(null!=mActivityWeakReference){
            mActivityWeakReference.clear();
        }
    }

    /**
     * 垂直列表适配器
     */
    private class PlayListVerticalPagerAdapter extends VerticalPagerAdapter {

        @Override
        public int getCount() {
            return null==mListsBeanList?0:mListsBeanList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FollowVideoList.DataBean.ListsBean listsBean = mListsBeanList.get(position);
            if(null!=listsBean){
                VerticalPlayeViewPager videpPlayerViewPager = new VerticalPlayeViewPager((VerticalVideoPlayActivity) getActivity(),listsBean,position,mFragmentType);
                View view = videpPlayerViewPager.getView();
                view.setId(position);
                Logger.d(TAG,"添加了："+position);
                playerViews.put(position, videpPlayerViewPager);
                container.addView(view);
                return view;
            }
            return null;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(container.findViewById(position));
            Logger.d(TAG,"移除了："+position);
            playerViews.remove(position);
        }
    }

    //==========================================加载更多回调=========================================

    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }

    @Override
    public void showVideoDataList(FollowVideoList data) {
        listSize+=data.getData().getLists().size();
        if(null!=mListsBeanList) mListsBeanList.addAll(data.getData().getLists());
        if(null!=mVerticalPagerAdapter) mVerticalPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showVideoDataListEmpty(String data) {
        ToastUtils.shoCenterToast(data);
        if(mPage >0){
            mPage--;
        }
    }

    @Override
    public void showVideoDataListError(String data) {
        ToastUtils.shoCenterToast(data);
        if (mPage > 0) {
            mPage--;
        }
    }

    /**
     * 话题列表,需要将数据转换成通用的数据结构
     * @param data
     */
    @Override
    public void showTopicVideoListFinlish(TopicVideoList data) {
        listSize+=data.getData().getVideo_list().size();
        List<TopicVideoList.DataBean.VideoListBean> video_list = data.getData().getVideo_list();
        if(null==mListsBeanList) mListsBeanList=new ArrayList<>();
        for (TopicVideoList.DataBean.VideoListBean video : video_list) {
            FollowVideoList.DataBean.ListsBean videoListBean=new FollowVideoList.DataBean.ListsBean();
            videoListBean.setVideo_id(video.getVideo_id());
            videoListBean.setUser_id(video.getUser_id());
            videoListBean.setIs_interest(video.getIs_interest());
            videoListBean.setPath(video.getPath());
            videoListBean.setType(video.getType());
            videoListBean.setComment_times(video.getComment_times());
            videoListBean.setIs_follow(video.getIs_follow());
            videoListBean.setCollect_times(video.getCollect_times());
            videoListBean.setCover(video.getCover());
            videoListBean.setAdd_time(video.getAdd_time());
            videoListBean.setDesp(video.getDesp());
            videoListBean.setNickname(video.getNickname());
            videoListBean.setLogo(video.getLogo());
            videoListBean.setPlay_times(video.getPlay_times());
            videoListBean.setShare_times(video.getShare_times());
            videoListBean.setDownload_permiss(video.getDownload_permiss());
            mListsBeanList.add(videoListBean);
        }
        if(null!=mVerticalPagerAdapter) mVerticalPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void showTopicVideoListEmpty(String data) {
        ToastUtils.shoCenterToast(data);
        if(mPage >0){
            mPage--;
        }
    }

    @Override
    public void showTopicVideoListError(String data) {
        ToastUtils.shoCenterToast(data);
        if(mPage >0){
            mPage--;
        }
    }
}
