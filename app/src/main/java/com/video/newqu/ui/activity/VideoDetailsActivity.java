package com.video.newqu.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.VideoComentListAdapter;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.bean.ComentList;
import com.video.newqu.bean.PlayCountInfo;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.bean.SingComentInfo;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.bean.VideoInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Cheeses;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityVideoDetailsBinding;
import com.video.newqu.databinding.VideoComentListItemEmptyBinding;
import com.video.newqu.databinding.VideoDetailsHeaderLayoutBinding;
import com.video.newqu.listener.OnPostPlayStateListener;
import com.video.newqu.listener.PerfectClickListener;
import com.video.newqu.listener.TopicClickListener;
import com.video.newqu.listener.VideoComendClickListener;
import com.video.newqu.ui.contract.VideoDetailsContract;
import com.video.newqu.ui.dialog.InputKeyBoardDialog;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.VideoDetailsPresenter;
import com.video.newqu.util.AnimationUtil;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ContentCheckKey;
import com.video.newqu.util.Logger;
import com.video.newqu.util.PostPlayStateHanderUtils;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.TextViewTopicSpan;
import com.video.newqu.util.TimeUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.util.VideoDownloadComposrTask;
import com.video.newqu.view.layout.VideoGroupRelativeLayout;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import com.video.newqu.view.widget.GlideCircleTransform;
import com.xinqu.videoplayer.XinQuVideoPlayer;
import com.xinqu.videoplayer.XinQuVideoPlayerStandard;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/5/25 9:26
 * 视频详情，留言评论列表
 */

public class VideoDetailsActivity extends BaseActivity<ActivityVideoDetailsBinding> implements VideoDetailsContract.View,
        BaseQuickAdapter.RequestLoadMoreListener, TopicClickListener , VideoComendClickListener,OnPostPlayStateListener {

    private static final String TAG = VideoDetailsActivity.class.getSimpleName();
    private String mVideoId;
    private VideoDetailsPresenter mVideoDetailsPresenter;
    private int  mPage=0;//当前显示留言页数
    private int  mPageSize=20;//每页留言条数
    private List<ComentList.DataBean.CommentListBean> mCommentListBeen=new ArrayList<>();
    private VideoComentListAdapter mVideoComentListAdapter;
    private String mVideoAuthorID;//视频作者ID
    private VideoInfo.DataBean.InfoBean mVideoInfo;//视频信息
    private String toUserID="0";
    private ComentList.DataBean.CommentListBean mCommentListBeanInfo;
    private VideoComentListItemEmptyBinding mComentEmptybindView;
    private int mVideoViewHeight=0;
    private LinearLayoutManager mLinearLayoutManager;
    private ScaleAnimation mFollowScaleAnimation;
    private boolean isPostPlayState=false;//是否已经上传播放次数
    private VideoDetailsHeaderLayoutBinding headerLsyoutBinding;
    private boolean mIsHistory;

    /**
     * 入口
     * @param context
     * @param videoID 视频ID
     */
    public static void start(Context context, String videoID, String videoAuthorID,boolean isHistory) {
        Intent intent=new Intent(context,VideoDetailsActivity.class);
        intent.putExtra("video_id",videoID);
        intent.putExtra("video_author_id",videoAuthorID);
        intent.putExtra("isHistory",isHistory);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
        initIntent();
        mVideoDetailsPresenter = new VideoDetailsPresenter(this);
        mVideoDetailsPresenter.attachView(this);

        mVideoInfo= (VideoInfo.DataBean.InfoBean) ApplicationManager.getInstance().getCacheExample().getAsObject(mVideoId);
        initAdapter();
        initVideoData();
        mCommentListBeen= (List<ComentList.DataBean.CommentListBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(mVideoId+"_comlist");
        if(null==mVideoInfo){
            showLoadingViews("精彩视频马上呈现");
        }
        if(null!=mCommentListBeen&&mCommentListBeen.size()>0){
            upDataNewDataAdapter();
        }
        getVideoInfo();
    }


    @Override
    public void initViews() {
        View.OnClickListener onClickListenet=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    //返回
                    case R.id.btn_back:
                        onBackPressed();
                        break;
                    //菜单
                    case R.id.btn_menu:
                        showMenu();
                        break;
                    //分享
                    case R.id.btn_share:
                        shareVideo();
                        break;

                    //表情 打开输入框，并打表情面板
                    case R.id.btn_iv_face_icon:
                        showInputKeyBoardDialog(false,true,"输入评论内容");
                        break;
                    //打开输入框，并打开键盘
                    case R.id.tv_input_content:
                        showInputKeyBoardDialog(true,false,"输入评论内容");
                        break;
                    //发送消息
                    case R.id.btn_tv_send:
                        sendWordsMessage();
                        break;
                    //关注
                    case R.id.btn_ll_follow:
                        onFollowUser();
                        break;
                    //点击了用户头像
                    case R.id.iv_video_author_icon:
                        if(null!=mVideoInfo){
                            AuthorDetailsActivity.start(VideoDetailsActivity.this,mVideoInfo.getUser_id());
                        }
                        break;
                    //下载视频
                    case R.id.btn_download:
                        downloadVideo();
                        break;
                }
            }
        };

        bindingView.btnBack.setOnClickListener(onClickListenet);
        bindingView.btnMenu.setOnClickListener(onClickListenet);
        bindingView.btnShare.setOnClickListener(onClickListenet);
        bindingView.btnDownload.setOnClickListener(onClickListenet);
        bindingView.btnPrice.setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                priceVideo(true);
            }
        });

        bindingView.btnIvFaceIcon.setOnClickListener(onClickListenet);
        bindingView.btnTvSend.setOnClickListener(onClickListenet);
        bindingView.btnLlFollow.setOnClickListener(onClickListenet);
        bindingView.ivVideoAuthorIcon.setOnClickListener(onClickListenet);
        bindingView.tvInputContent.setOnClickListener(onClickListenet);

        mFollowScaleAnimation = AnimationUtil.followAnimation();
        //刷新

        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadComentList();
            }
        });

        //监听文字变化
        bindingView.tvInputContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!TextUtils.isEmpty(charSequence)&&charSequence.length()>0){
                    if(null!=bindingView)  bindingView.btnTvSend.setTextColor(CommonUtils.getColor(R.color.text_orgin_selector));
                }else{
                    if(null!=bindingView)  {
                        bindingView.btnTvSend.setTextColor(CommonUtils.getColor(R.color.colorTabText));
                        bindingView.tvInputContent.setHint("说点什么...");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        bindingView.llTopBarBg.measure(width,height);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) bindingView.topBarBg.getLayoutParams();
        layoutParams.width=RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height=  bindingView.llTopBarBg.getMeasuredHeight();
        bindingView.topBarBg.setLayoutParams(layoutParams);
        //初始化固定的顶部标题栏背景View
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_share_bg_cover);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        bitmapDrawable.mutate().setAlpha(0);
        bindingView.topBarBg.setImageDrawable(bitmapDrawable);

    }

    @Override
    public void initData() {

    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingViews("精彩视频马上呈现");
        getVideoInfo();
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        mLinearLayoutManager = new LinearLayoutManager(VideoDetailsActivity.this);
        bindingView.recyerView.setLayoutManager(mLinearLayoutManager);
        bindingView.recyerView.setHasFixedSize(false);
        mVideoComentListAdapter = new VideoComentListAdapter(mCommentListBeen,this,this);
        mVideoComentListAdapter.showEmptyView(true);
        mVideoComentListAdapter.setOnLoadMoreListener(this);
        bindingView.recyerView.setAdapter(mVideoComentListAdapter);
        //播放器和作者信息的头部
        headerLsyoutBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.video_details_header_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mVideoComentListAdapter.addHeaderView(headerLsyoutBinding.getRoot());
        headerLsyoutBinding.videoItemListUserName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(null!=mVideoInfo&&!TextUtils.isEmpty(headerLsyoutBinding.videoItemListUserName.getText().toString())){
                    AuthorDetailsActivity.start(VideoDetailsActivity.this,mVideoAuthorID);
                }
            }
        });
        initFooterEmptyView();
        bindingView.recyerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                //隐藏输入框
                if(dy<0){
                    hideMenuTabView();
                //显示输入框
                }else if(dy>0){
                    showMenuTabView();
                }
                if(0==mLinearLayoutManager.findFirstVisibleItemPosition()){
                    switchChangeVideoPlayerLocation(getScrolledDistance());
                }
            }
        });
    }

    private boolean inputIsShow=true;
    /**
     * 隐藏菜单
     */
    public void hideMenuTabView() {
        if(!inputIsShow){
            return;
        }
        inputIsShow=false;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) bindingView.llBottomInput.getLayoutParams();
        int fabBottomMargin = lp.bottomMargin;
        bindingView.llBottomInput.animate().translationY(bindingView.llBottomInput.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
    }

    /**
     * 显示菜单
     */
    public void showMenuTabView() {
        if(inputIsShow){
            return;
        }
        inputIsShow=true;
        bindingView.llBottomInput.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
    }


    /**
     * 显示和隐藏小播放器
     * @param scollYDistance
     */
    private boolean max=false;
    private boolean min=false;

    private void switchChangeVideoPlayerLocation(int scollYDistance) {
        if(null==mVideoInfo){
            return;
        }
        if(0!=mVideoViewHeight){
            //向上滑动的距离>=视频头部
            if(scollYDistance>=mVideoViewHeight&&!min){
                min=true;
                max=false;
                headerLsyoutBinding.videoPlayer.startWindowTiny(TextUtils.isEmpty(mVideoInfo.getType())?1:Integer.parseInt(mVideoInfo.getType()));
            }else if(scollYDistance<mVideoViewHeight&&!max) {
                min=false;
                max=true;
                headerLsyoutBinding.videoPlayer.backPress();
            }
        }
        Drawable drawable = bindingView.topBarBg.getDrawable();
        if(null==drawable) return;
        if (scollYDistance <= 0) {
            drawable.mutate().setAlpha(0);
        } else if (scollYDistance > 0 && scollYDistance <= mVideoViewHeight) {
            float scale = (float) scollYDistance / mVideoViewHeight;
            float alpha = (255 * scale);
            drawable.mutate().setAlpha((int) alpha);
            if(null!=bindingView&&bindingView.tvPlayMessage.getVisibility()==View.VISIBLE){
                bindingView.tvPlayMessage.setVisibility(View.GONE);
            }
        } else {
            drawable.mutate().setAlpha(255);
        }
        bindingView.topBarBg.setImageDrawable(drawable);
    }



    private int getScrolledDistance() {
        int position = mLinearLayoutManager.findFirstVisibleItemPosition();
        View firstVisiableChildView = mLinearLayoutManager.findViewByPosition(position);
        int itemHeight = firstVisiableChildView.getHeight();
        return (position) * itemHeight - firstVisiableChildView.getTop();
    }


    /**
     * 下载视频
     */
    private void downloadVideo() {
        Logger.d(TAG,"downloadVideo");
        if(TextUtils.isEmpty(mVideoAuthorID)) return;
        if(null==mVideoInfo||TextUtils.isEmpty(mVideoInfo.getPath()))  return;
        //检查SD读写权限
        RxPermissions.getInstance(VideoDetailsActivity.this).request(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(null!=aBoolean&&aBoolean){
                    //用户已登录
                    if(null!=VideoApplication.getInstance().getUserData()){
                        //发布此时品的主人正式观看的用户自己
                        if(TextUtils.equals(mVideoAuthorID,VideoApplication.getLoginUserID())){
                            Logger.d(TAG,"是发布视频的作者自己");
                            new VideoDownloadComposrTask(VideoDetailsActivity.this,mVideoInfo.getPath()).start();
                        }else{
                            //用户允许下载
                            if(null!=mVideoInfo.getDownload_permiss()&&TextUtils.equals("0",mVideoInfo.getDownload_permiss())){
                                Logger.d(TAG,"作者允许别人下载");
                                new VideoDownloadComposrTask(VideoDetailsActivity.this,mVideoInfo.getPath()).start();
                                //用户不允许下载
                            }else{
                                ToastUtils.shoCenterToast("发布此视频的用户未开放他人下载此视频权限！");
                            }
                        }
                        //用户未登录
                    }else{
                        if(!VideoDetailsActivity.this.isFinishing()){
                            login();
                        }
                    }
                }else{
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(VideoDetailsActivity.this)
                            .setTitle("SD读取权限申请失败")
                            .setMessage("存储权限被拒绝，请务必授予我们存储权限！是否现在去设置？");
                    builder.setNegativeButton("去设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SystemUtils.getInstance().startAppDetailsInfoActivity(VideoDetailsActivity.this,141);
                        }
                    });
                    builder.show();
                }
            }
        });
    }


    /**
     * 添加脚步,代替setEmptyView（）；的方案
     */
    private void initFooterEmptyView() {

        List<ComentList.DataBean.CommentListBean> data = mVideoComentListAdapter.getData();
        //移除
        if(null!=data&&data.size()>0){
            if(null!=mVideoComentListAdapter&&mVideoComentListAdapter.getFooterViewsCount()>0){
                mVideoComentListAdapter.removeFooterView(mComentEmptybindView.getRoot());
            }
            //添加
        }else{
            if(null!=mVideoComentListAdapter&&mVideoComentListAdapter.getFooterViewsCount()<=0){
                mComentEmptybindView = DataBindingUtil.inflate(getLayoutInflater(), R.layout.video_coment_list_item_empty, (ViewGroup) bindingView.recyerView.getParent(), false);
                mComentEmptybindView.tvEmptyView.setText("没有留言，说两句吧~");
                mComentEmptybindView.ivEmptyView.setImageResource(R.drawable.iv_com_message_empty);
                mVideoComentListAdapter.addFooterView(mComentEmptybindView.getRoot());
            }
        }
    }

    /**
     * 为适配器刷新新数据
     */
    private void upDataNewDataAdapter() {
        mVideoComentListAdapter.setNewData(mCommentListBeen);
        initFooterEmptyView();
    }

    /**
     * 为适配器增加新数据，添加至第0个位置
     */
    private void updataAddDataToTopAdapter() {
        mVideoComentListAdapter.addData(0,mCommentListBeanInfo);
        initFooterEmptyView();
        //替换最新的缓存
        ApplicationManager.getInstance().getCacheExample().remove(mVideoId+"_comlist");
        ApplicationManager.getInstance().getCacheExample().put(mVideoId+"_comlist", (Serializable) mCommentListBeen);
        //每次发表评论成功，滚动至顶部
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                bindingView.recyerView.smoothScrollToPosition(1);
            }
        });
    }

    /**
     * 为适配器添加新数据，位于最下面
     */
    private void updataAddDataAdapter() {
        mVideoComentListAdapter.addData(mCommentListBeen);
        initFooterEmptyView();
    }


    /**
     * 获取传递数据
     */
    private void initIntent() {
        Intent intent = getIntent();
        mVideoId = intent.getStringExtra("video_id");
        mVideoAuthorID = intent.getStringExtra("video_author_id");
        mIsHistory = intent.getBooleanExtra("isHistory",false);
        if(TextUtils.isEmpty(mVideoId)){
            ToastUtils.shoCenterToast("错误");
            finish();
            return;
        }
    }

    /**
     * 获取视频详细信息
     */
    private void getVideoInfo() {
        if(null!=mVideoDetailsPresenter){
            mVideoDetailsPresenter.getVideoInfo(VideoApplication.getLoginUserID(),mVideoAuthorID,mVideoId);
        }
    }

    /**
     * 初始化视频详情信息
     */
    private void initVideoData() {
        initLayoutParams();
        initHeaderViewData();
        createPlayVideo();
    }



    /**
     * 打开输入法键盘
     * @param showKeyboard 是否显示输入法
     * @param showFaceBoard 是否显示表情面板
     */
    private void showInputKeyBoardDialog(boolean showKeyboard,boolean showFaceBoard,String hintText) {
        InputKeyBoardDialog inputKeyBoardDialog = new InputKeyBoardDialog(VideoDetailsActivity.this);
        inputKeyBoardDialog.setInputText(bindingView.tvInputContent.getText().toString());
        inputKeyBoardDialog.setParams(showKeyboard,showFaceBoard);
        inputKeyBoardDialog.setHintText(hintText);
        inputKeyBoardDialog.setBackgroundWindown(0.1f);
        inputKeyBoardDialog.setIndexOutErrorText("评论内容超过字数限制");
        inputKeyBoardDialog.setOnKeyBoardChangeListener(new InputKeyBoardDialog.OnKeyBoardChangeListener() {
            //文字发生了变化
            @Override
            public void onChangeText(String inputText) {
                if(!TextUtils.isEmpty(inputText)){
                    SpannableString topicStyleContent = TextViewTopicSpan.getTopicStyleContent(inputText, CommonUtils.getColor(R.color.app_text_style), bindingView.tvInputContent,null,null);
                    bindingView.tvInputContent.setText(topicStyleContent);
                }else{
                    toUserID="0";
                    bindingView.tvInputContent.setText(inputText);
                }
            }

            //提交
            @Override
            public void onSubmit() {
                sendWordsMessage();
            }
        });
         inputKeyBoardDialog.show();
    }

    /**
     * 根据视频的宽高缩放播放器的宽高
     */
    private void initLayoutParams() {

        if(null==mVideoInfo){
            return;
        }
        //设置视频的窗口大小，非常重要
        int videoType=0;
        if(TextUtils.isEmpty(mVideoInfo.getType())){
            if(!TextUtils.isEmpty(mVideoInfo.getVideo_width())&&!TextUtils.isEmpty(mVideoInfo.getVideo_height())){
                int videoWidth = Integer.parseInt(mVideoInfo.getVideo_width());
                int videoHeight = Integer.parseInt(mVideoInfo.getVideo_height());
                if(videoWidth==videoHeight){
                    videoType=3;
                }else if(videoWidth>videoHeight){
                    videoType=1;
                }else if(videoWidth<videoHeight){
                    videoType=2;
                }
            }
        }else{
            videoType=Integer.parseInt(mVideoInfo.getType());
        }
        setVideoRatio(videoType,TextUtils.isEmpty(mVideoInfo.getVideo_width())?0:Integer.parseInt(mVideoInfo.getVideo_width()),TextUtils.isEmpty(mVideoInfo.getVideo_height())?0:Integer.parseInt(mVideoInfo.getVideo_height()),headerLsyoutBinding.videoPlayer,headerLsyoutBinding.reItemVideo);
        //布局的全局宽高变化监听器
        ViewTreeObserver viewTreeObserver = headerLsyoutBinding.reItemVideo.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout() {
                mVideoViewHeight=headerLsyoutBinding.reItemVideo.getHeight();
                headerLsyoutBinding.reVideoGroup.getLayoutParams().height=mVideoViewHeight;
                headerLsyoutBinding.reItemVideo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }



    /**
     * 设置视频宽高
     * @param videoType 视频 宽高类型
     * @param videoWidth 视频分辨率-宽
     * @param videoHeight 视频分辨率-高
     * @param video_player 播放器
     */
    public static void setVideoRatio(int videoType, int videoWidth, int videoHeight, XinQuVideoPlayerStandard video_player, View view) {

        //只有Type,没有宽高
        if(0!=videoType&&0==videoWidth){
            switch (videoType) {
                //默认，正方形
                case 0:
                case 3:
                    video_player.widthRatio=Constant.VIDEO_RATIO_MOON;
                    video_player.heightRatio=Constant.VIDEO_RATIO_MOON;
                    break;
                //宽
                case 1:
                    video_player.widthRatio=16;
                    video_player.heightRatio=9;
                    break;
                //长
                case 2:
                    video_player.widthRatio=3;
                    video_player.heightRatio=4;
                    break;
                default:
                    video_player.widthRatio=9;
                    video_player.heightRatio=16;
            }
        //有Type并且有宽高
        }else if(0!=videoType&&videoWidth>0&&videoHeight>0){
            switch (videoType) {
                //默认，正方形
                case 0:
                case 3:
                    video_player.widthRatio=Constant.VIDEO_RATIO_MOON;
                    video_player.heightRatio=Constant.VIDEO_RATIO_MOON;
                    break;
                //宽
                case 1:
                    double videoHorizontalRatio = new BigDecimal((float)videoWidth/videoHeight).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    //5:4
                    if(1.25==videoHorizontalRatio){
                        video_player.widthRatio=5;
                        video_player.heightRatio=4;
                        //16:9
                    }else if(videoHorizontalRatio>=1.78){
                        video_player.widthRatio=16;
                        video_player.heightRatio=9;
                        //4:3
                    }else if(videoHorizontalRatio>=1.33){
                        video_player.widthRatio=4;
                        video_player.heightRatio=3;
                        //5:4
                    }else{
                        video_player.widthRatio=16;
                        video_player.heightRatio=9;
                    }
                    Logger.d(TAG,"有Type并且有宽高 视频类型   宽---缩放比例："+videoHorizontalRatio);
                    break;
                //长
                case 2:
                    double videoVerticaRatio = new BigDecimal((float)videoHeight/videoWidth).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                    //5:4
                    if(1.25==videoVerticaRatio){
                        video_player.widthRatio=4;
                        video_player.heightRatio=5;
                    //16:9
                    }else if(videoVerticaRatio>=1.60){
                        video_player.widthRatio=9;
                        video_player.heightRatio=16;
                    //4:3
                    }else if(videoVerticaRatio>=1.33){
                        video_player.widthRatio=3;
                        video_player.heightRatio=4;
                    //5:4
                    }else{
                        video_player.widthRatio=3;
                        video_player.heightRatio=4;
                    }
                    Logger.d(TAG,"有Type并且有宽高 视频类型   长---缩放比例："+videoVerticaRatio);
                    break;
                default:
                    video_player.widthRatio=Constant.VIDEO_RATIO_MOON;
                    video_player.heightRatio=Constant.VIDEO_RATIO_MOON;
            }
        //没Type也没有宽高
        }else{
            video_player.widthRatio=1;
            video_player.heightRatio=1;
        }
        int heightRatio = video_player.getHeightRatio();
        int widthRatio = video_player.getWidthRatio();
        int specHeight = (int) ((ScreenUtils.getScreenWidth() * (float) heightRatio) / widthRatio);
        if(null!=view) view.getLayoutParams().height=specHeight;
    }


    /**
     * 初始化播放，一进来满足条件自动播放视频
     */
    private void createPlayVideo() {

        if(null==mVideoInfo){
            return;
        }

        //点赞
        bindingView.btnPrice.setImageResource(1==mVideoInfo.getIs_interest()?R.drawable.btn_nav_like_selector_red:R.drawable.btn_nav_like_selector_white);
        headerLsyoutBinding.reVideoGroup.setIsPrice(1==mVideoInfo.getIs_interest()?true:false);

        if(!VideoDetailsActivity.this.isFinishing()){

            //设置大播放器封面
            Glide.with(this)
                    .load(mVideoInfo.getCover())
                    .crossFade()//渐变
                    .thumbnail(0.1f)
                    .error(R.drawable.iv_empty_bg_error)
                    .placeholder(R.drawable.video_empty_bg)
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .centerCrop()//中心点缩放
                    .skipMemoryCache(true)//跳过内存缓存
                    .into(headerLsyoutBinding.videoPlayer.thumbImageView);

            //设置播放器路径信息
            String proxyUrl=mVideoInfo.getPath();
            //设置播放器路径信息
            HttpProxyCacheServer proxy = VideoApplication.getProxy();
            if(null!=proxy){
                proxyUrl= proxy.getProxyUrl(mVideoInfo.getPath());
            }

            headerLsyoutBinding.videoPlayer.setUp(proxyUrl, XinQuVideoPlayer.SCREEN_WINDOW_LIST, ConfigSet.getInstance().isPalyerLoop(),mVideoInfo.getDesp());
            headerLsyoutBinding.videoPlayer.setOnPlayerCallBackListener(new XinQuVideoPlayer.OnPlayerCallBackListener() {
                @Override
                public void callBack() {
                    if(null!=mVideoDetailsPresenter){
                        mVideoDetailsPresenter.postPlayCount(VideoApplication.getLoginUserID(),mVideoInfo.getVideo_id());
                    }
                }
            });
            //播放完成就上传播放记录
            headerLsyoutBinding.videoPlayer.setOnPlayCompletionListener(new XinQuVideoPlayer.OnPlayCompletionListener() {
                @Override
                public void onCompletion() {
                    if(!isPostPlayState){
                        if(!VideoDetailsActivity.this.isFinishing()){
                            PostPlayStateHanderUtils.postVideoPlayState(mVideoInfo.getVideo_id(),(int)headerLsyoutBinding.videoPlayer.getDuration(),1,VideoDetailsActivity.this);
                        }
                    }
                }
            });
            //如果是历史记录过来的，直接播放
            if(!mIsHistory){
                //用户设置了允许WIFI网络下自动播放
                if(1==Utils.getNetworkType()&&ConfigSet.getInstance().isWifiAuthPlayer()){
                    headerLsyoutBinding.videoPlayer.startVideo();
                    //用户设置了允许移动网络下自动播放
                }else if(2==Utils.getNetworkType()&&ConfigSet.getInstance().isMobilePlayer()){
                    headerLsyoutBinding.videoPlayer.startVideo();
                }else{
                    //有网络但用户未开启自动播放
                    if(Utils.isCheckNetwork()){
                        headerLsyoutBinding.videoPlayer.startVideo();
                    }
                }
            }else{
                headerLsyoutBinding.videoPlayer.startVideo();
            }
        }
        headerLsyoutBinding.reVideoGroup.setImageVisibility();
        //初始化双击点赞
        headerLsyoutBinding.reVideoGroup.setOnDoubleClickListener(new VideoGroupRelativeLayout.OnDoubleClickListener() {
            @Override
            public void onDoubleClick() {
                priceVideo(false);
            }

            @Override
            public void onClick() {
                Logger.d(TAG,"onClick");
            }
        });
    }


    /**
     * 设置头部数据
     */
    private void initHeaderViewData() {

        if(null==mVideoInfo){
            return;
        }
        if(null==headerLsyoutBinding){
            return;
        }

        if(!VideoDetailsActivity.this.isFinishing()){
            //作者封面
            Glide.with(VideoDetailsActivity.this)
                    .load(Utils.imageUrlChange(mVideoInfo.getLogo()))
                    .error(R.drawable.iv_mine)
                    .placeholder(R.drawable.iv_mine)
                    .crossFade()//渐变
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .centerCrop()//中心点缩放
                    .skipMemoryCache(true)//跳过内存缓存
                    .transform(new GlideCircleTransform(VideoDetailsActivity.this))
                    .into(bindingView.ivVideoAuthorIcon);
            //用户信息
            try {
                headerLsyoutBinding.videoItemListUserName.setText(TextUtils.isEmpty(mVideoInfo.getNickname())?"火星人":mVideoInfo.getNickname());
                //设置视频介绍，需要单独处理
                String decode = URLDecoder.decode(TextUtils.isEmpty(mVideoInfo.getDesp())?"":mVideoInfo.getDesp(), "UTF-8");
                SpannableString topicStyleContent = TextViewTopicSpan.getTopicStyleContent(decode, CommonUtils.getColor(R.color.record_text_color), headerLsyoutBinding.videoItemListTitle, this,null);
                headerLsyoutBinding.videoItemListTitle.setText(topicStyleContent);
                headerLsyoutBinding.llHeaderVideoDespView.setVisibility(TextUtils.isEmpty(mVideoInfo.getDesp())?View.GONE:View.VISIBLE);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            headerLsyoutBinding.tvItemPlayCount.setText((TextUtils.isEmpty(mVideoInfo.getPlay_times())?"0":mVideoInfo.getPlay_times())+" 次播放");
            String add_time = mVideoInfo.getAdd_time()+"000";
            headerLsyoutBinding.tvUploadTime.setText(TimeUtils.getTilmNow(Long.parseLong(add_time))+" 发布");
            headerLsyoutBinding.tvCommendCount.setText((TextUtils.isEmpty(mVideoInfo.getComment_times())?"0":mVideoInfo.getComment_times())+" 评论");
            headerLsyoutBinding.tvLikeCount.setText((TextUtils.isEmpty(mVideoInfo.getCollect_times())?"0":mVideoInfo.getCollect_times())+" 喜欢");
            //是自己
            if(!TextUtils.isEmpty(mVideoAuthorID)&&TextUtils.equals(mVideoAuthorID,VideoApplication.getLoginUserID())){
                isVisibilityView(bindingView.llFollowView,false);
            }else{
                isVisibilityView(bindingView.llFollowView,1==mVideoInfo.getIs_follow()?false:true);
            }
        }
    }




    /**
     * 加载评论列表
     */
    private void loadComentList() {
        if(null==mVideoInfo){
            return;
        }
        if(null!=mVideoDetailsPresenter){
            mPage++;
            mVideoDetailsPresenter.getComentList(mVideoInfo.getVideo_id(),mPage+"",mPageSize+"");
        }
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {
        if(null!=mCommentListBeen&&mCommentListBeen.size()>=10){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mVideoComentListAdapter.setEnableLoadMore(true);
            loadComentList();
        }else{
            bindingView.swiperefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    mVideoComentListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
        }
    }




    /**
     * 分享
     */
    private void shareVideo() {

        if(null==mVideoInfo){
            return;
        }
        if(!TextUtils.equals("0",mVideoInfo.getIs_private())){
            ToastUtils.showErrorToast(VideoDetailsActivity.this,null,null,"私密视频无法分享，请先更改隐私权限");
            return;
        }

        XinQuVideoPlayer.goOnPlayOnPause();
        ShareInfo shareInfo=new ShareInfo();
        shareInfo.setDesp("新趣小视频:"+mVideoInfo.getDesp());
        shareInfo.setTitle("新趣小视频分享");
        shareInfo.setUrl(mVideoInfo.getPath());
        shareInfo.setVideoID(mVideoInfo.getVideo_id());
        shareInfo.setImageLogo(mVideoInfo.getCover());
        onShare(shareInfo);
    }


    @Override
    protected void onShareDialogDismiss() {
        super.onShareDialogDismiss();
        resume();
    }


    /**
     * 提供给子界面的登录方法
     */
    public void login(){
        Intent intent=new Intent(VideoDetailsActivity.this,LoginGroupActivity.class);
        startActivityForResult(intent, Constant.INTENT_LOGIN_EQUESTCODE);
        overridePendingTransition( R.anim.menu_enter,0);//进场动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //登录意图，需进一步确认
        if(Constant.INTENT_LOGIN_EQUESTCODE==requestCode&&resultCode==Constant.INTENT_LOGIN_RESULTCODE){
            if(null!=data){
                boolean booleanExtra = data.getBooleanExtra(Constant.INTENT_LOGIN_STATE, false);
                if(booleanExtra){
                    VideoApplication.isLogin=true;
                    VideoApplication.isWorksChange=false;
                    VideoApplication.isFolloUser=false;
                    getVideoInfo();//刷新
                    if (null!=VideoApplication.getInstance().getUserData()&&!VideoApplication.getInstance().userIsBinDingPhone()) {
                        binDingPhoneNumber();
                    }
                }
            }
        }
    }


    /**
     * 对视频点赞
     */
    private void priceVideo(boolean showDialog) {

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mVideoInfo) return;
        //已经登录
        if(null!=VideoApplication.getInstance().getUserData()){
            if(!TextUtils.equals("0",mVideoInfo.getIs_private())){
                ToastUtils.showErrorToast(VideoDetailsActivity.this,null,null,"私密视频无法收藏，请先更改隐私权限");
                return;
            }
            if(!TextUtils.equals("1",mVideoInfo.getStatus())){
                String status = mVideoInfo.getStatus();
                String message="点赞失败";
                if(TextUtils.equals("0",status)){
                    message="暂时无法点赞，此视频正在审核中!";
                }else if(TextUtils.equals("2",status)){
                    message="点赞失败，此视频审核未通过!";
                }
                ToastUtils.shoCenterToast(message);
                return;
            }
            if(showDialog){
                if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isPriseVideo()){
                    showProgressDialog(1==mVideoInfo.getIs_interest()?"取消点赞中..":"点赞中..",true);
                    mVideoDetailsPresenter.onPriseVideo(mVideoInfo.getVideo_id(),VideoApplication.getLoginUserID());
                }
            }else{
                if(null!=headerLsyoutBinding&&null!=headerLsyoutBinding.reVideoGroup){
                    headerLsyoutBinding.reVideoGroup.startPriceAnimation();
                }
                if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isPriseVideo()){
                    mVideoDetailsPresenter.onPriseVideo(mVideoInfo.getVideo_id(),VideoApplication.getLoginUserID());
                }
            }
        //未登录
        }else{
            ToastUtils.shoCenterToast("点赞需要登录账户");
            login();
        }
    }




    /**
     * 关注处理
     */
    private void onFollowUser() {

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mVideoInfo){
            return;
        }
        //是都已登录
        if(null!=VideoApplication.getInstance().getUserData()){
            if(TextUtils.equals(VideoApplication.getLoginUserID(),mVideoInfo.getUser_id())){
                ToastUtils.shoCenterToast("自己时刻都在关注着自己");
                return;
            }
            if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isFollowUser()){
                mVideoDetailsPresenter.onFollowUser(mVideoInfo.getUser_id(),VideoApplication.getLoginUserID());
            }
        }else{
            login();
        }
    }

    /**
     * 发送留言消息
     */
    private void sendWordsMessage() {
        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mVideoInfo){
            return;
        }

        String wordsmMessage = bindingView.tvInputContent.getText().toString();
        if(TextUtils.isEmpty(wordsmMessage)){
            ToastUtils.shoCenterToast("评论内容不能为空！");
            return;
        }

        if(!TextUtils.equals("0",mVideoInfo.getIs_private())){
            ToastUtils.shoCenterToast("私密视频无法评论，请先更改隐私权限");
            return;
        }


        if(TextUtils.equals("0",mVideoInfo.getStatus())){
            ToastUtils.shoCenterToast("暂时无法评论，此视频正在审核中!");
            return;
        }
        if(TextUtils.equals("2",mVideoInfo.getStatus())){
            ToastUtils.shoCenterToast("评论失败，此视频审核未通过!");
            return;
        }

        if(null!=VideoApplication.getInstance().getUserData()){
            if(null!=mVideoInfo){
                showProgressDialog("留言中...",true);
                try {
                    boolean isContrasts=ContentCheckKey.getInstance().contrastKey(wordsmMessage);
                    if(isContrasts){
                        wordsmMessage= Cheeses.ALTERNATE_TEXT[Utils.getRandomNum(0,4)];
                    }
                    String encode = URLEncoder.encode(wordsmMessage, "UTF-8");
                    if(null!=mVideoDetailsPresenter){
                        mVideoDetailsPresenter.addComentMessage(VideoApplication.getLoginUserID(),mVideoInfo.getVideo_id(),encode,toUserID);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }else{
            login();
        }
    }


    /**
     * 删除视频
     */
    private void deleteVideo() {
        showProgressDialog("删除视频中...",true);
        if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isDeteleVideo()){
            mVideoDetailsPresenter.deleteVideo(VideoApplication.getLoginUserID(),mVideoId);
        }
    }


    /**
     * 删除视频提示
     */
    private void deleteVideoTips() {
        //删除视频提示
        new android.support.v7.app.AlertDialog.Builder(VideoDetailsActivity.this)
                .setTitle("删除视频提示")
                .setMessage(getResources().getString(R.string.detele_video_tips))
                .setNegativeButton("取消", null)
                .setPositiveButton("删除",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteVideo();
                            }
                        }).setCancelable(false).show();
    }





    /**
     * 显示菜单
     */
    private void showMenu() {

        if(TextUtils.isEmpty(mVideoId))return;
        if(null==mVideoInfo){
            return;
        }
        if(!this.isFinishing()){
            List<VideoDetailsMenu> list=new ArrayList<>();
            //是发布此视频的用作者自己
            if(null!=VideoApplication.getInstance().getUserData()&&TextUtils.equals(mVideoInfo.getUser_id(),VideoApplication.getLoginUserID())){
                //原本私密的视频
                if(TextUtils.equals("1",mVideoInfo.getIs_private())){
                    //私密和公开属性
                    VideoDetailsMenu videoDetailsMenu1=new VideoDetailsMenu();
                    videoDetailsMenu1.setItemID(1);
                    videoDetailsMenu1.setTextColor("#FF576A8D");
                    videoDetailsMenu1.setItemName("将此视频设置为公开视频");
                    list.add(videoDetailsMenu1);
                    //原本公开的视频
                }else{
                    VideoDetailsMenu videoDetailsMenu1=new VideoDetailsMenu();
                    videoDetailsMenu1.setItemID(1);
                    videoDetailsMenu1.setTextColor("#FF576A8D");
                    videoDetailsMenu1.setItemName("将此视频设置为私密视频");
                    list.add(videoDetailsMenu1);
                    //是否允许他人下载此视频
                    VideoDetailsMenu videoDetailsMenu2=new VideoDetailsMenu();
                    videoDetailsMenu2.setItemID(2);
                    videoDetailsMenu2.setTextColor("#FF576A8D");
                    //原本允许别人下载此作品
                    if(null!=mVideoInfo.getDownload_permiss()&&TextUtils.equals("0",mVideoInfo.getDownload_permiss())){
                        videoDetailsMenu2.setItemName("不允许别人下载此视频");
                        //原本不允许别人下载此作品
                    }else{
                        videoDetailsMenu2.setItemName("允许别人下载此视频");
                    }
                    list.add(videoDetailsMenu2);
                }
                //删除视频
                VideoDetailsMenu videoDetailsMenu3=new VideoDetailsMenu();
                videoDetailsMenu3.setItemID(3);
                videoDetailsMenu3.setTextColor("#FFFF5000");
                videoDetailsMenu3.setItemName("删除此视频");
                list.add(videoDetailsMenu3);
            }else{

                VideoDetailsMenu videoDetailsMenu5=new VideoDetailsMenu();
                videoDetailsMenu5.setItemID(5);
                videoDetailsMenu5.setTextColor("#FFFF5000");
                videoDetailsMenu5.setItemName("举报此用户");
                list.add(videoDetailsMenu5);

                VideoDetailsMenu videoDetailsMenu4=new VideoDetailsMenu();
                videoDetailsMenu4.setItemID(4);
                videoDetailsMenu4.setTextColor("#FFFF5000");
                videoDetailsMenu4.setItemName("举报此视频");
                list.add(videoDetailsMenu4);
            }
            CommonMenuDialog commonMenuDialog =new CommonMenuDialog(VideoDetailsActivity.this);
            commonMenuDialog.setData(list);
            commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
                @Override
                public void onItemClick(int itemID) {
                    //公开、私密视频
                    switch (itemID) {
                        case 1:
                            if(null!=VideoApplication.getInstance().getUserData()){
                                if(TextUtils.equals("0",mVideoInfo.getIs_private())){
                                    privateVideoTips();
                                }else{
                                    if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isPrivateVideo()){
                                        showProgressDialog("操作中..",true);
                                        mVideoDetailsPresenter.setVideoPrivateState(mVideoId,VideoApplication.getLoginUserID());
                                    }
                                }
                            }
                            break;
                        //下载权限
                        case 2:
                            if(null!=VideoApplication.getInstance().getUserData()){
                                if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isDownloadPermiss()){
                                   showProgressDialog("操作中..",true);
                                    mVideoDetailsPresenter.changeVideoDownloadPermission(mVideoId,VideoApplication.getLoginUserID());
                                }
                            }
                            break;
                        //删除视频
                        case 3:
                            if(null!=VideoApplication.getInstance().getUserData()){
                                deleteVideoTips();
                            }
                            break;
                        //举报视频
                        case 4:
                            if(null!=VideoApplication.getInstance().getUserData()){
                                onReportVideo(mVideoId);
                            }else{
                                ToastUtils.shoCenterToast("举报视频需要登录账户");
                                login();
                            }
                            break;
                        //举报用户
                        case 5:
                            if(null!=VideoApplication.getInstance().getUserData()){
                                onReportUser(mVideoInfo.getUser_id());
                            }else{
                                ToastUtils.shoCenterToast("举报用户需要登录账户");
                                login();
                            }
                            break;
                    }
                }
            });
            XinQuVideoPlayer.goOnPlayOnPause();
            commonMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    XinQuVideoPlayer.goOnPlayOnResume();
                }
            });
            commonMenuDialog.show();
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    /**
     * 举报视频
     * @param video_id
     */
    private void onReportVideo(String video_id) {
        if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isReportVideo()){
            showProgressDialog("举报视频中...",true);
            mVideoDetailsPresenter.onReportVideo(VideoApplication.getLoginUserID(),video_id);
        }
    }

    /**
     * 视频私密状态设置提示
     */
    private void privateVideoTips() {

        new android.support.v7.app.AlertDialog.Builder(VideoDetailsActivity.this)
                .setTitle("隐私视频设置")
                .setMessage(getResources().getString(R.string.set_peivate_video_tips))
                .setNegativeButton("取消", null)
                .setPositiveButton("确定",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isPrivateVideo()){
                                    showProgressDialog("操作中..",true);
                                    mVideoDetailsPresenter.setVideoPrivateState(mVideoInfo.getVideo_id(),VideoApplication.getLoginUserID());
                                }
                            }
                        }).setCancelable(false).show();
    }


    /**
     * 举报用户
     * @param accuseUserId
     */
    private void onReportUser(String accuseUserId) {
        if(TextUtils.equals(VideoApplication.getLoginUserID(),mVideoAuthorID)){
            showErrorToast(null,null,"自己不能举报自己");
            return;
        }

        if(null!=mVideoDetailsPresenter&&!mVideoDetailsPresenter.isReportUser()){
            showProgressDialog("举报用户中...",true);
            mVideoDetailsPresenter.onReportUser(VideoApplication.getLoginUserID(),accuseUserId);
        }
    }

    /**
     * 是否显示关注按钮
     * @param view
     * @param isVisibility
     */
    private void isVisibilityView(final View view, boolean isVisibility) {
        if(null==view) return;
        if(isVisibility){
            if(view.getVisibility()==View.VISIBLE) return;
            view.setVisibility(View.VISIBLE);
            TranslateAnimation translateAnimation = AnimationUtil.moveLeftToViewLocation();
            view.startAnimation(translateAnimation);
        }else{
            if(view.getVisibility()==View.GONE) return;
            TranslateAnimation translateAnimation = AnimationUtil.moveToViewRight();
            translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            view.startAnimation(translateAnimation);
        }
    }




    /**
     * 根据Targe打开新的界面
     * @param title
     * @param fragmentTarge
     */
    protected void startTargetActivity(int fragmentTarge,String title,String authorID,int authorType,String topicID) {
        if(!VideoDetailsActivity.this.isFinishing()){
            Intent intent=new Intent(VideoDetailsActivity.this, ContentFragmentActivity.class);
            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentTarge);
            intent.putExtra(Constant.KEY_TITLE,title);
            intent.putExtra(Constant.KEY_AUTHOR_ID,authorID);
            intent.putExtra(Constant.KEY_AUTHOR_TYPE,authorType);
            intent.putExtra(Constant.KEY_VIDEO_TOPIC_ID,topicID);
            startActivity(intent);
        }
    }



//-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=联网回调-=-=-=-=-=-=-=-=-=-=-=-=--=-=-=-=-=-=-=-=-=-=



    /**
     * 获取视频详细信息回调
     * @param data
     */
    @Override
    public void showVideoInfoResult(VideoInfo data) {
        showContentView();
        mVideoInfo = data.getData().getInfo();
        if(null!=mVideoInfo&&TextUtils.isEmpty(mVideoInfo.getPath())){
            ApplicationManager.getInstance().getCacheExample().remove(mVideoId);
            showErrorToast(null,null,"视频不存在");
            finish();
            return;
        }
        ApplicationManager.getInstance().getCacheExample().remove(mVideoId);
        ApplicationManager.getInstance().getCacheExample().put(mVideoId,mVideoInfo);
        initVideoData();
        //获取视频留言
        mPage=0;
        loadComentList();
    }


    @Override
    public void showLoadVideoInfoError() {
        if(null==mVideoInfo){//在没有缓存的情况下
            showLoadErrorView();
            showErrorToast(null,null,"加载失败");
        }
    }

    /**
     * 加载留言列表成功
     * @param data
     */
    @Override
    public void showComentList(ComentList data) {
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoComentListAdapter.loadMoreComplete();
            }
        });
        if(1==mPage){
            if(null!=mCommentListBeen){
                mCommentListBeen.clear();
            }
            mCommentListBeen=data.getData().getComment_list();
            ApplicationManager.getInstance().getCacheExample().remove(mVideoId+"_comlist");
            ApplicationManager.getInstance().getCacheExample().put(mVideoId+"_comlist", (Serializable) mCommentListBeen);
            upDataNewDataAdapter();
        }else{
            mCommentListBeen=data.getData().getComment_list();
            updataAddDataAdapter();
        }
    }

    @Override
    public void showComentList(String videoID, ComentList data) {

    }

    /**
     * 留言列表为空
     * @param data
     */
    @Override
    public void showComentListEmpty(String data) {
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoComentListAdapter.loadMoreEnd();
            }
        });
        if(1==mPage){
            if(null!=mCommentListBeen){
                mCommentListBeen.clear();
            }
            upDataNewDataAdapter();
        }
        if(mPage>1){
            mPage--;
        }
    }

    /**
     * 加载留言列表失败
     */
    @Override
    public void showComentListError() {
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoComentListAdapter.loadMoreFail();
            }
        });
        if(mPage>1){
            mPage--;
        }
    }

    /**
     * 增加留言回调
     * @param data
     */
    @Override
    public void showAddComentRelult(SingComentInfo data) {
        closeProgressDialog();
        bindingView.tvInputContent.setText("");
        bindingView.tvInputContent.setHint("说点什么...");
        toUserID="0";
        ToastUtils.shoCenterToast("评论成功");
        SingComentInfo.DataBean.InfoBean info = data.getData().getInfo();
        if(null!=info){
            mCommentListBeanInfo = new ComentList.DataBean.CommentListBean();
            mCommentListBeanInfo.setAdd_time(String.valueOf(info.getAdd_time()));
            mCommentListBeanInfo.setComment(info.getComment());
            mCommentListBeanInfo.setId(info.getId());
            mCommentListBeanInfo.setLogo(info.getLogo());
            mCommentListBeanInfo.setNickname(info.getNickname());
            mCommentListBeanInfo.setUser_id(info.getUser_id());
            mCommentListBeanInfo.setvideo_id(info.getVideo_id());
            mCommentListBeanInfo.setTo_nickname(info.getTo_nickname());
            mCommentListBeanInfo.setTo_user_id(info.getTo_user_id());
            mCommentListBeanInfo.setComment_id(info.getComment_id());
            updataAddDataToTopAdapter();
        }
    }

    /**
     * 删除视频结果
     * @param data
     */
    @Override
    public void showDeteleVideoResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&&TextUtils.equals(Constant.DELETE_VIDEO_CONTENT,jsonObject.getString("msg"))){
                //删除成功
                String  videoID= new JSONObject(jsonObject.getString("data")).getString("video_id");
                if(!TextUtils.isEmpty(videoID)){
                    ToastUtils.shoCenterToast("删除成功");
                    VideoApplication.isWorksChange=true;
                    finish();
                }
            }else{
                showErrorToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            showErrorToast(null,null,"删除视频失败");
            e.printStackTrace();
        }
    }

    /**
     * 公开或者私有视频
     * @param result
     */
    @Override
    public void showSetVideoPrivateStateResult(String result) {
        closeProgressDialog();
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject=new JSONObject(result);
                if(null!=jsonObject&&jsonObject.length()>0){
                    //修改权限成功
                    if(1==jsonObject.getInt("code")){
                        if(!TextUtils.isEmpty(jsonObject.getString("msg"))){
                            ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                        }
                        JSONObject dataObject=new JSONObject(jsonObject.getString("data"));
                        int isPrivate = dataObject.getInt("is_private");
                        if(null!=mVideoInfo){
                            mVideoInfo.setIs_private(isPrivate+"");
                            ApplicationManager.getInstance().getCacheExample().remove(mVideoId);
                            ApplicationManager.getInstance().getCacheExample().put(mVideoId,mVideoInfo);
                            VideoApplication.isWorksChange=true;
                        }
                    }else{
                        if(!TextUtils.isEmpty(jsonObject.getString("msg"))){
                            ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 更改了视频下载权限
     * @param result
     */
    @Override
    public void showChangeVideoDownloadPermissionResult(String result) {
        closeProgressDialog();
        if (!TextUtils.isEmpty(result)) {
            try {
                JSONObject jsonObject=new JSONObject(result);
                if(null!=jsonObject&&jsonObject.length()>0){
                    //修改权限成功
                    if(1==jsonObject.getInt("code")){
                        if(!TextUtils.isEmpty(jsonObject.getString("msg"))){
                            ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                        }
                        JSONObject dataObject=new JSONObject(jsonObject.getString("data"));
                        int isPrivate = dataObject.getInt("download_permiss");
                        if(null!=mVideoInfo){
                            mVideoInfo.setDownload_permiss(isPrivate+"");
                            ApplicationManager.getInstance().getCacheExample().remove(mVideoId);
                            ApplicationManager.getInstance().getCacheExample().put(mVideoId,mVideoInfo);
                            VideoApplication.isWorksChange=true;
                        }
                    }else{
                        if(!TextUtils.isEmpty(jsonObject.getString("msg"))){
                            ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 点赞视频成功
     * @param data
     */
    @Override
    public void showPriseResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")){
                JSONObject resultData = new JSONObject(jsonObject.getString("data"));
                //点赞成功
                if(TextUtils.equals(Constant.PRICE_SUCCESS,jsonObject.getString("msg"))){
                    mVideoInfo.setIs_interest(1);
                    headerLsyoutBinding.reVideoGroup.startPriceAnimation();
                    bindingView.btnPrice.setImageResource(R.drawable.btn_nav_like_selector_red);
                    if(null!=headerLsyoutBinding){
                        headerLsyoutBinding.tvLikeCount.setText(resultData.getString("collect_times")+" 喜欢");
                    }
                    bindingView.btnPrice.startAnimation(mFollowScaleAnimation);
                    //取消点赞成功
                }else if(TextUtils.equals(Constant.PRICE_UNSUCCESS,jsonObject.getString("msg"))){
                    mVideoInfo.setIs_interest(0);
//                    bindingView.btnPrice.setImageResource(0==followIconState?R.drawable.btn_nav_like_selector_white:R.drawable.btn_nav_like_selector_gray);
                    bindingView.btnPrice.setImageResource(R.drawable.btn_nav_like_selector_white);
                    if(null!=headerLsyoutBinding){
                        headerLsyoutBinding.tvLikeCount.setText(resultData.getString("collect_times")+" 喜欢");
                    }
                    headerLsyoutBinding.reVideoGroup.setIsPrice(false);
                }
                VideoApplication.isWorksChange=true;
            }else{
                showErrorToast(null,null,"收藏失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }



    /**
     * 关注成功
     * @param data
     */
    @Override
    public void showFollowUserResult(String data) {
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")){
                //关注成功
                if(TextUtils.equals(Constant.FOLLOW_SUCCESS,jsonObject.getString("msg"))){
                    mVideoInfo.setIs_follow(1);
                    showFinlishToast(null,null,"关注成功");
                    isVisibilityView(bindingView.llFollowView,false);
                }else if(TextUtils.equals(Constant.FOLLOW_UNSUCCESS,jsonObject.getString("msg"))){
                    mVideoInfo.setIs_follow(0);
                    showFinlishToast(null,null,"取消关注成功");
                }
                VideoApplication.isFolloUser=true;
            }else{
                showErrorToast(null,null,"关注失败");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void showPostPlayCountResult(String data) {
        if(TextUtils.isEmpty(data)){
            return;
        }
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&& TextUtils.equals(Constant.PLAY_COUNT_SUCCESS,jsonObject.getString("msg"))){
                PlayCountInfo playCountInfo = new Gson().fromJson(data, PlayCountInfo.class);
                PlayCountInfo.DataBean.InfoBean info = playCountInfo.getData().getInfo();
                headerLsyoutBinding.tvItemPlayCount.setText(info.getPlaty_times()+" 次播放");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 举报用户回调
     * @param data
     */
    @Override
    public void showReportUserResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")){
                showFinlishToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *  举报视频回调
     * @param data
     */
    @Override
    public void showReportVideoResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")){
                showFinlishToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 联网错误
     */
    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }


    /**
     * 播放信息上传回调
     * @param newPlayCount
     */
    @Override
    public void onPostPlayStateComple(String newPlayCount) {
        isPostPlayState=true;
        if(!VideoDetailsActivity.this.isFinishing()){
            headerLsyoutBinding.tvItemPlayCount.setText(newPlayCount+"次播放");
        }
    }

    /**
     * 上传播放信息失败
     */
    @Override
    public void onPostPlayStateError() {
        isPostPlayState=false;
    }


    /**
     * 点击了话题
     * @param topic
     */
    @Override
    public void onTopicClick(String topic) {
        if(!TextUtils.isEmpty(topic)){
            startTargetActivity(Constant.KEY_FRAGMENT_TYPE_TOPIC_VIDEO_LISTT,topic,VideoApplication.getLoginUserID(),0,topic);
        }
    }

    /**
     * 点击了连接
     * @param url
     */
    @Override
    public void onUrlClick(String url) {
        WebViewActivity.loadUrl(VideoDetailsActivity.this,url,"未知");
    }


    @Override
    public void onAuthoeClick(String author) {
        AuthorDetailsActivity.start(VideoDetailsActivity.this,author);
    }


    /**
     * 点击了用户头像
     * @param userID
     */
    @Override
    public void onAuthorIconClick(String userID) {
        AuthorDetailsActivity.start(VideoDetailsActivity.this,userID);
    }

    /**
     * 点击了留言列表
     * @param data
     */
    @Override
    public void onAuthorItemClick(ComentList.DataBean.CommentListBean data) {
        if(null!=data){
            toUserID=data.getUser_id();
            showMenuTabView();
            bindingView.tvInputContent.setHint("回复 "+data.getNickname());
            showInputKeyBoardDialog(true,false,"回复 "+data.getNickname());
        }
    }


    @Override
    public void onBackPressed() {
        if (XinQuVideoPlayer.backPress()) {
            return;
        }
        if(SharedPreferencesUtil.getInstance().getBoolean(Constant.KEY_MAIN_INSTANCE,false)){
            super.onBackPressed();
        }else{
            Intent intent=new Intent(VideoDetailsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resume();
    }

    private void resume() {
        if(!mIsHistory){
            if(1==Utils.getNetworkType()&& ConfigSet.getInstance().isWifiAuthPlayer()){
                XinQuVideoPlayer.goOnPlayOnResume();
            }else if(2==Utils.getNetworkType()&& ConfigSet.getInstance().isMobilePlayer()){
                XinQuVideoPlayer.goOnPlayOnResume();
            }
        }else{
            XinQuVideoPlayer.goOnPlayOnResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        XinQuVideoPlayer.goOnPlayOnPause();
    }


    @Override
    public void onDestroy() {
        XinQuVideoPlayer.releaseAllVideos();
        if(null!=mVideoDetailsPresenter){
            mVideoDetailsPresenter.detachView();
        }
        //没有上传播放记录postVideoPlayState
        if (!isPostPlayState&&null!=mVideoInfo) {
            //大于三秒才会上传播放信息记录
            ApplicationManager.getInstance().postVideoPlayState(mVideoInfo.getVideo_id(), (int)headerLsyoutBinding.videoPlayer.getCurrentPositionWhenPlaying(), 0, null);
        }
        super.onDestroy();
        headerLsyoutBinding=null;
        if(null!=mCommentListBeen) mCommentListBeen.clear();
        mCommentListBeen=null;mFollowScaleAnimation=null;mVideoComentListAdapter=null;mLinearLayoutManager=null;mComentEmptybindView=null;
        mVideoDetailsPresenter=null;mVideoInfo=null;
        Runtime.getRuntime().gc();
    }
}
