package com.video.newqu.base;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.video.newqu.R;
import com.video.newqu.contants.Constant;
import com.video.newqu.listener.PerfectClickListener;
import com.video.newqu.listener.SnackBarListener;
import com.video.newqu.util.ToastUtils;


/**
 * TinyHung@outlook.com
 * 2017/3/17 15:46
 * 片段基类
 */
public abstract class BaseMediaFragment<VS extends ViewDataBinding> extends Fragment {


    // 布局view
    protected VS bindingView;
    // 加载失败
    private LinearLayout mRefresh;
    protected Context context;
    private LinearLayout mLoadingView;
    private AnimationDrawable mAnimationDrawable;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View ll = inflater.inflate(R.layout.base_media_fragment, null);
        bindingView = DataBindingUtil.inflate(getActivity().getLayoutInflater(), getLayoutId(), null, false);
        if(null!=bindingView){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            bindingView.getRoot().setLayoutParams(params);
            RelativeLayout content = (RelativeLayout) ll.findViewById(R.id.view_content);
            content.addView(bindingView.getRoot());
        }
        return ll;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();

        mRefresh = getView(R.id.ll_loading_error);
        mLoadingView = getView(R.id.ll_loading_view);
        ImageView iv_loading_icon = (ImageView) getView(R.id.iv_loading_icon);
        mAnimationDrawable = (AnimationDrawable) iv_loading_icon.getDrawable();
        // 点击加载失败布局
        mRefresh.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                onRefresh();
            }
        });
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



    protected <T extends View> T getView(int id) {
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
     * 显示加载中
     */
    protected void showLoadingView(String message) {

        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
        if (mRefresh.getVisibility() != View.GONE) {
            mRefresh.setVisibility(View.GONE);
        }
        if(mLoadingView.getVisibility()!=View.VISIBLE){
            mLoadingView.setVisibility(View.VISIBLE);
        }

        if(null!=mAnimationDrawable&&!mAnimationDrawable.isRunning()){
            mAnimationDrawable.start();
        }
    }

    /**
     * 加载失败
     */
    protected void showLoadingError() {

        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
        if(null!=mAnimationDrawable&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }
        if(mLoadingView.getVisibility()!=View.GONE){
            mLoadingView.setVisibility(View.GONE);
        }
        if (mRefresh.getVisibility() != View.VISIBLE) {
            mRefresh.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示内容
     */
    protected void showContentView() {


        if(null!=mAnimationDrawable&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }
        if(mLoadingView.getVisibility()!=View.GONE){
            mLoadingView.setVisibility(View.GONE);
        }
        if (mRefresh.getVisibility() != View.GONE) {
            mRefresh.setVisibility(View.GONE);
        }
        if (bindingView.getRoot().getVisibility() != View.VISIBLE) {
            bindingView.getRoot().setVisibility(View.VISIBLE);
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }


    /**
     * 失败吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    protected void showErrorToast(String action, SnackBarListener snackBarListener, String message){
        ToastUtils.showSnackebarStateToast(getActivity().getWindow().getDecorView(),action,snackBarListener, R.drawable.snack_bar_error_white, Constant.SNACKBAR_ERROR,message);
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
        ToastUtils.showSnackebarStateToast(getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT),action,snackBarListener, R.drawable.snack_bar_done_white, Constant.SNACKBAR_DONE,message);
    }

    protected boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

}
