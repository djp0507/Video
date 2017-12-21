package com.video.newqu.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ksyun.media.shortvideo.utils.AuthInfoManager;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.MineMenuAdapter;
import com.video.newqu.adapter.XinQuFragmentPagerAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.MineTabInfo;
import com.video.newqu.bean.MineUserInfo;
import com.video.newqu.bean.NotifactionMessageInfo;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentMineBinding;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.manager.ThreadManager;
import com.video.newqu.ui.activity.AuthorDetailsActivity;
import com.video.newqu.ui.activity.ClipImageActivity;
import com.video.newqu.ui.activity.MainActivity;
import com.video.newqu.ui.activity.MediaPictruePhotoActivity;
import com.video.newqu.ui.contract.UserMineContract;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.UserInfoPresenter;
import com.video.newqu.util.AndroidNFileUtils;
import com.video.newqu.util.AnimationUtil;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.FileUtils;
import com.video.newqu.util.KSYAuthorPermissionsUtil;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.view.widget.GlideCircleTransform;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/5/22 16:02
 * 首页-我的
 */

public class MineFragment extends BaseFragment<FragmentMineBinding> implements UserMineContract.View {

    private static final String TAG = MineFragment.class.getSimpleName();
    private static List<MineTabInfo> mMineTabInfos=null;
    private static ArrayList<Fragment> mFragmentList;
    private static MineMenuAdapter mMenuAdapter;
    private static UserInfoPresenter mUserInfoPresenter;
    private static MineUserInfo.DataBean.InfoBean mUserInfo;
    private MainActivity mMainActivity;
    private AnimationDrawable mAnimationDrawable;
    private boolean isUpdata=true;//默认是否需要刷新
    private int mImageBgHeight;
    private final static int PERMISSION_REQUEST_CAMERA = 1;//摄像
    private boolean isNewMsgEcho=true;//是否回显消息

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) context;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_mine;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showContentView();
        mUserInfoPresenter = new UserInfoPresenter(getActivity());
        mUserInfoPresenter.attachView(this);
        mUserInfo= (MineUserInfo.DataBean.InfoBean)ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINE_USER_DATA);
        initTabAdapter();
        initHeaderUserData();
        //已经登陆了
        if(null!= VideoApplication.getInstance().getUserData()){
            if(null!=mMainActivity){
                mMainActivity.showMineRefreshTips();
            }
            checkedMsgCount();
        }else{
            isUpdata=false;
        }
    }


    @Override
    protected void initViews() {
        int minHeight=0;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            minHeight= SystemUtils.getStatusBarHeight(getActivity());
            if(minHeight<=0){
                minHeight=ScreenUtils.dpToPxInt(25);
            }
        }
        bindingView.topEmptyView.getLayoutParams().height=minHeight;

        if(ScreenUtils.getScreenHeight()>=1280){
            bindingView.frameLayout.getLayoutParams().height=ScreenUtils.dpToPxInt(250);
        }else{
            bindingView.frameLayout.getLayoutParams().height=ScreenUtils.dpToPxInt(230);
        }
        bindingView.collapseToolbar.setMinimumHeight(minHeight);

        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    //设置
                    case R.id.btn_setting:
                        startTargetActivity(Constant.KEY_FRAGMENT_TYPE_SETTINGS,"设置中心",null,0);
                        break;

                    //头像，个人信息编辑
                    case R.id.iv_user_icon:
                        if(null==VideoApplication.getInstance().getUserData()){
                            if(null!=mMainActivity){
                                mMainActivity.login();
                            }
                        }else{
                            if(null==mUserInfo){
                                ToastUtils.shoCenterToast("用户信息过期，请重新登录!");
                                canelUserData();
                                return;
                            }
                            CompleteUserDataDialogFragment.getInstance(mUserInfo,"修改个人信息",Constant.MODE_USER_EDIT).setOnDismissListener(new CompleteUserDataDialogFragment.OnDismissListener() {
                                @Override
                                public void onDismiss(boolean change) {
                                    if(change){
                                        updataViewUI();
                                    }
                                }
                            }).show(getChildFragmentManager(),"edit");
                        }
                        break;
                    //点击了更多，查看用户信息
                    case R.id.re_user_data_view:
                        if(null==mUserInfo){
                            ToastUtils.shoCenterToast("用户信息过期，请重新登录!");
                            canelUserData();
                            return;
                        }
                        if(null==VideoApplication.getInstance().getUserData()){
                            if(null!=mMainActivity){
                                mMainActivity.login();
                            }
                        }else{
                            AuthorDetailsActivity.start(getActivity(),VideoApplication.getLoginUserID());
                        }
                        break;
                    //我的粉丝
                    case R.id.tv_fans_count:
                        if(null==mUserInfo){
                            ToastUtils.shoCenterToast("用户信息过期，请重新登录!");
                            canelUserData();
                            return;
                        }
                        if(null==VideoApplication.getInstance().getUserData()){
                            if(null!=mMainActivity){
                                mMainActivity.login();
                            }
                        }else{
                            startTargetActivity(Constant.KEY_FRAGMENT_TYPE_FANS_LIST,"我的粉丝",VideoApplication.getLoginUserID(),1);
                        }

                        break;
                    //我的关注
                    case R.id.tv_follow_count:
                        if(null==mUserInfo){
                            ToastUtils.shoCenterToast("用户信息过期，请重新登录!");
                            canelUserData();
                            return;
                        }
                        if(null==VideoApplication.getInstance().getUserData()){
                            if(null!=mMainActivity){
                                mMainActivity.login();
                            }
                        }else{
                            startTargetActivity(Constant.KEY_FRAGMENT_TYPE_FOLLOW_USER_LIST,"我关注的用户",VideoApplication.getLoginUserID(),1);
                        }

                        break;
                    //拍照/选择本地照片
                    case R.id.iv_user_image_bg:
                        if(null==VideoApplication.getInstance().getUserData()){
                            if(null!=mMainActivity){
                                mMainActivity.login();
                            }
                        }else{
                            showPictureSelectorPop();
                        }
                        break;
                    //分享自己的主页
                    case R.id.btn_share:
                        shareMineHome();
                        break;
                    //通知消息
                    case R.id.btn_notifaction:
                        startTargetActivity(Constant.KEY_FRAGMENT_NOTIFACTION,"通知消息",null,0);
                        break;
                }
            }
        };
        bindingView.btnSetting.setOnClickListener(onClickListener);
        bindingView.ivUserIcon.setOnClickListener(onClickListener);
        bindingView.reUserDataView.setOnClickListener(onClickListener);
        bindingView.ivUserImageBg.setOnClickListener(onClickListener);
        bindingView.tvFansCount.setOnClickListener(onClickListener);
        bindingView.tvFollowCount.setOnClickListener(onClickListener);
        bindingView.btnShare.setOnClickListener(onClickListener);
        bindingView.btnNotifaction.setOnClickListener(onClickListener);
        bindingView.ivUserImageBg.measure(0,0);
        mImageBgHeight = bindingView.frameLayout.getLayoutParams().height;
        //添加滚动监听
        bindingView.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if(mImageBgHeight>0&&Math.abs(verticalOffset)>=mImageBgHeight){
                    if(bindingView.viewLine.getVisibility()!=View.GONE){
                        bindingView.viewLine.setVisibility(View.GONE);
                    }
                }else{
                    if(bindingView.viewLine.getVisibility()!=View.VISIBLE){
                        bindingView.viewLine.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }

    /**
     * 分享自己的主页
     */
    private void shareMineHome() {
        if(null==VideoApplication.getInstance().getUserData()){
            if(null!=mMainActivity){
                mMainActivity.login();
                return;
            }
        }else{
            if(null!=VideoApplication.getInstance().getUserData()&&!TextUtils.isEmpty(VideoApplication.getInstance().getUserData().getId())){
                ShareInfo shareInfo=new ShareInfo();
                shareInfo.setDesp("我在新趣小视频安家啦！这是我的主页，快来围观我吧！");
                shareInfo.setTitle("快来加入新趣，我在新趣等你！");
                shareInfo.setUserID(VideoApplication.getInstance().getUserData().getId());
                shareInfo.setUrl("http://app.nq6.com/home/user/index?user_id="+shareInfo.getUserID());
                if(null!=mMainActivity&&!mMainActivity.isFinishing()){
                    mMainActivity.shareMineHome(shareInfo);
                }
            }
        }
    }


    @Override
    protected void onVisible() {
        super.onVisible();
        //未登录，总是弹出登录框
        if(null==VideoApplication.getInstance().getUserData()){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(null!=mMainActivity){
                        mMainActivity.login();
                    }
                }
            },200);
//            SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_MINE_LOGIN_CODE,1);
        }
        if(isUpdata&&null!=VideoApplication.getInstance().getUserData()&&null!=bindingView&&null!=mMenuAdapter){
            if(null!=mUserInfoPresenter&&!mUserInfoPresenter.isLoading()){
                showFreshLodingView();
                getUserData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkedMsgCount();
        isNewMsgEcho=false;
    }

    /**
     * 获取用户基本信息
     */
    private void getUserData() {
        if(null!=mUserInfoPresenter&&!mUserInfoPresenter.isLoading()&&!getActivity().isFinishing()){
            mUserInfoPresenter.getUserInfo(VideoApplication.getLoginUserID());
        }
    }

    /**
     * 给外界调用的刷新界面
     */
    public void updataViewUI(){
        if(null!=VideoApplication.getInstance().getUserData()){
            isUpdata=true;
            if(null!=bindingView&&null!=mMenuAdapter&&null!=mUserInfoPresenter&&!mUserInfoPresenter.isLoading()){
                getUserData();
            }
            setChildViewRefresh(true);
        }else{
            isUpdata=false;
            canelUserData();
            setChildViewRefresh(false);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMainActivity=null;
    }


    /**
     * 初始化所有子界面
     */
    private void initTabAdapter() {
        //初始化菜单
        if(null==mMineTabInfos) mMineTabInfos=new ArrayList<>();
        mMineTabInfos.add(new MineTabInfo(getResources().getString(R.string.mine_fragment_works_title),0, true));
        mMineTabInfos.add(new MineTabInfo(getResources().getString(R.string.mine_fragment_like_title),0, false));
        mMineTabInfos.add(new MineTabInfo(getResources().getString(R.string.mine_fragment_message_title),0, false));
        bindingView.tabGridView.setNumColumns(3);
        mMenuAdapter = new MineMenuAdapter(getActivity(), mMineTabInfos);
        bindingView.tabGridView.setAdapter(mMenuAdapter);
        //设置菜单,跟随屏幕滚动的
        bindingView.tabGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                switchItemState(position,true);
            }
        });
        //初始化子界面
        if(null==mFragmentList) mFragmentList=new ArrayList<>();
        mFragmentList.add(new HomeWorksFragment());
        mFragmentList.add(new HomeLikeVideoFragment());
        mFragmentList.add(new HomeMessageFragment());
        bindingView.viewPager.setOffscreenPageLimit(3);
        bindingView.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switchItemState(position,false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bindingView.viewPager.setAdapter(new XinQuFragmentPagerAdapter(getChildFragmentManager(),mFragmentList));
        bindingView.viewPager.setCurrentItem(0);
    }


    /**
     * 切换Itemd的显示状态
     * @param position
     */
    private void switchItemState(int position,boolean isClick) {
        if(null!=mMineTabInfos&&mMineTabInfos.size()>0){
            for (int i = 0; i < mMineTabInfos.size(); i++) {
                MineTabInfo mineTabInfo = mMineTabInfos.get(i);
                if(mineTabInfo.isSelector()){
                    mineTabInfo.setSelector(false);
                }
            }
            mMineTabInfos.get(position).setSelector(true);
            updataTabAdapter();
        }
        //是否是点击切换
        if(isClick)  bindingView.viewPager.setCurrentItem(position);
    }


    /**
     * 刷新标题栏适配器
     */
    private void updataTabAdapter() {
        if(null!=mMenuAdapter){
            mMenuAdapter.setNewData(mMineTabInfos);
            mMenuAdapter.notifyDataSetChanged();
        }
    }



    /**
     * 刷新头部和列表数据
     */
    private void initHeaderUserData() {
        initUserData();
    }

    /**
     * 初始化用户信息
     */
    private void initUserData() {
        if(null==mUserInfo) return;
        if(null!=bindingView){
            //已登录，刷新
            if(null!=VideoApplication.getInstance().getUserData()&&null!=mUserInfo){
                bindingView.tvUserName.setText(TextUtils.isEmpty(mUserInfo.getNickname())?"火星人":mUserInfo.getNickname());
                try {
                    String decode = URLDecoder.decode(TextUtils.isEmpty(mUserInfo.getSignature())?"本宝宝暂时没有个性签名":mUserInfo.getSignature(), "UTF-8");
                    bindingView.tvUserDesp.setText(decode);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                bindingView.tvUserGrade.setText("Lv"+(TextUtils.isEmpty(mUserInfo.getRank())?"1":mUserInfo.getRank()));
                bindingView.tvFansCount.setText(TextUtils.isEmpty(mUserInfo.getFans())?"0粉丝":mUserInfo.getFans()+"粉丝");
                bindingView.tvFollowCount.setText(TextUtils.isEmpty(mUserInfo.getFollows())?"0关注":mUserInfo.getFollows()+"关注");
                bindingView.ivUserSex.setImageResource(TextUtils.isEmpty(mUserInfo.getGender())?R.drawable.iv_icon_sex_women:TextUtils.equals("女",mUserInfo.getGender())?R.drawable.iv_icon_sex_women:R.drawable.iv_icon_sex_man);
                //用户头像
                Glide.with(this)
                        .load(TextUtils.isEmpty(mUserInfo.getLogo())?R.drawable.iv_mine:mUserInfo.getLogo())
                        .error(R.drawable.iv_mine)
                        .placeholder(R.drawable.iv_mine)
                        .crossFade()//渐变
                        .animate(R.anim.item_alpha_in)//加载中动画
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .centerCrop()//中心点缩放
                        .skipMemoryCache(true)//跳过内存缓存
                        .transform(new GlideCircleTransform(getActivity()))
                        .into(bindingView.ivUserIcon);
                //设置头部背景封面
                Glide.with(this)
                        .load(TextUtils.isEmpty(mUserInfo.getImage_bg())?R.drawable.iv_mine_bg:mUserInfo.getImage_bg())
                        .error(R.drawable.iv_mine_bg)
                        .placeholder(R.drawable.iv_mine_bg)
                        .thumbnail(0.1f)
                        .animate(R.anim.item_alpha_in)//加载中动画
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .skipMemoryCache(true)//跳过内存缓存
                        .into(bindingView.ivUserImageBg);

                if(null!=mMineTabInfos&&mMineTabInfos.size()>0){
                    mMineTabInfos.get(0).setAboutCount(Integer.parseInt(TextUtils.isEmpty(mUserInfo.getVideo_count())?"0":mUserInfo.getVideo_count()));
                    mMineTabInfos.get(1).setAboutCount(Integer.parseInt(TextUtils.isEmpty(mUserInfo.getCollect_times())?"0":mUserInfo.getCollect_times()));
//                mMineTabInfos.get(2).setAboutCount(0); 交给消息界面来设置数据
                }
                updataTabAdapter();//刷新标题栏

                //未登录，还原
            }else{
                canelUserData();
            }
        }
    }

    /**
     * 清空用户所有信息
     */
    private void canelUserData() {
        bindingView.ivUserIcon.setImageResource(R.drawable.iv_mine);
        bindingView.ivUserImageBg.setImageResource(R.drawable.iv_mine_bg);
        bindingView.ivUserSex.setImageResource(R.drawable.iv_icon_sex_women);
        bindingView.tvFansCount.setText(0+"粉丝");
        bindingView.tvFollowCount.setText(0+"关注");
        bindingView.tvUserName.setText("--");
        bindingView.tvUserDesp.setText("--");
        bindingView.tvUserGrade.setText("Lv"+0);
        if(null!=mMineTabInfos&&mMineTabInfos.size()>0){
            mMineTabInfos.get(0).setAboutCount(0);
            mMineTabInfos.get(1).setAboutCount(0);
            mMineTabInfos.get(2).setAboutCount(0);
        }
        updataTabAdapter();//刷新标题栏
        mUserInfo=null;//个人信息信息的所有数据
    }




    /**
     * 刷新子界面
     * @param poistion 要刷新的界面角标
     */
    private void updataChildViewToPoistion(int poistion) {
        if(null!=mFragmentList&&mFragmentList.size()>0){
            Fragment fragment = mFragmentList.get(poistion);
            if(null!=fragment){
                if(fragment instanceof HomeWorksFragment){
                    ((HomeWorksFragment) fragment).fromMainUpdata();
                }else if(fragment instanceof HomeLikeVideoFragment){
                    ((HomeLikeVideoFragment) fragment).fromMainUpdata();
                }else if(fragment instanceof HomeMessageFragment){
                    ((HomeMessageFragment) fragment).fromMainUpdata();
                }
            }
        }
    }

    /**
     * 是否显示登录界面
     * @param isShow
     */
    public void isShowLoginView(boolean isShow) {
        if(null!=mFragmentList&&mFragmentList.size()>0){
            Fragment fragment = mFragmentList.get(0);
            if(null!=fragment&&fragment instanceof HomeWorksFragment){
                ((HomeWorksFragment) fragment).isShowLoginView(isShow);
            }
            Fragment likeFragment = mFragmentList.get(1);
            if(null!=likeFragment&&likeFragment instanceof HomeLikeVideoFragment){
                ((HomeLikeVideoFragment) likeFragment).isShowLoginView(isShow);
            }

            Fragment messageFragment = mFragmentList.get(2);
            if(null!=messageFragment&&messageFragment instanceof HomeMessageFragment){
                ((HomeMessageFragment) messageFragment).isShowLoginView(isShow);
            }
        }
    }




    @Override
    public void onDestroy() {
        if(null!=mUserInfoPresenter){
            mUserInfoPresenter.detachView();
        }
        super.onDestroy();
    }



    /**
     * 显示顶部刷新动画
     */
    private void showFreshLodingView() {
        if(null!=bindingView&&null!=bindingView.loadMoreLoadingView&&bindingView.loadMoreLoadingView.getVisibility()!=View.VISIBLE){
            bindingView.loadMoreLoadingView.setVisibility(View.VISIBLE);
            bindingView.loadMoreLoadingView.startAnimation(AnimationUtil.moveToViewTopLocation2());
            if(null==mAnimationDrawable) mAnimationDrawable = (AnimationDrawable) bindingView.ivLoadingIcon.getDrawable();
            if(!mAnimationDrawable.isRunning()) mAnimationDrawable.start();
        }
    }

    /**
     * 隐藏顶部刷新动画
     */
    private void hideFreshLodingView(){
        if(bindingView.loadMoreLoadingView.getVisibility()!=View.GONE){
            TranslateAnimation translateAnimation = AnimationUtil.moveToViewTop2();
            bindingView.loadMoreLoadingView.startAnimation(translateAnimation);

            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bindingView.loadMoreLoadingView.setVisibility(View.GONE);
                    if(null!=mAnimationDrawable&&mAnimationDrawable.isRunning()) mAnimationDrawable.stop();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }


    /**
     * 刷新标题中的消息数量
     * @param count
     */
    public void updataTab(int count) {
        if(null!=mMineTabInfos&&mMineTabInfos.size()>0){
            mMineTabInfos.get(2).setAboutCount(count);
            updataTabAdapter();
        }
    }

    /**
     * 刷新TAB数量
     * @param index 刷新第几个Item的Count
     */
    public void updataMineTabCount(int index) {
        if(index<0||index>2) return;
        //我的作品界面
        if(null!=mMineTabInfos&&mMineTabInfos.size()>0){
            if(index>=mMineTabInfos.size()) return;
            MineTabInfo mineTabInfo = mMineTabInfos.get(index);
            if(null!=mineTabInfo){
                int aboutCount = mineTabInfo.getAboutCount();
                if(aboutCount>0){
                    aboutCount--;
                }
                mineTabInfo.setAboutCount(aboutCount);
            }
            updataTabAdapter();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    /**
     * 刷新通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if (null != event) {
            if (TextUtils.equals(Constant.EVENT_UPDATA_MESSAGE_UI, event.getMessage())) {
                bindingView.viewTips.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 检查未读消息数量
     */
    private void checkedMsgCount() {
        if(null== VideoApplication.getInstance().getUserData()) return;
        List<NotifactionMessageInfo> messageList= (List<NotifactionMessageInfo>) ApplicationManager.getInstance().getCacheExample().getAsObject(VideoApplication.getLoginUserID()+Constant.CACHE_USER_MESSAGE);
        if(null!=messageList&&messageList.size()>0){
            int badgeCount=0;
            for (NotifactionMessageInfo notifactionMessageInfo : messageList) {
                if(!notifactionMessageInfo.isRead()){
                    badgeCount++;
                }
            }
            //处理桌面图标
            if(badgeCount>0){
                if(null!=mMainActivity){
                    mMainActivity.setMessageCount(badgeCount);
                }
                ShortcutBadger.applyCount(context.getApplicationContext(), badgeCount); //for 1.1.4+
                bindingView.viewTips.setVisibility(View.VISIBLE);
            }else{
                ShortcutBadger.applyCount(context.getApplicationContext(), 0); //for 1.1.4+
                bindingView.viewTips.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * 照片选择弹窗
     */
    private void showPictureSelectorPop() {
        try {
            //初始化
            if(null==mOutFilePath)  mOutFilePath = new File(Constant.IMAGE_PATH + IMAGE_DRR_PATH);

            //删除前面的缓存
            if(mOutFilePath.exists()&&mOutFilePath.isFile()){
                FileUtils.deleteFile(mOutFilePath);
            }
            mTempFile = new File(Constant.IMAGE_PATH + IMAGE_DRR_PATH_TEMP);
            if(mTempFile.exists()&&mTempFile.isFile()){
                FileUtils.deleteFile(mTempFile);
            }

        }catch (Exception e){
            showErrorToast(null,null,e.getMessage());
        }finally {

            List<VideoDetailsMenu> list=new ArrayList<>();
            VideoDetailsMenu videoDetailsMenu1=new VideoDetailsMenu();
            videoDetailsMenu1.setItemID(1);
            videoDetailsMenu1.setTextColor("#FF576A8D");
            videoDetailsMenu1.setItemName("从相册选择");
            list.add(videoDetailsMenu1);

            VideoDetailsMenu videoDetailsMenu2=new VideoDetailsMenu();
            videoDetailsMenu2.setItemID(2);
            videoDetailsMenu2.setTextColor("#FF576A8D");
            videoDetailsMenu2.setItemName("拍一张");
            list.add(videoDetailsMenu2);

            CommonMenuDialog commonMenuDialog =new CommonMenuDialog((AppCompatActivity) getActivity());
            commonMenuDialog.setData(list);
            commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
                @Override
                public void onItemClick(int itemID) {
                    //取消关注
                    switch (itemID) {
                        case 1:
                            headImageFromGallery();
                            break;
                        case 2:
                            headImageFromCameraCap();
                            break;
                    }
                }
            });
            commonMenuDialog.show();
        }
    }





    /**
     * 设置所有的子界面为待刷新状态
     * @param refresh
     */
    private void setChildViewRefresh(boolean refresh) {
        if(null!=mFragmentList&&mFragmentList.size()>0){
            HomeWorksFragment worksFragment = (HomeWorksFragment) mFragmentList.get(0);
            worksFragment.setRefresh(refresh);
            HomeLikeVideoFragment likeFragment = (HomeLikeVideoFragment) mFragmentList.get(1);
            likeFragment.setRefresh(refresh);
            HomeMessageFragment messageFragment = (HomeMessageFragment) mFragmentList.get(2);
            messageFragment.changeUI();
        }
    }



    /**
     * 显示用户基本信息
     * @param data
     */
    @Override
    public void showUserInfo(MineUserInfo data) {
        hideFreshLodingView();
        isUpdata=false;
        mUserInfo = data.getData().getInfo();
        ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MINE_USER_DATA);
        ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MINE_USER_DATA,mUserInfo);//这个存储期限应该是无限期的
        initUserData();
        updataChildViewToPoistion(bindingView.viewPager.getCurrentItem());
    }



    /**
     * 用户长传背景封面信息回调
     * @param data
     */
    @Override
    public void showPostImageBGResult(String data) {
        try {
            if(null!=mOutFilePath&&mOutFilePath.exists()&&mOutFilePath.isFile()){
                FileUtils.deleteFile(mOutFilePath);
                mOutFilePath=null;
            }
            if(null!=mTempFile&&mTempFile.exists()&&mTempFile.isFile()){
                FileUtils.deleteFile(mTempFile);
                mTempFile=null;
            }
        }catch (Exception e){

        }

        if(!TextUtils.isEmpty(data)){
            try {
                JSONObject jsonObject=new JSONObject(data);
                if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.UPLOAD_USER_PHOTO_SUCCESS)){

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingProgressedView.setResultsCompletes("上传成功", CommonUtils.getColor(R.color.app_style),true,Constant.PROGRESS_CLOSE_DELYAED_TIE);
                            mLoadingProgressedView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                //等待其动画播放完成消失后就刷新界面
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    getUserData();
                                }
                            });
                        }
                    });
                }else{
                    closeProgressDialog();
                    ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                }
            } catch (JSONException e) {
                closeProgressDialog();
                ToastUtils.shoCenterToast("上传失败");
                e.printStackTrace();
            }
        }else{
            closeProgressDialog();
        }
    }


    @Override
    public void showErrorView() {
        hideFreshLodingView();
        closeProgressDialog();
    }

    @Override
    public void complete() {
        closeProgressDialog();
    }



    //====================================拍摄图片And图片选择=========================================

    private File mTempFile;
    private File mOutFilePath;
    private static final String IMAGE_DRR_PATH = "photo_image.jpg";//最终输出图片
    private static final String IMAGE_DRR_PATH_TEMP = "photo_image_temp.jpg";//临时图片
    private static final int INTENT_CODE_GALLERY_REQUEST = 0xa0;//相册
    private static final int INTENT_CODE_CAMERA_REQUEST = 0xa1;//相册


    // 从本地相册选取图片作为头像
    private void headImageFromGallery() {
        Intent intentFromGallery = new Intent();
        // 设置文件类型
        intentFromGallery.setType("image/*");//选择图片
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentFromGallery, INTENT_CODE_GALLERY_REQUEST);
    }

    // 启动相机拍摄照片
    private void headImageFromCameraCap() {
        //检查SD读写权限
        RxPermissions.getInstance(getActivity()).request(Manifest.permission.CAMERA).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(null!=aBoolean&&aBoolean){
                    //判断相机是否可用
                    PackageManager pm = getActivity().getPackageManager();
                    boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                            || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                            || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD
                            || Camera.getNumberOfCameras() > 0;
                    //调用系统相机拍摄
                    if(hasACamera){
                        AndroidNFileUtils.startActionCapture(getActivity(),mTempFile,INTENT_CODE_CAMERA_REQUEST);
                        //使用自定义相机拍摄
                    }else{
                        Intent intent=new Intent(getActivity(),MediaPictruePhotoActivity.class);
                        intent.putExtra("output",mOutFilePath.getAbsolutePath());
                        intent.putExtra("output-max-width",800);
                        startActivityForResult(intent,Constant.REQUEST_TAKE_PHOTO);
                    }
                }else{
                    checkedPermission();
                }
            }
        });
    }

    /**
     * 检查拍照权限
     */
    private void checkedPermission() {
        int cameraPerm = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
                ToastUtils.shoCenterToast("大23，需要检测权限");
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_CAMERA);
            }
        } else {
            headImageFromCameraCap();
        }
    }

    /**
     * 获取权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    headImageFromCameraCap();
                } else {
                    ToastUtils.shoCenterToast("要正常使用拍摄功能，请务必授予拍照权限！");
                }
                break;
            }
        }
    }


    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== Activity.RESULT_CANCELED){
            return;
        }
        try {
            //拍照和裁剪返回
            if (resultCode == Activity.RESULT_OK && data != null && (requestCode == Constant.REQUEST_CLIP_IMAGE || requestCode == Constant.REQUEST_TAKE_PHOTO)) {
                String path = ClipImageActivity.ClipOptions.createFromBundle(data).getOutputPath();
                if (path != null) {
                    File imageFile = new File(path);
                    if(imageFile.exists()&&imageFile.isFile()){
                        showProgressDialog("上传中...",true);
                        mUserInfoPresenter.onPostImageBG(VideoApplication.getLoginUserID(),imageFile.getAbsolutePath());
                    }
                }else{
                    showErrorToast(null,null,"操作错误");
                }
                //本地相册选取的图片,转换为Path路径后再交给裁剪界面处理
            }else if(requestCode== INTENT_CODE_GALLERY_REQUEST){
                if(null!=data){
                    ContentResolver resolver =getActivity().getContentResolver();
                    Uri originalUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                        if(null!=bitmap){
                            String filePath = FileUtils.saveBitmap(bitmap, Constant.IMAGE_PATH + IMAGE_DRR_PATH_TEMP);
                            startClipActivity(filePath,mOutFilePath.getAbsolutePath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        showErrorToast(null,null,"操作错误"+e.getMessage());
                    }
                }
             //系统照相机拍照完成回调
            }else if(requestCode==INTENT_CODE_CAMERA_REQUEST){
                startClipActivity(mTempFile.getAbsolutePath(),mOutFilePath.getAbsolutePath());
            }
        }catch (Exception e){
            showErrorToast(null,null,"操作错误"+e.getMessage());
        }
    }

    /**
     * 系统相机拍摄返回
     */
    public void clippingPictures(){
        if(null!=mTempFile&&mTempFile.exists()&&null!=mOutFilePath){
            startClipActivity(mTempFile.getAbsolutePath(),mOutFilePath.getAbsolutePath());
        }
    }

    /**
     * 去裁剪
     * @param inputFilePath
     * @param outputFilePath
     */
    private void startClipActivity(String inputFilePath, String outputFilePath) {
        Intent intent = new Intent(getActivity(), ClipImageActivity.class);
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 2);
        intent.putExtra("maxWidth", 800);
        intent.putExtra("tip", "");
        intent.putExtra("inputPath", inputFilePath);
        intent.putExtra("outputPath", outputFilePath);
        intent.putExtra("clipCircle",false);
        startActivityForResult(intent, Constant.REQUEST_CLIP_IMAGE);
    }

    /**
     * 来自首页的刷新命令
     */
    public void fromMainUpdata() {
        //改成刷新个人信息后直接刷新正在显示的子Fragment
        if(null!=bindingView&&null!=VideoApplication.getInstance().getUserData()){
            if(null!=mUserInfoPresenter&&!mUserInfoPresenter.isLoading()){
                showFreshLodingView();
                mUserInfoPresenter.getUserInfo(VideoApplication.getLoginUserID());
            }else{
                showErrorToast(null,null,"点击太频繁了");
            }
        }else{
            if(null!=mMainActivity&&!mMainActivity.isFinishing()){
                ToastUtils.shoCenterToast("登录后才能刷新数据");
                mMainActivity.login();
            }
        }
    }


    /**
     * 切换界面
     * @param childIndex
     */
    public void currentChildView(int childIndex) {
        if(null==mFragmentList||mFragmentList.size()<=0) return;
        if(childIndex<0||childIndex>=mFragmentList.size()) return;
        if(null!=bindingView&&null!=bindingView.viewPager){
            bindingView.viewPager.setCurrentItem(childIndex,true);
        }
    }
}
