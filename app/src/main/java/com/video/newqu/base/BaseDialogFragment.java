package com.video.newqu.base;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.listener.PerfectClickListener;
import com.video.newqu.listener.SnackBarListener;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.dialog.LoadingProgressView;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.ToastUtils;

/**
 * TinyHung@outlook.com
 * 2017/3/17 15:46
 * 片段基类
 */

public abstract class BaseDialogFragment<VS extends ViewDataBinding> extends DialogFragment {

    // 子布局view
    protected VS bindingView;
    protected AppCompatActivity context;
    protected LoadingProgressView mLoadingProgressedView;
    //未登录
    private LinearLayout mLl_login_view;
    //数据加载失败界面
    private LinearLayout mLl_error_view;
    //数据加载中界面
    private LinearLayout mLl_loading_view;
    //加载中动画
    private AnimationDrawable mAnimationDrawable;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context= (AppCompatActivity) context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.BOTTOM);//((ViewGroup) window.findViewById(android.R.id.content))
        View ll = inflater.inflate(R.layout.fragment_base, (ViewGroup) window.findViewById(R.id.content),false);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        window.setLayout(ScreenUtils.getScreenWidth(),ScreenUtils.getScreenHeight());//这2行,和上面的一样,注意顺序就行;
        window.setWindowAnimations(R.style.HomeItemPopupAnimation);
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.dimAmount=0.0f;
        window.setAttributes(attributes);

        bindingView = DataBindingUtil.inflate(getActivity().getLayoutInflater(), getLayoutId(), null, false);
        if(null!=bindingView){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            bindingView.getRoot().setLayoutParams(params);
            RelativeLayout contentView = (RelativeLayout) ll.findViewById(R.id.re_conttent_view);
            contentView.addView(bindingView.getRoot());
        }
        return ll;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
        mLl_login_view = getView(R.id.ll_login_view);
        mLl_error_view = getView(R.id.ll_error_view);
        mLl_loading_view = getView(R.id.ll_loading_view);

        ImageView iv_loading_icon = getView(R.id.iv_loading_icon);
        mAnimationDrawable = (AnimationDrawable) iv_loading_icon.getDrawable();

        mLl_error_view.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                onRefresh();
            }
        });
        //点击了头像登录
        getView(R.id.iv_header_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
        //登录
        getView(R.id.bt_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });
        //默认显示加载中的状态
        showLoadingView("初始化中...");
    }

    /**
     * 根据Targe打开新的界面
     * @param title
     * @param fragmentTarge
     */
    protected void startTargetActivity(int fragmentTarge,String title,String authorID,int authorType) {
        Intent intent=new Intent(getActivity(), ContentFragmentActivity.class);
        intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentTarge);
        intent.putExtra(Constant.KEY_TITLE,title);
        intent.putExtra(Constant.KEY_AUTHOR_ID,authorID);
        intent.putExtra(Constant.KEY_AUTHOR_TYPE,authorType);
        getActivity().startActivity(intent);
    }


    /**
     * 根据Targe打开新的界面
     * @param title
     * @param fragmentTarge
     */
    protected void startTargetActivity(int fragmentTarge,String title,String authorID,int authorType,String topicID) {
        Intent intent=new Intent(getActivity(), ContentFragmentActivity.class);
        intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentTarge);
        intent.putExtra(Constant.KEY_TITLE,title);
        intent.putExtra(Constant.KEY_AUTHOR_ID,authorID);
        intent.putExtra(Constant.KEY_AUTHOR_TYPE,authorType);
        intent.putExtra(Constant.KEY_VIDEO_TOPIC_ID,topicID);
        getActivity().startActivity(intent);
    }



    /**
     * 显示登录界面
     */
    protected void showLoginView(){

        if(null!=mAnimationDrawable&&null!=context&&!context.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(null!=mLl_loading_view&&mLl_loading_view.getVisibility()!=View.GONE){
            mLl_loading_view.setVisibility(View.GONE);
        }

        if(null!=mLl_error_view&&mLl_error_view.getVisibility()!=View.GONE){
            mLl_error_view.setVisibility(View.GONE);
        }

        if(null!=bindingView&&null!=bindingView.getRoot()&&bindingView.getRoot().getVisibility()!=View.GONE){
            bindingView.getRoot().setVisibility(View.GONE);
        }

        if(null!=mLl_login_view&&mLl_login_view.getVisibility()!=View.VISIBLE){
            mLl_login_view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示加载中
     */
    protected void showLoadingView(String message){

        if(null!=bindingView.getRoot()&&bindingView.getRoot().getVisibility()!=View.GONE){
            bindingView.getRoot().setVisibility(View.GONE);
        }

        if(null!=mLl_error_view&&mLl_error_view.getVisibility()!=View.GONE){
            mLl_error_view.setVisibility(View.GONE);
        }

        if(null!=mLl_login_view&&mLl_login_view.getVisibility()!=View.GONE){
            mLl_login_view.setVisibility(View.GONE);
        }

        if(null!=mLl_loading_view&&mLl_loading_view.getVisibility()!=View.VISIBLE){
            mLl_loading_view.setVisibility(View.VISIBLE);
        }


        if(null!=mAnimationDrawable&&!getActivity().isFinishing()&&!mAnimationDrawable.isRunning()){
            mAnimationDrawable.start();
        }
    }


    /**
     * 显示界面内容
     */
    protected void showContentView() {

        if(null!=mAnimationDrawable&&null!=context&&!context.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(null!=mLl_loading_view&&mLl_loading_view.getVisibility()!=View.GONE){
            mLl_loading_view.setVisibility(View.GONE);
        }

        if(null!=mLl_error_view&&mLl_error_view.getVisibility()!=View.GONE){
            mLl_error_view.setVisibility(View.GONE);
        }

        if(null!=mLl_login_view&&mLl_login_view.getVisibility()!=View.GONE){
            mLl_login_view.setVisibility(View.GONE);
        }


        if(null!=bindingView&&null!=bindingView.getRoot()&&bindingView.getRoot().getVisibility()!=View.VISIBLE){
            bindingView.getRoot().setVisibility(View.VISIBLE);
        }
    }


    /**
     * 显示加载失败
     */
    protected void showLoadingErrorView() {
        if(null!=mAnimationDrawable&&null!=context&&!context.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(null!=mLl_loading_view&&mLl_loading_view.getVisibility()!=View.GONE){
            mLl_loading_view.setVisibility(View.GONE);
        }

        if(null!=bindingView&&null!=bindingView.getRoot()&&bindingView.getRoot().getVisibility()!=View.GONE){
            bindingView.getRoot().setVisibility(View.GONE);
        }

        if(null!=mLl_login_view&&mLl_login_view.getVisibility()!=View.GONE){
            mLl_login_view.setVisibility(View.GONE);
        }

        if(null!=mLl_error_view&&mLl_error_view.getVisibility()!=View.VISIBLE){
            mLl_error_view.setVisibility(View.VISIBLE);
        }
    }

    protected void saveLocationHistoryList(final FollowVideoList.DataBean.ListsBean data) {
        if(null==data) return;
        new Thread(){
            public static final String TAG ="saveLocationHistoryList" ;

            @Override
            public void run() {
                super.run();
                Logger.d(TAG,"");
                UserPlayerVideoHistoryList userLookVideoList=new UserPlayerVideoHistoryList();
                userLookVideoList.setUserName(TextUtils.isEmpty(data.getNickname())?"火星人":data.getNickname());
                userLookVideoList.setUserSinger("该宝宝没有个性签名");
                userLookVideoList.setUserCover(data.getLogo());
                userLookVideoList.setVideoDesp(data.getDesp());
                userLookVideoList.setVideoLikeCount(TextUtils.isEmpty(data.getCollect_times())?"0":data.getCollect_times());
                userLookVideoList.setVideoCommendCount(TextUtils.isEmpty(data.getComment_count())?"0":data.getComment_count());
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
                ApplicationManager.getInstance().getUserPlayerDB().insertNewPlayerHistoryOfObject(userLookVideoList);
            }
        }.start();
    }



    protected abstract void initViews();

    /**
     * 在这里实现Fragment数据的缓加载.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    protected void onInvisible() {

    }

    protected void onVisible() {

    }



    /**
     * 子类可实现此登录方法
     */
    protected void onLogin() {

    }

    protected <T extends View> T getView(int id) {
        if(null==getView()) return null;
        return (T) getView().findViewById(id);
    }

    /**
     * 布局
     */
    public abstract int getLayoutId();

    /**
     * 加载失败后点击后的操作
     */
    protected void onRefresh() {

    }


    /**
     * 设置进度框文字
     * @param message
     */
    protected void setProgressDialogMessage(String message){
        if(null!=mLoadingProgressedView&&!getActivity().isFinishing()&&mLoadingProgressedView.isShowing()){
            mLoadingProgressedView.setMessage(message);
        }
    }



    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress,boolean isCancle){
        if(null==mLoadingProgressedView){
            mLoadingProgressedView = new LoadingProgressView(getActivity(),isProgress);
        }
        if(!getActivity().isFinishing()){
            mLoadingProgressedView.setMessage(message);
            mLoadingProgressedView.onSetCanceledOnTouchOutside(isCancle);
            mLoadingProgressedView.show();
        }
    }



    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress){
        if(null==mLoadingProgressedView){
            mLoadingProgressedView = new LoadingProgressView(getActivity(),isProgress);
        }
        if(!getActivity().isFinishing()){
            mLoadingProgressedView.setMessage(message);
            mLoadingProgressedView.show();
        }
    }

    /**
     * 关闭进度框
     */
    protected void closeProgressDialog(){
        if(null!=mLoadingProgressedView&&!getActivity().isFinishing()&&mLoadingProgressedView.isShowing()){
            mLoadingProgressedView.dismiss();
            mLoadingProgressedView=null;
        }
    }

    /**
     * 失败吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    protected void showErrorToast(String action, SnackBarListener snackBarListener, String message){
        if(null!=getActivity()){
            ToastUtils.showSnackebarStateToast(getActivity().getWindow().getDecorView(),action,snackBarListener, R.drawable.snack_bar_error_white, Constant.SNACKBAR_ERROR,message);
        }
    }

    /**
     * 统一的网络设置入口
     */
    protected void showNetWorkTips(){

        showErrorToast("网络设置", new SnackBarListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);//直接进入网络设置
                startActivity(intent);
            }
        }, "没有可用的网络链接");
    }

    /**
     * 成功吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    protected void showFinlishToast(String action, SnackBarListener snackBarListener, String message){
        if(null!=getActivity()){
            ToastUtils.showSnackebarStateToast(getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT),action,snackBarListener, R.drawable.snack_bar_done_white, Constant.SNACKBAR_DONE,message);
        }
    }

    protected boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


    @Override
    public void onDetach() {
        super.onDetach();
        context=null;
    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}
