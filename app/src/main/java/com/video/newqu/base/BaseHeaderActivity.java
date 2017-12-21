package com.video.newqu.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.video.newqu.R;
import com.video.newqu.base.back.SwipeBackActivityBase;
import com.video.newqu.base.back.SwipeBackActivityHelper;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.contants.Constant;
import com.video.newqu.listener.SnackBarListener;
import com.video.newqu.manager.ActivityCollectorManager;
import com.video.newqu.ui.activity.WebViewActivity;
import com.video.newqu.ui.dialog.LoadingProgressView;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.StatusBarUtil;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.layout.SwipeBackLayout;
import com.video.newqu.view.widget.ZoomScrollView;

import java.lang.reflect.Method;

/**
 * TinyHung@outlook.com
 * 2017-06-09 19:33
 * 专用于协助子类完成头部渐变效果的父亲
 */

public abstract class BaseHeaderActivity<HV extends ViewDataBinding, SV extends ViewDataBinding>
        extends TopBaseActivity implements SwipeBackActivityBase {

    private static final String TAG = BaseHeaderActivity.class.getSimpleName();

//    protected BaseHeaderTitleBarBinding bindingTitleView;
    protected HV bindingHeaderView;
    protected SV bindingContentView;
    private int slidingDistance;
    private int imageBgHeight;
    private LoadingProgressView mLoadingProgressedView;


    protected <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SwipeBackActivityHelper(this);
        mHelper.onActivityCreate();
        ActivityCollectorManager.addActivity(this);
        SwipeBackLayout swipeBackLayout = getSwipeBackLayout();
        swipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);
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
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollectorManager.removeActivity(this);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        View ll = getLayoutInflater().inflate(R.layout.activity_base_head, null);

        // 内容
        bindingContentView = DataBindingUtil.inflate(getLayoutInflater(), layoutResID, null, false);
        // 头部
        bindingHeaderView = DataBindingUtil.inflate(getLayoutInflater(), setHeaderLayout(), null, false);
        // 标题
//        bindingTitleView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.base_header_title_bar, null, false);

        // title (如自定义很强可以拿出去)
//        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        bindingTitleView.getRoot().setLayoutParams(titleParams);
//        RelativeLayout mTitleContainer = (RelativeLayout) ll.findViewById(R.id.title_container);
//        mTitleContainer.addView(bindingTitleView.getRoot());
//        getWindow().setContentView(ll);

        // header
        RelativeLayout.LayoutParams headerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bindingHeaderView.getRoot().setLayoutParams(headerParams);
        RelativeLayout mHeaderContainer = (RelativeLayout) ll.findViewById(R.id.header_container);
        mHeaderContainer.addView(bindingHeaderView.getRoot());
        getWindow().setContentView(ll);

        // content
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        bindingContentView.getRoot().setLayoutParams(params);
        RelativeLayout mContainer = (RelativeLayout) ll.findViewById(R.id.container);
        mContainer.addView(bindingContentView.getRoot());
        getWindow().setContentView(ll);
        // 初始化滑动渐变
        initSlideShapeTheme(setHeaderImgUrl(), setHeaderImageView());
        // 设置toolbar
        setToolBar();
//        bindingTitleView.ivBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
        initViews();
    }

    protected abstract void initViews();
    protected abstract int setHeaderLayout();
    protected abstract Object setHeaderImgUrl();
    protected abstract ImageView setHeaderImageView();


    /**
     * 设置头部header布局 左侧的图片(需要设置曲线路径切换动画时重写)
     */
    protected ImageView setHeaderPicView() {
        return new ImageView(this);
    }


    public void setTitle(String text) {
//        bindingTitleView.tvTitleUserName.setText(text);
    }



    /**
     * 设置toolbar
     */
    protected void setToolBar() {

//        setSupportActionBar(bindingTitleView.tbBaseTitle);
//        ActionBar actionBar = getSupportActionBar();
//        if (actionBar != null) {
//            //去除默认Title显示
//            actionBar.setDisplayShowTitleEnabled(false);
////            actionBar.setDisplayHomeAsUpEnabled(true);
////            actionBar.setHomeAsUpIndicator(R.drawable.icon_back);
//        }
//        // 手动设置才有效果
//        bindingTitleView.tbBaseTitle.setTitleTextAppearance(this, R.style.ToolBar_Title);
//        bindingTitleView.tbBaseTitle.setSubtitleTextAppearance(this, R.style.Toolbar_SubTitle);
//        bindingTitleView.tbBaseTitle.inflateMenu(R.menu.about_header_menu);
//        bindingTitleView.tbBaseTitle.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_white));
//
//        bindingTitleView.tbBaseTitle.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                switch (item.getItemId()) {
//                    //服务条款
//                    case R.id.actionbar_service:
//                        loadServiceClause();
//                        break;
//                    //分享
//                    case R.id.actionbar_share:
//                        shareIntent();
//                        break;
//                }
//                return false;
//            }
//        });
    }


    private void loadServiceClause() {
        WebViewActivity.loadUrl(BaseHeaderActivity.this,"http://v.nq6.com/user_services.html","新趣服务条款");
    }


    // TODO: 2017/7/30官网
    private void shareIntent() {
        ShareInfo shareInfo=new ShareInfo();
        shareInfo.setDesp("短视频笑不停，年轻人都在看! 地球人都关注的短视频神器!");
        shareInfo.setTitle("新趣小视频");
        shareInfo.setUrl("http://v.nq6.com");
        shareInfo.setVideoID("");
        onShare(shareInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about_header_menu, menu);
        return true;
    }

    /**
     * 显示popu内的图片
     */
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        if (menu != null) {
            if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
                try {
                    Method m = menu.getClass().getDeclaredMethod(
                            "setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "onMenuOpened...unable to set icons for overflow menu", e);
                }
            }
        }
        return super.onPrepareOptionsPanel(view, menu);
    }

    /**
     * *** 初始化滑动渐变 一定要实现 ******
     *
     * @param imgUrl    header头部的高斯背景imageUrl
     * @param mHeaderBg header头部高斯背景ImageView控件
     */
    protected void initSlideShapeTheme(Object imgUrl, ImageView mHeaderBg) {

//        setImgHeaderBg(imgUrl);
//
//        // toolbar 的高
//        int toolbarHeight = bindingTitleView.tbBaseTitle.getLayoutParams().height;
//        final int headerBgHeight = toolbarHeight + StatusBarUtil.getStatusBarHeight(this);
//
//        // 使背景图向上移动到图片的最低端，保留（titlebar+statusbar）的高度
//        ViewGroup.LayoutParams params = bindingTitleView.ivBaseTitlebarBg.getLayoutParams();
//        ViewGroup.MarginLayoutParams ivTitleHeadBgParams = (ViewGroup.MarginLayoutParams) bindingTitleView.ivBaseTitlebarBg.getLayoutParams();
//        int marginTop = params.height - headerBgHeight;
//        ivTitleHeadBgParams.setMargins(0, -marginTop, 0, 0);
//
//        bindingTitleView.ivBaseTitlebarBg.setImageAlpha(0);
//        StatusBarUtils.setTranslucentImageHeader(this, 0, bindingTitleView.tbBaseTitle);
//
//        // 上移背景图片，使空白状态栏消失(这样下方就空了状态栏的高度)
//        if (mHeaderBg != null) {
//            ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mHeaderBg.getLayoutParams();
//            layoutParams.setMargins(0, -StatusBarUtil.getStatusBarHeight(this), 0, 0);
//
//            ViewGroup.LayoutParams imgItemBgparams = mHeaderBg.getLayoutParams();
//            // 获得高斯图背景的高度
//            imageBgHeight = imgItemBgparams.height;
//        }

        // 变色
        initScrollViewListener();
        initNewSlidingParams();
    }


    /**
     * 加载titlebar背景
     */
    private void setImgHeaderBg(Object imgUrl) {
        // 高斯模糊背景 原来 参数：12,5  23,4
//        Glide.with(this).load(imgUrl)
//                .error(R.drawable.item_defult)
//                .bitmapTransform(new BlurTransformation(this, 23, 4)).listener(new RequestListener<Object, GlideDrawable>() {
//            @Override
//            public boolean onException(Exception e, Object model, Target<GlideDrawable> target, boolean isFirstResource) {
//                return false;
//            }
//
//            @Override
//            public boolean onResourceReady(GlideDrawable resource, Object model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
//                bindingTitleView.tbBaseTitle.setBackgroundColor(Color.TRANSPARENT);
//                bindingTitleView.ivBaseTitlebarBg.setImageAlpha(0);
//                bindingTitleView.ivBaseTitlebarBg.setVisibility(View.VISIBLE);
//                return false;
//            }
//        }).into(bindingTitleView.ivBaseTitlebarBg);
    }


    private void initScrollViewListener() {
        ((ZoomScrollView) findViewById(R.id.zoom_scrollview)).setScrollViewListener(new ZoomScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ZoomScrollView scrollView, int x, int y, int oldx, int oldy) {
                scrollChangeHeader(y);
            }
        });
    }

    private void initNewSlidingParams() {
        int titleBarAndStatusHeight = (int) (CommonUtils.getDimens(R.dimen.nav_bar_height) + StatusBarUtil.getStatusBarHeight(this));
        // 减掉后，没到顶部就不透明了
        slidingDistance = imageBgHeight - titleBarAndStatusHeight - (int) (CommonUtils.getDimens(R.dimen.base_header_activity_slide_more));
    }

    /**
     * 根据页面滑动距离改变Header方法
     */
    private void scrollChangeHeader(int scrolledY) {
//        if (scrolledY < 0) {
//            scrolledY = 0;
//        }
//        float alpha = Math.abs(scrolledY) * 1.0f / (slidingDistance);
//
//        Drawable drawable = bindingTitleView.ivBaseTitlebarBg.getDrawable();
//
//        if (drawable == null) {
//            return;
//        }
//        if (scrolledY <= slidingDistance) {
//            // title部分的渐变
//            drawable.mutate().setAlpha((int) (alpha * 255));
//            bindingTitleView.ivBaseTitlebarBg.setImageDrawable(drawable);
//            bindingTitleView.tvTitleUserName.setTextColor(Color.argb((int) (int) (alpha * 255), 255, 255, 255));//标题文字颜色
//        } else {
//            drawable.mutate().setAlpha(255);
//            bindingTitleView.ivBaseTitlebarBg.setImageDrawable(drawable);
//            bindingTitleView.tvTitleUserName.setTextColor(Color.argb((int) 255, 255, 255, 255));//标题文字颜色
//        }
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
    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress){
        if(null==mLoadingProgressedView){
            mLoadingProgressedView = new LoadingProgressView(this,isProgress);
        }
        mLoadingProgressedView.setMessage(message);
        mLoadingProgressedView.show();
    }

    /**
     * 关闭进度框
     */
    protected void closeProgressDialog(){
        if(null!=mLoadingProgressedView&&mLoadingProgressedView.isShowing()){
            mLoadingProgressedView.dismiss();
            mLoadingProgressedView=null;
        }
    }

    /**
     * 带有动画的完成并消失进度框
     */
    protected void finlishProgressDialog(String message,int textColor,boolean isFinlish,int duration){
        if(null!=mLoadingProgressedView&&mLoadingProgressedView.isShowing()){
            mLoadingProgressedView.setResultsCompletes(message,textColor,isFinlish,duration);
            mLoadingProgressedView=null;
        }
    }
}
