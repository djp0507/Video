package com.video.newqu;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import com.blankj.utilcode.util.Utils;
import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.kk.securityhttp.domain.GoagalInfo;
import com.kk.securityhttp.net.contains.HttpConfig;
import com.ksyun.media.player.KSYHardwareDecodeWhiteList;
import com.mob.MobSDK;
import com.umeng.analytics.MobclickAgent;
import com.umeng.socialize.Config;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.UMShareAPI;
import com.video.newqu.bean.UserData;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.manager.ThreadManager;
import com.video.newqu.util.ACache;
import com.video.newqu.util.CommonDateParseUtil;
import com.video.newqu.util.ContentCheckKey;
import com.video.newqu.util.DateParseUtil;
import com.video.newqu.util.FaceConversionUtil;
import com.video.newqu.util.KSYAuthorPermissionsUtil;
import com.video.newqu.util.SharedPreferencesUtil;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;

/**
 *  TinyHung@outlook.com
 *  2017/5/20 10:53
 */

public class VideoApplication extends Application {

    private static VideoApplication mInstance;
    public static Context appContext;
    public static boolean videoComposeFinlish;
    private UserData.DataBean.InfoBean mUserData=null;
    public static  String mUuid;
    public static boolean isWorksChange=false;//用户中心是否发生了变化
    public static  boolean isUpload=false;//用户上传了视频
    public static  boolean isLogin=false;//用户登录了
    public static  boolean isUnLogin=false;//用户注销了登录
    public static boolean isFolloUser=false;//用户关注列表发生了变化
    public static int mToday;
    public static int mBuildChanleType=0;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);
    }

    //极光 appkey:24dca46eb98b3f3d93667b20  secret:d5ac29ce99a07ab51a7b8e26
    public static VideoApplication getInstance() {
        return mInstance;
    }

    /**友盟分享设置每次都授权
     UMShareConfig config = new UMShareConfig();
     config.isNeedAuthOnGetUserInfo(true);
     UMShareAPI.get(InfoDetailActivity.this).setShareConfig(config);\
     鉴权：MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDMWEKeQiYlkjHPlByIVlbqcYsHlJ5SYkjXqVgvgqbSemloFDfySaYxsWnv8cyq9agYlb8PxjjQcWWqL06O5HKp1cedbUbjlZ3mRb3qnkLH4j8QTQjyPW6F2nvWln6djF10b2RdGSsYZYqLEk1QkQk50QL1gUiL8KtPiDReJet4uQIDAQAB
     */
    @Override
    public void onCreate() {
        super.onCreate();
        appContext = VideoApplication.this;
        mInstance = VideoApplication.this;
        Config.DEBUG=true;
        PlatformConfig.setWeixin("wx2d62a0f011b43f32", "c43aeb050a3eab9f723c04cfc0525800");//设置微信SDK账号
        PlatformConfig.setSinaWeibo("994868311", "908f16503b8ebe004cdf9395cebe1b14","http://sns.whalecloud.com/sina2/callback");//设置微博分享/登录SDK//https://api.weibo.com/oauth2/default.html
        PlatformConfig.setQQZone("1106176094","Pkas3I3J2OpaZzsH");//设置QQ/空间SDK账号
        //初始化友盟分享
        UMShareAPI.get(VideoApplication.this);
        Utils.init(this);
        SharedPreferencesUtil.init(getApplicationContext(), getPackageName() + "xinquConfig", Context.MODE_MULTI_PROCESS);
        ACache cache = ACache.get(VideoApplication.this);
        ApplicationManager.getInstance().setCacheExample(cache);//初始化后需要设置给通用管理者
        UserData.DataBean.InfoBean userData = (UserData.DataBean.InfoBean)  ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_USER_DATA);
        setUserData(userData,false);
        mToday = Integer.parseInt(CommonDateParseUtil.getNowDay());
        mUuid = GoagalInfo.get().uuid;
        HttpConfig.setPublickey(Constant.URL_PRIVATE_KEY);
        //极光消息推送
        JPushInterface.setDebugMode(true);
        JPushInterface.init(VideoApplication.this);
//        BasicPushNotificationBuilder builder = new BasicPushNotificationBuilder(getApplicationContext());
//        builder.notificationFlags=0x666;//通知的标记
//        JPushInterface.setDefaultPushNotificationBuilder(builder);
        ContentCheckKey.getInstance().init();
        GoagalInfo.get().init(VideoApplication.this);
        ApplicationManager.getInstance().initSDPath();
        //金山云硬解白名单
        KSYHardwareDecodeWhiteList.getInstance().init(VideoApplication.this);
        MobclickAgent.setScenarioType(VideoApplication.this, MobclickAgent.EScenarioType.E_UM_NORMAL);//普通统计模式
        ConfigSet.getInstance().init();
        //第一次打开程序,初始化设置项
        if(1!=SharedPreferencesUtil.getInstance().getInt(Constant.IS_FIRST_START)){
            ConfigSet.getInstance().initSetting();
            SharedPreferencesUtil.getInstance().putInt(Constant.IS_FIRST_START,1);
        }

        if(1!=SharedPreferencesUtil.getInstance().getInt(Constant.IS_FIRST_START_DB)){
            if(ApplicationManager.getInstance().getVideoUploadDB().getUploadVideoList().size()>0){
                try {
                    ApplicationManager.getInstance().getVideoUploadDB().deteleAllUploadList();
                }catch (Exception e){
                }
            }
            SharedPreferencesUtil.getInstance().putInt(Constant.IS_FIRST_START_DB,1);
        }
        //如果从来未保存星期几，就保存当天的星期日期，用来标记一个礼拜扫描一个本地视频
        if(0==SharedPreferencesUtil.getInstance().getInt(Constant.SETTING_TODAY_WEEK_SUNDY)){
            int todayWeekSundy = DateParseUtil.getTodayWeekSundy();
            SharedPreferencesUtil.getInstance().putInt(Constant.SETTING_TODAY_WEEK_SUNDY,todayWeekSundy);//保存第一次安装时候的星期日期
        }
        Fresco.initialize(VideoApplication.this);//动态贴纸解析必须
        MobSDK.init(VideoApplication.this, "1ecf369922dc5", "aaf891da7ce90d40d52de6bedf5bf89c");
        //初始化全局异常拦截
        //CrashHanlder.getInstance().init(VideoApplication.this);
        //LeakCanary.install(VideoApplication.this);//内存泄漏检测
        //初始化表情包
        FaceConversionUtil.getInstace().getFileText(getApplicationContext());
        //在这里与金山云通信获得授权
        KSYAuthorPermissionsUtil.init();
    }



    private HttpProxyCacheServer proxy;
    public static HttpProxyCacheServer getProxy() {
        VideoApplication app = (VideoApplication) appContext.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    /**
     * 构造100M大小的缓存池
     * @return
     */
    private HttpProxyCacheServer newProxy() {
        //SD卡已挂载并且可读写
        int cacheSize = 100 * 1024 * 1024;
        //线使用内部缓存
        return new HttpProxyCacheServer.Builder(this)
                .cacheDirectory(new File(ApplicationManager.getInstance().getVideoCacheDir()))
                .maxCacheSize(cacheSize)//1BG缓存大小上限
                .build();
    }


    /**
     * 更新用户信息
     * @param userData
     */
    public synchronized void setUserData(UserData.DataBean.InfoBean userData,boolean isSerializable) {
        mUserData = userData;
        //未注册，去注册
        if(null!=userData&&!SharedPreferencesUtil.getInstance().getBoolean(Constant.JG_ISREGISTER_PLUSH,false)){
            JPushInterface.setAlias(VideoApplication.this, (int) System.currentTimeMillis(),"xinqu_id_"+userData.getId());//极光推送别名
            Set<String> tags=new HashSet<>();
            tags.add("test_user");
            JPushInterface.setTags(this, (int)System.currentTimeMillis(),tags);//极光推送标签
        //注销
        }else if(null==userData){
            //清空通知ID
            JPushInterface.deleteAlias(VideoApplication.this, (int) System.currentTimeMillis());
            JPushInterface.setAlias(VideoApplication.this, (int) System.currentTimeMillis(),"");
        }
        if(isSerializable){
            //序列化一個對象到緩存
            ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_USER_DATA);
            ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_USER_DATA,userData);
        }
        if(null==userData){
            mUserData=null;
        }
    }

    public UserData.DataBean.InfoBean getUserData() {
        return mUserData;
    }

    /**
     * 返回当前登录用户ID，如果登录返回USERID,未登录直接返回设备号
     * @return
     */
    public static String getLoginUserID() {
        try {
            if(null==VideoApplication.getInstance().getUserData()){
                return "0";
            }
            if(TextUtils.isEmpty(VideoApplication.getInstance().getUserData().getId())){
                return "0";
            }
            return VideoApplication.getInstance().getUserData().getId();
        }catch (Exception e){
            return "0";
        }
    }

    public boolean userIsBinDingPhone() {
        return null==VideoApplication.getInstance().getUserData()?false:!TextUtils.isEmpty(VideoApplication.getInstance().getUserData().getPhone());
    }
}
