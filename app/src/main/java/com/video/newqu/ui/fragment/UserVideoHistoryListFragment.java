package com.video.newqu.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.UserHistoryVideoListAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.SubmitEvent;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentHistoryListBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.listener.OnUserPlayerHistoryClickListener;
import com.video.newqu.model.RecyclerViewSpacesItem;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.activity.VerticalHistoryVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.UserHistoryContract;
import com.video.newqu.ui.presenter.UserHistoryPresenter;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/10/12
 * 用户观看过的视频记录
 * 为了排序，直接一次性加载全部，取消分页
 */

public class UserVideoHistoryListFragment extends BaseFragment<FragmentHistoryListBinding> implements UserHistoryContract.View,OnUserPlayerHistoryClickListener {

    private static final String TAG = UserVideoHistoryListFragment.class.getSimpleName();
//    private int page=0;
//    private int pageSize=10;
    private UserHistoryVideoListAdapter mUserHistoryVideoListAdapter;
    private UserHistoryPresenter mUserHistoryPresenter;
    private ContentFragmentActivity mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (ContentFragmentActivity) context;
    }

    @Override
    protected void initViews() {
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                page=0;
                loadListData();
            }
        });
    }


    @Override
    public int getLayoutId() {
        return R.layout.fragment_history_list;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showLoadingView("加载历史记录中..");
        initAdapter();
        mUserHistoryPresenter = new UserHistoryPresenter();
        mUserHistoryPresenter.attachView(this);
//        page=0;
        loadListData();
        //第一次使用弹出使用提示
        if(1!= SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_USER_HISTORY_CODE)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //删除视频提示
                    new android.support.v7.app.AlertDialog.Builder(getActivity())
                            .setTitle(R.string.hint)
                            .setTitle("历史观看记保存录规则")
                            .setMessage("播放视频记录最多只保存最近播放的200条视频记录，超过200条以新替旧。播放完成的视频在不删除缓存文件的情况下再次播放无需消耗流量")
                            .setPositiveButton("知道了",
                                    null).setCancelable(false).show();
                    SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_USER_HISTORY_CODE,1);
                }
            },800);
        }
    }


    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),3,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new RecyclerViewSpacesItem(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(false);
        mUserHistoryVideoListAdapter = new UserHistoryVideoListAdapter(null,this);
        mUserHistoryVideoListAdapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
            @Override
            public void onLoadMoreRequested() {
                bindingView.recyerView.post(new Runnable() {
                    @Override
                    public void run() {
                        mUserHistoryVideoListAdapter.loadMoreEnd();
                    }
                });
            }
        });
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mUserHistoryVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有播放视频记录");
        bindingView.recyerView.setAdapter(mUserHistoryVideoListAdapter);
    }


    /**
     * 加载历史记录
     */
    private void loadListData() {
        if(null!=mUserHistoryPresenter&&!mUserHistoryPresenter.isVideoLoading()){
//            page++;
//            mUserHistoryPresenter.getVideoHistoryList(page,pageSize);
            mUserHistoryPresenter.getAllVideoHistoryList();
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
     * 提交事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SubmitEvent event){
        if(null!=event){
            if(TextUtils.equals("caneal_history",event.getMessage())){
                new android.support.v7.app.AlertDialog.Builder(getActivity())
                        .setTitle("删除提示")
                        .setMessage("清空历史播放记录后无法恢复，确定继续吗？")
                        .setNegativeButton("取消",null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(null!=mUserHistoryVideoListAdapter){
                                    ApplicationManager.getInstance().getUserPlayerDB().deteleAllPlayerHistoryList();
                                    mUserHistoryVideoListAdapter.setNewData(null);
                                    if(null!=mContext&&!mContext.isFinishing()){
//                                        page=0;
                                        loadListData();
                                    }
                                }
                            }
                        }).setCancelable(false).show();
            }
        }
    }

    /**
     * 订阅界面刷新
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChangingViewEvent event){
        if(null!=event&&Constant.FRAGMENT_TYPE_HOSTORY==event.getFragmentType()){
            final int poistion = event.getPoistion();
//            this.page=event.getPage();
            if(null!=mUserHistoryVideoListAdapter){
                bindingView.recyerView.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            bindingView.recyerView.scrollToPosition(poistion);
                        }catch (Exception e){

                        }
                    }
                });
            }
        }
    }



    //=======================================点击时间回调=============================================

    @Override
    public void onUserIcon(String userID) {
        if(!TextUtils.isEmpty(userID)){
            AuthorDetailsActivity.start(getActivity(),userID);
        }
    }

    @Override
    public void onDeleteVideo(UserPlayerVideoHistoryList data, int poistion) {

        ApplicationManager.getInstance().getUserPlayerDB().deletePlayerHistoryOfObject(data);
        if(null!=mUserHistoryVideoListAdapter){
            mUserHistoryVideoListAdapter.remove(poistion);
        }
        List<UserPlayerVideoHistoryList> newData = mUserHistoryVideoListAdapter.getData();
        if(null!=mContext&&!mContext.isFinishing()){
            if(null!=newData&&newData.size()>0){
                mContext.showCanealHistoryMenu(true);
            }else{
                mContext.showCanealHistoryMenu(false);
            }
        }
    }

    @Override
    public void onItemClick(int poistion) {
        if(null!=mUserHistoryVideoListAdapter) {
            List<UserPlayerVideoHistoryList> data = mUserHistoryVideoListAdapter.getData();
            if (null != data && data.size() > 0) {
                //全屏
                if (ConfigSet.getInstance().isPlayerModel()) {
//                    VerticalHistoryVideoPlayActivity.start(getActivity(), Constant.FRAGMENT_TYPE_HOSTORY,poistion,page,pageSize,data);
                    VerticalHistoryVideoPlayActivity.start(getActivity(), Constant.FRAGMENT_TYPE_HOSTORY,poistion,data);
                    //单个
                } else {
                    UserPlayerVideoHistoryList userPlayerVideoHistoryList = data.get(poistion);
                    if (null != userPlayerVideoHistoryList && !TextUtils.isEmpty(userPlayerVideoHistoryList.getVideoId())) {
                        VideoDetailsActivity.start(getActivity(), userPlayerVideoHistoryList.getVideoId(), userPlayerVideoHistoryList.getUserId(), true);
                    }
                }
            }
        }
    }



    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }

    @Override
    public void showVideoHistoryList(List<UserPlayerVideoHistoryList> data) {
        if(null!=mContext&&!mContext.isFinishing()){
            mContext.showCanealHistoryMenu(true);
        }
        showContentView();

        bindingView.swiperefreshLayout.setRefreshing(false);
        if(null!=mUserHistoryVideoListAdapter){
            mUserHistoryVideoListAdapter.setNewData(data);
//            if(1==page){
//                mUserHistoryVideoListAdapter.setNewData(data);
//            }else{
//                mUserHistoryVideoListAdapter.addData(data);
//            }
        }
    }

    @Override
    public void showVideoHistoryListEmpty(String data) {
        showContentView();
        if(null!=mUserHistoryVideoListAdapter){
            List<UserPlayerVideoHistoryList> dataList = mUserHistoryVideoListAdapter.getData();
            if(null!=mContext&&!mContext.isFinishing()&&null==dataList||dataList.size()<=0){
                mContext.showCanealHistoryMenu(false);
            }
            bindingView.swiperefreshLayout.setRefreshing(false);
        }
//        if(page>0){
//            page--;
//        }
    }

    @Override
    public void onDestroy() {
        if(null!=mUserHistoryPresenter){
            mUserHistoryPresenter.detachView();
        }
        mUserHistoryVideoListAdapter=null;
        super.onDestroy();
    }
}
