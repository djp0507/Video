package com.video.newqu.base;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.video.newqu.R;
import com.video.newqu.databinding.BasePagerBinding;

/**
 * TinyHung@Outlook.com
 * 2017/9/11
 * 视频编辑界面的Pager基类
 */

public abstract class BasePager <T extends ViewDataBinding>{

    protected T bindingView;
    private BasePagerBinding baseBindingView;
    protected final Activity mContext;
    private AnimationDrawable mAnimationDrawable;


    public BasePager(Activity context){
        this.mContext=context;
    }

    public void setContentView(int layoutID){
        //父View
        baseBindingView = DataBindingUtil.inflate(LayoutInflater.from(mContext), R.layout.base_pager, null, false);
        //子View
        bindingView = DataBindingUtil.inflate(mContext.getLayoutInflater(),layoutID, null, false);
        //父内容容器
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bindingView.getRoot().setLayoutParams(params);
        baseBindingView.viewContent.addView(bindingView.getRoot());//添加至父容器

        mAnimationDrawable = (AnimationDrawable) baseBindingView.ivErrorIcon.getDrawable();
        if(null!=mAnimationDrawable&&!mAnimationDrawable.isRunning()){
            mAnimationDrawable.start();
        }
        baseBindingView.llLoadingError.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });
    }


    public View getView() {
        return null==baseBindingView?null: baseBindingView.getRoot();
    }


    protected void showLoadingView() {

        if(baseBindingView.viewContent.getVisibility()!=View.GONE){
            baseBindingView.viewContent.setVisibility(View.GONE);
        }
        if(baseBindingView.llLoadingError.getVisibility()!=View.GONE){
            baseBindingView.llLoadingError.setVisibility(View.GONE);
        }
        if(baseBindingView.llLoadingView.getVisibility()!=View.VISIBLE){
            baseBindingView.llLoadingView.setVisibility(View.VISIBLE);
        }
        if(null!=mAnimationDrawable&&!mAnimationDrawable.isRunning()){
            mAnimationDrawable.start();
        }
    }



    protected void showContentView() {

        if(baseBindingView.llLoadingError.getVisibility()!=View.GONE){
            baseBindingView.llLoadingError.setVisibility(View.GONE);
        }
        if(baseBindingView.llLoadingView.getVisibility()!=View.GONE){
            baseBindingView.llLoadingView.setVisibility(View.GONE);
        }

        if(null!=mAnimationDrawable&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(baseBindingView.viewContent.getVisibility()!=View.VISIBLE){
            baseBindingView.viewContent.setVisibility(View.VISIBLE);
        }
    }


    protected void showLoadErrorView() {

        if(baseBindingView.viewContent.getVisibility()!=View.GONE){
            baseBindingView.viewContent.setVisibility(View.GONE);
        }

        if(baseBindingView.llLoadingView.getVisibility()!=View.GONE){
            baseBindingView.llLoadingView.setVisibility(View.GONE);
        }

        if(null!=mAnimationDrawable&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(baseBindingView.llLoadingError.getVisibility()!=View.VISIBLE){
            baseBindingView.llLoadingError.setVisibility(View.VISIBLE);
        }
    }


    /**子类实现可实现刷新功能*/
    protected  void onRefresh(){

    }
}
