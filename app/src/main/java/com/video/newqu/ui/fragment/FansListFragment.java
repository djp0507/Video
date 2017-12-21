package com.video.newqu.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.FansListAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.FansInfo;
import com.video.newqu.bean.FollowUserList;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentRecylerBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.listener.OnFansClickListener;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.contract.FansContract;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.FansPresenter;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/6/1 14:44
 * 粉丝列表  用户自己的，别人的
 */

public class FansListFragment extends BaseFragment<FragmentRecylerBinding> implements FansContract.View,OnFansClickListener, BaseQuickAdapter.RequestLoadMoreListener {

    private FansPresenter mFansPresenter;
    private int mPage=0;
    private  int mPageSize=20;
    private List<FansInfo.DataBean.ListBean> mListBeanList=null;
    private FansListAdapter mFansListAdapter;
    private String mAuthorID;
    private int mObjectType;

    /**
     * 创造实例
     * @param objectType 0:别人 1:自己
     * @param authorID
     * @return
     */
    public static FansListFragment newInstance(int objectType, String authorID){
        FansListFragment fansListFragment=new FansListFragment();
        Bundle bundle=new Bundle();
        bundle.putString(Constant.KEY_AUTHOR_ID,authorID);
        bundle.putInt(Constant.KEY_AUTHOR_TYPE,objectType);
        fansListFragment.setArguments(bundle);
        return fansListFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取出参数
        Bundle arguments = getArguments();
        if(null!=arguments) {
            mAuthorID = arguments.getString(Constant.KEY_AUTHOR_ID);
            mObjectType = arguments.getInt(Constant.KEY_AUTHOR_TYPE);
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
                loadFansList();
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFansPresenter = new FansPresenter(getActivity());
        mFansPresenter.attachView(this);
        showLoadingView("获取粉丝列表中...");
        initAdapter();
        //1：我的粉丝
        if(1==mObjectType){
            mListBeanList= (List<FansInfo.DataBean.ListBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINE_FANS_LIST);
            if(null!=mListBeanList) mListBeanList=new ArrayList<>();
            if(null!=mListBeanList&&mListBeanList.size()>0){
                showContentView();
            }
        }
        mPage=0;
        loadFansList();
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingView("获取粉丝列表中...");
        mPage=0;
        loadFansList();
    }

    @Override
    public void onDestroy() {
        if(null!=mFansPresenter){
            mFansPresenter.detachView();
        }
        super.onDestroy();
    }

    /**
     *初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFansListAdapter = new FansListAdapter(mListBeanList,mObjectType,this);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mFansListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("还没有粉丝呢~");
        mFansListAdapter.setOnLoadMoreListener(this);
        bindingView.recyerView.setAdapter(mFansListAdapter);

    }

    /**
     * 只新增数据
     */
    private void updataAddNewDataAdapter() {
        mFansListAdapter.addData(mListBeanList);
    }


    /**
     * 刷新全新适配器
     */
    private void updataNewDataAdapter() {
        showContentView();
        mFansListAdapter.setNewData(mListBeanList);
    }



    /**
     * 加载粉丝列表
     */
    private void loadFansList() {
        mPage++;
        mFansPresenter.getFanslist(mAuthorID,mPage+"",mPageSize+"");
    }



    /**
     * 关注事件
     * @param listBean
     * @param position
     */
    private void followUser(FansInfo.DataBean.ListBean listBean, int position) {

        if(!Utils.isCheckNetwork()){
            showErrorToast(null,null,"没有网络连接");
            return;
        }

        //已关注，弹出取消关注窗口
        if(1==listBean.getBoth_fans()){
            showUnFollowMenu(listBean);
        //未关注，直接关注
        }else{
            if(TextUtils.equals(VideoApplication.getLoginUserID(),listBean.getFans_user_id())){
                showErrorToast(null,null,"自己无法关注自己");
                return;
            }
            showProgressDialog("关注中...",true);
            mFansPresenter.onFollowUser(VideoApplication.getLoginUserID(),listBean.getFans_user_id());
        }
    }


    /**
     * 弹出取消关注窗口
     * @param listBean
     */
    private void showUnFollowMenu(final FansInfo.DataBean.ListBean listBean) {

        List<VideoDetailsMenu> list=new ArrayList<>();
        VideoDetailsMenu videoDetailsMenu1=new VideoDetailsMenu();
        videoDetailsMenu1.setItemID(1);
        videoDetailsMenu1.setTextColor("#FF576A8D");
        videoDetailsMenu1.setItemName("取消关注");
        list.add(videoDetailsMenu1);

        CommonMenuDialog commonMenuDialog =new CommonMenuDialog((AppCompatActivity) getActivity());
        commonMenuDialog.setData(list);
        commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
            @Override
            public void onItemClick(int itemID) {
                //取消关注
                switch (itemID) {
                    case 1:
                        if(!Utils.isCheckNetwork()){
                            ToastUtils.shoCenterToast("没有网络连接");
                            return;
                        }
                        unFollowUser(listBean);
                        break;
                }
            }
        });
        commonMenuDialog.show();
    }

    /**
     * 取消关注用户
     * @param listBean
     */
    private void unFollowUser(FansInfo.DataBean.ListBean listBean) {
        showProgressDialog("取消关注中...",true);
        mFansPresenter.onFollowUser(VideoApplication.getLoginUserID(),listBean.getFans_user_id());
    }

    /**
     * 加载更多数据
     */
    @Override
    public void onLoadMoreRequested() {

        if(null!=mListBeanList&&mListBeanList.size()>=10){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mFansListAdapter.setEnableLoadMore(true);
            loadFansList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mFansListAdapter.loadMoreFail();//加载失败
                    }else{
                        mFansListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }


    //==========================================点击事件=============================================

    /**
     * 条目点击事件
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        try {
            List<FansInfo.DataBean.ListBean>  data = mFansListAdapter.getData();
            if(null!=data&&data.size()>0){
                FansInfo.DataBean.ListBean listBean = data.get(position);
                AuthorDetailsActivity.start(getActivity(),listBean.getFans_user_id());
            }
        }catch (Exception e){

        }
    }

    /**
     * 关注事件
     * @param position
     * @param data
     */
    @Override
    public void onFollowFans(int position, FansInfo.DataBean.ListBean data) {
        followUser(data,position);
    }

    @Override
    public void onFollowUser(int position, FollowUserList.DataBean.ListBean data) {

    }

    @Override
    public void onMenuClick(FollowUserList.DataBean.ListBean data) {

    }

    //======================================加载数据回调==============================================
    /**
     * 粉丝列表加载成功
     * @param data
     */
    @Override
    public void showFansList(FansInfo data) {
        showContentView();
        bindingView.swiperefreshLayout.setRefreshing(false);
        mFansListAdapter.loadMoreComplete();//加载完成
        if(1==mPage){
            if(null!=mListBeanList){
                mListBeanList.clear();
            }
            mListBeanList=data.getData().getList();
            //只缓存自己的粉丝
            if(1==mObjectType){
                Log.d("FansListFragment", "showFansList: 添加缓存");
                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FANS_LIST);
                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FANS_LIST, (Serializable) mListBeanList, Constant.CACHE_TIME);
            }
            updataNewDataAdapter();
        }else {
            mListBeanList=data.getData().getList();
            updataAddNewDataAdapter();
        }
    }

    /**
     * 粉丝列表加载为空
     * @param data
     */
    @Override
    public void showFansListEmpty(String data) {
        showContentView();
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mFansListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //没有关注用户，替换空的缓存
        if(mPage==1){
            if(null!=mListBeanList){
                mListBeanList.clear();
            }
            Log.d("FansListFragment", "showFansListEmpty: 没有关注用户");
            //只缓存自己的粉丝
            if(1==mObjectType){
                Log.d("FansListFragment", "showFansListEmpty: 只缓存自己的粉丝");
                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FANS_LIST);
                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FANS_LIST, (Serializable) mListBeanList, Constant.CACHE_TIME);
            }
            updataNewDataAdapter();
        }
        if(mPage>1){
            mPage=0;
        }
    }

    /**
     * 获取粉丝列表失败
     * @param data
     */
    @Override
    public void showFansListError(String data) {

        if(1==mPage&&null==mListBeanList||mListBeanList.size()<=0){
            showLoadingErrorView();
        }

        if(mPage>0){
            mPage--;
        }
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mFansListAdapter.loadMoreFail();
            }
        });

    }

    /**
     * 关注结果
     * @param text
     */
    @Override
    public void showFollowUser(String text) {
        closeProgressDialog();
        mListBeanList = mFansListAdapter.getData();
        int poistion=0;
        try {
            JSONObject jsonObject=new JSONObject(text);
            if(1==jsonObject.getInt("code")&& TextUtils.equals(Constant.FOLLOW_SUCCESS,jsonObject.getString("msg"))){
                String userID = new JSONObject(jsonObject.getString("data")).getString("user_id");
                for (int i = 0; i < mListBeanList.size(); i++) {
                    if(TextUtils.equals(userID,mListBeanList.get(i).getFans_user_id())){
                        poistion=i;
                        mListBeanList.get(i).setBoth_fans(1);
                        break;
                    }
                }

                updateView(poistion);
            }else if(1==jsonObject.getInt("code")&& TextUtils.equals(Constant.FOLLOW_UNSUCCESS,jsonObject.getString("msg"))){
                String userID = new JSONObject(jsonObject.getString("data")).getString("user_id");
                for (int i = 0; i < mListBeanList.size(); i++) {
                    if(TextUtils.equals(userID,mListBeanList.get(i).getFans_user_id())){
                        poistion=i;
                        mListBeanList.get(i).setBoth_fans(0);
                        break;
                    }
                }
                updateView(poistion);
            }
            VideoApplication.isFolloUser=true;
            showFinlishToast(null,null,jsonObject.getString("msg"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷新单个条目
     * @param poistion
     */
    private void updateView(int poistion) {
        mFansListAdapter.notifyItemChanged(poistion);
        //如果当前为第一页并且是用户自己的粉丝列表，替换修改数据后的最新缓存
        if(1==mObjectType&&1==mPage){
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FANS_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FANS_LIST, (Serializable) mListBeanList, Constant.CACHE_TIME);
        }
    }


    /**
     *关注者列表，这里不用
     * @param data
     */
    @Override
    public void showFollowUserList(FollowUserList data) {

    }

    @Override
    public void showFollowUserListEmpty(String data) {

    }

    @Override
    public void showFollowUserListError(String data) {

    }


    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }
}
