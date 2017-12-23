package com.video.newqu.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.XinQuFragmentPagerAdapter;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityVerticalVideoPlayBinding;
import com.video.newqu.ui.fragment.VerticalAuthorDetailsFragment;
import com.video.newqu.ui.fragment.VerticalVideoPlayFragment;
import com.video.newqu.util.Logger;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import java.util.ArrayList;
import java.util.List;
import com.xinqu.videoplayer.full.WindowVideoPlayer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/12/5 16:06
 * 上次滑动切换视频，左右滑动切换 用户中心/视频播放
 * 必须传入FragmnetType以确定源View
 * 垂直滑动列表播放器
 */

public class VerticalVideoPlayActivity extends BaseActivity<ActivityVerticalVideoPlayBinding> {

    private static final String TAG =VerticalVideoPlayActivity.class.getSimpleName();
    private List<Fragment> mFragments;
    private int mItemPoistion;

    //    /**
//     * 首页，或其他界面跳转而来，需要传入fragmentType
//     * @param context
//     * @param fragmentType 谁跳转过来了？ 需要根据fragmentType确定AIP和返回后刷新源跳转的界面
//     * @param json 源数据
//     * @param position 源界面点击的Item的角标位置
//     * @param mPage 刷新到第几页了
//     */
//    public static void start(Context context, int fragmentType,String json, int position, int mPage,String userID) {
//        Intent intent=new Intent(context,VerticalVideoPlayActivity.class);
//        intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentType);
//        intent.putExtra(Constant.KEY_POISTION,position);
//        intent.putExtra(Constant.KEY_PAGE,mPage);
//        intent.putExtra(Constant.KEY_AUTHOE_ID,userID);
//        intent.putExtra(Constant.KEY_JSON,json);
//        context.startActivity(intent);
//    }

    @Override
    public void initViews() {

    }

    @Override
    public void initData() {
        //检查SD卡权限
        RxPermissions.getInstance(VerticalVideoPlayActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {



            @Override
            public void call(Boolean aBoolean) {
                if(null!=aBoolean&&aBoolean){
                    Intent intent = getIntent();
                    if(null==intent) {
                        ToastUtils.shoCenterToast("错误!");
                        finish();
                        return;
                    }
                    //初始化视频列表和用户详情界面，全屏的视频列表界面有无限个，用户详情界面只有一个
                    int fragmentType = intent.getIntExtra(Constant.KEY_FRAGMENT_TYPE, Constant.FRAGMENT_TYPE_HOT);//默认热门列表界面进入
                    String json = intent.getStringExtra(Constant.KEY_JSON);
                    String authorID = intent.getStringExtra(Constant.KEY_AUTHOE_ID);
                    int itemPoistion = intent.getIntExtra(Constant.KEY_POISTION, 0);
                    int page = intent.getIntExtra(Constant.KEY_PAGE, 0);
                    String topic=null;
                    if(fragmentType==Constant.FRAGMENT_TYPE_HOME_TOPIC||fragmentType==Constant.FRAGMENT_TYPE_TOPIC_LIST){
                        topic=intent.getStringExtra(Constant.KEY_TOPIC);
                    }
                    mFragments = new ArrayList<>();
                    mFragments.add(VerticalVideoPlayFragment.newInstance(json,fragmentType,authorID,itemPoistion,page,topic));
                    mFragments.add(new VerticalAuthorDetailsFragment());
                    //监听显示状态
                    bindingView.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        @Override
                        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                        }
                        @Override
                        public void onPageSelected(int position) {
                            mItemPoistion=position;
                            //暂停视频播放，如果正在播放的话
                            if(1==position){
                                WindowVideoPlayer.goOnPlayOnPause();
                                onChildFragmentResume(false);
                            }
                        }

                        @Override
                        public void onPageScrollStateChanged(int state) {
                            //手势滑动停止的时候，并且回到了视频播放界面
                            Logger.d(TAG,"state="+state);
                            if(state==0&&0==mItemPoistion){
                                WindowVideoPlayer.goOnPlayOnResume();
                                onChildFragmentResume(true);
                            }
                        }
                    });
                    bindingView.viewPager.setAdapter(new XinQuFragmentPagerAdapter(getSupportFragmentManager(), mFragments));
                    showGuideView();
                }else{
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(VerticalVideoPlayActivity.this)
                            .setTitle("SD读取权限被拒绝!")
                            .setMessage("播放失败！SD卡读取权限被拒！我们为您播放视频需要您授予SD卡读写权限，是否前往应用设置中心设置权限？完成设置后请重新开启本界面");
                    builder.setNegativeButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SystemUtils.getInstance().startAppDetailsInfoActivity(VerticalVideoPlayActivity.this,141);
                        }
                    });
                    builder.show();
                }
            }
        });
    }

    /**
     * 手动调用生命周期
     * @param flag
     */
    private void onChildFragmentResume(boolean flag) {
        if(null!=mFragments&&mFragments.size()>0){
            Fragment fragment = mFragments.get(0);
            if(fragment instanceof VerticalVideoPlayFragment){
                ((VerticalVideoPlayFragment)fragment).onChildResume(flag);
            }
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vertical_video_play);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


    /**
     * 显示功能向导，第一次进入这个界面才会显示
     */
    private void showGuideView() {
        //首次进入提示向导
        if(Utils.isCheckNetwork()&&1==Utils.getNetworkType()&&!SharedPreferencesUtil.getInstance().getBoolean(Constant.SETTING_VIDEOS_PLAYER_FIRST_CLICK,false)){
            try {
                //加载双击点赞的引导图层
                final ViewStub viewStub = (ViewStub)findViewById(R.id.click_price);
                if(null!=viewStub){
                    final View click_paice_view = viewStub.inflate();
                    if(null!=click_paice_view){
                        click_paice_view.setVisibility(View.VISIBLE);
                        Animation clickViewVisibleAnimation = new AlphaAnimation(0.0f, 1.0f);
                        clickViewVisibleAnimation.setDuration(300);
                        clickViewVisibleAnimation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                RelativeLayout relativeLayout = (RelativeLayout) click_paice_view.findViewById(R.id.re_click_root_view);
                                relativeLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Animation clickViewVisibleAnimation = new AlphaAnimation(1.0f, 0.0f);
                                        clickViewVisibleAnimation.setDuration(300);
                                        clickViewVisibleAnimation.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                click_paice_view.setVisibility(View.GONE);
                                                SharedPreferencesUtil.getInstance().putBoolean(Constant.SETTING_VIDEOS_PLAYER_FIRST_CLICK,true);
                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });
                                        click_paice_view.startAnimation(clickViewVisibleAnimation);
                                    }
                                });
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        click_paice_view.startAnimation(clickViewVisibleAnimation);
                    }
                }
            }catch (Exception e){

            }
        }
    }

    public void login() {
        Intent intent = new Intent(VerticalVideoPlayActivity.this, LoginGroupActivity.class);
        startActivityForResult(intent, Constant.INTENT_LOGIN_EQUESTCODE);
        overridePendingTransition( R.anim.menu_enter,0);//进场动画
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //登录意图，需进一步确认
        if (Constant.INTENT_LOGIN_EQUESTCODE == requestCode && resultCode == Constant.INTENT_LOGIN_RESULTCODE) {
            if (null != data) {
                boolean booleanExtra = data.getBooleanExtra(Constant.INTENT_LOGIN_STATE, false);
                VideoApplication.isLogin=true;
                if (null!=VideoApplication.getInstance().getUserData()&&!VideoApplication.getInstance().userIsBinDingPhone()) {
                    binDingPhoneNumber();
                }
            }
        }
    }


    public void setCureenItem(int index) {
        if(null!=bindingView) bindingView.viewPager.setCurrentItem(index);
    }


    public int getCureenItem() {
        if(null!=bindingView) {
            return bindingView.viewPager.getCurrentItem();
        }
        return 0;
    }

    @Override
    public void onDestroy() {
        WindowVideoPlayer.releaseAllVideos();
        super.onDestroy();
        if(null!=mFragments)mFragments.clear();
        mFragments=null;
        Runtime.getRuntime().gc();
    }

    @Override
    public void onBackPressed() {
        //当前显示的是用户中心界面
        if(null!=bindingView&&bindingView.viewPager.getCurrentItem()==1){
            setCureenItem(0);
            return;
        }
        super.onBackPressed();
    }
}
