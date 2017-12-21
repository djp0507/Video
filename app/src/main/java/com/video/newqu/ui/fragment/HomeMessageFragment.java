package com.video.newqu.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.MessageListAdapter;
import com.video.newqu.base.BaseMineFragment;
import com.video.newqu.bean.NotifactionMessageInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.MineAuthorRecylerviewEmptyLayoutBinding;
import com.video.newqu.databinding.MineMessageFragmentRecylerBinding;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.MainActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.activity.WebViewActivity;
import com.video.newqu.ui.contract.MessageContract;
import com.video.newqu.ui.presenter.MessagePresenter;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/6/5 10:26
 * 我的消息,推送
 */

public class HomeMessageFragment extends BaseMineFragment<MineMessageFragmentRecylerBinding> implements BaseQuickAdapter.RequestLoadMoreListener , MessageContract.View {

    private MessageListAdapter mMessageListAdapter;
    private MainActivity mMainActivity;
    private WeakReference<MessagePresenter> mPresenterWeakReference;
    private int mPage=0;
    private int mPageSize=10;



    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showContentView();
        initPresenter();
        initAdapter();
    }

    @Override
    protected void initViews() {
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                getMessageList();
            }
        });
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(null!=bindingView&&null!=mMessageListAdapter&&null==mMessageListAdapter.getData()||mMessageListAdapter.getData().size()<=0){
            bindingView.recyerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindingView.swiperefreshLayout.setRefreshing(true);
                    if(null==mPresenterWeakReference||null==mPresenterWeakReference.get()){
                        initPresenter();
                    }
                    if(null!=VideoApplication.getInstance().getUserData()&&!mPresenterWeakReference.get().isLoading()){
                        mPage=0;
                        getMessageList();
                    }
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
        if(null!=mPresenterWeakReference&&null!=mPresenterWeakReference.get()){
            mPresenterWeakReference.get().detachView();
        }
        super.onDestroy();
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();
        mPage=0;
        getMessageList();
    }


    private void initPresenter() {
        MessagePresenter messagePresenter = new MessagePresenter(getActivity());
        messagePresenter.attachView(this);
        mPresenterWeakReference = new WeakReference<MessagePresenter>(messagePresenter);
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager( new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mMessageListAdapter = new MessageListAdapter(null);
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
                    List<NotifactionMessageInfo> data = mMessageListAdapter.getData();
                    if(null!=data&&mMessageListAdapter.getData().size()>0){
                        NotifactionMessageInfo messageInfo = data.get(poistion);
                        if(null!=messageInfo){
                            switch (messageInfo.getItemType()) {
                                //视频
                                case 0:
                                    String video_id = messageInfo.getVideo_id();
                                    if(!TextUtils.isEmpty(video_id)){
                                        VideoDetailsActivity.start(getActivity(),video_id,messageInfo.getUser_id(),false);
                                    }
                                    break;
                                //网页
                                case 1:
                                    if(null!=messageInfo.getWebUrl()){
                                        WebViewActivity.loadUrl(getActivity(),messageInfo.getWebUrl(),"百度图片");
                                    }
                                    break;
                                //其他（暂时测试话题）
                                case 2:
                                    if(null!=messageInfo.getTopic()){
                                        startTargetActivity(Constant.KEY_FRAGMENT_TYPE_TOPIC_VIDEO_LISTT,messageInfo.getTopic(),VideoApplication.getLoginUserID(),0,messageInfo.getTopic());
                                    }
                                    break;
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
            public void onOthorClick(String topic) {
                if(null!=topic){
                    startTargetActivity(Constant.KEY_FRAGMENT_TYPE_TOPIC_VIDEO_LISTT,topic,VideoApplication.getLoginUserID(),0,topic);
                }
            }
        });
    }


    @Override
    public void onLoadMoreRequested() {
        if(null!=mMessageListAdapter){
            mMessageListAdapter.setEnableLoadMore(true);
            getMessageList();
        }
    }



    /**
     * 第一次加载和加载更多
     */
    private void getMessageList() {
        if(null==mPresenterWeakReference||null==mPresenterWeakReference.get()){
            initPresenter();
        }
        if(null!=VideoApplication.getInstance().getUserData()&&!mPresenterWeakReference.get().isLoading()){
            mPage++;
            mPresenterWeakReference.get().getMessageList(VideoApplication.getLoginUserID(),mPage+"",mPageSize+"");
        }
    }


    @Override
    public void showMessageInfo(List<NotifactionMessageInfo> data) {
        bindingView.swiperefreshLayout.setRefreshing(false);
        if(null!= mMessageListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMessageListAdapter.loadMoreComplete();//加载完成
                }
            });
            if(1==mPage){
                mMessageListAdapter.setNewData(data);
            }else{
                mMessageListAdapter.addData(data);
            }
        }
    }

    @Override
    public void getMessageError(String data) {
        if(1==mPage&&null==mMessageListAdapter.getData()||mMessageListAdapter.getData().size()<=0){
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
        if(mPage>0){
            mPage--;
        }
    }


    @Override
    public void getMessageEmpty(String data) {
        bindingView.swiperefreshLayout.setRefreshing(false);
        if (null != mMessageListAdapter) {
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMessageListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
        }
        if(mPage>0){
            mPage--;
        }
    }

    @Override
    public void showErrorView() {

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
        if(null==mPresenterWeakReference||null==mPresenterWeakReference.get()){
            initPresenter();
        }
        if(null!= mMessageListAdapter){
            if(!mPresenterWeakReference.get().isLoading()){
                bindingView.swiperefreshLayout.setRefreshing(true);
                mPage=0;
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