package com.video.newqu.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.FollowUserListAdapter;
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
 * 已关注的用户列表，用户自己的和别人的 1：自己的 0:其他用户的
 */

public class FollowUserListFragment extends BaseFragment<FragmentRecylerBinding> implements FansContract.View
        ,OnFansClickListener, BaseQuickAdapter.RequestLoadMoreListener {

    private FansPresenter mFansPresenter;
    private int mPage=0;
    private  int mPageSize=20;
    private List<FollowUserList.DataBean.ListBean> mListBeanList=null;
    private FollowUserListAdapter mFollowUserListAdapter;
    private String mAuthorID;
    private int mObjectType;

    /**
     * 创造实例
     * @param objectType
     * @param authorID
     * @return
     */
    public static FollowUserListFragment newInstance(int objectType, String authorID){
        FollowUserListFragment followUserListFragment=new FollowUserListFragment();
        Bundle bundle=new Bundle();
        bundle.putString(Constant.KEY_AUTHOR_ID,authorID);
        bundle.putInt(Constant.KEY_AUTHOR_TYPE,objectType);
        followUserListFragment.setArguments(bundle);
        return followUserListFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        //取出参数
        if(null!=arguments){
            mAuthorID = arguments.getString(Constant.KEY_AUTHOR_ID);
            mObjectType = arguments.getInt(Constant.KEY_AUTHOR_TYPE);
        }
    }


    @Override
    public void onDestroy() {
        if(null!=mFansPresenter){
            mFansPresenter.detachView();
        }
        super.onDestroy();
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
                loadFollowsList();
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
        showLoadingView("获取关注用户中...");
        initAdapter();
        //1：我的关注 0:别人的
        if(1==mObjectType){

            mListBeanList= (List<FollowUserList.DataBean.ListBean>)  ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINE_FOLLOW_USER_LIST);
            if(null==mListBeanList) mListBeanList=new ArrayList<>();
            if(null!=mListBeanList&&mListBeanList.size()>0){
                updataNewDataAdapter();
            }
        }
        mPage=0;
        loadFollowsList();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingView("获取关注用户中...");
        mPage=0;
        loadFollowsList();
    }

    /**
     *初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setHasFixedSize(false);
        bindingView.recyerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mFollowUserListAdapter = new FollowUserListAdapter(mListBeanList,mObjectType,this);
        mFollowUserListAdapter.setOnLoadMoreListener(this);
        setEmptyView();
        bindingView.recyerView.setAdapter(mFollowUserListAdapter);
    }


    /**
     * 设置空内容显示
     */
    private void setEmptyView() {
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mFollowUserListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有关注的用户呢~!");
    }

    /**
     * 只新增数据
     */
    private void updataAddNewDataAdapter() {
        mFollowUserListAdapter.addData(mListBeanList);
    }


    /**
     * 刷新全新适配器
     */
    private void updataNewDataAdapter() {
        showContentView();
        mFollowUserListAdapter.setNewData(mListBeanList);
    }


    /**
     * 弹出取消窗口
     * @param data
     */
    private void showUnFollowMenu(final FollowUserList.DataBean.ListBean data) {

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
                        unFollowUser(data);
                        break;
                }
            }
        });
        commonMenuDialog.show();
    }


    /**
     * 取消关注用户
     * @param data
     */
    private void unFollowUser(FollowUserList.DataBean.ListBean data) {
        showProgressDialog("取消关注中...",true);
        mFansPresenter.onFollowUser(VideoApplication.getLoginUserID(),data.getUser_id());
    }

    /**
     * 关注用户
     * @param data
     */
    private void followUser(FollowUserList.DataBean.ListBean data) {

        if(!Utils.isCheckNetwork()){
            ToastUtils.shoCenterToast("没有网络连接");
            return;
        }
        if(null!=VideoApplication.getInstance().getUserData()&&TextUtils.equals(VideoApplication.getLoginUserID(),data.getUser_id())){
            ToastUtils.shoCenterToast("自己无法关注自己");
            return;
        }
        showProgressDialog("关注中...",true);
        mFansPresenter.onFollowUser(VideoApplication.getLoginUserID(),data.getUser_id());
    }



    /**
     * 加载关注列表
     */
    private void loadFollowsList() {
        mPage++;
        mFansPresenter.getFollowUserList(mAuthorID,mPage+"",mPageSize+"");
    }


    /**
     * 刷新单个条目
     * @param poistion
     */
    private void updateView(int poistion) {
        mFollowUserListAdapter.notifyItemChanged(poistion);
        //如果当前为第一页并且是用户自己的粉丝列表，替换修改数据后的最新缓存
        if(1==mObjectType&&1==mPage){
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FOLLOW_USER_LIST);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FOLLOW_USER_LIST, (Serializable) mListBeanList, Constant.CACHE_TIME);
        }
    }

    /**
     * 加载更多数据
     */
    @Override
    public void onLoadMoreRequested() {
        if(null!=mListBeanList&&mListBeanList.size()>=10){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mFollowUserListAdapter.setEnableLoadMore(true);
            loadFollowsList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mFollowUserListAdapter.loadMoreFail();//加载失败
                    }else{
                        mFollowUserListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }

    //=======================================点击事件回调=============================================

    /**
     * 条目点击事件
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        try {
            List<FollowUserList.DataBean.ListBean>  data = mFollowUserListAdapter.getData();
            if(null!=data&&data.size()>0){
                FollowUserList.DataBean.ListBean listBean = data.get(position);
                AuthorDetailsActivity.start(getActivity(),listBean.getUser_id());
            }
        }catch (Exception e){

        }
    }

    @Override
    public void onFollowFans(int position, FansInfo.DataBean.ListBean data) {

    }

    /**
     * 关注用户
     * @param position
     * @param data
     */
    @Override
    public void onFollowUser(int position, FollowUserList.DataBean.ListBean data) {
        followUser(data);
    }

    /**
     * 打开菜单
     * @param data
     */
    @Override
    public void onMenuClick(FollowUserList.DataBean.ListBean data) {
        showUnFollowMenu(data);
    }


    //=======================================加载数据回调=============================================

    /**
     * 关注结果
     * @param text
     */
    @Override
    public void showFollowUser(String text) {
        closeProgressDialog();
        mListBeanList = mFollowUserListAdapter.getData();
        int poistion=0;
        try {
            JSONObject jsonObject=new JSONObject(text);
            if(1==jsonObject.getInt("code")&& TextUtils.equals(Constant.FOLLOW_SUCCESS,jsonObject.getString("msg"))){
                String userID = new JSONObject(jsonObject.getString("data")).getString("user_id");
                for (int i = 0; i < mListBeanList.size(); i++) {
                    if(TextUtils.equals(userID,mListBeanList.get(i).getUser_id())){
                        poistion=i;
                        mListBeanList.get(i).setIs_follow(1);
                        break;
                    }
                }
                updateView(poistion);
            }else if(1==jsonObject.getInt("code")&& TextUtils.equals(Constant.FOLLOW_UNSUCCESS,jsonObject.getString("msg"))){
                String userID = new JSONObject(jsonObject.getString("data")).getString("user_id");
                for (int i = 0; i < mListBeanList.size(); i++) {
                    if(TextUtils.equals(userID,mListBeanList.get(i).getUser_id())){
                        poistion=i;
                        mListBeanList.get(i).setIs_follow(0);
                        break;
                    }
                }
                VideoApplication.isFolloUser=true;
                updateView(poistion);
            }
            showFinlishToast(null,null,jsonObject.getString("msg"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 获取关注着列表成功
     * @param data
     */
    @Override
    public void showFollowUserList(FollowUserList data) {
        showContentView();
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mFollowUserListAdapter.loadMoreComplete();//加载完成
            }
        });

        if(1==mPage){
            if(null!=mListBeanList){
                mListBeanList.clear();
            }
            mListBeanList=data.getData().getList();
            updataNewDataAdapter();
            //只缓存自己的粉丝
            if(1==mObjectType){

                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FOLLOW_USER_LIST);
                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FOLLOW_USER_LIST, (Serializable) mListBeanList, Constant.CACHE_TIME);
            }
        }else {
            mListBeanList=data.getData().getList();
            updataAddNewDataAdapter();
        }
    }

    /**
     * 获取关注者列表为空
     * @param data
     */
    @Override
    public void showFollowUserListEmpty(String data) {

        showContentView();
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mFollowUserListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //没有关注用户，替换空的缓存
        if(mPage==1){
            if(null!=mListBeanList){
                mListBeanList.clear();
            }
            //只缓存自己的粉丝
            if(1==mObjectType){
                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_FOLLOW_USER_LIST);
                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_FOLLOW_USER_LIST, (Serializable) mListBeanList, Constant.CACHE_TIME);
            }
            updataNewDataAdapter();
        }
        if(mPage>1){
            mPage=0;
        }
    }

    /**
     * 获取关注着列表失败
     * @param data
     */
    @Override
    public void showFollowUserListError(String data) {

        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mFollowUserListAdapter.loadMoreFail();
            }
        });


        if(1==mPage&&null==mListBeanList||mListBeanList.size()<=0){
            showLoadingErrorView();
        }

        if(mPage>0){
            mPage--;
        }
    }

    @Override
    public void showFansList(FansInfo data) {

    }

    @Override
    public void showFansListEmpty(String data) {

    }

    @Override
    public void showFansListError(String data) {

    }

    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }
}
