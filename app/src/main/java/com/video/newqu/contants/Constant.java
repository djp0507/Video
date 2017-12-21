package com.video.newqu.contants;

import android.os.Environment;

import com.video.newqu.VideoApplication;
import com.video.newqu.util.FileUtils;

/**
 * TinyHung@outlook.com
 * 2017/3/21 16:48
 */
public interface Constant {

    //关闭Popup等待时间
    long CLOSE_POPUPWINDOW_WAIT_TIME = 300;

    /**
     * 公钥
     */
    String URL_PRIVATE_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA1LvntfKTpiXY3ZASDA8o\n" +
            "99DcSV7J5hVMJGWPW/pC6/5xGapWVnSAeD8DrOoDSQzlrm703Fut9srhucjqxtae\n" +
            "k07uCuI9UCfZkF7CpOMqFKx92qrVEQ4eK9Uhfr6tYthA1SJQPRALM6Lw2q9TPefx\n" +
            "/btoDZS+gkNd/ZmK79P4WxybkMO3dPApo6FJslqw6M63AiNnbuGsUf4M6nUI1nW6\n" +
            "ZwhWPTBbZ7LkbgeW8n6mnrOwGySV1TvuLoMRHncbq0o+p3zK1IONmPUaZnZH9WxY\n" +
            "8ZVRRc7+7R50Um36fg+aIj2jIaWTX0dhzUEplPLlZkCKEvEYLNPS6KKMXTNPuitj\n" +
            "+ZljnwB5wONiYCz1XBiDY7RqN6dhep/LZw2e1k9RxsrH0oQTxciOjI8pBYdRIMg3\n" +
            "B9EW3K4w/NxCr+Ii9EMDLeKa8HCkQ1eq0C8ux2UEydJFtYx4twRL+Qy6KUI5Jw8k\n" +
            "Dffce2iAbFgZnfySvkjAMZFUoR9/nMwA1p9DpxZHKF1tNL8LFlra1qtfR69mf5n3\n" +
            "7UJNTClEKaeS/lYmgASWcRQM1J+1Trw3W6dc2H4OgLL5C2rb26gCQtS9E9mx2L6p\n" +
            "XwAdzc/81NbuKjeIGwDgIzCa9UgMG8jbmDkejRupOAUEh/VTIRXRe6E428+hvvC3\n" +
            "NhIDl0PR8qsRhqZPkAeG0ScCAwEAAQ==";

//        String URL_PRIVATE_KEY = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAy/M1AxLZjZOyJToExpn1\n" +
//            "hudAWySRzS+aGwNVdX9QX6vK38O7WUA7h/bYqBu+6tTRC6RnL9BksMrIf5m6D3rt\n" +
//            "faYYmxfe/FI4ZD5ybIhFuRUi95e/J2vQVElsSNqSz7ewXquZpKZAqlzH4hGgOqmO\n" +
//            "THlrwiQwX66bS7x7kDmvxMd5ZRGhTvz62kpKb/orcnWQ1KElNc/bIzTtv3jsrMgH\n" +
//            "FVdFZfev91ew4Kf1YJbqGBGKslBsIoGsgTxI94T6d6XEFxSzdvrRwKhOobXIaOhZ\n" +
//            "o3GBCZIA/1ZOwLK6RyrWdprz+60xifcYIkILdZ7yPazSfHCVHFY6o/fQjK4dxQDW\n" +
//            "Gw0fxN9QX+v3+48nW7QIBx4KNYNIW/eetGhXpOwV4PjNt15fcwJkKsx2W3VQuh93\n" +
//            "jdYB4xMyDUnRwb9np/QR1rmbzSm5ySGkmD7NAj03V+O82Nx4uxsdg2H7EQdVcY7e\n" +
//            "6dEdpLYp2p+VkDd9t/5y1D8KtC35yDwraaxXveTMfLk8SeI/Yz4QaX6dolZEuUWa\n" +
//            "tLaye2uA0w25Ee35irmaNDLhDr804B7U7M4kkbwY7ijvvhnfb1NwFY5lw/2/dZqJ\n" +
//            "x2gH3lXVs6AM4MTDLs4BfCXiq2WO15H8/4Gg/2iEk8QhOWZvWe/vE8/ciB2ABMEM\n" +
//            "vvSb829OOi6npw9i9pJ8CwMCAwEAAQ==";

    /**
     * 文件存储目录
     */
    String BASE_CACHE_PATH=Environment.getExternalStorageDirectory().getAbsoluteFile()+"/XinQu/";
    String PATH_DATA = FileUtils.createRootPath(VideoApplication.getInstance()) + "newqu/cache/";//新趣内部缓存
    String IMAGE_PATH = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/XinQu/Photo/";
    String STICKER_IMAGE_PATH = IMAGE_PATH+"Sticker/";
    String DOWNLOAD_PATH = BASE_CACHE_PATH+"File/.Download/";
    String DOWNLOAD_WATERMARK_VIDEO_PATH = BASE_CACHE_PATH+"Video/";
    String PATH_TXT = PATH_DATA + "/book/";
    String PATH_EPUB = PATH_DATA + "/epub";
    String PATH_CHM = PATH_DATA + "/chm";

    String BASE_PATH = VideoApplication.getInstance().getCacheDir().getPath();


    /**
     * 关闭Books 我的 列表编辑菜单栏
     */
    String CLOSE_EDIT_MENU ="close_edit_menu" ;
    String SNACKBAR_ERROR = "snackbar_error";
    String SNACKBAR_DONE = "snackbar_done";
    String SUFFIX_TXT = ".txt";
    String SUFFIX_PDF = ".pdf";
    String SUFFIX_EPUB = ".epub";
    String SUFFIX_ZIP = ".zip";
    String SUFFIX_CHM = ".chm";


    /**
     * QQ,微信登录字段
     */
    String LOGIN_NICK_NAME = "nickname";
    String LOGIN_CITY = "city";
    String LOGIN_LOGO = "logo";
    String LOGIN_GENDER = "gender";
    String LOGIN_PROVINCE = "province";
    String LOGIN_OPENID = "open_id";
    String LOGIN_IMEIL = "imeil";
    String LOGIN_TYPE = "type";

    /**
     * 设置
     */
    String SETTING_MOBILE_UPLOAD = "setting_mobile_upload";
    String SETTING_MOBILE_PLAYER = "setting_mobile_player";
    String SETTING_WIFI_AUTH_PLAYER = "setting_wifi_auth_player";
    String SETTING_VIDEO_PLAYER_LOOP = "setting_video_player_loop";
    String SETTING_VIDEO_PLAYER_CACHE = "setting_video_player_cache";
    String SETTING_VIDEO_WATERMARK = "setting_video_watermark";
    String SETTING_VIDEO_SAVE_VIDEO = "setting_video_save_video";
    String SETTING_VIDEO_PLAYER_MOLDER = "setting_video_player_molder";
    String SETTING_PLAYER_VIDEO_ISSHOW_AUTOCOMMENT = "setting_player_video_isshow_autocomment";

    /**
     * 缓存
     */
    //关注列表
    String CACHE_FOOLOW_VIDEO_LIST = "cache_fool_video_list";
    //热门视频
    String CACHE_HOT_VIDEO_LIST = "cache_hot_video_list";
    //个人中心基本数据
    String CACHE_MINE_USER_DATA ="cache_mine_user_data";
    //登录信息
    String CACHE_USER_DATA = "cache_user_data";
    //我的粉丝列表
    String CACHE_MINE_FANS_LIST = "cache_fans_list";
    //我的关注列表
    String CACHE_MINE_FOLLOW_USER_LIST = "cache_follow_user_list";
    //我的作品
    String CACHE_MINE_WORKS = "cache_mine_works";
    //我的收藏视频
    String CACHE_MINE_FOLLOW_VIDEO_LIST = "cache_mine_follow_video_list";
    //发现
    String CACHE_FIND_VIDEO_LIST = "cache_find_video_list";
    //话题标签缓存
    String CACHE_TOPIC_LIST = "cache_topic_list";
    //新的消息
    String CACHE_NEW_SING_MESSAGE = "cache_new_sing_message";
    //国际区号
    String CACHE_COUNTRY_NUMBER_LIST = "cache_country_number_list";
    //
    String REGISTER_OPEN_APP ="register_open_app";

    String CACHE_DAY_WEICHAT_VIDEO_LIAT = "day_weichat_video_liat";

    String CACHE_LOCATION_VIDEO_LIST = "cache_location_video_list";

    String CACHE_TEMP_LOCATION_VIDEO_LIST = "cache_temp_location_video_list";

    String CACHE_MINT_MAKE_STICKER_LIST = "cache_mint_make_sticker_list";
    String CACHE_MEDIA_EDIT_COPTIONS = "cache_media_edit_coptions";
    String CACHE_MEDIA_EDIT_MUSIC = "cache_media_edit_music";
    String CACHE_USER_MESSAGE ="cache_user_message";

    String CACHE_MEDIA_MUSIC_CATEGORY_LIST = "cache_media_music_category_list";
    String CACHE_MEDIA_RECORED_LIKE_MUSIC = "cache_media_recored_like_music";
    /**
     * Event事件
     */

    //修改个人信息提交按钮
    String EVENT_SUBMIT = "submit";
    //调整界面类型.我的，作品
    String EVENT_UP_WORKS_UI = "up_works_ui";

    //刷新消息界面
    String EVENT_UPDATA_MESSAGE_UI = "event_updata_message_ui";

    //有新的消息
    String EVENT_NEW_MESSAGE = "event_new_message";

    //刷新个人中心
    String EVENT_MAIN_UPDATA_MINE_WORKS_FOLLOW = "event_main_updata_mine_works_follow";
    //刷新关注列表和热门列表
    String EVENT_HOME_FOLLOW_HOT_LIST ="event_home_follow_hot_list" ;
    String EVENT_VIDEO_PLAY_PAGE_UPDATA ="event_video_play_page_updata" ;
    String EVENT_TOPIC_VIDEO_PLAY_PAGE_UPDATA ="event_topic_video_play_page_updata" ;
    String EVENT_TOPIC_VIDEO_PLAY_PAGE_DETELE ="event_topic_video_play_page_detele" ;
    String EVENT_HISTORY_VIDEO_PLAY_PAGE_UPDATA ="event_history_video_play_page_updata" ;

    /**
     * 数据匹配
     */
    //收藏视频
    CharSequence PRICE_SUCCESS = "collect_success";
    //取消收藏成功
    CharSequence PRICE_UNSUCCESS = "uncollect_success";
    //关注成功
    CharSequence FOLLOW_SUCCESS = "关注成功";
    //取消关注成功
    CharSequence FOLLOW_UNSUCCESS = "取消关注成功";

    //播放次数统计
    CharSequence PLAY_COUNT_SUCCESS = "增加播放记录成功";
    String REPORT_USER_RESULT ="举报成功";
    String UPLOAD_USER_PHOTO_SUCCESS = "上传图像成功";
    String DELETE_VIDEO_CONTENT="删除视频成功";

    /**
     * SP配置
     */

    //当前首页显示在第几页
    String CUREEN_FRAGMENT = "cureen_fragment";

    //我的界面，我的作品视频列表类型
    String MINE_WORKS_ITEM_TYPE = "works_list_item";

    String SETTING_TODAY_WEEK_SUNDY = "setting_today_week_sundy";


    //设置
    String SETTING_SPLASH ="setting_splash" ;
    String SETTING_SW_WIFI_AUTO_PLAY = "setting_sw_wifi_auto_play";
    String SETTING_SW_WIFI_UPLOAD = "setting_sw_wifi_upload";
    String SETTING_SW_AUTO_ADD_WATERMARK = "setting_sw_auto_add_watermark";
    String SETTING_SW_SAVE_VIDEO_LOCATION = "setting_sw_save_video_location";
    String JPUSH_ALIAS_SETTING_FINLISH = "jpush_alias_setting_finlish";//记录当前登录用户是否完成别名和标签设置
    //激光推送通知
    String JIPULSH_NOTIFACTION_ID = "jipulsh_notifaction_id";
    //当天日期
    String SETTING_DAY = "setting_day";
    String SETTING_VERSTION_CODE ="setting_verstion_code" ;
    String SETTING_FIRST_START = "setting_first_start";
    String SETTING_FIRST_MAKE = "SETTING_FIRST_MAKE";
    String SETTING_VIDEOS_PLAYER_FIRST_CLICK = "setting_videos_player_first_click";
    String IS_DELETE_PHOTO_DIR = "is_delete_photo_dir";

    //用户中心界面展示风格
    String AUTHOR_TAB_STYLE = "author_tab_style";
    //上传视频
    String IS_UPLOAD_VIDEO = "has_upload_video";
    //官方网站
    String WEB_URL = "http://zhsq.cr173.com";
    //缓存有效期 单位:秒 默认12小时
    int CACHE_TIME =42300 ;
    //加载对话框延时关闭时间
    int PROGRESS_CLOSE_DELYAED_TIE = 1800;
    //是否第一次进来
    String IS_FIRST_COMON = "is_first_comon";
    //是否已注册极光推送别名
    String JG_ISREGISTER_PLUSH = "jg_isregister_plush";
    /**
     * Intent
     */
    //登录意图
    int INTENT_LOGIN_EQUESTCODE = 111;
    //登录成功意图
    int INTENT_LOGIN_RESULTCODE = 222;
    //话题列表登录意图
    int INTENT_LOGIN_TOPIC = 333;
    //上传视频意图
    int INTENT_UPLOAD_VIDEO=444;

    String INTENT_LOGIN_STATE = "login_state";

    /**
     * 自定义广播意图
     */
    String ACTION_MESSAGE_UPDATA_UI = "com.video.newqu.updata";//需要刷新界面
    String ACTION_DOWNLO0AD_APKFINLISH = "com.video.newqu.download";//软件默默下载完成
    int NOTIFACATION_ID_MESSAGE=8;//消息通知的ID

    int UPLOAD_HANDEL_NOFINLISH = 111;
    int UPLOAD_HANDEL_PROGRESS = 112;
    int UPLOAD_HANDEL_FINLISH = 113;
    int UPLOAD_HANDEL_FAI = 114;
    int UPLOAD_HANDEL_START = 115;
    int UPLOAD_HANDEL_TASKRUNING = 116;
//    视频的缩放宽高比例
    int VIDEO_RATIO_MOON=1;//正方形
    int VIDEO_RATIO_LONF_WIDTH=6;//长-宽
    int VIDEO_RATIO_LONF_HEIGHT=7;//长-高
    int VIDEO_RATIO_WIDE_WIDTH=16;//宽-宽
    int VIDEO_RATIO_WIDE_HEIGHT=9;//宽-高

//    拍摄图片
    int REQUEST_CLIP_IMAGE = 2028;
    int REQUEST_TAKE_PHOTO = 2029;
    //阿里云上传
    String STS_ENDPOINT ="http://oss-cn-shenzhen.aliyuncs.com" ;//终端
    String STS_CALLBACKADDRESS ="http://app.nq6.com/api/OssCallback/index";//回调地址
    String STS_BUCKET = "nq6-video";//分区
    String STS_SERVER = "http://app.nq6.com/api/index/oss_tmp_key";//获取Token的服务端IP地址
    String STS_HOST = "app.nq6.com";//host
    String STS_CALLBACL_CONTENT_TYPE = "application/json";//回调类型

    //关键功能提示
    String TIPS_MAIN_CODE = "tips_main_code";
    String TIPS_SCANVIDEO_CODE = "tips_scanvideo_code";
    String TIPS_RECORD_CODE = "tips_record_code";
    String TIPS_MEDIA_EDIT_CODE = "tips_media_edit_code";
    String TIPS_HOT_CODE = "tips_hot_code";
    String TIPS_VIDEO_LIST_CODE = "tips_video_list_code";
    String TIPS_MEDIA_CAPTIONS_CODE ="tips_media_captions_code" ;
    String TIPS_MAIN_LOGIN_CODE ="tips_main_login_code";
    String TIPS_MINE_REFRESH_CODE = "tips_mine_refresh_code";
    String TIPS_VIDEO_SEND_CODE = "tips_video_send_code";
    String TIPS_VIDEO_DETAOLS_INPUT_CODE ="tips_video_detaols_input_code" ;
    String TIPS_VIDEO_DETAOLS_SAMLWINDOWN_CODE = "tips_video_detaols_samlwindown_code";
    String TIPS_USER_HISTORY_CODE = "tips_user_history_code";
    String TIPS_GEADE_CODE = "tips_geade_code";

    //首页我的关注、热门、我的作品、喜欢、用户中心作品 跳转至视频播放滑动列表界面色的标记
    String KEY_FRAGMENT_TYPE = "key_fragment_type";
    String KEY_POISTION="key_poistion";
    String KEY_PAGE="key_page";
    String KEY_JSON="key_json";
    String KEY_TOPIC="key_topic";
    String KEY_AUTHOE_ID="key_authoe_id";
    String KEY_TOPIC_ID="key_topic_id";
    String KEY_REFRESH = "key_refresh";
    String KEY_PAGE_SIZE = "key_page_size";


    int FRAGMENT_TYPE_FOLLOW = 0x1;//关注列表
    int FRAGMENT_TYPE_HOT=0x2;//热门
    int FRAGMENT_TYPE_WORKS = 0x3;//我发布的
    int FRAGMENT_TYPE_LIKE = 0x4;//收藏
    int FRAGMENT_TYPE_AUTHOE_CORE = 0x5;//用户中心
    int FRAGMENT_TYPE_TOPIC_LIST = 0x6;//话题分类列表
    int FRAGMENT_TYPE_SEARCH = 0x7;//搜索界面
    int FRAGMENT_TYPE_HOME_TOPIC = 0x8;//首页的话题界面
    int FRAGMENT_TYPE_HOSTORY = 0x9;//历史记录
    int FRAGMENT_TYPE_VERTICAL_AUTHOR = 0x10;//历史记录


    String TIPS_MINE_LOGIN_CODE = "tips_mine_login_code";
    //设置中心
    int KEY_FRAGMENT_TYPE_SETTINGS = 0x1;
    //粉丝列表
    int KEY_FRAGMENT_TYPE_FANS_LIST = 0x2;
    //关注的用户列表
    int KEY_FRAGMENT_TYPE_FOLLOW_USER_LIST = 0x3;
    //用户信息修改
    int KEY_FRAGMENT_TYPE_USER_DATA_EDIT = 0x4;
    //话题分类视频列表
    int KEY_FRAGMENT_TYPE_TOPIC_VIDEO_LISTT = 0x5;
    //话题列表
    int KEY_FRAGMENT_TYPE_TOPIC_LISTT= 0x6;
    //用户中心
    int KEY_FRAGMENT_TYPE_AUTHOR_DETAILS = 0x7;
    //播放视频历史记录
    int KEY_FRAGMENT_TYPE_PLSYER_HISTORY = 0x8;
    //音乐分类下列表
    int KEY_FRAGMENT_TYPE_MUSIC_CATEGORY_LIST = 0x9;
    //反馈
    int KEY_FRAGMENT_SERVICES = 0x10;
    //通知消息
    int KEY_FRAGMENT_NOTIFACTION = 0x11;

    //约定参数
    String KEY_AUTHOR_TYPE = "key_author_type";
    String KEY_AUTHOR_ID = "key_author_id";
    String KEY_VIDEO_TOPIC_ID = "key_video_topic_id";
    String KEY_TITLE = "key_title";

    String IS_FIRST_START = "is_first_start";
    //友盟事件统计
    String UM_EVENT_PLAYER_VIDEO_COUNT = "play_video_count";
    String UM_EVENT_TOPIC_PREVIEW = "preview_topic_list_count";
    String UM_EVENT_TOPIC_TYPE = "click_topic_video";
    String UM_EVENT_HOT_CLICK = "hotvideo_click";
    String UM_EVENT_RECORD_COUNT = "record_video_click“";
    String UM_EVENT_LOCATION_VIDEO_COUNT = "location_video_edit_Compile";
    String UM_EVENT_TOPIC_MEINV = "um_event_topic_meinv";

    //选择音乐
    int MEDIA_START_MUSIC_REQUEST_CODE = 88;
    int MEDIA_START_MUSIC_RESULT_CODE = 89;
    //打开音乐的分类界面
    int MEDIA_START_MUSIC_CATEGORY_REQUEST_CODE = 90;
    int MEDIA_START_MUSIC_CATEGORY_RESULT_CODE = 91;
    //选择封面
    int MEDIA_START_COVER_REQUEST_CODE = 92;
    int MEDIA_START_COVER_RESULT_CODE = 93;
    //选择话题
    int MEDIA_START_TOPIC_REQUEST_CODE = 94;
    int MEDIA_START_TOPIC_RESULT_CODE = 95;
    //验证手机号码
    int MEDIA_BINDING_PHONE_REQUEST = 96;
    int MEDIA_BINDING_PHONE_RESULT = 97;



    String CACHE_MEDIA_RECORED_RECOMMEND_MUSIC = "cache_media_recored_recommend_music";
    String EVENT_UPDATA_MUSIC_PLAYER = "event_updata_music_player";
    //音乐板块
    String KEY_MEDIA_KEY_MUSIC_ID = "music_id";
    String KEY_MEDIA_KEY_MUSIC_PATH = "music_path";
    String KEY_MEDIA_KEY_FPS = "key_media_key_fps";
    String MEDIA_KEY_MUSIC_CATEGORY_ID = "media_key_music_category_id";
    String MEDIA_KEY_MUSIC_TOP = "media_key_music_top";
    //视频拍摄和编辑参数约定
    String KEY_MEDIA_RECORD_PRAMER_VIDEO_PATH = "key_media_record_video_path";
    String KEY_MEDIA_RECORD_PRAMER_SOURCETYPE = "key_media_record_sourcetype";
    String STICKER_STICKERID_LIST = "sticker_stickerid_list";
    String CACHE_MEDIA_EDIT_STICKER_LIST = "cache_media_edit_sticker_list";

    //视频合成Event
    int VIDEO_COMPOSE_STARTED = 10;
    int VIDEO_COMPOSE_PROGRESS = 11;
    int VIDEO_COMPOSE_FINLISHED = 12;
    int VIDEO_UPLOAD_STARTED = 13;
    String KEY_INTENT_MEDIA_THUBM = "thbum_seek_time";
    String INTENT_BINDING_TIPS ="intent_binding_tips" ;
    //文件找不到
    int UPLOAD_ERROR_CODE_FILE_NOTFIND = 1;
    int UPLOAD_ERROR_CODE_CLIENTEXCEPTION = 2;
    int UPLOAD_ERROR_CODE_SERVICEEXCEPTION = 3;
    int UPLOAD_ERROR_CODE_OTHER = 4;
    String IS_FIRST_START_DB = "is_first_start_db";
    int MEDIA_VIDEO_EDIT_MAX_DURTION = 300 * 1000;//视频处理的最大时长,单位毫秒
    int MEDIA_VIDEO_EDIT_MIN_DURTION = 5 * 1000;//视频处理的最大时长,单位毫秒
    int MODE_USER_COMPLETE = 1;
    int MODE_USER_EDIT = 2;
    //评分
    String GRADE_IS_SHOW = "grade_is_show";
    String GRADE_LOGIN_COUNT ="grade_login_count" ;
    String GRADE_PLAYER_VIDEO_COUNT ="grade_player_video_count" ;
    String GRADE_TODAY_TIPS ="grade_today_tips" ;
    String KEY_MAIN_INSTANCE = "KEY_MAIN_INSTANCE";
    int VERTICAL_VIDEO_PLAY_REQUESTCODE = 101;
    String EVENT_UPDATA_MINE_WORKS_MESSAGE = "enevt_updata_my_video";
}
