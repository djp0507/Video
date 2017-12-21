package com.video.newqu.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.alibaba.fastjson.JSONArray;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.TopicVideoListAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.TopicVideoList;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentRecylerBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.listener.VideoComentClickListener;
import com.video.newqu.mode.StaggerSpacesItemDecoration2;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.TopicVideoContract;
import com.video.newqu.ui.presenter.TopicVideoPresenter;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/10/1 14:44
 * 话题列表视频
 */

public class TopicVideoListFragment extends BaseFragment<FragmentRecylerBinding> implements  BaseQuickAdapter.RequestLoadMoreListener,VideoComentClickListener, TopicVideoContract.View {

    private int mPage=0;
    private List<TopicVideoList.DataBean.VideoListBean> mTopicVideoList;
    private String mTopicID;
    private TopicVideoPresenter mTopicVideoPresenter;
    private TopicVideoListAdapter mTopicVideoListAdapter;

    /**
     * 创造实例
     * @param topicID 话题ID
     * @return
     */
    public static TopicVideoListFragment newInstance(String topicID){
        TopicVideoListFragment fansListFragment=new TopicVideoListFragment();
        Bundle bundle=new Bundle();
        bundle.putString(Constant.KEY_VIDEO_TOPIC_ID,topicID);
        fansListFragment.setArguments(bundle);
        return fansListFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取出参数
        Bundle arguments = getArguments();
        if(null!=arguments) {
            mTopicID = arguments.getString(Constant.KEY_VIDEO_TOPIC_ID);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recyler;
    }


    @Override
    protected void initViews() {

        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadTopicVideoList();
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTopicVideoPresenter = new TopicVideoPresenter(getActivity());
        mTopicVideoPresenter.attachView(this);
        if(!TextUtils.isEmpty(mTopicID)){
            mTopicID=Utils.slipTopic(mTopicID);

            mTopicVideoList= (List<TopicVideoList.DataBean.VideoListBean>)   ApplicationManager.getInstance().getCacheExample().getAsObject(mTopicID);
            if(null==mTopicVideoList) mTopicVideoList=new ArrayList<>();
            initAdapter();
            if(null==mTopicVideoList||mTopicVideoList.size()<=0){
                showLoadingView("精彩视频即将呈现..");
                mPage=0;
                loadTopicVideoList();
            }else{
                showContentView();
                bindingView.recyerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        mPage=0;
                        loadTopicVideoList();
                    }
                },500);
            }
        }else{
            ToastUtils.shoCenterToast("错误!");
            getActivity().finish();
        }
    }


    /**
     * 加载话题视频列表
     */
    private void loadTopicVideoList() {
        if(null!=mTopicVideoPresenter&&!mTopicVideoPresenter.isLoading()){
            mPage++;
            mTopicVideoPresenter.getTopicVideoList(VideoApplication.getLoginUserID(),mTopicID,mPage+"");
        }
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingView("精彩视频即将呈现..");
        mPage=0;
        loadTopicVideoList();
    }

    @Override
    public void onDestroy() {
        if(null!=mTopicVideoPresenter){
            mTopicVideoPresenter.detachView();
        }
        super.onDestroy();
    }

    /**
     *初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),2,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new StaggerSpacesItemDecoration2(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(true);
        mTopicVideoListAdapter = new TopicVideoListAdapter(mTopicVideoList,this);
        mTopicVideoListAdapter.setOnLoadMoreListener(this);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mTopicVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有发现与此话题相关的视频");
        bindingView.recyerView.setAdapter(mTopicVideoListAdapter);
    }

    /**
     * 只新增数据
     */
    private void updataAddNewDataAdapter() {
        mTopicVideoListAdapter.addData(mTopicVideoList);
    }


    /**
     * 刷新全新适配器
     */
    private void updataNewDataAdapter() {
        showContentView();
        mTopicVideoListAdapter.setNewData(mTopicVideoList);
    }


    /**
     * 加载更多数据
     */
    @Override
    public void onLoadMoreRequested() {

        if(null!=mTopicVideoList&&mTopicVideoList.size()>=10&&null!=mTopicVideoListAdapter){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mTopicVideoListAdapter.setEnableLoadMore(true);
            loadTopicVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mTopicVideoListAdapter.loadMoreFail();//加载失败
                    }else{
                        mTopicVideoListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }


    //==========================================点击事件=============================================

    @Override
    public void onAuthorClick(String userID) {
        AuthorDetailsActivity.start(getActivity(),userID);
    }

    /**
     * 条目点击事件
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        if(null!=mTopicVideoListAdapter){
            List<TopicVideoList.DataBean.VideoListBean> data = mTopicVideoListAdapter.getData();
            if(null!=data&&data.size()>0){
                //全屏
                if(ConfigSet.getInstance().isPlayerModel()){
                    try{
                        FollowVideoList.DataBean followDataBean=new FollowVideoList.DataBean();
                        List<FollowVideoList.DataBean.ListsBean> videoListBeenList=new ArrayList<>();
                        for (TopicVideoList.DataBean.VideoListBean video : data) {
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
                            videoListBeenList.add(videoListBean);
                        }
                        followDataBean.setLists(videoListBeenList);
                        FollowVideoList followVideoList=new FollowVideoList();
                        followVideoList.setData(followDataBean);
                        String json = JSONArray.toJSON(followVideoList).toString();
                        if(!TextUtils.isEmpty(json)){
                            Intent intent=new Intent(getActivity(),VerticalVideoPlayActivity.class);
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_TOPIC_LIST);
                            intent.putExtra(Constant.KEY_POISTION,position);
                            intent.putExtra(Constant.KEY_PAGE,mPage);
                            intent.putExtra(Constant.KEY_AUTHOE_ID,VideoApplication.getLoginUserID());
                            intent.putExtra(Constant.KEY_JSON,json);
                            intent.putExtra(Constant.KEY_TOPIC,mTopicID);
                            startActivity(intent);
                        }
                    }catch (Exception e){

                    }
                    //单个
                }else{
                    TopicVideoList.DataBean.VideoListBean videoListBean = data.get(position);
                    if(null!=videoListBean&&!TextUtils.isEmpty(videoListBean.getVideo_id())){
                        saveLocationHistoryList(videoListBean);
                        VideoDetailsActivity.start(getActivity(),videoListBean.getVideo_id(),videoListBean.getUser_id(),false);
                    }
                }
            }
        }
    }


    private void saveLocationHistoryList(final TopicVideoList.DataBean.VideoListBean data) {
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
                userLookVideoList.setVideoCommendCount(TextUtils.isEmpty(data.getComment_times())?"0":data.getComment_times());
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


    //======================================加载数据回调==============================================

    @Override
    public void showErrorView() {
        closeProgressDialog();
    }


    @Override
    public void complete() {

    }

    @Override
    public void showTopicVideoListFinlish(TopicVideoList data) {
        if(null!=mTopicVideoListAdapter&&null!=getActivity()&&!getActivity().isFinishing()){
            showContentView();
            bindingView.swiperefreshLayout.setRefreshing(false);
            mTopicVideoListAdapter.loadMoreComplete();
            if(1==mPage){
                if(null!=mTopicVideoList){
                    mTopicVideoList.clear();
                }
                mTopicVideoList=data.getData().getVideo_list();
                ApplicationManager.getInstance().getCacheExample().remove(mTopicID);
                ApplicationManager.getInstance().getCacheExample().put(mTopicID, (Serializable) mTopicVideoList, Constant.CACHE_TIME);
                updataNewDataAdapter();
            }else {
                mTopicVideoList=data.getData().getVideo_list();
                updataAddNewDataAdapter();
            }
        }
    }

    @Override
    public void showTopicVideoListEmpty(String data) {
        if(null!=mTopicVideoListAdapter&&null!=getActivity()&&!getActivity().isFinishing()){
            showContentView();
            bindingView.swiperefreshLayout.setRefreshing(false);
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mTopicVideoListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
            //没有关注用户，替换空的缓存
            if(mPage==1){
                if(null!=mTopicVideoList){
                    mTopicVideoList.clear();
                }
                ApplicationManager.getInstance().getCacheExample().remove(mTopicID);
                ApplicationManager.getInstance().getCacheExample().put(mTopicID, (Serializable) mTopicVideoList, Constant.CACHE_TIME);
                updataNewDataAdapter();
            }
            if(mPage>0){
                mPage--;
            }
        }
    }

    @Override
    public void showTopicVideoListError(String data) {
        if(null!=mTopicVideoListAdapter&&null!=getActivity()&&!getActivity().isFinishing()){
            if(1==mPage&&null==mTopicVideoList||mTopicVideoList.size()<=0){
                showLoadingErrorView();
            }

            if(mPage>0){
                mPage--;
            }
            bindingView.swiperefreshLayout.setRefreshing(false);
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mTopicVideoListAdapter.loadMoreFail();
                }
            });
        }
    }

    @Override
    public void showReportUserResult(String data) {

    }

    @Override
    public void showReportVideoResult(String data) {

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

    /**
     * 订阅播放结果，以刷新界面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChangingViewEvent event) {
        if(null!=event&&Constant.FRAGMENT_TYPE_TOPIC_LIST!=event.getFragmentType())return;
        final int poistion = event.getPoistion();
        mPage=event.getPage();
        List<FollowVideoList.DataBean.ListsBean> listsBeanList = event.getListsBeanList();
        if(null!=listsBeanList&&listsBeanList.size()>0){
            if(null!=mTopicVideoList) mTopicVideoList.clear();
            for (FollowVideoList.DataBean.ListsBean video : listsBeanList) {
                TopicVideoList.DataBean.VideoListBean videoListBean=new TopicVideoList.DataBean.VideoListBean();
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
                mTopicVideoList.add(videoListBean);
            }
            if(null!=mTopicVideoListAdapter){
                mTopicVideoListAdapter.setNewData(mTopicVideoList);
                bindingView.recyerView.post(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.recyerView.scrollToPosition(poistion);
                    }
                });
            }
        }
    }
}
