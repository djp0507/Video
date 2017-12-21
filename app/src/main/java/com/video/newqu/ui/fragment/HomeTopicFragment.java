package com.video.newqu.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.alibaba.fastjson.JSONArray;
import com.umeng.analytics.MobclickAgent;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.HomeTopicAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.FindVideoListInfo;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentRecylerBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.listener.HomeTopicItemClickListener;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.HomeTopicContract;
import com.video.newqu.ui.presenter.HomeTopicPresenter;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * TinyHung@outlook.com
 * 2017/5/24 9:16
 * 话题
 */

public class HomeTopicFragment extends BaseFragment<FragmentRecylerBinding> implements HomeTopicContract.View, BaseQuickAdapter.RequestLoadMoreListener,HomeTopicItemClickListener {


    private static final String TAG = HomeTopicFragment.class.getSimpleName();
    private HomeTopicPresenter mHomeTopicPresenter;
    private int page=0;
    private int pageSize=10;
    private boolean isRefresh;
    private List<FindVideoListInfo.DataBean> mDataBeanList=null;
    private HomeTopicAdapter mHomeTopicAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recyler;
    }


    @Override
    protected void initViews() {
        //刷新监听器
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=0;
                loadVideoList();
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDataBeanList= (List<FindVideoListInfo.DataBean>)  ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_FIND_VIDEO_LIST);//读取缓存
        if(null==mDataBeanList) mDataBeanList=new ArrayList<>();
        mHomeTopicPresenter = new HomeTopicPresenter(getActivity());
        mHomeTopicPresenter.attachView(this);
        initAdapter();
        //如果一进来是自己，直接刷新
        if(2== SharedPreferencesUtil.getInstance().getInt(Constant.CUREEN_FRAGMENT)&&null!= mHomeTopicPresenter &&!mHomeTopicPresenter.isLoading()){
            if(null==mDataBeanList||mDataBeanList.size()<=0){
                showLoadingView("精彩视频，即将呈现...");
                page=0;
                loadVideoList();
            }else{
                bindingView.recyerView.post(new Runnable() {
                    @Override
                    public void run() {
                        showContentView();
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        page=0;
                        loadVideoList();
                    }
                });
            }
        }else{
            isRefresh=true;
        }
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(isRefresh&&null!=bindingView&&null!=mHomeTopicAdapter&&null!= mHomeTopicPresenter &&!mHomeTopicPresenter.isLoading()){
            if(null==mDataBeanList||mDataBeanList.size()<=0){
                showLoadingView("精彩视频稍后呈现...");
                page=0;
                loadVideoList();
            }else{
                showContentView();
                bindingView.swiperefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        page=0;
                        loadVideoList();
                    }
                },500);
            }
        }
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();
        page=0;
        showLoadingView("精彩视频稍后呈现...");
        loadVideoList();
    }



    /**
     * 加载数据
     */
    private void loadVideoList() {
        page++;
        mHomeTopicPresenter.getHomeTopicDataList(VideoApplication.getLoginUserID(),page+"",pageSize+"");
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        mHomeTopicAdapter = new HomeTopicAdapter(mDataBeanList,this);
        mHomeTopicAdapter.setOnLoadMoreListener(this);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mHomeTopicAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有发现视频，下拉刷新试试看");
        bindingView.recyerView.setAdapter(mHomeTopicAdapter);
    }



    /**
     * 根据Targe打开新的界面
     * @param title
     * @param fragmentTarge
     */
    protected void startTargetActivity(int fragmentTarge,String title,String authorID,int authorType,String topicID) {
        Intent intent=new Intent(getActivity(), ContentFragmentActivity.class);
        intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentTarge);
        intent.putExtra(Constant.KEY_TITLE,title);
        intent.putExtra(Constant.KEY_AUTHOR_ID,authorID);
        intent.putExtra(Constant.KEY_AUTHOR_TYPE,authorType);
        intent.putExtra(Constant.KEY_VIDEO_TOPIC_ID,topicID);
        getActivity().startActivity(intent);
    }

    /**
     * 点击了查看更多
     * @param topicID
     */
    @Override
    public void onGroupItemClick(String topicID) {
        if(!TextUtils.isEmpty(topicID)){
            if(!ConfigSet.IS_DEBUG){
                if(Utils.isContainKey(topicID)){
                    MobclickAgent.onEvent(getActivity(),Constant.UM_EVENT_TOPIC_MEINV);
                }
            }
            startTargetActivity(Constant.KEY_FRAGMENT_TYPE_TOPIC_VIDEO_LISTT,topicID,VideoApplication.getLoginUserID(),0,topicID);
        }
    }

    /**
     * 点击了子条目中的某个poistion
     * @param groupPoistion 父条目
     * @param childPoistion 子条目
     */
    @Override
    public void onChildItemClick(String topicID,int groupPoistion, int childPoistion) {
        if(null!=mHomeTopicAdapter){
            List<FindVideoListInfo.DataBean> data = mHomeTopicAdapter.getData();
            if(null!=data&&data.size()>0){
                //取出父条目元素
                FindVideoListInfo.DataBean dataBean = data.get(groupPoistion);
                if(null!=dataBean){
                    List<FindVideoListInfo.DataBean.VideosBean> videos = dataBean.getVideos();
                    //携带首页某个父元素中的所有子元素数据到播放器界面
                    if(null!=videos&&videos.size()>0){
                        //全屏
                        if(ConfigSet.getInstance().isPlayerModel()){
                            //封装
                            try{
                                FollowVideoList.DataBean followDataBean=new FollowVideoList.DataBean();
                                List<FollowVideoList.DataBean.ListsBean> videoListBeenList=new ArrayList<>();
                                for (FindVideoListInfo.DataBean.VideosBean video : videos) {
                                    FollowVideoList.DataBean.ListsBean videoListBean=new FollowVideoList.DataBean.ListsBean();
                                    videoListBean.setVideo_id(video.getVideo_id());
                                    videoListBean.setUser_id(video.getUser_id());
                                    videoListBean.setIs_interest(video.getIs_interest());
                                    videoListBean.setPath(video.getPath());
                                    videoListBean.setType(video.getType());
                                    videoListBean.setComment_times(video.getComment_count());
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
                                    videoListBeenList.add(videoListBean);
                                }
                                followDataBean.setLists(videoListBeenList);
                                FollowVideoList followVideoList=new FollowVideoList();
                                followVideoList.setData(followDataBean);
                                String json = JSONArray.toJSON(followVideoList).toString();

                                if(!TextUtils.isEmpty(json)){
                                    Intent intent=new Intent(getActivity(),VerticalVideoPlayActivity.class);
                                    intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_HOME_TOPIC);
                                    intent.putExtra(Constant.KEY_POISTION,childPoistion);
                                    intent.putExtra(Constant.KEY_PAGE,1);
                                    intent.putExtra(Constant.KEY_AUTHOE_ID,VideoApplication.getLoginUserID());
                                    intent.putExtra(Constant.KEY_JSON,json);
                                    intent.putExtra(Constant.KEY_TOPIC,topicID);
                                    startActivity(intent);
                                    return;
                                }
                            }catch (Exception e){
                                ToastUtils.shoCenterToast("播放错误"+e.getMessage());
                            }
                        //单个
                        }else{
                            FindVideoListInfo.DataBean.VideosBean videosBean = videos.get(childPoistion);
                            if(null!=videosBean&&!TextUtils.isEmpty(videosBean.getId())){
                                saveLocationHistoryList(videosBean);
                                VideoDetailsActivity.start(getActivity(),videosBean.getId(),videosBean.getUser_id(),false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void saveLocationHistoryList(final FindVideoListInfo.DataBean.VideosBean data) {
        if(null==data) return;
        new Thread(){
            @Override
            public void run() {
                super.run();
                UserPlayerVideoHistoryList userLookVideoList=new UserPlayerVideoHistoryList();
                userLookVideoList.setUserName(TextUtils.isEmpty(data.getNickname())?"火星人":data.getNickname());
                userLookVideoList.setUserSinger("该宝宝没有个性签名");
                userLookVideoList.setUserCover(data.getLogo());
                userLookVideoList.setVideoDesp(data.getDesp());
                userLookVideoList.setVideoLikeCount(TextUtils.isEmpty(data.getCollect_times())?"0":data.getCollect_times());
                userLookVideoList.setVideoCommendCount(TextUtils.isEmpty(data.getComment_count())?"0":data.getComment_count());
                userLookVideoList.setVideoShareCount(TextUtils.isEmpty(data.getShare_times())?"0":data.getShare_times());
                userLookVideoList.setUserId(data.getUser_id());
                userLookVideoList.setVideoId(data.getVideo_id());
                userLookVideoList.setVideoCover(data.getCover());
                userLookVideoList.setUploadTime(data.getAdd_time());
                userLookVideoList.setAddTime(System.currentTimeMillis());
                userLookVideoList.setIs_interest(data.getIs_interest());
                userLookVideoList.setIs_follow(data.getIs_follow());
                userLookVideoList.setVideoPath(data.getPath());
                userLookVideoList.setVideoPlayerCount(TextUtils.isEmpty(data.getPlay_times())?"0":data.getPlay_times());
                userLookVideoList.setVideoType(TextUtils.isEmpty(data.getType())?"2":data.getType());
                userLookVideoList.setDownloadPermiss(data.getDownload_permiss());
                ApplicationManager.getInstance().getUserPlayerDB().insertNewPlayerHistoryOfObject(userLookVideoList);
            }
        }.start();
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {

        if(null!=mDataBeanList&&mDataBeanList.size()>=3){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mHomeTopicAdapter.setEnableLoadMore(true);
            loadVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mHomeTopicAdapter.loadMoreFail();//加载失败
                    }else{
                        mHomeTopicAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }

    /**
     * 为适配器刷新新数据
     */
    private void upDataNewDataAdapter() {
        mHomeTopicAdapter.setNewData(mDataBeanList);
    }

    /**
     * 为适配器增加数据
     */
    private void updataAddDataAdapter() {
        mHomeTopicAdapter.addData(mDataBeanList);
    }




    /**
     * 加载发现列表成功
     * @param data
     */

    @Override
    public void showHomeTopicDataList(FindVideoListInfo data) {
        showContentView();
        isRefresh=false;
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mHomeTopicAdapter.loadMoreComplete();//加载完成
            }
        });

        //更新适配器数据为全新
        if(1==page){//替换最新缓存
            bindingView.swiperefreshLayout.setRefreshing(false);
            if(null!=mDataBeanList){
                mDataBeanList.clear();
            }
            mDataBeanList = data.getData();
            upDataNewDataAdapter();
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_FIND_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_FIND_VIDEO_LIST, (Serializable) mDataBeanList, Constant.CACHE_TIME);
            //仅增加新数据
        }else{
            mDataBeanList=data.getData();
            updataAddDataAdapter();
        }
    }

    /**
     * 加载发现列表为空
     * @param data
     */
    @Override
    public void showHomeTopicDataEmpty(String data) {

        showContentView();
        isRefresh=false;
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mHomeTopicAdapter.loadMoreEnd();//加载为空
            }
        });

        //更新适配器数据为全新
        if(1==page){//替换最新缓存
            bindingView.swiperefreshLayout.setRefreshing(false);
            if(null!=mDataBeanList){
                mDataBeanList.clear();
            }
            upDataNewDataAdapter();
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_FIND_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_FIND_VIDEO_LIST, (Serializable) mDataBeanList, Constant.CACHE_TIME);
            //仅增加新数据
        }
        if(page>1){
            page--;
        }
    }

    /**
     * 加载发现失败
     * @param data
     */
    @Override
    public void showHomeTopicDataError(String data) {

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mHomeTopicAdapter.loadMoreFail();//加载失败
            }
        });
        if(1==page){
            bindingView.swiperefreshLayout.setRefreshing(false,-1);
        }
        if(1==page&&null==mDataBeanList||mDataBeanList.size()<=0){
            showLoadingErrorView();
        }
        if (page > 0) {
            page--;
        }
    }


    @Override
    public void showErrorView() {
    }

    @Override
    public void complete() {

    }

    @Override
    public void onDestroy() {
        if(null!=mHomeTopicPresenter){
            mHomeTopicPresenter.detachView();
        }
        super.onDestroy();
    }

    /**
     * 来自外界的刷新命令
     */
    public void fromMainUpdata() {
        if (null != mHomeTopicAdapter && null != mHomeTopicPresenter) {
            if (!mHomeTopicPresenter.isLoading()) {
                if(null!=mHomeTopicAdapter){
                    List<FindVideoListInfo.DataBean> data = mHomeTopicAdapter.getData();
                    if(null!=data&&data.size()>0){
                        bindingView.recyerView.post(new Runnable() {
                            @Override
                            public void run() {
                                bindingView.recyerView.scrollToPosition(0);
                            }
                        });
                    }
                }
                bindingView.swiperefreshLayout.setRefreshing(true);
                page = 0;
                loadVideoList();
            } else {
                showErrorToast(null, null, "刷新太频繁了");
            }
        } else {
            showErrorToast(null, null, "刷新错误!");
        }
    }
}
