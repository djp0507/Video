package com.video.newqu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.alibaba.fastjson.JSONArray;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.UserVideoListAdapter;
import com.video.newqu.base.BaseAuthorActivity;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.MineUserInfo;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityAuthorDetailsBinding;
import com.video.newqu.databinding.MineAuthorRecylerviewEmptyLayoutBinding;
import com.video.newqu.listener.OnUserVideoListener;
import com.video.newqu.model.RecyclerViewSpacesItem;
import com.video.newqu.ui.contract.AuthorDetailContract;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.AuthorDetailPresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.widget.GlideCircleTransform;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/5/25 15:16
 * 作者个人中心详情界面--新特性版本
 */

public class AuthorDetailsActivity extends BaseAuthorActivity<ActivityAuthorDetailsBinding> implements AuthorDetailContract.View,BaseQuickAdapter.RequestLoadMoreListener,OnUserVideoListener {

    public static final String TAG =AuthorDetailsActivity.class.getSimpleName();
    private int tabIType=0;//用户未设置，0为默认网格，1：列表模式，跟随用户习惯
    private String mAuthorID;//用户ID
    private AuthorDetailPresenter mAuthorDetailPresenter;
    private MineUserInfo.DataBean.InfoBean mInfoBean;
    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList=null;
    private int mPage=0;
    private int  mPageSize=20;
    private AnimationDrawable mAnimationDrawable;
    private UserVideoListAdapter mVideoListAdapter;
    private int mHeaderViewHeight;

    /**
     * 入口
     * @param context
     * @param authorID 用户ID
     */
    public static void start(Context context, String authorID) {
        Intent intent=new Intent(context,AuthorDetailsActivity.class);
        intent.putExtra("author_id",authorID);
        intent.putExtra("is_follow","");
        context.startActivity(intent);
    }

    /**
     * 入口
     * @param context
     * @param authorID 用户ID
     */
    public static void start(Context context, String authorID,String isFollow) {
        Intent intent=new Intent(context,AuthorDetailsActivity.class);
        intent.putExtra("author_id",authorID);
        intent.putExtra("is_follow",isFollow);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_details);
        EventBus.getDefault().register(this);
        initIntent();
        initAdapter();
        showLoadingView("获取用户视频中...");
        mAuthorDetailPresenter = new AuthorDetailPresenter(AuthorDetailsActivity.this);
        mAuthorDetailPresenter.attachView(this);
        mInfoBean= (MineUserInfo.DataBean.InfoBean) ApplicationManager.getInstance().getCacheExample().getAsObject(mAuthorID);
        initUserData();
        tabIType=SharedPreferencesUtil.getInstance().getInt(Constant.AUTHOR_TAB_STYLE,0);
        mListsBeanList= (List<FollowVideoList.DataBean.ListsBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(mAuthorID+"_video_list");
        isMine(mAuthorID);
        if(null==mListsBeanList) mListsBeanList=new ArrayList<>();
        if(null!=mInfoBean&&null!=mListsBeanList&&mListsBeanList.size()>0){
            bindingView.userVideoCount.setText(mListsBeanList.size()+"作品");
            upDataNewDataAdapter();
        }
        loadUserInfo();//加载用户数据
    }


    @Override
    public void initViews() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    //点击别人，弹出举报弹窗，点击自己无选项
                    case R.id.iv_user_icon:
                        if(null==mInfoBean) return;
                        MediaSingerImagePreviewActivity.start(AuthorDetailsActivity.this,mInfoBean.getLogo(),bindingView.ivUserIcon);
                        break;
                    //关注
                    case R.id.re_add:
                        onFollowUser();
                        break;
                    //等级
                    case R.id.tv_user_grade:

                        break;
                    //返回
                    case R.id.btn_back:
                        onBackPressed();
                        break;
                    //菜单
                    case R.id.iv_menu:
//                        onMenu();
                        showUserDetailsDataDialog();
                        break;
                    //粉丝
                    case R.id.tv_fans_count:
                        lookFans();
                        break;
                    //关注
                    case R.id.tv_follow_count:
                        lookFollows();
                        break;
                    //分享
                    case R.id.btn_share:
                        shareMineHome();
                        break;
                }
            }
        };
        //标题栏上的按钮在父类那里
        baseBinding.btnBack.setOnClickListener(onClickListener);
        baseBinding.ivMenu.setOnClickListener(onClickListener);
        baseBinding.btnShare.setOnClickListener(onClickListener);

        bindingView.ivUserIcon.setOnClickListener(onClickListener);
        bindingView.reAdd.setOnClickListener(onClickListener);
        bindingView.tvFansCount.setOnClickListener(onClickListener);
        bindingView.tvFollowCount.setOnClickListener(onClickListener);
        //刷新监听
//        bindingView.swiperefreshLayout.setColorSchemeColors(CommonUtils.getColor(R.color.colorTabText));
//        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                bindingView.swiperefreshLayout.setRefreshing(true);
//                mPage=0;
//                loadVideoList();
//            }
//        });
        mAnimationDrawable = (AnimationDrawable) bindingView.ivLoadingIcon.getDrawable();
        bindingView.llErrorView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null==mInfoBean){
                    showLoadingView("获取用户信息和作品中...");
                    loadUserInfo();//加载用户数据
                }else{
                    showLoadingView("获取用户发布的作品中...");
                    mPage=0;
                    loadVideoList();//直接加载用户发布的视频
                }
            }
        });

        baseBinding.ivMenu.setVisibility(null!=VideoApplication.getInstance().getUserData()&&!TextUtils.isEmpty(mAuthorID)&&TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())?View.GONE:View.VISIBLE);

        int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        baseBinding.llTopTitle.measure(width,height);
        bindingView.llHeaderView.measure(width,height);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) baseBinding.topBarBg.getLayoutParams();
        layoutParams.width=RelativeLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height=  baseBinding.llTopTitle.getMeasuredHeight();
        baseBinding.topBarBg.setLayoutParams(layoutParams);
        //变化值
        mHeaderViewHeight =bindingView.llHeaderView.getMeasuredHeight();
        bindingView.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int abs = Math.abs(verticalOffset);
                if(abs<=mHeaderViewHeight/2){
                    //标题栏用户昵称
                    float textScale = (float) abs / (mHeaderViewHeight/2);
                    float textAlpha = (255 * textScale);
                    baseBinding.tvTitleUserName.setTextColor(Color.argb((int) textAlpha, 255, 255, 255));//标题栏用户昵称
                }
                //界面用户昵称
                if(abs<=(mHeaderViewHeight/3)){
                    float textSubScale = (float) abs / (mHeaderViewHeight/3);
                    float textSubAlpha = (255 * textSubScale);
                    bindingView.tvSubtitleUserName.setTextColor(Color.argb((int) Utils.absValue(textSubAlpha,0,255), 255, 255, 255));//界面下边用户昵称
                }
                //处理标题栏背景图片渐变透明度
                float drawableAlpha =abs* 1.0f / mHeaderViewHeight;
                Drawable drawable = baseBinding.topBarBg.getDrawable();
                if(null==drawable)return;
                drawable.mutate().setAlpha((int) (drawableAlpha * 255));
                baseBinding.topBarBg.setImageDrawable(drawable);
            }
        });
    }

    @Override
    public void initData() {

    }



    /**
     * 分享用户的主页
     */
    private void shareMineHome() {
        if(!TextUtils.isEmpty(mAuthorID)){
            ShareInfo shareInfo=new ShareInfo();

            String nikeName="";
            if(null!=mInfoBean){
                if(!TextUtils.isEmpty(mInfoBean.getNickname())){
                    try {
                        nikeName=URLDecoder.decode(mInfoBean.getNickname(),"UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
            shareInfo.setDesp("我在新趣小视频安家啦！新奇好玩的新趣视频，期待你的加入！ "+(TextUtils.isEmpty(nikeName)?"":nikeName));
            shareInfo.setTitle("快来加入新趣，我在新趣等你！");
            shareInfo.setUserID(mAuthorID);
            shareInfo.setUrl("http://app.nq6.com/home/user/index?user_id="+shareInfo.getUserID());
            shareMineHome(shareInfo);
        }
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(AuthorDetailsActivity.this,3,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new RecyclerViewSpacesItem(ScreenUtils.dpToPxInt(0.8f)));
        bindingView.recyerView.setHasFixedSize(true);
        mVideoListAdapter = new UserVideoListAdapter(mListsBeanList,3,this);
        MineAuthorRecylerviewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(AuthorDetailsActivity.this.getLayoutInflater(),R.layout.mine_author_recylerview_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.tvItemName.setText("该用户还没有发布视频~");
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        mVideoListAdapter.setOnLoadMoreListener(this);
        bindingView.recyerView.setAdapter(mVideoListAdapter);
        //初始化固定的顶部标题栏背景View
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.home_share_bg_cover);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(bitmap);
        bitmapDrawable.mutate().setAlpha(0);
        baseBinding.topBarBg.setImageDrawable(bitmapDrawable);
    }


    /**
     * 刷新适配器所有数据
     */
    private void upDataNewDataAdapter() {
        showContentView();
        mVideoListAdapter.setNewData(mListsBeanList);
    }


    /**
     * 为适配器添加新的数据
     */
    private void updataAddDataAdapter() {
        mVideoListAdapter.addData(mListsBeanList);
    }


    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {
        if(null!=mListsBeanList&&mListsBeanList.size()>=10){
//            bindingView.swiperefreshLayout.setRefreshing(false);
            mVideoListAdapter.setEnableLoadMore(true);
            loadVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mVideoListAdapter.loadMoreFail();//加载失败
                    }else{
                        mVideoListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }




    /**
     * 是自己，关注按钮置为灰色,
     * @param userID
     */
    private void isMine(String userID) {
        if(!TextUtils.isEmpty(userID)&&TextUtils.equals(userID,VideoApplication.getLoginUserID())){
            bindingView.reAdd.setBackgroundResource(R.drawable.bg_item_follow_gray_transpent_selector);
            bindingView.ivAdd.setImageResource(R.drawable.ic_min_add_gray);
            bindingView.tvAdd.setTextColor(CommonUtils.getColor(R.color.common_h2));
        }
    }


    /**
     * 获取意图对象
     */
    private void initIntent() {
        Intent intent = getIntent();
        if(intent != null) {
            mAuthorID =intent .getStringExtra("author_id");
            if(TextUtils.isEmpty(mAuthorID)){
                ToastUtils.shoCenterToast("错误");
                finish();
            }
        }
    }

    /**
     * 加载用户所有视频
     */
    private void loadVideoList() {
        mPage++;
        mAuthorDetailPresenter.getUpLoadVideoList(mAuthorID, VideoApplication.getLoginUserID(),mPage+"",mPageSize+"");
    }


    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        mAuthorDetailPresenter.getUserInfo(mAuthorID);
    }



    /**
     * 提供给子界面的登录方法
     */
    public void login(){
        Intent intent=new Intent(AuthorDetailsActivity.this,LoginGroupActivity.class);
        startActivityForResult(intent,Constant.INTENT_LOGIN_EQUESTCODE);
        overridePendingTransition( R.anim.menu_enter,0);//进场动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //登录意图，需进一步确认
        if(Constant.INTENT_LOGIN_EQUESTCODE==requestCode&&resultCode==Constant.INTENT_LOGIN_RESULTCODE){
            if(null!=data){
                boolean booleanExtra = data.getBooleanExtra(Constant.INTENT_LOGIN_STATE, false);
                //登录成功,刷新子界面
                if(booleanExtra){
                    VideoApplication.isLogin=true;
                    VideoApplication.isFolloUser=false;
                    mPage=0;
                    loadUserInfo();
                }
            }
        }
    }


    //==========================================数据请求回调==========================================

    /**
     * 用户基本信息结果回调
     * @param data
     */
    @Override
    public void showUserInfo(MineUserInfo data,String userID) {
        mInfoBean = data.getData().getInfo();
        if(null!=mInfoBean)
            ApplicationManager.getInstance().getCacheExample().remove(mAuthorID);
        ApplicationManager.getInstance().getCacheExample().put(mAuthorID,mInfoBean);
            String videoCount = TextUtils.isEmpty(mInfoBean.getVideo_count()) ? "0" : mInfoBean.getVideo_count();
            bindingView.userVideoCount.setText(videoCount+"作品");
            initUserData();
            loadVideoList();
            //H5跳转而来根据动作是否关注用户
            if(null!=getIntent()&&null!=getIntent().getStringExtra("is_follow")&&TextUtils.equals("1",getIntent().getStringExtra("is_follow"))){
                if(null!=VideoApplication.getInstance().getUserData()){
                    if(TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())){
                        showErrorToast(null,null,"自己时刻都在关注着自己！");
                        return;
                    }
                    //未关注
                    if(0==mInfoBean.getIs_follow()){
                        onFollow();
                    }
                }else{
                    login();
                }
            }
    }

    /**
     * 关注用户结果回调
     * @param isFollow
     * @param text
     */
    @Override
    public void showFollowUser(Boolean isFollow,String text) {
        closeProgressDialog();
        //关注成功
        if(null!=isFollow&&isFollow){
        mInfoBean.setIs_follow(1);
        //取消关注成功
        }else if(null!=isFollow&&!isFollow){
            mInfoBean.setIs_follow(0);
        }
        VideoApplication.isFolloUser=true;
        showFinlishToast(null,null,text);
        switchIsFollow();//切换关注状态
    }

    /**
     * 举报用户结果回调
     * @param data
     */
    @Override
    public void showReportUserResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(jsonObject.length()>0&&1==jsonObject.getInt("code")){
                ToastUtils.shoCenterToast(jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }




    /**
     * 用户的所有视频成功回调
     * @param data
     */
    @Override
    public void showUpLoadVideoList(FollowVideoList data) {
        showContentView();
//        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreComplete();//加载完成
            }
        });
        //替换为全新数据
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            mListsBeanList=data.getData().getLists();
            ApplicationManager.getInstance().getCacheExample().remove(mAuthorID+"_video_list");
            ApplicationManager.getInstance().getCacheExample().put(mAuthorID+"_video_list", (Serializable) mListsBeanList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
            //添加数据
        }else{
            mListsBeanList=data.getData().getLists();
            updataAddDataAdapter();
        }
    }

    /**
     * 获取用户视频为空
     * @param data
     */
    @Override
    public void showUpLoadVideoListEmpty(String data) {
        showContentView();
//        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //如果当前用户在第一页的时候获取视频为空，表示该用户没有关注用户
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            ApplicationManager.getInstance().getCacheExample().remove(mAuthorID+"_video_list");
            ApplicationManager.getInstance().getCacheExample().put(mAuthorID+"_video_list", (Serializable) mListsBeanList, Constant.CACHE_TIME);
            upDataNewDataAdapter();
        }
        //还原当前的页数
        if (mPage > 1) {
            mPage--;
        }
    }

    /**
     * 获取用户视频错误
     * @param data
     */
    @Override
    public void showUpLoadVideoListError(String data) {

//        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mVideoListAdapter.loadMoreFail();
            }
        });

        if(1==mPage&&null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingErrorView();
        }
        if(mPage>0){
            mPage--;
        }
    }




    /**
     * 当加载错误的时候
     */
    @Override
    public void showErrorView() {
        if(1==mPage&&null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingErrorView();
        }
        if(mPage>0){
            mPage--;
        }
    }

    @Override
    public void complete() {

    }

    /**
     * 关注处理
     */
    private void onFollowUser() {

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mInfoBean){
            return;
        }
        if(TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())){
            showErrorToast(null,null,"自己时刻都在关注着自己！");
            return;
        }

        if(null!= VideoApplication.getInstance().getUserData()){
            //已关注
            if(1==mInfoBean.getIs_follow()){
                showUnFollowMenu();
                //未关注
            }else{
                onFollow();
            }
        }else{
            login();
        }
    }


    //===========================================数据绑定===========================================

    /**
     * 初始化用户基本信息
     */
    private void initUserData() {

        if(null==mInfoBean){
            return;
        }else{
            try {
                baseBinding.tvTitleUserName.setText(TextUtils.isEmpty(mInfoBean.getNickname()) ? "火星人" : mInfoBean.getNickname());
                bindingView.tvSubtitleUserName.setText(TextUtils.isEmpty(mInfoBean.getNickname()) ? "火星人" : mInfoBean.getNickname());
                String decode = URLDecoder.decode(TextUtils.isEmpty(mInfoBean.getSignature())?"宝宝暂时没有个性签名":mInfoBean.getSignature(), "UTF-8");
                bindingView.tvUserDesp.setText(decode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            bindingView.tvUserGrade.setText("Lv"+(TextUtils.isEmpty(mInfoBean.getRank())?"1":mInfoBean.getRank()));
            bindingView.tvFansCount.setText(TextUtils.isEmpty(mInfoBean.getFans())?"0粉丝":Utils.changeNumberFormString(mInfoBean.getFans()));
            bindingView.tvFansCount.setText(TextUtils.isEmpty(mInfoBean.getFans())?"0粉丝":mInfoBean.getFans()+"粉丝");
            bindingView.tvFollowCount.setText(TextUtils.isEmpty(mInfoBean.getFollows())?"0关注":mInfoBean.getFollows()+"关注");
            bindingView.ivUserSex.setImageResource(TextUtils.isEmpty(mInfoBean.getGender())?R.drawable.iv_icon_sex_women:TextUtils.equals("女",mInfoBean.getGender())?R.drawable.iv_icon_sex_women:R.drawable.iv_icon_sex_man);
            //是否对该作者已关注
            switchIsFollow();
            setHeaderImageBG();
        }
    }

    /**
     * 是否对该作者已关注
     */
    private void switchIsFollow() {
        if(null==mInfoBean){
            return;
        }
        bindingView.reAdd.setBackgroundResource(R.drawable.text_bg_round_app_style_pressed_true_selector);
        bindingView.ivAdd.setImageResource(1==mInfoBean.getIs_follow()?R.drawable.iv_follow_true_white:R.drawable.ic_min_add_white);
        bindingView.tvAdd.setText(TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())?"关 注":1==mInfoBean.getIs_follow()?"已关注":"关 注");
        bindingView.tvAdd.setTextColor(CommonUtils.getColor(R.color.white));
        isMine(mAuthorID);
    }

    /**
     * 关注用户
     */
    private void onFollow() {
        showProgressDialog("关注中，请稍后...",true);
        mAuthorDetailPresenter.onFollowUser(mInfoBean.getId(), VideoApplication.getLoginUserID());
    }


    /**
     * 消息
     */
    private void onMessage() {

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mInfoBean){
            return;
        }
    }



    /**
     * 举报用户
     * @param userId
     */
    private void onReportUser(String userId) {
        showProgressDialog("举报用户中...",true);
        mAuthorDetailPresenter.onReportUser(VideoApplication.getLoginUserID(),userId);
    }


    /**
     * 设置头部背景图片
     */
    private void setHeaderImageBG() {
        if(null==mInfoBean){
            return;
        }
        if(!AuthorDetailsActivity.this.isFinishing()){
            //设置背景封面
            Glide.with(this)
                    .load(TextUtils.isEmpty(mInfoBean.getImage_bg())?R.drawable.iv_mine_bg:Utils.imageUrlChange(mInfoBean.getImage_bg()))
                    .error(R.drawable.iv_mine_bg)
                    .placeholder(R.drawable.iv_mine_bg)
                    .crossFade()//渐变
                    .thumbnail(0.1f)
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .skipMemoryCache(true)//跳过内存缓存
                    .into(bindingView.ivHeaderBg);
            //作者封面
            Glide.with(this)
                    .load(TextUtils.isEmpty(mInfoBean.getLogo())?R.drawable.iv_mine:Utils.imageUrlChange(mInfoBean.getLogo()))
                    .error(R.drawable.iv_mine)
                    .placeholder(R.drawable.iv_mine)
                    .crossFade()//渐变
                    .thumbnail(0.1f)
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .centerCrop()//中心点缩放
                    .skipMemoryCache(true)//跳过内存缓存
                    .transform(new GlideCircleTransform(this))
                    .into(bindingView.ivUserIcon);
        }
    }

    /**
     * 弹出取消关注窗口
     */
    private void showUnFollowMenu() {
        List<VideoDetailsMenu> list=new ArrayList<>();
        VideoDetailsMenu videoDetailsMenu1=new VideoDetailsMenu();
        videoDetailsMenu1.setItemID(1);
        videoDetailsMenu1.setTextColor("#FF576A8D");
        videoDetailsMenu1.setItemName("取消关注");
        list.add(videoDetailsMenu1);

        CommonMenuDialog commonMenuDialog =new CommonMenuDialog(AuthorDetailsActivity.this);
        commonMenuDialog.setData(list);
        commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
            @Override
            public void onItemClick(int itemID) {
                //取消关注
                switch (itemID) {
                    case 1:
                        onFollow();
                        break;
                }
            }
        });
        commonMenuDialog.show();
    }






    /**
     * 查看关注列表
     */
    private void lookFollows() {
        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mInfoBean){
            return;
        }
        String title;
        int type=0;
        //是自己
        if(null!= VideoApplication.getInstance().getUserData()&&TextUtils.equals(mAuthorID, VideoApplication.getLoginUserID())){
            title="我关注的人";
            type=1;
        }else{
            title=mInfoBean.getNickname()+"关注的人";
            type=0;
        }
        startTargetActivity(Constant.KEY_FRAGMENT_TYPE_FOLLOW_USER_LIST,title,mAuthorID,type);
    }


    /**
     * 根据Targe打开新的界面
     * @param title
     * @param fragmentTarge
     */
    protected void startTargetActivity(int fragmentTarge,String title,String authorID,int authorType) {
        Intent intent=new Intent(AuthorDetailsActivity.this, ContentFragmentActivity.class);
        intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentTarge);
        intent.putExtra(Constant.KEY_TITLE,title);
        intent.putExtra(Constant.KEY_AUTHOR_ID,authorID);
        intent.putExtra(Constant.KEY_AUTHOR_TYPE,authorType);
        startActivity(intent);
    }


    /**
     * 查看粉丝列表
     */
    private void lookFans() {

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mInfoBean){
            return;
        }
        String title;
        int type=0;
        //是自己
        if(null!= VideoApplication.getInstance().getUserData()&&TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())){
            title="我的粉丝";
            type=1;
        }else{
            title=mInfoBean.getNickname()+"的粉丝";
            type=0;
        }
        startTargetActivity(Constant.KEY_FRAGMENT_TYPE_FANS_LIST,title,mAuthorID,type);
    }


    /**
     * 显示详细信息
     */

    private void showUserDetailsDataDialog() {

        if(TextUtils.isEmpty(mAuthorID)) return;
        if(null==mInfoBean) return;

        if(!AuthorDetailsActivity.this.isFinishing()){
            List<VideoDetailsMenu> list=new ArrayList<>();
            //是发布此视频的用作者自己
            if(null!=VideoApplication.getInstance().getUserData()&&TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())){
                VideoDetailsMenu videoDetailsMenu6=new VideoDetailsMenu();
                videoDetailsMenu6.setItemID(6);
                videoDetailsMenu6.setTextColor("#FF576A8D");
                videoDetailsMenu6.setItemName("点击复制用户ID: "+mAuthorID);
                list.add(videoDetailsMenu6);
            }else{
                VideoDetailsMenu videoDetailsMenu6=new VideoDetailsMenu();
                videoDetailsMenu6.setItemID(6);
                videoDetailsMenu6.setTextColor("#FF576A8D");
                videoDetailsMenu6.setItemName("点击复制用户ID: "+mAuthorID);
                list.add(videoDetailsMenu6);

                VideoDetailsMenu videoDetailsMenu7=new VideoDetailsMenu();
                videoDetailsMenu7.setItemID(7);
                videoDetailsMenu7.setTextColor("#FFFF7044");
                videoDetailsMenu7.setItemName("举报此用户");
                list.add(videoDetailsMenu7);
            }
            CommonMenuDialog commonMenuDialog =new CommonMenuDialog(AuthorDetailsActivity.this);
            commonMenuDialog.setData(list);
            commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
                @Override
                public void onItemClick(int itemID) {
                    //复制ID
                    switch (itemID) {
                        case 6:
                            Utils.copyString(mAuthorID);
                            ToastUtils.shoCenterToast("已复制");
                            break;
                        //举报用户
                        case 7:
                            //去登录
                            if(null== VideoApplication.getInstance().getUserData()){
                                ToastUtils.shoCenterToast("该操作需要登录！");
                                login();
                            }else{
                                if(null!=mInfoBean){
                                    onReportUser(mInfoBean.getId());
                                }
                            }
                            break;
                    }
                }
            });
            commonMenuDialog.show();
        }
    }
    //=======================================点击事件回调============================================

    @Override
    public void onItemClick(int position) {
        if(null!=mVideoListAdapter){
            List<FollowVideoList.DataBean.ListsBean> data = mVideoListAdapter.getData();
            if(null!= data && data.size()>0){
                //全屏
                if(ConfigSet.getInstance().isPlayerModel()){
                    //改成获取当前视频的
                    try{
                        FollowVideoList.DataBean dataBean=new FollowVideoList.DataBean();
                        dataBean.setLists(data);
                        FollowVideoList followVideoList=new FollowVideoList();
                        followVideoList.setData(dataBean);
                        String json = JSONArray.toJSON(followVideoList).toString();
                        if(!TextUtils.isEmpty(json)){
                            Intent intent=new Intent(AuthorDetailsActivity.this,VerticalVideoPlayActivity.class);
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_AUTHOE_CORE);
                            intent.putExtra(Constant.KEY_POISTION,position);
                            intent.putExtra(Constant.KEY_PAGE,mPage);
                            intent.putExtra(Constant.KEY_AUTHOE_ID,mAuthorID);
                            intent.putExtra(Constant.KEY_JSON,json);
                            startActivity(intent);
                        }
                    }catch (Exception e){

                    }
                    //单个
                }else{
                    FollowVideoList.DataBean.ListsBean listsBean = data.get(position);
                    if(null!=listsBean&&!TextUtils.isEmpty(listsBean.getVideo_id())){
                        saveLocationHistoryList(listsBean);
                        VideoDetailsActivity.start(AuthorDetailsActivity.this,listsBean.getVideo_id(),listsBean.getUser_id(),false);
                    }
                }
            }
        }
    }

    @Override
    public void onLongClick(String videoID) {

    }

    @Override
    public void onDeleteVideo(String videoID) {

    }

    @Override
    public void onPublicVideo(String videoID) {

    }

    @Override
    public void onUnFollowVideo(String videoID) {

    }

    @Override
    public void onHeaderIcon(String userID) {

    }


    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        SharedPreferencesUtil.getInstance().putInt(Constant.AUTHOR_TAB_STYLE,tabIType);
        SharedPreferencesUtil.getInstance().putInt(Constant.AUTHOR_TAB_STYLE,tabIType);
        if(SharedPreferencesUtil.getInstance().getBoolean(Constant.KEY_MAIN_INSTANCE,false)){
            super.onBackPressed();
        }else{
            Intent intent=new Intent(AuthorDetailsActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        Utils.vars=null;
        if(null!=mAuthorDetailPresenter){
            mAuthorDetailPresenter.detachView();
        }
        if(null!=mListsBeanList){
            mListsBeanList.clear();
        }
        mListsBeanList=null;mAnimationDrawable=null;mVideoListAdapter=null;mAuthorDetailPresenter=null;mInfoBean=null;mAuthorID=null;
        Runtime.getRuntime().gc();
    }




    private void saveLocationHistoryList(final FollowVideoList.DataBean.ListsBean data) {
        if(null==data) return;
        new Thread(){
            @Override
            public void run() {
                super.run();
                UserPlayerVideoHistoryList userLookVideoList=new UserPlayerVideoHistoryList();
                userLookVideoList.setUserName(TextUtils.isEmpty(data.getNickname())?"火星人":data.getNickname());
                userLookVideoList.setUserSinger("该宝宝没有个性签名");
                userLookVideoList.setUserCover(data.getLogo());
                userLookVideoList.setVideoDesp(data.getDesp());
                userLookVideoList.setVideoLikeCount(TextUtils.isEmpty(data.getCollect_times())?"0":data.getCollect_times());
                userLookVideoList.setVideoCommendCount(TextUtils.isEmpty(data.getComment_count())?"0":data.getComment_count());
                userLookVideoList.setVideoShareCount(TextUtils.isEmpty(data.getShare_times())?"0":data.getShare_times());
                userLookVideoList.setUserId(data.getUser_id());
                userLookVideoList.setVideoId(data.getVideo_id());
                userLookVideoList.setVideoCover(data.getCover());
                userLookVideoList.setUploadTime(data.getAdd_time());
                userLookVideoList.setAddTime(System.currentTimeMillis());
                userLookVideoList.setIs_interest(data.getIs_interest());
                userLookVideoList.setIs_follow(data.getIs_follow());
                userLookVideoList.setVideoPath(data.getPath());
                userLookVideoList.setVideoPlayerCount(TextUtils.isEmpty(data.getPlay_times())?"0":data.getPlay_times());
                userLookVideoList.setVideoType(TextUtils.isEmpty(data.getType())?"2":data.getType());
                ApplicationManager.getInstance().getUserPlayerDB().insertNewPlayerHistoryOfObject(userLookVideoList);
            }
        }.start();
    }


    /**
     * 显示加载中
     */
    protected void showLoadingView(String message){

        if(bindingView.recyerView.getVisibility()!=View.GONE){
            bindingView.recyerView.setVisibility(View.GONE);
        }
        if(null!=bindingView.llErrorView&&bindingView.llErrorView.getVisibility()!=View.GONE){
            bindingView.llErrorView.setVisibility(View.GONE);
        }

        if(null!=bindingView.llLoadingView&&bindingView.llLoadingView.getVisibility()!=View.VISIBLE){
            bindingView.llLoadingView.setVisibility(View.VISIBLE);
        }

        if(null!=mAnimationDrawable&&!AuthorDetailsActivity.this.isFinishing()&&!mAnimationDrawable.isRunning()){
            mAnimationDrawable.start();
        }
    }


    /**
     * 显示界面内容
     */
    protected void showContentView() {

        if(null!=mAnimationDrawable&&!AuthorDetailsActivity.this.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(null!=bindingView.llLoadingView&&bindingView.llLoadingView.getVisibility()!=View.GONE){
            bindingView.llLoadingView.setVisibility(View.GONE);
        }

        if(null!=bindingView.llErrorView&&bindingView.llErrorView.getVisibility()!=View.GONE){
            bindingView.llErrorView.setVisibility(View.GONE);
        }

        if(bindingView.recyerView.getVisibility()!=View.VISIBLE){
            bindingView.recyerView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 显示加载失败
     */
    protected void showLoadingErrorView() {


        if(null!=mAnimationDrawable&&!AuthorDetailsActivity.this.isFinishing()&&mAnimationDrawable.isRunning()){
            mAnimationDrawable.stop();
        }

        if(null!=bindingView.llLoadingView&&bindingView.llLoadingView.getVisibility()!=View.GONE){
            bindingView.llLoadingView.setVisibility(View.GONE);
        }

        if(bindingView.recyerView.getVisibility()!=View.GONE){
            bindingView.recyerView.setVisibility(View.GONE);
        }

        if(null!=bindingView.llErrorView&&bindingView.llErrorView.getVisibility()!=View.VISIBLE){
            bindingView.llErrorView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 订阅播放结果，以刷新界面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChangingViewEvent event) {
        if(null!=event&&Constant.FRAGMENT_TYPE_AUTHOE_CORE!=event.getFragmentType())return;
        final int poistion = event.getPoistion();
        mPage=event.getPage();
        List<FollowVideoList.DataBean.ListsBean> listsBeanList = event.getListsBeanList();
        if(null!=mVideoListAdapter){
            mVideoListAdapter.setNewData(listsBeanList);
            if(null!=listsBeanList&&listsBeanList.size()>0){
                bindingView.recyerView.post(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.recyerView.scrollToPosition(poistion);
                    }
                });
            }
        }
    }
}
