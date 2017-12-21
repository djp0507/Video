package com.video.newqu.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.alibaba.fastjson.JSONArray;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.HotVideoListAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentHotRecylerBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.listener.VideoComentClickListener;
import com.video.newqu.mode.StaggerSpacesItemDecoration2;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.HotVideoContract;
import com.video.newqu.ui.presenter.HotVideoPresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.SharedPreferencesUtil;
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
 * 2017/5/23 16:10
 * 热门视频
 */

public class HomeHotVideoFragment extends BaseFragment<FragmentHotRecylerBinding> implements  HotVideoContract.View, BaseQuickAdapter.RequestLoadMoreListener,VideoComentClickListener {

    private static final String TAG =HomeHotVideoFragment.class.getSimpleName();
    private HotVideoListAdapter mHotVideoListAdapter;
    private HotVideoPresenter mHotVideoPresenter;
    private int page=0;//当前页数
    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList=null;
    public void setRefresh(boolean refresh) {
        isRefresh = refresh;
    }
    private boolean isRefresh=false;//是否需要刷新


    @Override
    public int getLayoutId() {
        return R.layout.fragment_hot_recyler;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListsBeanList= (List<FollowVideoList.DataBean.ListsBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_HOT_VIDEO_LIST);//读取缓存
        if(null==mListsBeanList) mListsBeanList=new ArrayList<>();
        if(null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingView("获取热门视频中...");
        }else{
            showContentView();
        }
        mHotVideoPresenter = new HotVideoPresenter(getActivity());
        mHotVideoPresenter.attachView(this);
        initAdapter();
        upDataView();

        //第一次使用弹出使用提示
        if(1!=SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_HOT_CODE)&&null!=mHotVideoListAdapter&&null!=mHotVideoListAdapter.getData()&&mHotVideoListAdapter.getData().size()>0){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindingView.tvTipsMessage.setVisibility(View.VISIBLE);
                    bindingView.tvTipsMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bindingView.tvTipsMessage.setVisibility(View.GONE);
                        }
                    });
                    SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_HOT_CODE,1);
                }
            },1000);
        }
    }


    /**
     * 刷新自己
     */
    public void upDataView() {
        //显示是自己，直接刷新,否则记录刷新
        if(1==SharedPreferencesUtil.getInstance().getInt(Constant.CUREEN_FRAGMENT)){
            if(null!=bindingView&&null!=mHotVideoListAdapter&&null!=mHotVideoPresenter&&!mHotVideoPresenter.isLoading()){
                if(null==mListsBeanList||mListsBeanList.size()<=0){
                    showLoadingView("获取热门视频中...");
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
            }
        }else{
            isRefresh=true;
        }
    }



    @Override
    protected void onRefresh() {
        super.onRefresh();
        page=0;
        showLoadingView("获取热门视频中...");
        loadVideoList();
    }

    /**
     * 清空登录前的数据
     */
    public void removeList() {
        if(null==VideoApplication.getInstance().getUserData()&&null!= mHotVideoListAdapter){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            upDataNewDataAdapter();
        }
    }


    @Override
    protected void onVisible() {
        super.onVisible();
        if(isRefresh&&null!=bindingView&&null!=mHotVideoListAdapter&&null!=mHotVideoPresenter&&!mHotVideoPresenter.isLoading()){
            if(null==mListsBeanList||mListsBeanList.size()<=0){
                showLoadingView("获取热门视频中...");
                page=0;
                loadVideoList();
            }else{
                bindingView.swiperefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showContentView();
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        page=0;
                        loadVideoList();
                    }
                },500);
            }
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }


    @Override
    protected void initViews() {

        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(null!=bindingView.tvTipsMessage&&bindingView.tvTipsMessage.getVisibility()==View.VISIBLE){
                    bindingView.tvTipsMessage.setVisibility(View.GONE);
                }
                page=0;
                loadVideoList();
            }
        });
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),2,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new StaggerSpacesItemDecoration2(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(true);
        mHotVideoListAdapter = new HotVideoListAdapter(mListsBeanList,HomeHotVideoFragment.this);
        mHotVideoListAdapter.setOnLoadMoreListener(this);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mHotVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有视频，下拉刷新试试看");
        bindingView.recyerView.setAdapter(mHotVideoListAdapter);
    }

    /**
     * 点击了用户图标
     * @param userID
     */
    @Override
    public void onAuthorClick(String userID) {
        AuthorDetailsActivity.start(getActivity(),userID);
    }

    /**
     * 点击了条目
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        if(null!=mHotVideoListAdapter){
            List<FollowVideoList.DataBean.ListsBean> data = mHotVideoListAdapter.getData();
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
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_HOT);
                            intent.putExtra(Constant.KEY_POISTION,position);
                            intent.putExtra(Constant.KEY_PAGE,page);
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
     * 为适配器刷新新数据
     */
    private void upDataNewDataAdapter() {
        mHotVideoListAdapter.setNewData(mListsBeanList);
    }

    /**
     * 为适配器增加数据
     */
    private void updataAddDataAdapter() {
        mHotVideoListAdapter.addData(mListsBeanList);
    }

    /**
     * 加载数据
     */
    private void loadVideoList() {
        page++;
        //加载热门数据的时候，如果当前是未登录用户，则使用设备号
        mHotVideoPresenter.getHotVideoList(page+"",VideoApplication.getLoginUserID());

    }

    @Override
    public void onLoadMoreRequested() {
        if(null!=mListsBeanList&&mListsBeanList.size()>=7){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mHotVideoListAdapter.setEnableLoadMore(true);
            loadVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mHotVideoListAdapter.loadMoreFail();//加载失败
                    }else{
                        mHotVideoListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
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
        if(null!=event&&Constant.FRAGMENT_TYPE_HOT!=event.getFragmentType())return;
        final int poistion = event.getPoistion();
        page=event.getPage();
        List<FollowVideoList.DataBean.ListsBean> listsBeanList = event.getListsBeanList();
        if(null!=listsBeanList&&listsBeanList.size()>0&&null!=mHotVideoListAdapter){
            mHotVideoListAdapter.setNewData(listsBeanList);
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
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if(null!=mHotVideoPresenter){
            mHotVideoPresenter.detachView();
        }
        super.onDestroy();
    }


    /**
     * 其他加载错误
     */
    @Override
    public void showErrorView() {

    }

    /**
     * 加载完成
     */
    @Override
    public void complete() {

    }

    /**
     * 视频列表加载成功
     * @param data
     */
    @Override
    public void showHotVideoList(FollowVideoList data) {
        showContentView();
        isRefresh=false;
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mHotVideoListAdapter.loadMoreComplete();//加载完成
            }
        });

        //更新适配器数据为全新
        if(1==page){//替换最新缓存
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            mListsBeanList=data.getData().getLists();
            if(0==VideoApplication.mBuildChanleType){
                bindingView.swiperefreshLayout.setRefreshing(false,mListsBeanList.size());
            }else{
                bindingView.swiperefreshLayout.setRefreshing(false);
            }

            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_HOT_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_HOT_VIDEO_LIST, (Serializable) mListsBeanList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
            //仅增加新数据
        }else{
            mListsBeanList=data.getData().getLists();
            updataAddDataAdapter();
        }
    }



    /**
     * 视频列表加载为空
     * @param data
     */
    @Override
    public void showHotVideoListEmpty(String data) {
        showContentView();
        isRefresh=false;

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mHotVideoListAdapter.loadMoreEnd();//加载为空
            }
        });

        //更新适配器数据为全新
        if(1==page){//替换最新缓存
            bindingView.swiperefreshLayout.setRefreshing(false);
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            upDataNewDataAdapter();
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_HOT_VIDEO_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_HOT_VIDEO_LIST, (Serializable) mListsBeanList, Constant.CACHE_TIME);
        }
        if(page>1){
            page--;
        }
    }

    /**
     * 视频列表加载失败
     * @param data
     */
    @Override
    public void showHotVideoListError(String data) {

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mHotVideoListAdapter.loadMoreFail();//加载失败
            }
        });

        if(1==page){
            bindingView.swiperefreshLayout.setRefreshing(false,-1);
        }
        if(page==1&&null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingErrorView();
        }
        if (page > 0) {
            page--;
        }
    }

    /**
     * 来自外界的刷新命令
     */
    public void fromMainUpdata() {
        if(null!=mHotVideoListAdapter&&null!=mHotVideoPresenter){
            if(!mHotVideoPresenter.isLoading()){
                if(null!=mHotVideoListAdapter){
                    List<FollowVideoList.DataBean.ListsBean> data = mHotVideoListAdapter.getData();
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
                page=0;
                loadVideoList();
            }else{
                showErrorToast(null,null,"刷新太频繁了");
            }
        }else{
            showErrorToast(null,null,"刷新错误!");
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }
}
