package com.video.newqu.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import com.video.newqu.R;
import com.video.newqu.base.back.SwipeBackActivityBase;
import com.video.newqu.base.back.SwipeBackActivityHelper;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityBaseAuthorBinding;
import com.video.newqu.listener.SnackBarListener;
import com.video.newqu.manager.ActivityCollectorManager;
import com.video.newqu.ui.dialog.LoadingProgressView;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.layout.SwipeBackLayout;

import static com.video.newqu.manager.ActivityLoginCollectorManager.addActivity;

/**
 * TinyHung@outlook.com
 * 2017/3/19 14:51
 * 用户中心父类
 */

public abstract  class BaseAuthorActivity<SV extends ViewDataBinding> extends TopBaseActivity implements SwipeBackActivityBase {

    // 布局view
    protected SV bindingView;
    protected ActivityBaseAuthorBinding baseBinding;
    protected LoadingProgressView mLoadingProgressedView;

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
        baseBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_base_author, null, false);
        bindingView = DataBindingUtil.inflate(getLayoutInflater(), layoutResID, null, false);
        //content
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bindingView.getRoot().setLayoutParams(params);
        RelativeLayout mContainer = (RelativeLayout) baseBinding.getRoot().findViewById(R.id.container);
        mContainer.addView(bindingView.getRoot());
        getWindow().setContentView(baseBinding.getRoot());
        initViews();
        initData();
    }


    public abstract void initViews();
    public abstract void initData();


    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress,boolean isCancle){
        if(!BaseAuthorActivity.this.isFinishing()){
            if(null==mLoadingProgressedView){
                mLoadingProgressedView = new LoadingProgressView(this,isProgress);
            }
            mLoadingProgressedView.onSetCanceledOnTouchOutside(isCancle);
            mLoadingProgressedView.setMessage(message);
            mLoadingProgressedView.show();
        }
    }

    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress){
        if(!BaseAuthorActivity.this.isFinishing()){
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
            if(!BaseAuthorActivity.this.isFinishing()){
                if(null!=mLoadingProgressedView&&mLoadingProgressedView.isShowing()){
                    mLoadingProgressedView.dismiss();
                    mLoadingProgressedView=null;
                }
            }
        }catch (Exception e){

        }
    }
    /**
     * 失败吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    public void showErrorToast(String action, SnackBarListener snackBarListener, String message){
        ToastUtils.showSnackebarStateToast(getWindow().getDecorView(),action,snackBarListener, R.drawable.snack_bar_error_white, Constant.SNACKBAR_ERROR,message);
    }

    /**
     * 成功吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    public void showFinlishToast(String action, SnackBarListener snackBarListener, String message){
        ToastUtils.showSnackebarStateToast(getWindow().getDecorView(),action,snackBarListener, R.drawable.snack_bar_done_white, Constant.SNACKBAR_DONE,message);
    }
}
