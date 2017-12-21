package com.video.newqu.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSONArray;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.UserFollowVideoListAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.CommonEmptyViewBinding;
import com.video.newqu.databinding.FragmentVideoFollowBinding;
import com.video.newqu.listener.VideoComentClickListener;
import com.video.newqu.mode.StaggerSpacesItemDecoration2;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.MainActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.FollowContract;
import com.video.newqu.ui.presenter.FollowPresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.SharedPreferencesUtil;
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
 * 2017/5/22 18:09
 * 我的关注视频列表
 */

public class HomeFollowVideoFragment extends BaseFragment<FragmentVideoFollowBinding> implements FollowContract.View,VideoComentClickListener,BaseQuickAdapter.RequestLoadMoreListener {

    public static final String TAG=HomeFollowVideoFragment.class.getSimpleName();
    boolean mFull = false;
    private FollowPresenter mFollowPresenter;
    private int mPage=0;
    private int pageSize=10;
    private List<FollowVideoList.DataBean.ListsBean> mFollowVideoList=null;
    private MainActivity mMainActivity;
    private UserFollowVideoListAdapter mVideoListAdapter;
    private boolean isRefresh=true;//是否自动刷新


    @Override
    public int getLayoutId() {
        return R.layout.fragment_video_follow;
    }


    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(isRefresh&&null!=VideoApplication.getInstance().getUserData()&&null!=bindingView&&null!=mFollowPresenter&&!mFollowPresenter.isLoading()){
            if(null==mFollowVideoList||mFollowVideoList.size()<=0){
                showLoadingView("获取关注的视频中...");
                mPage=0;
                loadVideoList();
            }else{
                bindingView.swiperefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showContentView();
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        mPage=0;
                        loadVideoList();
                    }
                },500);
            }
        }
    }



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //已登录
        if(null!=VideoApplication.getInstance().getUserData()){
            showContentView();
            //未登录
        }else{
            showLoginView();//显示登录界面
        }

        mFollowVideoList= (List<FollowVideoList.DataBean.ListsBean>)  ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_FOOLOW_VIDEO_LIST);//读取缓存
        if(null==mFollowVideoList) mFollowVideoList=new ArrayList<>();
        mFollowPresenter = new FollowPresenter(getActivity());
        mFollowPresenter.attachView(this);
        initAdapter();//初始化普通列表
        //如果已经登录并且一进来是自己，自动刷新
        if(isRefresh&&0==SharedPreferencesUtil.getInstance().getInt(Constant.CUREEN_FRAGMENT)&&null!=VideoApplication.getInstance().getUserData()){

            if(null!=mFollowPresenter&&!mFollowPresenter.isLoading()){
                if(null==mFollowVideoList||mFollowVideoList.size()<=0){
                    showLoadingView("获取关注的视频中...");
                    mPage=0;
                    loadVideoList();
                }else{
                    bindingView.recyerView.post(new Runnable() {
                        @Override
                        public void run() {
                            showContentView();
                            bindingView.swiperefreshLayout.setRefreshing(true);
                            mPage=0;
                            loadVideoList();
                        }
                    });
                }
            }
        }else{
            isRefresh=true;
        }
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),2,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new StaggerSpacesItemDecoration2(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(true);
        mVideoListAdapter = new UserFollowVideoListAdapter(mFollowVideoList,4,this);
        mVideoListAdapter.setOnLoadMoreListener(this);
        CommonEmptyViewBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.common_empty_view, (ViewGroup) bindingView.recyerView.getParent(),false);
        emptyViewbindView.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mMainActivity){
                    mMainActivity.currentHomeFragmentChildItemView(0,2);
                }
            }
        });
        mVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        bindingView.recyerView.setAdapter(mVideoListAdapter);
    }



    /**
     * 该方法，判断 RecyclerView 是否到最后，
     * computeVerticalScrollExtent()是当前屏幕显示的区域高度
     * computeVerticalScrollOffset() 是当前屏幕之前滑过的距离 --- 获取滑动的实际值
     * computeVerticalScrollRange()是整个View控件的高度
     * @param recyclerView
     * @return
     */
    public static boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null) return false;
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange()){
            return true;
        }
        return false;
    }


    /**
     * 初始化数据
     */
    private void initData() {
        //已登录
        if(null!=VideoApplication.getInstance().getUserData()){
            showContentView();
            mPage=0;
            loadVideoList();
        //未登录
        }else{
            //如果之前登录过有缓存，清理掉
            if(null!=mFollowVideoList) mFollowVideoList.clear();
            //清空缓存
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_FOOLOW_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_FOOLOW_VIDEO_LIST, (Serializable) mFollowVideoList, Constant.CACHE_TIME);
            //刷新界面
            upDataNewDataAdapter();
            showLoginView();//显示登录界面
            isRefresh=true;
        }
    }




    /**
     * 刷新界面数据
     */
    public void upDataView() {
        initData();
    }


    /**
     * 登录
     */
    private void login() {
        mMainActivity.login();
    }


    /**
     * 登录事件
     */
    @Override
    protected void onLogin() {
        super.onLogin();
        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }
        login();
    }

    @Override
    protected void initViews() {
        //刷新监听器
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //未登录情况下不允许刷新
                if(null==VideoApplication.getInstance().getUserData()){
                    bindingView.swiperefreshLayout.setRefreshing(false);
                    return;
                }
                //还原当前页数
                mPage=0;
                loadVideoList();
            }
        });
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        //还原当前页数
        mPage=0;
        showLoadingView("获取关注的视频中...");
        loadVideoList();
    }






    /**
     * 刷新适配器所有数据
     */
    private void upDataNewDataAdapter() {
        mVideoListAdapter.setNewData(mFollowVideoList);
    }


    /**
     * 为适配器添加新的数据
     */
    private void updataAddDataAdapter() {
        mVideoListAdapter.addData(mFollowVideoList);
    }



    /**
     * 加载数据
     */
    private void loadVideoList() {
        if(null!=VideoApplication.getInstance().getUserData()&&null!=mFollowPresenter&&!mFollowPresenter.isLoading()){
            mPage++;
            mFollowPresenter.getFollowVideoList(VideoApplication.getLoginUserID(),mPage+"",pageSize+"");
        }
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {

        if(null!=mFollowVideoList&&mFollowVideoList.size()>=2){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mVideoListAdapter.setEnableLoadMore(true);
            loadVideoList();
        }else{
            bindingView.swiperefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mVideoListAdapter.loadMoreFail();//加载失败
                    }else{
                        mVideoListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (newConfig.orientation != ActivityInfo.SCREEN_ORIENTATION_USER) {
            mFull = false;
        } else {
            mFull = true;
        }
    }


//===========================================数据加载回调============================================
    /**
     * 获取关注列表成功
     * @param data
     */
    @Override
    public void showloadFollowVideoList(FollowVideoList data) {
        showContentView();
        isRefresh=false;

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreComplete();//加载完成
            }
        });

        //替换为全新数据
        if(1==mPage){
            bindingView.swiperefreshLayout.setRefreshing(false);
            List<FollowVideoList.DataBean.ListsBean> listsBeen=new ArrayList<>();
            for (FollowVideoList.DataBean.ListsBean listsBean : data.getData().getLists()) {
                listsBeen.add(listsBean);
            }
            showNewMessageDot(mVideoListAdapter.getData(),listsBeen);

            if(null!=mFollowVideoList){
                mFollowVideoList.clear();
            }

            mFollowVideoList=data.getData().getLists();
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_FOOLOW_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_FOOLOW_VIDEO_LIST, (Serializable) mFollowVideoList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
        //添加数据
        }else{
            mFollowVideoList=data.getData().getLists();
            updataAddDataAdapter();
        }
    }

    /**
     * 显示新消息小圆点
     */
    private void showNewMessageDot(List<FollowVideoList.DataBean.ListsBean> oldList, List<FollowVideoList.DataBean.ListsBean> newList) {
        int count = Utils.compareToDataHasNewData(oldList, newList);
        if(count>0&&null!=mMainActivity){
            mMainActivity.showNewMessageDot(count);
        }
    }




    /**
     * @param response
     */
    @Override
    public void showloadFollowListEmptry(String response) {

        showContentView();

        isRefresh=false;

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //如果当前用户在第一页的时候获取视频为空，表示该用户没有关注用户
        if(1==mPage){
            bindingView.swiperefreshLayout.setRefreshing(false);
            if(null!=mFollowVideoList){
                mFollowVideoList.clear();
            }
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_FOOLOW_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_FOOLOW_VIDEO_LIST, (Serializable) mFollowVideoList, 30000);
            upDataNewDataAdapter();
        }
        //还原当前的页数
        if (mPage > 0) {
            mPage--;
        }
    }


    /**
     * 获取关注列表失败
     */
    @Override
    public void showloadFollowListError() {


        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreFail();
            }
        });
        if(1==mPage){
            bindingView.swiperefreshLayout.setRefreshing(false,-1);
        }
        if(mPage==1&&null==mFollowVideoList||mFollowVideoList.size()<=0){
            showLoadingErrorView();
        }
        if(mPage>0){
            mPage--;
        }
    }


    /**
     * 热门列表回调，这里不需要
     * @param data
     */
    @Override
    public void showHotVideoList(FollowVideoList data) {

    }

    @Override
    public void showHotVideoListEmpty(String data) {

    }

    @Override
    public void showHotVideoListError(String data) {

    }

    /**
     * 举报用户回调
     * @param data
     */
    @Override
    public void showReportUserResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.REPORT_USER_RESULT)){
                showFinlishToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 举报视频回调
     * @param data
     */
    @Override
    public void showReportVideoResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.REPORT_USER_RESULT)){
                showFinlishToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 其他联网错误回调
     */
    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    /**
     * 其他联网完成回调
     */
    @Override
    public void complete() {

    }

//===========================================点击事件回调============================================

    @Override
    public void onAuthorClick(String userID) {
        AuthorDetailsActivity.start(getActivity(),userID);
    }

    /**
     * 条目点击事件
     * @param position
     */
    @Override
    public void onItemClick( int position) {
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
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_FOLLOW);
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
        if(null!=event&&Constant.FRAGMENT_TYPE_FOLLOW!=event.getFragmentType())return;
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


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if(null!=mFollowPresenter){
            mFollowPresenter.detachView();
        }
        super.onDestroy();
    }


    /**
     * 来自外界的刷新命令
     */
    public void fromMainUpdata() {
        if(null==VideoApplication.getInstance().getUserData()){
            ToastUtils.shoCenterToast("登录后才能获取订阅的视频");
            if(null!=mMainActivity&&!mMainActivity.isFinishing()){
                mMainActivity.login();
            }
            return;
        }
        if(null!=mVideoListAdapter&&null!=mFollowPresenter){
            if(!mFollowPresenter.isLoading()){
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
                loadVideoList();
            }else{
                showErrorToast(null,null,"刷新太频繁了");
            }
        }else{
            showErrorToast(null,null,"刷新错误！");
        }
    }
}
