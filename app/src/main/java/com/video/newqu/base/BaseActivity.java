package com.video.newqu.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.video.newqu.R;
import com.video.newqu.base.back.SwipeBackActivityBase;
import com.video.newqu.base.back.SwipeBackActivityHelper;
import com.video.newqu.databinding.ActivityVideoBaseBinding;
import com.video.newqu.listener.PerfectClickListener;
import com.video.newqu.manager.ActivityCollectorManager;
import com.video.newqu.ui.dialog.LoadingProgressView;
import com.video.newqu.util.Utils;
import com.video.newqu.view.layout.SwipeBackLayout;
import java.lang.ref.WeakReference;

import static com.video.newqu.manager.ActivityLoginCollectorManager.addActivity;

/**
 * TinyHung@outlook.com
 * 2017/3/19 14:51
 * 所有Activity的父类
 */

public abstract  class BaseActivity<SV extends ViewDataBinding> extends TopBaseActivity implements SwipeBackActivityBase {

    // 布局view
    protected SV bindingView;
    protected LoadingProgressView mLoadingProgressedView;
    private View btn_iv_back;
    private AnimationDrawable mAnimationDrawable;
    private WeakReference<ActivityVideoBaseBinding> mBaseBindingWeakReference;

    protected <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollectorManager.addActivity(this);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
        swipeBackLayout.setScrollThresHold(0.8f);
    }




    private SwipeBackActivityHelper mHelper;

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate();
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v == null && mHelper != null)
            return mHelper.findViewById(id);
        return v;
    }


    @Override
    public SwipeBackLayout getSwipeBackLayout() {
        return mHelper.getSwipeBackLayout();
    }

    @Override
    public void setSwipeBackEnable(boolean enable) {
        getSwipeBackLayout().setEnableGesture(enable);
    }


    @Override
    public void scrollToFinishActivity() {
        Utils.convertActivityToTranslucent(this);
        getSwipeBackLayout().scrollToFinishActivity();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        ActivityCollectorManager.removeActivity(this);
    }



    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        ActivityVideoBaseBinding baseBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_video_base, null, false);
        mBaseBindingWeakReference = new WeakReference<ActivityVideoBaseBinding>(baseBinding);
        bindingView = DataBindingUtil.inflate(getLayoutInflater(), layoutResID, null, false);
        //content
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bindingView.getRoot().setLayoutParams(params);
        RelativeLayout mContainer = (RelativeLayout) mBaseBindingWeakReference.get().getRoot().findViewById(R.id.container);
        mContainer.addView(bindingView.getRoot());
        getWindow().setContentView(mBaseBindingWeakReference.get().getRoot());

        btn_iv_back = getView(R.id.btn_iv_back);

        btn_iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mAnimationDrawable = (AnimationDrawable) mBaseBindingWeakReference.get().ivLoadingIcon.getDrawable();
        // 点击加载失败布局
        mBaseBindingWeakReference.get().reLoadingError.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                onRefresh();
            }
        });

        initViews();
        initData();
    }



    public abstract void initViews();
    public abstract void initData();


    /**
     * 显示加载中
     */
    protected void showLoadingViews(String message) {

        if(bindingView.getRoot().getVisibility()!=View.GONE){
            bindingView.getRoot().setVisibility(View.GONE);
        }
        if(mBaseBindingWeakReference.get().llLoadingView.getVisibility()!=View.VISIBLE){
            mBaseBindingWeakReference.get().llLoadingView.setVisibility(View.VISIBLE);
        }

        if(null!=mAnimationDrawable&&!BaseActivity.this.isFinishing()&&!mAnimationDrawable.isRunning()){
            mAnimationDrawable.start();
        }

        if(mBaseBindingWeakReference.get().reLoadingError.getVisibility()!=View.GONE){
            mBaseBindingWeakReference.get().reLoadingError.setVisibility(View.GONE);
        }
    }


    /**
     * 显示加载完成
     */
    protected void showContentView() {

        if(null!=mAnimationDrawable&&!BaseActivity.this.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }
        if(mBaseBindingWeakReference.get().llLoadingView.getVisibility()!=View.GONE){
            mBaseBindingWeakReference.get().llLoadingView.setVisibility(View.GONE);
        }

        if (mBaseBindingWeakReference.get().reLoadingError.getVisibility() != View.GONE) {
            mBaseBindingWeakReference.get().reLoadingError.setVisibility(View.GONE);
        }
        if (bindingView.getRoot().getVisibility() != View.VISIBLE) {
            bindingView.getRoot().setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示加载失败
     */
    protected void showLoadErrorView() {

        if(null!=mAnimationDrawable&&!BaseActivity.this.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(mBaseBindingWeakReference.get().llLoadingView.getVisibility()!=View.GONE){
            mBaseBindingWeakReference.get().llLoadingView.setVisibility(View.GONE);
        }
        if (mBaseBindingWeakReference.get().reLoadingError.getVisibility() != View.VISIBLE) {
            mBaseBindingWeakReference.get().reLoadingError.setVisibility(View.VISIBLE);
        }
        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
    }


    /**
     * 失败后点击刷新
     */
    protected void onRefresh() {

    }




    protected void goneView(final View... views) {
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    protected void visibleView(final View... views) {
        if (views != null && views.length > 0) {
            for (View view : views) {
                if (view != null) {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }




    protected boolean isVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }


    /**
     * 设置进度框文字
     * @param message
     */
    public void setProgressDialogMessage(String message){
        if(null!=mLoadingProgressedView&&mLoadingProgressedView.isShowing()){
            mLoadingProgressedView.setMessage(message);
        }
    }


    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    public void showProgressDialog(String message,boolean isProgress,boolean isCancle){
        if(!BaseActivity.this.isFinishing()){
            if(null==mLoadingProgressedView){
                mLoadingProgressedView = new LoadingProgressView(this,isProgress);
            }
            mLoadingProgressedView.setCancelable(isCancle);
            mLoadingProgressedView.setMessage(message);
            mLoadingProgressedView.show();
        }
    }

    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    public void showProgressDialog(String message,boolean isProgress){
        if(!BaseActivity.this.isFinishing()){
            if(null==mLoadingProgressedView){
                mLoadingProgressedView = new LoadingProgressView(this,isProgress);
            }
            mLoadingProgressedView.setMessage(message);
            mLoadingProgressedView.show();
        }
    }

    /**
     * 关闭进度框
     */
    public void closeProgressDialog(){
        try {
            if(!BaseActivity.this.isFinishing()){
                if(null!=mLoadingProgressedView&&mLoadingProgressedView.isShowing()){
                    mLoadingProgressedView.dismiss();
                    mLoadingProgressedView=null;
                }
            }
        }catch (Exception e){

        }
    }
}
