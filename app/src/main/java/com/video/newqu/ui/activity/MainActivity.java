package com.video.newqu.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import com.ksyun.media.shortvideo.utils.AuthInfoManager;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tencent.bugly.Bugly;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.sina.helper.MD5;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.XinQuFragmentPagerAdapter;
import com.video.newqu.base.TopBaseActivity;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.bean.UpdataApkInfo;
import com.video.newqu.bean.WeiChactVideoInfo;
import com.video.newqu.bean.WeiXinVideo;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.contants.NetContants;
import com.video.newqu.databinding.ActivityMainBinding;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.listener.OnUpdataStateListener;
import com.video.newqu.manager.APKUpdataManager;
import com.video.newqu.manager.ActivityCollectorManager;
import com.video.newqu.manager.DBScanWeiCacheManager;
import com.video.newqu.manager.ThreadManager;
import com.video.newqu.service.DownLoadService;
import com.video.newqu.ui.contract.MainContract;
import com.video.newqu.ui.dialog.ExitAppDialog;
import com.video.newqu.ui.dialog.LocationVideoUploadDialog;
import com.video.newqu.ui.dialog.TakePicturePopupWindow;
import com.video.newqu.ui.dialog.BuildManagerDialog;
import com.video.newqu.ui.fragment.HomeFragment;
import com.video.newqu.ui.fragment.MineFragment;
import com.video.newqu.ui.presenter.MainPresenter;
import com.video.newqu.upload.manager.BatchFileUploadManager;
import com.video.newqu.util.DateParseUtil;
import com.video.newqu.util.GradeUtil;
import com.video.newqu.util.KSYAuthorPermissionsUtil;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScanWeixin;
import com.video.newqu.util.ShareUtils;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.util.VideoComposeProcessor;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import cn.jpush.android.api.JPushInterface;
import me.leolin.shortcutbadger.ShortcutBadger;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/5/20 10:20
 * 主页
 */

public class MainActivity extends TopBaseActivity implements MainContract.View {

    private static final String TAG = MainActivity.class.getSimpleName();
    private List<Fragment> mFragments=null;
    private ActivityMainBinding bindingView;
    private int mCureenViewIndex=0;
    private RadioButton[] mRadioButtons;
    private boolean isLogin=false;//登录成功后是否显示我的界面
    //保证程序尽量不OOM，只好牺牲了
    private WeakReference<List<WeiXinVideo>> mListWeakReference=null;
    private WeakReference<ScanWeixin> mWeakReferenceScanWeiXin=null;
    private WeakReference<BatchFileUploadManager> mUploadManagerWeakReference=null;
    private WeakReference<MainPresenter> mMainPresenterWeakReference=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        VideoApplication.mBuildChanleType = SystemUtils.getPublishChannel();//渠道ID
        super.onCreate(savedInstanceState);
        SharedPreferencesUtil.getInstance().putBoolean(Constant.KEY_MAIN_INSTANCE,true);
        bindingView = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mMainPresenterWeakReference = new WeakReference<MainPresenter>(new MainPresenter(MainActivity.this));
        mMainPresenterWeakReference.get().attachView(this);
        EventBus.getDefault().register(this);
        initWidgets();
    }


    /**
     * 初始化控件
     */
    private void initWidgets() {
        if(null==mFragments) mFragments = new ArrayList<>();
        mFragments.add(new HomeFragment());
        mFragments.add(new MineFragment());
        bindingView.vpView.setAdapter(new XinQuFragmentPagerAdapter(getSupportFragmentManager(), mFragments));
        bindingView.vpView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCureenViewIndex=position;
                if(1==position){
                    goneTabMessageCount();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bindingView.vpView.setCurrentItem(0);
        mCureenViewIndex=0;


        mRadioButtons=new RadioButton[2];
        mRadioButtons[0]=bindingView.rbHome;
        mRadioButtons[1]=bindingView.rbMine;
        View.OnClickListener onTabClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int childViewIndex=0;
                if(null!=bindingView.tvTipsMineMessage&&bindingView.tvTipsMineMessage.getVisibility()==View.VISIBLE){
                    bindingView.tvTipsMineMessage.setVisibility(View.GONE);
                }
                switch (v.getId()) {
                    case R.id.rb_home:
                        childViewIndex=0;
                        break;
                    case R.id.rb_mine:
                        childViewIndex=1;
                        break;
                }

                //如果未登录，拦截
                if(1==childViewIndex&&null==VideoApplication.getInstance().getUserData()){
                    isLogin=true;
                    login();
                    return;
                }
                //将再次点击事件拦截，用于处理刷新
                if(mCureenViewIndex==childViewIndex){
                    updataChildView(childViewIndex);
                    return;
                }
                bindingView.vpView.setCurrentItem(childViewIndex);
            }
        };
        bindingView.rbHome.setOnClickListener(onTabClickListener);
        bindingView.rbMine.setOnClickListener(onTabClickListener);


        //拍摄/上传
        bindingView.reMenuCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=bindingView.tvTipsMessage&&bindingView.tvTipsMessage.getVisibility()==View.VISIBLE){
                    bindingView.tvTipsMessage.setVisibility(View.GONE);
                }
                //必须登录才能使用拍摄和编辑功能
                if(null==VideoApplication.getInstance().getUserData()){
                    login();
                    return;
                }

                TakePicturePopupWindow picturePopupWindow=new TakePicturePopupWindow(MainActivity.this);
                picturePopupWindow.setOnTakePictureListener(new TakePicturePopupWindow.OnTakePictureListener() {
                    @Override
                    public void onClick(int type) {
                        if(1==type){
                            Intent intent = new Intent(MainActivity.this, MediaRecordActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.menu_enter, 0);//进场动画
                        }else if(2==type){
                            MediaLocationVideoListActivity.start(MainActivity.this);
                        }

                    }
                });
                picturePopupWindow.showAtLocation(getWindow().getDecorView(), Gravity.BOTTOM,0,0);
            }
        });

        //第一次使用弹出使用提示
        if(1!=SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_MAIN_CODE)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    bindingView.tvTipsMessage.setVisibility(View.VISIBLE);
                    bindingView.tvTipsMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            bindingView.tvTipsMessage.setVisibility(View.GONE);
                        }
                    });
                    SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_MAIN_CODE,1);
                }
            },1000);
        }

        //第一次安装应用
        if (null!=mMainPresenterWeakReference&&null!= mMainPresenterWeakReference.get()&&!SharedPreferencesUtil.getInstance().getBoolean(Constant.REGISTER_OPEN_APP)) {
            mMainPresenterWeakReference.get().registerApp();
        }
        //不是第一次启动并且如果刚好是一个礼拜了
        if(1==SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_MAIN_CODE)&&SharedPreferencesUtil.getInstance().getInt(Constant.SETTING_TODAY_WEEK_SUNDY)==DateParseUtil.getTodayWeekSundy()){
            //如果今天未扫描视频
            if(!SharedPreferencesUtil.getInstance().getBoolean(Constant.SETTING_DAY)){
                File filePath = new File(NetContants.WEICHAT_VIDEO_PATH);
                //如果微信聊天文件夹存在
                if(filePath.exists()){
                    new ScanWeChatDirectoryTask().execute(filePath.getAbsolutePath());
                    //不存在微信文件夹，检查更新
                }else{
                    Bugly.init(this, "2f71d3ad00", false);
                    checkedUploadVideoEvent();//检查上传任务
                }
            }else{
                Bugly.init(this, "2f71d3ad00", false);
                checkedUploadVideoEvent();//检查上传任务
            }
        }else{
            //如果不是刚好一个礼拜，还原扫描状态为未扫描
            SharedPreferencesUtil.getInstance().putBoolean(Constant.SETTING_DAY,false);//标记为今天已扫描
            Bugly.init(this, "2f71d3ad00", false);
            checkedUploadVideoEvent();//检查上传任务
        }
    }




    /**
     * 拦截的刷新事件
     * @param poistion
     */
    private void updataChildView(int poistion) {
        if(null!=mFragments&&mFragments.size()>0){
            Fragment fragment = mFragments.get(poistion);
            if(null!=fragment){
                if(fragment instanceof HomeFragment){
                    ((HomeFragment) fragment).fromMainUpdata();
                }else if(fragment instanceof MineFragment){
                    ((MineFragment) fragment).fromMainUpdata();
                }
            }
        }
    }


    /**
     * 显示点击我的刷新信息提示
     */
    public void showMineRefreshTips(){
        //上传功能提示不能和刷新功能提示一起出现
        if(1!=SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_MINE_REFRESH_CODE)&&bindingView.tvTipsMessage.getVisibility()!=View.VISIBLE){
            if(bindingView.tvTipsMineMessage.getVisibility()!=View.VISIBLE){
                bindingView.tvTipsMineMessage.setVisibility(View.VISIBLE);
                bindingView.tvTipsMineMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bindingView.tvTipsMineMessage.setVisibility(View.GONE);
                    }
                });
                SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_MINE_REFRESH_CODE,1);
            }
        }
    }

    /**
     * 扫描本地视频
     */
    private class ScanWeChatDirectoryTask extends AsyncTask<String,Void,List<WeiXinVideo>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(null==mWeakReferenceScanWeiXin||null==mWeakReferenceScanWeiXin.get()){
                ScanWeixin scanWeixin = new ScanWeixin();
                mWeakReferenceScanWeiXin=new WeakReference<ScanWeixin>(scanWeixin);
            }
        }

        @Override
        protected List<WeiXinVideo> doInBackground(String... params) {
            if(null!=params&&params.length>0){
                if(null!=mWeakReferenceScanWeiXin&&null!=mWeakReferenceScanWeiXin.get()){
                    mWeakReferenceScanWeiXin.get().setExts("mp4");
                    mWeakReferenceScanWeiXin.get().setScanEvent(true);
                    mWeakReferenceScanWeiXin.get().setEvent(false);
                    mWeakReferenceScanWeiXin.get().setMinDurtion(3);
                    mWeakReferenceScanWeiXin.get().setMaxDurtion(Constant.MEDIA_VIDEO_EDIT_MAX_DURTION);
                    List<WeiXinVideo> weiXinVideos = mWeakReferenceScanWeiXin.get().scanFiles(params[0]);
                    List<WeiXinVideo> newVideoList=null;
                    if (null != weiXinVideos && weiXinVideos.size() > 0) {
                        //对视频时间进行倒序排序
                        Collections.sort(weiXinVideos, new Comparator<WeiXinVideo>() {
                            @Override
                            public int compare(WeiXinVideo o1, WeiXinVideo o2) {
                                return o2.getVideoCreazeTime().compareTo(o1.getVideoCreazeTime());
                            }
                        });
                        DBScanWeiCacheManager DBScanWeiCacheManager = new DBScanWeiCacheManager(MainActivity.this);
                        newVideoList = new ArrayList<>();
                        //只保留9个最新视频,且与上次不能重复
                        List<WeiXinVideo> locationVideoList = DBScanWeiCacheManager.getUploadVideoList();//之前扫描的所有记录
                        if (null != locationVideoList && locationVideoList.size() > 0) {
                            for (int i = 0; i < weiXinVideos.size(); i++) {
                                if (newVideoList.size() >= 9) {
                                    break;
                                }
                                WeiXinVideo weiXinVideo = weiXinVideos.get(i);
                                boolean flag = false;
                                for (int j = 0; j < locationVideoList.size(); j++) {
                                    WeiXinVideo locationVideo = locationVideoList.get(j);
                                    if (TextUtils.equals(weiXinVideo.getFileName(), locationVideo.getFileName())) {
                                        flag = true;
                                        break;
                                    }
                                }
                                if(!flag) {
                                    newVideoList.add(weiXinVideo);
                                }
                            }
                        } else {
                            for (int i = 0; i < weiXinVideos.size(); i++) {
                                if (newVideoList.size() >= 9) {
                                    break;
                                }
                                newVideoList.add(weiXinVideos.get(i));
                            }
                        }
                        if(null!=newVideoList&&newVideoList.size()>0){
                            for (int i = 0; i < newVideoList.size(); i++) {
                                WeiXinVideo weiXinVideo = newVideoList.get(i);
                                DBScanWeiCacheManager.insertNewUploadVideoInfo(weiXinVideo);
                            }
                        }
                        return newVideoList;
                    } else {
                        return null;
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<WeiXinVideo> weiXinVideos) {
            super.onPostExecute(weiXinVideos);
            if(null!=weiXinVideos&&weiXinVideos.size()>0){
                try {
                    //当前运行的是自己
                    if(null!=SystemUtils.getTopActivity()){
                        if(!MainActivity.this.isFinishing()&&TextUtils.equals("com.video.newqu.ui.activity.MainActivity", SystemUtils.getTopActivity())){
                            LocationVideoUploadDialog locationVideoUploadDialog = new LocationVideoUploadDialog(MainActivity.this);
                            locationVideoUploadDialog.setData(weiXinVideos);
                            locationVideoUploadDialog.setOnDialogUploadListener(new LocationVideoUploadDialog.OnDialogUploadListener() {
                                @Override
                                public void onUploadVideo() {
                                    checkedUploadVideoEvent();
                                }
                            });
                            locationVideoUploadDialog.show();
                        }else{
                            mListWeakReference = new WeakReference<List<WeiXinVideo>>(weiXinVideos);
                        }
                    }else{
                        LocationVideoUploadDialog locationVideoUploadDialog = new LocationVideoUploadDialog(MainActivity.this);
                        locationVideoUploadDialog.setData(weiXinVideos);
                        locationVideoUploadDialog.setOnDialogUploadListener(new LocationVideoUploadDialog.OnDialogUploadListener() {
                            @Override
                            public void onUploadVideo() {
                                checkedUploadVideoEvent();
                            }
                        });
                        locationVideoUploadDialog.show();
                    }
                }catch (Exception e){
                    mListWeakReference = new WeakReference<List<WeiXinVideo>>(weiXinVideos);
                }
            }
        }
    }



    /**
     * 检查更新
     */
    private void checkedUpdata() {
        new APKUpdataManager(MainActivity.this).checkedBuild(new OnUpdataStateListener() {
            @Override
            public void onNeedUpdata( UpdataApkInfo updataApkInfo) {
                final UpdataApkInfo.DataBean dataBean = updataApkInfo.getData();
                if(null!=dataBean){
                    BuildManagerDialog buildManagerDialog =new BuildManagerDialog(MainActivity.this, R.style.UpdataDialogAnimation);
                    buildManagerDialog.setUpdataData(dataBean);
                    buildManagerDialog.setOnUpdataListener(new BuildManagerDialog.OnUpdataListener() {
                        @Override
                        public void onUpdata() {
                            //检查SD卡权限
                            RxPermissions.getInstance(MainActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    if(null!=aBoolean&&aBoolean){
                                        Intent service = new Intent(MainActivity.this, DownLoadService.class);
                                        service.putExtra("downloadurl", dataBean.getDownload());
                                        if(1==Utils.getNetworkType()){
                                            ToastUtils.shoCenterToast("正在下载中");
                                        }else{
                                            ToastUtils.shoCenterToast("下载任务将在连接WIFI后自动开始,请不要关闭本软件");
                                        }
                                        startService(service);
                                    }else{
                                        ToastUtils.shoCenterToast("下载失败！SD卡下载权限被拒绝");
                                    }
                                }
                            });
                        }
                    });
                    buildManagerDialog.show();
                }
            }

            @Override
            public void onNotUpdata(String data) {
                checkedUploadVideoEvent();//检查上传任务
            }

            @Override
            public void onUpdataError(String data) {
                checkedUploadVideoEvent();//检查上传任务
            }
        });
    }


    public int getCureenPoistion() {
        return mCureenViewIndex;
    }


    public void onResume() {
        super.onResume();
        //用户登录了
        if (VideoApplication.isLogin && null != VideoApplication.getInstance().getUserData()) {
            setLoginViewIsShow(false);
            upChildViewToPoistion(-1);
            VideoApplication.isLogin = false;
        }
        //用户注销了登录
        if (VideoApplication.isUnLogin && null == VideoApplication.getInstance().getUserData()) {
            setLoginViewIsShow(true);
            upChildViewToPoistion(-1);
            VideoApplication.isUnLogin = false;
        }
        //用户关注列表发生了变化
        if (VideoApplication.isFolloUser && null != VideoApplication.getInstance().getUserData()) {
            upChildViewToPoistion(1);
            VideoApplication.isFolloUser = false;
        }

        //用户是否合并单个视频成功
        if(VideoApplication.videoComposeFinlish){
            if(null!=mFragments&&mFragments.size()>0){
                Fragment fragment = mFragments.get(0);
                if(null!=fragment&&fragment instanceof HomeFragment){
                    VideoApplication.videoComposeFinlish = false;
                    ((HomeFragment) fragment).showUploadList(true);
                }
            }
        }
        //用户发布的视频列表数据发生了变化
        if(VideoApplication.isWorksChange){
            if(null!=mFragments&&mFragments.size()>0){
                Fragment fragment = mFragments.get(1);
                if(null!=fragment&&fragment instanceof MineFragment){
                    ((MineFragment) fragment).updataViewUI();
                }
                VideoApplication.isWorksChange=false;
            }
        }
        //用户添加了视频合成任务
        if(VideoApplication.isUpload){
            setCureenIndex(0);
            VideoApplication.isUpload=false;
        }

        if(null==VideoApplication.getInstance().getUserData()){
            setCureenIndex(0);
        }

        if (null!=mListWeakReference&&null!=mListWeakReference.get()&&mListWeakReference.get().size()>0&&!MainActivity.this.isFinishing()) {
            LocationVideoUploadDialog locationVideoUploadDialog = new LocationVideoUploadDialog(MainActivity.this);
            locationVideoUploadDialog.setData(mListWeakReference.get());
            locationVideoUploadDialog.setOnDialogUploadListener(new LocationVideoUploadDialog.OnDialogUploadListener() {
                @Override
                public void onUploadVideo() {
                    if(null!=mListWeakReference.get()){
                        mListWeakReference.get().clear();
                        mListWeakReference.clear();
                    }
                    checkedUploadVideoEvent();
                }
            });
            locationVideoUploadDialog.show();
        }else{
            GradeUtil.init(this);
        }
    }

    /**
     * 显示某个界面
     * @param index
     */
    private void setCureenIndex(int index) {
        if(null!=bindingView){
            if(null!=mFragments&&mFragments.size()>0&&null!=mRadioButtons&&mRadioButtons.length>0){
                if(index!=bindingView.vpView.getCurrentItem()){
                    bindingView.vpView.setCurrentItem(index);
                }
                mRadioButtons[index].setChecked(true);
            }
        }
    }


    /**
     * 刷新首页我的子界面,让MineFragment根据角标自己刷新其嵌套的子Fragment
     * @param poistion
     */
    public void upChildViewToPoistion(int poistion) {
        if(null!=mFragments&&mFragments.size()>0){
            //首页和我的都需要刷新
            if(-1==poistion){
                Fragment homeFragment = mFragments.get(0);
                if(null!=homeFragment&&homeFragment instanceof HomeFragment){
                    ((HomeFragment) homeFragment).upAllChildView();
                }

                Fragment mineFragment = mFragments.get(1);
                if(null!=mineFragment&&mineFragment instanceof MineFragment){
                    ((MineFragment) mineFragment).updataViewUI();
                }
            //只刷新首页
            }else if (0==poistion){
                Fragment homeFragment = mFragments.get(0);
                if(null!=homeFragment&&homeFragment instanceof HomeFragment){
                    ((HomeFragment) homeFragment).upAllChildView();
                }
            //只刷新我的
            }else if(1==poistion){
                Fragment mineFragment = mFragments.get(1);
                if(null!=mineFragment&&mineFragment instanceof MineFragment){
                    ((MineFragment) mineFragment).updataViewUI();
                }
            }
        }
    }

    /**
     * 显示和隐藏登录界面
     * @param isShow
     */
    private void setLoginViewIsShow(boolean isShow) {
        if(null!=mFragments&&mFragments.size()>0){
            Fragment homeFragment = mFragments.get(0);
            if(null!=homeFragment&&homeFragment instanceof HomeFragment){
                ((HomeFragment) homeFragment).isShowLoginView(isShow);
            }

            Fragment mineFragment = mFragments.get(1);
            if(null!=mineFragment&&mineFragment instanceof MineFragment){
                ((MineFragment) mineFragment).isShowLoginView(isShow);
            }
        }
    }




    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }


    /**
     * 隐藏个人中心的消息数量标记
     */
    private void goneTabMessageCount() {
        if(bindingView.tvMenuMineMsgCount.getVisibility()==View.VISIBLE){
            bindingView.tvMenuMineMsgCount.setText("");
            bindingView.tvMenuMineMsgCount.setVisibility(View.GONE);
            ShortcutBadger.removeCount(getApplicationContext()); //for 1.1.4+
        }
    }


    /**
     * 结束APP
     */
    private void destoryApp() {
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
        ActivityCollectorManager.finlishAllActivity();
        finish();
    }



    /**
     * 设置首页 "我的" 消息数量
     *
     * @param count
     */
    public void setMessageCount(int count) {
        if (count<=0) {
            bindingView.tvMenuMineMsgCount.setVisibility(View.GONE);
            bindingView.tvMenuMineMsgCount.setText(count + "");
        } else {
            bindingView.tvMenuMineMsgCount.setVisibility(View.VISIBLE);
            bindingView.tvMenuMineMsgCount.setText(count + "");
        }
    }

    /**
     * 刷新首页的新消息数量
     * @param count
     */
    public void showNewMessageDot(int count) {
        if(null!=mFragments&&mFragments.size()>0){
            Fragment fragment = mFragments.get(0);
            if(null!=fragment&&fragment instanceof HomeFragment){
                ((HomeFragment) fragment).showNewMessageDot(count);
            }
        }
    }

    /**
     * 拦截返回和菜单事件
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        ExitAppDialog exitAppDialog=new ExitAppDialog(MainActivity.this);
        exitAppDialog.setOnDialogClickListener(new ExitAppDialog.OnDialogClickListener() {
            @Override
            public void onExitApp() {
                destoryApp();
            }
        });
        exitAppDialog.show();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //停止视频扫描，如果在扫描的话
        if(null!=mWeakReferenceScanWeiXin&&null!=mWeakReferenceScanWeiXin.get()){
            mWeakReferenceScanWeiXin.get().setScanEvent(false);//停止扫描
            mWeakReferenceScanWeiXin.clear();
        }
        if (null!=mUploadManagerWeakReference&&null != mUploadManagerWeakReference.get()){
            mUploadManagerWeakReference.get().pause();
            mUploadManagerWeakReference.clear();
        }
        SharedPreferencesUtil.getInstance().putBoolean(Constant.KEY_MAIN_INSTANCE,false);
        EventBus.getDefault().unregister(this);
        JPushInterface.stopPush(getApplicationContext());
        VideoComposeProcessor.getInstance().stopAllCompose();
        ApplicationManager.getInstance().onDestory();
        Runtime.getRuntime().gc();
    }

    /**
     * 提供给子界面的登录方法
     */
    public void login() {
        Intent intent = new Intent(MainActivity.this, LoginGroupActivity.class);
        startActivityForResult(intent, Constant.INTENT_LOGIN_EQUESTCODE);
        overridePendingTransition( R.anim.menu_enter,0);//进场动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode,resultCode,data);
        //登录意图，需进一步确认
        if (Constant.INTENT_LOGIN_EQUESTCODE == requestCode && resultCode == Constant.INTENT_LOGIN_RESULTCODE) {
            if (null != data) {
                if(isLogin){
                    setCureenIndex(1);
                }
                upChildViewToPoistion(-1);
                //登录成功,判断用户有没有绑定手机号码
                if (null!=VideoApplication.getInstance().getUserData()&&!VideoApplication.getInstance().userIsBinDingPhone()) {
                    binDingPhoneNumber();
                }
            }
        }else if(requestCode==0xa1){
            if(null!=mFragments&&mFragments.size()>0){
                Fragment fragment = mFragments.get(1);
                if(null!=fragment&&fragment instanceof MineFragment){
                    ((MineFragment)fragment).clippingPictures();
                }
            }
        //用户信息不补全界面
        }else if(requestCode==0xa2){
            EventBus.getDefault().post(new MessageEvent("clip_pic"));
        }
        isLogin=false;
    }

    /**
     * 分享
     * @param shareInfo
     * @param poistion
     */
    public void shareIntent(ShareInfo shareInfo, int poistion) {
        if(null== shareInfo)return;
        if(TextUtils.isEmpty(shareInfo.getVideoID())) return;
        String url = "http://app.nq6.com/home/show/index?id=" + shareInfo.getVideoID();
        String token = MD5.hexdigest(url + "xinqu_123456");
        shareInfo.setUrl(url+"&token=" + token);//+"&share_type="+"1"
        share(shareInfo,poistion);
    }

    /**
     * 分享
     */

    protected void share(ShareInfo shareInfo,int pistion) {
        if(null==shareInfo) return;

        switch (pistion) {
            case 0:
                ShareUtils.share(MainActivity.this,shareInfo, SHARE_MEDIA.WEIXIN,this);
                break;
            case 1:
                ShareUtils.share(MainActivity.this,shareInfo,SHARE_MEDIA.QQ,this);
                break;
            case 2:
                ShareUtils.share(MainActivity.this,shareInfo,SHARE_MEDIA.SINA,this);
                break;
            case 3:
                ShareUtils.share(MainActivity.this,shareInfo,SHARE_MEDIA.WEIXIN_CIRCLE,this);
                break;
            case 4:
                ShareUtils.share(MainActivity.this,shareInfo,SHARE_MEDIA.QZONE,this);
                break;
            case 5:
                shareOther(shareInfo);
                break;
            default:
                shareOther(shareInfo);
        }
    }


    /**
     * 刷新通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (null != event) {
            //收到了动态消息
            if (TextUtils.equals(Constant.EVENT_NEW_MESSAGE, event.getMessage())) {
                int extar = event.getExtar();
                setMessageCount(extar);
                Logger.d(TAG,"onMessageEvent,未读消息数量"+extar);
                EventBus.getDefault().post(new MessageEvent(Constant.EVENT_UPDATA_MESSAGE_UI));
             //网络发生了变化
            } else if (TextUtils.equals("event_home_updload_weicacht", event.getMessage())) {
                if(null!=mFragments&&mFragments.size()>0){
                    Fragment fragment = mFragments.get(0);
                    if(null!=fragment&&fragment instanceof HomeFragment){
                        ((HomeFragment) fragment).changeUploadVideoState();
                    }
                }
                checkedUploadVideoEvent();
                //金山云鉴权
                if(!AuthInfoManager.getInstance().getAuthState()){
                    ThreadManager.getInstance().createLongPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            //在这里与金山云通信获得授权
                            KSYAuthorPermissionsUtil.init();
                        }
                    });
                }
             //用户作品或者收藏列表发生了变化
            }else if(TextUtils.equals(Constant.EVENT_MAIN_UPDATA_MINE_WORKS_FOLLOW, event.getMessage())){
                if(null!=mFragments&&mFragments.size()>0){
                    Fragment fragment = mFragments.get(0);
                    if(null!=fragment&&fragment instanceof MineFragment){
                        ((MineFragment) fragment).updataViewUI();
                        VideoApplication.isWorksChange = false;
                    }
                }
             //需要刷新关注列表和热门列表
            }else if(TextUtils.equals(Constant.EVENT_HOME_FOLLOW_HOT_LIST,event.getMessage())){
                if(null!=mFragments&&mFragments.size()>0){
                    Fragment fragment = mFragments.get(0);
                    if(null!=fragment&&fragment instanceof HomeFragment){
                        ((HomeFragment) fragment).upAllChildView();
                    }
                }
            }
        }
    }


    /**
     * 检查本地上传列表中是否未完成的上传任务,微信的
     */
    private void checkedUploadVideoEvent() {

        List<WeiChactVideoInfo> uploadVideoList = ApplicationManager.getInstance().getWeiXinVideoUploadDB().getUploadVideoList();
        if (null != uploadVideoList && uploadVideoList.size() > 0) {
            //WIFI网络自动下载
            if (1 == Utils.getNetworkType()) {
                if (null==mUploadManagerWeakReference||null == mUploadManagerWeakReference.get()) {
                    BatchFileUploadManager.Builder builder = new BatchFileUploadManager.Builder();
                    mUploadManagerWeakReference = new WeakReference<BatchFileUploadManager>(builder.build());
                }
                mUploadManagerWeakReference.get().upload(uploadVideoList);
            } else {
                if(ConfigSet.getInstance().isMobileUpload()){
                    if (null==mUploadManagerWeakReference||null == mUploadManagerWeakReference.get()) {
                        BatchFileUploadManager.Builder builder = new BatchFileUploadManager.Builder();
                        mUploadManagerWeakReference = new WeakReference<BatchFileUploadManager>(builder.build());
                    }
                    mUploadManagerWeakReference.get().upload(uploadVideoList);
                }else{
                    if (null != mUploadManagerWeakReference.get()) mUploadManagerWeakReference.get().puseAllUploadTask();
                }
            }
        }
    }

    /**
     * 切换首页第几个Fragment的第几个childFragment
     * @param groupIndex 父Fragment(MainActivity的直接子Fragment)索引
     * @param childIndex 子Fragment(MainActivity的直接子Fragment的嵌套的子Fragment)索引
     */
    public void currentHomeFragmentChildItemView(int groupIndex,int childIndex) {
        if(null==mFragments||mFragments.size()<=0) return;
        if(groupIndex<0||groupIndex>=mFragments.size()) return;

        if(null!=mFragments&&mFragments.size()>0&&mFragments.size()>0&&null!=mRadioButtons&&mRadioButtons.length>0){

            if(bindingView.vpView.getCurrentItem()!=groupIndex){
                bindingView.vpView.setCurrentItem(groupIndex);
            }
            mRadioButtons[groupIndex].setChecked(true);
            Fragment fragment = mFragments.get(groupIndex);
            if(null!=fragment){
                if(fragment instanceof HomeFragment){
                    ((HomeFragment) fragment).currentChildView(childIndex);
                }else if(fragment instanceof MineFragment){
                    ((MineFragment) fragment).currentChildView(childIndex);
                }
            }
        }
    }
}
