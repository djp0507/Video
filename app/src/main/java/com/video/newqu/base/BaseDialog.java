package com.video.newqu.base;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.video.newqu.R;
import com.video.newqu.listener.PerfectClickListener;
import com.video.newqu.ui.dialog.LoadingProgressView;
import com.video.newqu.util.Utils;

/**
 * TinyHung@outlook.com
 * 2017/4/18 14:08
 * 统一的父Dialog
 */
public abstract class BaseDialog<VS extends ViewDataBinding> extends AppCompatDialog {

    protected final Context context;
    protected VS bindingView;
    private LinearLayout llProgressBar;
    private View refresh;
    private LoadingProgressView mLoadingProgressedView;

    public BaseDialog(Context context, int menuDialog) {
        super(context,menuDialog);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//去除标题
        this.context=context;
    }

    /**
     * 手动设置Dialog的宽
     */
    protected void setDialogWidth(){
        Utils.setDialogWidth(this);
    }
    protected <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        View rootView = getLayoutInflater().inflate(R.layout.dialog_base, null);
        bindingView = DataBindingUtil.inflate(getLayoutInflater(), layoutResID, null, false);
        RelativeLayout container = (RelativeLayout) rootView.findViewById(R.id.container);
        container.addView(bindingView.getRoot());
        getWindow().setContentView(rootView);

        llProgressBar = getView(R.id.ll_progress_bar);
        refresh = getView(R.id.ll_error_refresh);

        refresh.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                showLoading();
                onRefresh();
            }
        });
        showLoading();
        initViews();
        loadData();
    }

    protected abstract void initViews();

    protected abstract void loadData();

    protected void showLoading() {
        if (llProgressBar.getVisibility() != View.VISIBLE) {
            llProgressBar.setVisibility(View.VISIBLE);
        }

        if (bindingView.getRoot().getVisibility() != View.GONE) {
            bindingView.getRoot().setVisibility(View.GONE);
        }
        if (refresh.getVisibility() != View.GONE) {
            refresh.setVisibility(View.GONE);
        }
    }

    protected void showContentView() {
        if (llProgressBar.getVisibility() != View.GONE) {
            llProgressBar.setVisibility(View.GONE);
        }

        if (refresh.getVisibility() != View.GONE) {
            refresh.setVisibility(View.GONE);
        }
        if (bindingView.getRoot().getVisibility() != View.VISIBLE) {
            bindingView.getRoot().setVisibility(View.VISIBLE);
        }
    }

    protected void showError() {
        if (llProgressBar.getVisibility() != View.GONE) {
            llProgressBar.setVisibility(View.GONE);
        }

        if (refresh.getVisibility() != View.VISIBLE) {
            refresh.setVisibility(View.VISIBLE);
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


    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress){
        if(!BaseDialog.this.isShowing()){
            if(null==mLoadingProgressedView){
                mLoadingProgressedView = new LoadingProgressView(context,isProgress);
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
            if(!BaseDialog.this.isShowing()){
                if(null!=mLoadingProgressedView&&mLoadingProgressedView.isShowing()){
                    mLoadingProgressedView.dismiss();
                    mLoadingProgressedView=null;
                }
            }
        }catch (Exception e){

        }
    }
}
