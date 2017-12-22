package com.video.newqu.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.MessageListAdapter;
import com.video.newqu.base.BaseMineFragment;
import com.video.newqu.bean.NetMessageInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.MineAuthorRecylerviewEmptyLayoutBinding;
import com.video.newqu.databinding.MineMessageFragmentRecylerBinding;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.MainActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.activity.WebViewActivity;
import com.video.newqu.ui.contract.MessageContract;
import com.video.newqu.ui.presenter.MessagePresenter;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/6/5 10:26
 * 我的消息,推送
 */

public class HomeMessageFragment extends BaseMineFragment<MineMessageFragmentRecylerBinding> implements BaseQuickAdapter.RequestLoadMoreListener , MessageContract.View {

    private static final String TAG = HomeMessageFragment.class.getSimpleName();
    private MessageListAdapter mMessageListAdapter;
    private MainActivity mMainActivity;
    private MessagePresenter mMessagePresenter;
    private boolean isRefresh=true;//是否需要刷新

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showContentView();
        initAdapter();
        mMessagePresenter = new MessagePresenter(getActivity());
        mMessagePresenter.attachView(this);
        getMessageList();
    }

    @Override
    protected void initViews() {
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMessageList();
            }
        });
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(isRefresh&&null!=bindingView&&null!=mMessageListAdapter&&null!=mMessagePresenter&&!mMessagePresenter.isLoading()){
            bindingView.recyerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindingView.swiperefreshLayout.setRefreshing(true);
                    getMessageList();
                }
            },200);
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
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }


    @Override
    public int getLayoutId() {
        return R.layout.mine_message_fragment_recyler;
    }


    @Override
    public void onDestroy() {
        if(null!=mMessagePresenter) mMessagePresenter.detachView();
        super.onDestroy();
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();
        getMessageList();
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        List<NetMessageInfo.DataBean.ListBean> list= (List<NetMessageInfo.DataBean.ListBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_HOME_MESSAGE_LIST);//读取缓存
        if(null!=list&&list.size()>0){
            Collections.sort(list, new Comparator<NetMessageInfo.DataBean.ListBean>() {
                @Override
                public int compare(NetMessageInfo.DataBean.ListBean o1, NetMessageInfo.DataBean.ListBean o2) {
                    Long addTimeO2 = Long.parseLong(o2.getAdd_time());
                    Long addTimeO1 = Long.parseLong(o1.getAdd_time());
                    return addTimeO2.compareTo(addTimeO1);
                }
            });
        }
        bindingView.recyerView.setLayoutManager( new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mMessageListAdapter = new MessageListAdapter(list);
        bindingView.recyerView.setAdapter(mMessageListAdapter);
        MineAuthorRecylerviewEmptyLayoutBinding emptybindView = DataBindingUtil.inflate(getActivity().getLayoutInflater(), R.layout.mine_author_recylerview_empty_layout, (ViewGroup) bindingView.recyerView.getParent(), false);
        mMessageListAdapter.setEmptyView(emptybindView.getRoot());
        emptybindView.ivItemIcon.setImageResource(R.drawable.iv_message_empty);
        emptybindView.tvItemName.setText("暂时没有消息~");
        mMessageListAdapter.setOnLoadMoreListener(this);
        mMessageListAdapter.setOnItemClickListener(new MessageListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int poistion) {
                if(null!=mMessageListAdapter){
                    List<NetMessageInfo.DataBean.ListBean> data = mMessageListAdapter.getData();
                    if(null!=data&&mMessageListAdapter.getData().size()>0){
                        final NetMessageInfo.DataBean.ListBean listBean = data.get(poistion);
                        if(null!=listBean&&null!=listBean.getType()){
                            String type = listBean.getType();
                            if(!TextUtils.isEmpty(type)){
                                if(TextUtils.equals("1",type)){
                                    WebViewActivity.loadUrl(getActivity(),listBean.getUrl(),listBean.getAction());
                                }else if(TextUtils.equals("2",type)){
                                    VideoDetailsActivity.start(getActivity(),listBean.getVideo_id(),listBean.getUser_id(),false);
                                }else if(TextUtils.equals("-1",type)){
                                    if(!TextUtils.isEmpty(listBean.getAction())){
                                        //微信
                                        if(TextUtils.equals("weixin://",listBean.getAction())){
                                            Utils.copyString("新趣小视频");
                                            ToastUtils.shoCenterToast("已复制微信号");
                                            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity())
                                                    .setTitle("新趣小视频")
                                                    .setMessage(getResources().getString(R.string.open_weixin_tips));
                                            builder.setNegativeButton("算了", null);
                                            builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    try {
                                                        Uri uri = Uri.parse(listBean.getAction());
                                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                        startActivity(intent);
                                                    } catch (Exception e) {
                                                        //若无法正常跳转，在此进行错误处理
                                                        ToastUtils.shoCenterToast("无法跳转到微信，请检查设备是否安装了微信！");
                                                    }
                                                }
                                            });
                                            builder.setCancelable(false);
                                            builder.show();
                                            return;
                                        //话题
                                        }else if(TextUtils.equals("com.xinqu.media.topic",listBean.getAction())){
                                            Intent intent=new Intent(listBean.getAction());
                                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.KEY_FRAGMENT_TYPE_TOPIC_VIDEO_LISTT);
                                            intent.putExtra(Constant.KEY_TITLE,listBean.getUrl());
                                            intent.putExtra(Constant.KEY_VIDEO_TOPIC_ID,listBean.getUrl());
                                            startActivity(intent);
                                            return;
                                        //其他类型的
                                        }else {
                                            try {
                                                Intent intent = new Intent(listBean.getAction());
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                //若无法正常跳转，在此进行错误处理
                                                ToastUtils.shoCenterToast("处理失败："+e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            @Override
            public void onUserClick(String userID) {
                if(null!=userID){
                    AuthorDetailsActivity.start(getActivity(),userID);
                }
            }

            @Override
            public void onOthorClick(String action) {
                if(null!=action){
                    ToastUtils.shoCenterToast(action);
                }
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        if(null!=mMessageListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMessageListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
        }
    }

    /**
     * 第一次加载和加载更多
     */
    private void getMessageList() {
        if(null!=mMessagePresenter&&!mMessagePresenter.isLoading()){
            mMessagePresenter.getMessageList();
        }
    }

    @Override
    public void showMessageInfo(List<NetMessageInfo.DataBean.ListBean> data) {
        isRefresh=false;
        bindingView.swiperefreshLayout.setRefreshing(false);
        Fragment parentFragment = getParentFragment();
        if(null!=parentFragment&&parentFragment instanceof MineFragment){
            ((MineFragment) parentFragment).updataTab(data.size());
        }
        SharedPreferencesUtil.getInstance().putInt(Constant.KEY_MSG_COUNT,data.size());
        ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_HOME_MESSAGE_LIST);
        ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_HOME_MESSAGE_LIST, (Serializable) data,Constant.CACHE_TIME);
        if(null!= mMessageListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMessageListAdapter.loadMoreComplete();//加载完成
                }
            });
            Collections.sort(data, new Comparator<NetMessageInfo.DataBean.ListBean>() {
                @Override
                public int compare(NetMessageInfo.DataBean.ListBean o1, NetMessageInfo.DataBean.ListBean o2) {
                    Long addTimeO2 = Long.parseLong(o2.getAdd_time());
                    Long addTimeO1 = Long.parseLong(o1.getAdd_time());
                    return addTimeO2.compareTo(addTimeO1);
                }
            });
            mMessageListAdapter.setNewData(data);
        }
    }

    @Override
    public void showMessageError(String data) {
        isRefresh=false;
        if(null==mMessageListAdapter.getData()||mMessageListAdapter.getData().size()<=0){
            showLoadingErrorView();
        }
        bindingView.swiperefreshLayout.setRefreshing(false);
        if(null!= mMessageListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMessageListAdapter.loadMoreFail();
                }
            });
        }
    }

    @Override
    public void showErrorView() {
        isRefresh=false;
    }

    @Override
    public void complete() {

    }


    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity=null;
    }

    /**
     * 来自首页的刷新
     */
    public void fromMainUpdata() {

        if(null==VideoApplication.getInstance().getUserData()){
            if(null!=mMainActivity&&!mMainActivity.isFinishing()){
                ToastUtils.shoCenterToast("请先登录再刷新");
                mMainActivity.login();
            }
            return;
        }
        if(null!= mMessageListAdapter){
            if(null!=mMessagePresenter&&!mMessagePresenter.isLoading()){
                List<NetMessageInfo.DataBean.ListBean> data = mMessageListAdapter.getData();
                if(null!=data&&data.size()>0){
                    bindingView.recyerView.post(new Runnable() {
                        @Override
                        public void run() {
                            bindingView.recyerView.scrollToPosition(0);
                        }
                    });
                }
                bindingView.swiperefreshLayout.setRefreshing(true);
                getMessageList();
            }else{
                showErrorToast(null,null,"刷新太频繁了");
            }
        }else{
            showErrorToast(null,null,"刷新错误!");
        }
    }

    public void changeUI() {
        if(null==VideoApplication.getInstance().getUserData()){
            showLoginView();
        }else{
            showContentView();
        }
    }
}