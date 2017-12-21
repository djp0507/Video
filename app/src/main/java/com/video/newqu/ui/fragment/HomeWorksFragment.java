package com.video.newqu.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
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
import com.video.newqu.bean.WorksChangeEvemt;
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
import com.video.newqu.ui.activity.MediaRecordActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.WorksContract;
import com.video.newqu.ui.presenter.WorksPresenter;
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
 * 2017-05-24 19:28
 * 用户发布的视频
 */

public class HomeWorksFragment extends BaseMineFragment<MineFragmentRecylerBinding> implements WorksContract.View, BaseQuickAdapter.RequestLoadMoreListener,OnUserVideoListener {

    private static final String TAG = HomeWorksFragment.class.getSimpleName();
    private WorksPresenter mWorksPresenter;
    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList=null;
    private int mPage=0;
    private int mPageSize=20;
    private MainActivity mMainActivity;
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mListsBeanList= (List<FollowVideoList.DataBean.ListsBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINE_WORKS);
        if(null==mListsBeanList) mListsBeanList=new ArrayList<>();
        mWorksPresenter = new WorksPresenter(getActivity());
        mWorksPresenter.attachView(this);
        initAdapter();
        if(null!=VideoApplication.getInstance().getUserData()){
            if(null!=mListsBeanList&&mListsBeanList.size()>0){
                showContentView();
            }else{
                if(null!=mWorksPresenter&&!mWorksPresenter.isLoading()){
                    showLoadingView("获取我发布的作品中...");
                    mPage=0;
                    loadVideoList();
                }
            }
        }else{
            isShowLoginView(true);
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
    protected void onLogin() {
        super.onLogin();
        if(null!=mMainActivity&&!mMainActivity.isFinishing()){
            mMainActivity.login();
        }
    }


    @Override
    protected void onVisible() {
        super.onVisible();
        if(isRefresh&&null!=bindingView&&null!=VideoApplication.getInstance().getUserData()){
            if(null==mListsBeanList||mListsBeanList.size()<=0){
                showLoadingView("获取我发布的作品中...");
                mPage=0;
                loadVideoList();
            }else{
                bindingView.swiperefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        mPage=0;
                        loadVideoList();
                    }
                },500);
            }
        }else {
            if(null==VideoApplication.getInstance().getUserData()){
                isShowLoginView(true);
            }
        }
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        mPage=0;
        showLoadingView("获取我发布的作品中...");
        loadVideoList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if(null!=mWorksPresenter){
            mWorksPresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity=null;
    }


    @Override
    protected void initViews() {
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadVideoList();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.mine_fragment_recyler;
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),3,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new RecyclerViewSpacesItem(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(true);
        mVideoListAdapter = new UserVideoListAdapter(mListsBeanList,1,this);
        mVideoListAdapter.setOnLoadMoreListener(this);
        WorkEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.work_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        emptyViewbindView.ivIcon.setImageResource(R.drawable.iv_work_video_empty);
        emptyViewbindView.tvMessage.setText("发布视频让更多的人认识你");
        emptyViewbindView.startRecord.setText("开始制作");
        emptyViewbindView.startRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MediaRecordActivity.class);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.menu_enter, 0);//进场动画
            }
        });
        mVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        bindingView.recyerView.setAdapter(mVideoListAdapter);
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
            loadVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mVideoListAdapter.loadMoreFail();//加载失败
                    }else{
                        mVideoListAdapter.loadMoreEnd();//没有更多的数据了
                    }
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    /**
     * 刷新最新数据
     */
    public void updataView() {
        mPage=0;
        loadVideoList();
    }


    /**
     * 获取我的作品
     */
    private void loadVideoList() {
        if(null!=VideoApplication.getInstance().getUserData()&&null!=mWorksPresenter&&!mWorksPresenter.isLoading()){
            mPage++;
            mWorksPresenter.getUpLoadVideoList(VideoApplication.getLoginUserID(),VideoApplication.getLoginUserID(),mPage+"",mPageSize+"");
        }
    }




    /**
     * 显示视频列表
     * @param data
     */
    @Override
    public void showUpLoadVideoList(FollowVideoList data) {
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
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_WORKS);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_WORKS, (Serializable) mListsBeanList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
            //添加数据
        }else{
            mListsBeanList=data.getData().getLists();
            updataAddDataAdapter();
        }
    }

    /**
     * 加载视频列表为空
     * @param data
     */
    @Override
    public void showUpLoadVideoListEmpty(FollowVideoList data) {
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
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_WORKS);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_WORKS, (Serializable) mListsBeanList,  Constant.CACHE_TIME);
            upDataNewDataAdapter();
        }
        //还原当前的页数
        if (mPage > 1) {
            mPage--;
        }
    }

    /**
     * 加载视频列表失败
     * @param data
     */
    @Override
    public void showUpLoadVideoListError(String data) {
        if(1==mPage&&null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingErrorView();
        }
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreFail();
            }
        });

        if(mPage>0){
            mPage--;
        }
    }

    /**
     * 其他联网失败
     */
    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }


    /**
     * 用户删除视频回调
     * @param data
     */
    @Override
    public void showDeteleVideoResult(String data) {

        closeProgressDialog();
        int poistion = 0;
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&&TextUtils.equals(Constant.DELETE_VIDEO_CONTENT,jsonObject.getString("msg"))){
                //删除成功
                String  videoID= new JSONObject(jsonObject.getString("data")).getString("video_id");
                List<FollowVideoList.DataBean.ListsBean> data1 = mVideoListAdapter.getData();
                if(null!=data1&&data1.size()>0){
                    for (int i = 0; i < data1.size(); i++) {
                        FollowVideoList.DataBean.ListsBean listsBean = data1.get(i);
                        if(TextUtils.equals(videoID,listsBean.getVideo_id())){
                            poistion=i;
                            break;
                        }
                    }

                    ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_WORKS);
                    ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_WORKS, (Serializable) data1,  Constant.CACHE_TIME);
                    mVideoListAdapter.remove(poistion);
                    showFinlishToast(null,null,"删除视频成功");
                    //刷新Mine界面的视频数量
                    Fragment parentFragment = getParentFragment();
                    if(null!=parentFragment&&parentFragment instanceof MineFragment){
                        ((MineFragment) parentFragment).updataMineTabCount(0);
                    }
                }
            }else{
                showErrorToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            showErrorToast(null,null,"删除视频失败");
            e.printStackTrace();
        }
    }

    /**
     * 请求公开视频结果
     * @param result
     */
    @Override
    public void showPublicResult(String result) {
        closeProgressDialog();

        if(!TextUtils.isEmpty(result)){
            int poistion = 0;
            try {
                JSONObject jsonObject=new JSONObject(result);
                if(jsonObject.length()>0){
                    if(1==jsonObject.getInt("code")){
                        String  videoID= new JSONObject(jsonObject.getString("data")).getString("video_id");
                        if(!TextUtils.isEmpty(videoID)){
                            List<FollowVideoList.DataBean.ListsBean> data1 = mVideoListAdapter.getData();
                            if(null!=data1&&data1.size()>0){
                                for (int i = 0; i < data1.size(); i++) {
                                    FollowVideoList.DataBean.ListsBean listsBean = data1.get(i);
                                    if(TextUtils.equals(videoID,listsBean.getVideo_id())){
                                        listsBean.setIs_private("0");//公开请求成功后刷新缓存和界面状态
                                        poistion=i;
                                        break;
                                    }
                                }
                                mVideoListAdapter.notifyItemChanged(poistion);
                                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_WORKS);
                                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_WORKS, (Serializable) data1, Constant.CACHE_TIME);
                                showFinlishToast(null,null,"设置成功，视频审核通过后可以分享和评论");
                            }
                        }else{
                            showFinlishToast(null,null,"公开视频失败");
                        }
                    }else{
                        showFinlishToast(null,null,"公开视频失败");
                    }
                }
            } catch (JSONException e) {
                showFinlishToast(null,null,"公开视频失败");
                e.printStackTrace();
            }
        }
    }



//==========================================点击事件回调=============================================
    /**
     * 条目点击事件
     * @param position
     */
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
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_WORKS);
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
     * 条目长按事件
     * @param videoID
     */
    @Override
    public void onLongClick(String videoID) {

    }

    /**
     * 删除视频
     * @param videoID
     */
    @Override
    public void onDeleteVideo(String videoID) {
        deleteVideoTips(videoID);
    }

    /**
     * 公开视频
     * @param videoID
     */
    @Override
    public void onPublicVideo(String videoID) {
        publicVideo(videoID);
    }

    /**
     * 取消收藏视频
     * @param videoID
     */
    @Override
    public void onUnFollowVideo(String videoID) {

    }

    /**
     * 点击了头像
     * @param userID
     */
    @Override
    public void onHeaderIcon(String userID) {
        if(!TextUtils.isEmpty(userID)){
            AuthorDetailsActivity.start(getActivity(),userID);
        }
    }

    /**
     * 公开视频
     * @param videoID
     */
    private void publicVideo(String videoID) {
        if(!TextUtils.isEmpty(videoID)&&null!=mWorksPresenter) {
            if(!mWorksPresenter.isPublicing()){
                showProgressDialog("设置公开状态中...",true);
                mWorksPresenter.publicVideo(videoID,VideoApplication.getLoginUserID());
            }else{
                showErrorToast(null,null,"点击太过频繁");
            }
            return;
        }else{
            showErrorToast(null,null,"错误，请刷新重试！");
        }
    }

    /**
     * 删除视频
     * @param videoID
     */
    private void deleteVideoTips(final String videoID) {
        //删除视频提示
        new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setTitle(R.string.hint)
                .setMessage("确定删除此视频吗？删除后将不可恢复!")
                .setNegativeButton(
                        "删除",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(null!=mWorksPresenter&&!TextUtils.isEmpty(videoID)&&null!=mWorksPresenter){
                                    if(!mWorksPresenter.isDeletecing()){
                                        showProgressDialog("删除视频中...",true);
                                        mWorksPresenter.deleteVideo(VideoApplication.getLoginUserID(),videoID);
                                    }else{
                                        showErrorToast(null,null,"点击太过频繁");
                                    }
                                    return;
                                }else{
                                    showErrorToast(null,null,"错误，请刷新重试！");
                                }
                            }
                        })
                .setPositiveButton("取消",
                        null).setCancelable(false).show();
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
        if(null!=mVideoListAdapter&&null!=mWorksPresenter){
            if(!mWorksPresenter.isLoading()){
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
        if(null!=event&&Constant.FRAGMENT_TYPE_WORKS!=event.getFragmentType())return;
        final int poistion = event.getPoistion();
        mPage=event.getPage();
        List<FollowVideoList.DataBean.ListsBean> listsBeanList = event.getListsBeanList();
        if(null!=mVideoListAdapter){
            mVideoListAdapter.setNewData(listsBeanList);
            if(null!=listsBeanList&&listsBeanList.size()>0){
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
