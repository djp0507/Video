package com.video.newqu.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.UserVideoListAdapter;
import com.video.newqu.base.BaseMineFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.MineFragmentRecylerBinding;
import com.video.newqu.databinding.WorkEmptyLayoutBinding;
import com.video.newqu.listener.OnUserVideoListener;
import com.video.newqu.model.RecyclerViewSpacesItem;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.MainActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.FollowListContract;
import com.video.newqu.ui.presenter.FollowListPresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017-05-24 17:31
 * 用户收藏的视频
 */

public class HomeLikeVideoFragment extends BaseMineFragment<MineFragmentRecylerBinding> implements FollowListContract.View, BaseQuickAdapter.RequestLoadMoreListener,OnUserVideoListener {

    private static final String TAG = HomeLikeVideoFragment.class.getSimpleName();
    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList=new ArrayList<>();
    private int mPage=0;
    private int mPageSize=15;
    private MainActivity mMainActivity;
    private FollowListPresenter mFollowListPresenter;
    private UserVideoListAdapter mVideoListAdapter;
    private boolean isRefresh=true;

    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
        if(null!=VideoApplication.getInstance().getUserData()){
            showContentView();
        }else{
            showLoginView();
        }
    }




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }




    @Override
    public void onDestroy() {
        if(null!=mFollowListPresenter){
            mFollowListPresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    protected void initViews() {
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadFollowVideoList();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.mine_fragment_recyler;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListsBeanList= (List<FollowVideoList.DataBean.ListsBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST);
        if(null==mListsBeanList) mListsBeanList=new ArrayList<>();
        initAdapter();
        mFollowListPresenter = new FollowListPresenter(getActivity());
        mFollowListPresenter.attachView(this);
        //未登录
        if(null==VideoApplication.getInstance().getUserData()){
            isShowLoginView(true);
        }else{
            showContentView();
        }
    }

    public void isShowLoginView(boolean show){
        if(show){
            showLoginView();
        }else{
            showContentView();
        }
    }


    @Override
    protected void onVisible() {
        super.onVisible();
        if(isRefresh&&null!=VideoApplication.getInstance().getUserData()&&null!=bindingView&&null!=mFollowListPresenter&&!mFollowListPresenter.isLoading()){
            if(null==mListsBeanList|| mListsBeanList.size()<=0){
                showLoadingView("获取我点赞的视频中...");
                mPage=0;
                loadFollowVideoList();
            }else{
                bindingView.recyerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        mPage=0;
                        loadFollowVideoList();
                    }
                },500);
            }
        }
    }



    @Override
    protected void onRefresh() {
        super.onRefresh();
        mPage=0;
        showLoadingView("获取我点赞的视频中...");
        loadFollowVideoList();
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),3,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new RecyclerViewSpacesItem(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(true);
        mVideoListAdapter = new UserVideoListAdapter(mListsBeanList,2,this);
        mVideoListAdapter.setOnLoadMoreListener(this);
        WorkEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.work_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        emptyViewbindView.ivIcon.setImageResource(R.drawable.iv_fans_empty);
        emptyViewbindView.tvMessage.setText("点赞过的视频会出现在这里");
        emptyViewbindView.startRecord.setText("去逛逛");
        emptyViewbindView.startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=mMainActivity){
                    mMainActivity.currentHomeFragmentChildItemView(0,2);
                }
            }
        });
        mVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        bindingView.recyerView.setAdapter(mVideoListAdapter);
    }



    @Override
    protected void onLogin() {
        super.onLogin();
        if(null!=mMainActivity&&!mMainActivity.isFinishing()){
            mMainActivity.login();
        }
    }

    /**
     * 刷新适配器所有数据
     */
    private void upDataNewDataAdapter() {
        mVideoListAdapter.setNewData(mListsBeanList);
    }


    /**
     * 为适配器添加新的数据
     */
    private void updataAddDataAdapter() {
        mVideoListAdapter.addData(mListsBeanList);
    }


    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {
        if(null!=mListsBeanList&&mListsBeanList.size()>=7){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mVideoListAdapter.setEnableLoadMore(true);
            loadFollowVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mVideoListAdapter.loadMoreFail();//模拟加载失败
                    }else{
                        mVideoListAdapter.loadMoreEnd();//模拟加载完成
                    }
                }
            });
        }
    }


    /**
     * 清空所有数据
     */
    public void canelAllData() {
        if(null!=mListsBeanList) mListsBeanList.clear();
        upDataNewDataAdapter();
        isRefresh=true;
    }

    public void updataView(){
        mPage=0;
        loadFollowVideoList();
    }


    /**
     * 获取收藏的视列表
     */
    private void loadFollowVideoList() {
        if(null!=VideoApplication.getInstance().getUserData()&&null!=mFollowListPresenter&&!mFollowListPresenter.isLoading()){
            mPage++;
            mFollowListPresenter.getFollowVideoList(VideoApplication.getLoginUserID(),mPage+"",mPageSize+"");
        }
    }

    //=========================================加载数据回调==========================================

    /**
     * 获取收藏列表成功
     * @param data
     */
    @Override
    public void showFollowVideoList(FollowVideoList data) {
        showContentView();
        isRefresh=false;
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreComplete();//加载完成
            }
        });
        //替换为全新数据
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            mListsBeanList=data.getData().getLists();
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST, (Serializable) mListsBeanList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
            //添加数据
        }else{
            mListsBeanList=data.getData().getLists();
            updataAddDataAdapter();
        }
    }

    /**
     *获取收藏列表为空,如果当前界面还有数据，就清空数据再刷新界面
     * @param data
     */

    @Override
    public void showFollowVideoListEmpty(String data) {
        showContentView();
        isRefresh=false;
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //如果当前用户在第一页的时候获取视频为空，表示该用户没有关注用户
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST, (Serializable) mListsBeanList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
        }
        //还原当前的页数
        if (mPage > 1) {
            mPage--;
        }
    }

    /**
     * 获取收藏列表失败
     * @param data
     */
    @Override
    public void showFollowVideoListError(String data) {

        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreFail();
            }
        });

        if(mPage==1&&null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingErrorView();
        }
        if(mPage>0){
            mPage--;
        }
    }



    /**
     * 收藏视频回调
     * @param data
     */
    @Override
    public void showFollowVideoResult(String data) {

        closeProgressDialog();
        if(TextUtils.isEmpty(data)){
            return;
        }
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(jsonObject.length()>0&&1==jsonObject.getInt("code")){
                int poistion=0;
                //取消收藏成功
                if(TextUtils.equals(Constant.PRICE_UNSUCCESS,jsonObject.getString("msg"))){
                    String  videoID= new JSONObject(jsonObject.getString("data")).getString("video_id");
                    if(!TextUtils.isEmpty(videoID)){
                        List<FollowVideoList.DataBean.ListsBean> videoData = mVideoListAdapter.getData();
                        if(null!=videoData&&videoData.size()>0){
                            for (int i = 0; i < videoData.size(); i++) {
                                FollowVideoList.DataBean.ListsBean listsBean = videoData.get(i);
                                if(TextUtils.equals(videoID,listsBean.getVideo_id())){
                                    poistion=i;
                                    break;
                                }
                            }
                            if(null!=mVideoListAdapter) mVideoListAdapter.remove(poistion);
                            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST);
                            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FOLLOW_VIDEO_LIST, (Serializable) videoData, Constant.CACHE_TIME);
                            //刷新Mine界面的视频数量
                            Fragment parentFragment = getParentFragment();
                            if(null!=parentFragment&&parentFragment instanceof MineFragment){
                                ((MineFragment) parentFragment).updataMineTabCount(1);
                            }
                        }
                    }else{
                        showErrorToast(null,null,"取消收藏失败");
                    }
                }else{
                    showErrorToast(null,null,jsonObject.getString("msg"));
                }
            }else{
                showErrorToast(null,null,"取消收藏失败");
            }
        } catch (JSONException e) {
            showErrorToast(null,null,"取消收藏失败");
            e.printStackTrace();
        }
    }


    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }

    //========================================点击事件===============================================

    @Override
    public void onItemClick(int position) {
        if(null!=mVideoListAdapter){
            List<FollowVideoList.DataBean.ListsBean> data = mVideoListAdapter.getData();
            if(null!= data && data.size()>0){
                //全屏
                if(ConfigSet.getInstance().isPlayerModel()){
                    //改成获取当前视频的
                    try{
                        FollowVideoList.DataBean dataBean=new FollowVideoList.DataBean();
                        dataBean.setLists(data);
                        FollowVideoList followVideoList=new FollowVideoList();
                        followVideoList.setData(dataBean);
                        String json = JSONArray.toJSON(followVideoList).toString();
                        if(!TextUtils.isEmpty(json)){
                            Intent intent=new Intent(getActivity(),VerticalVideoPlayActivity.class);
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_LIKE);
                            intent.putExtra(Constant.KEY_POISTION,position);
                            intent.putExtra(Constant.KEY_PAGE,mPage);
                            intent.putExtra(Constant.KEY_AUTHOE_ID,VideoApplication.getLoginUserID());
                            intent.putExtra(Constant.KEY_JSON,json);
                            startActivity(intent);
                            return;
                        }
                    }catch (Exception e){
                        ToastUtils.shoCenterToast("播放错误"+e.getMessage());
                    }
                    //单个
                }else{
                    FollowVideoList.DataBean.ListsBean listsBean = data.get(position);
                    if(null!=listsBean&&!TextUtils.isEmpty(listsBean.getVideo_id())){
                        saveLocationHistoryList(listsBean);
                        VideoDetailsActivity.start(getActivity(),listsBean.getVideo_id(),listsBean.getUser_id(),false);
                    }
                }
            }
        }
    }

    /**
     * 长按事件
     * @param videoID
     */
    @Override
    public void onLongClick(String videoID) {

    }


    @Override
    public void onDeleteVideo(String videoID) {

    }

    @Override
    public void onPublicVideo(String videoID) {

    }

    /**
     * 取消收藏视频
     * @param videoID
     */
    @Override
    public void onUnFollowVideo(String videoID) {
        if(!TextUtils.isEmpty(videoID)&&null!=mFollowListPresenter){
            if(!mFollowListPresenter.isUnFollowing()){
                showProgressDialog("取消收藏中...",true);
                mFollowListPresenter.followVideo(videoID);
            }else{
                showErrorToast(null,null,"点击太过频繁");
            }
            return;
        }else{
            showErrorToast(null,null,"错误，请刷新重试！");
        }
    }

    @Override
    public void onHeaderIcon(String userID) {
        if(!TextUtils.isEmpty(userID)){
            AuthorDetailsActivity.start(getActivity(),userID);
        }else{
            showErrorToast(null,null,"错误，请刷新重试！");
        }
    }


    /**
     * 来自首页的刷新命令
     */
    public void fromMainUpdata() {
        if(null==VideoApplication.getInstance().getUserData()){
            if(null!=mMainActivity&&!mMainActivity.isFinishing()){
                ToastUtils.shoCenterToast("请先登录再刷新");
                mMainActivity.login();
            }
            return;
        }
        if(null!=mVideoListAdapter&&null!=mFollowListPresenter){
            if(!mFollowListPresenter.isLoading()){

                if(null!=mVideoListAdapter){
                    List<FollowVideoList.DataBean.ListsBean> data = mVideoListAdapter.getData();
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
                mPage=0;
                loadFollowVideoList();
            }else{
                showErrorToast(null,null,"刷新太频繁了");
            }
        }else{
            showErrorToast(null,null,"刷新错误!");
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

    /**
     * 订阅播放结果，以刷新界面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChangingViewEvent event) {
        if(null!=event&&Constant.FRAGMENT_TYPE_LIKE!=event.getFragmentType())return;
        final int poistion = event.getPoistion();
        mPage=event.getPage();
        List<FollowVideoList.DataBean.ListsBean> listsBeanList = event.getListsBeanList();
        if(null!=listsBeanList&&listsBeanList.size()>0&&null!=mVideoListAdapter){
            mVideoListAdapter.setNewData(listsBeanList);
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    bindingView.recyerView.scrollToPosition(poistion);
                }
            });
        }
    }
}
