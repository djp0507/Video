package com.video.newqu.ui.fragment;

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
import android.support.v7.app.AppCompatActivity;
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
import com.video.newqu.base.BaseAuthorFragment;
import com.video.newqu.bean.ChangingViewEvent;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.MineUserInfo;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityAuthorDetailsBinding;
import com.video.newqu.databinding.MineAuthorRecylerviewEmptyLayoutBinding;
import com.video.newqu.event.VerticalPlayMessageEvent;
import com.video.newqu.listener.OnUserVideoListener;
import com.video.newqu.model.RecyclerViewSpacesItem;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.activity.MediaSingerImagePreviewActivity;
import com.video.newqu.ui.activity.VerticalVideoPlayActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.contract.AuthorDetailContract;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.AuthorDetailPresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ScreenUtils;
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
import java.lang.ref.WeakReference;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/12/5.
 * 垂直的滑动列表用户中心界面
 */

public class VerticalAuthorDetailsFragment extends BaseAuthorFragment<ActivityAuthorDetailsBinding> implements AuthorDetailContract.View, BaseQuickAdapter.RequestLoadMoreListener
        ,OnUserVideoListener {

    public static final String TAG=VerticalAuthorDetailsFragment.class.getSimpleName();
    private UserVideoListAdapter mVideoListAdapter;
    private AuthorDetailPresenter mAuthorDetailPresenter;
    public String mAuthorID;//用户ID
    private MineUserInfo.DataBean.InfoBean mInfoBean;//用户基本信息
    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList=null;
    private int mPage=0;
    private int mPageSize=10;
    private int mHeaderViewHeight;
    private WeakReference<VerticalVideoPlayActivity> mActivityWeakReference;
    private WeakReference<AnimationDrawable> mDrawableWeakReference;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        VerticalVideoPlayActivity activity= (VerticalVideoPlayActivity) context;
        mActivityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected void initViews() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    //点击别人，弹出举报弹窗，点击自己无选项
                    case R.id.iv_user_icon:
                        if(null==mInfoBean) return;
                        MediaSingerImagePreviewActivity.start(getActivity(),mInfoBean.getLogo(),bindingView.ivUserIcon);
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
                    //刷新
                    case R.id.ll_error_view:
                        if(null==mInfoBean){
                            showLoadingView();
                            loadUserInfo();//加载用户数据
                        }else{
                            showLoadingView();
                            mPage=0;
                            loadVideoList();//直接加载用户发布的视频
                        }
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
        bindingView.llErrorView.setOnClickListener(onClickListener);
        baseBinding.ivMenu.setVisibility(null!=mAuthorID&&null!=VideoApplication.getInstance().getUserData()&&!TextUtils.isEmpty(mAuthorID)&&TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())?View.GONE:View.VISIBLE);

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
    public int getLayoutId() {
        return R.layout.activity_author_details;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        mAuthorDetailPresenter = new AuthorDetailPresenter(getActivity());
        mAuthorDetailPresenter.attachView(this);
        AnimationDrawable animationDrawable = (AnimationDrawable) bindingView.ivLoadingIcon.getDrawable();
        mDrawableWeakReference = new WeakReference<AnimationDrawable>(animationDrawable);
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(null!=bindingView&&null!=mVideoListAdapter&&!TextUtils.isEmpty(mAuthorID)){
            if(null==mInfoBean&&null==mListsBeanList){

                mInfoBean= (MineUserInfo.DataBean.InfoBean)   ApplicationManager.getInstance().getCacheExample().getAsObject(mAuthorID);
                if(null!=mInfoBean){
                    initUserData();
                }
                if(null==mVideoListAdapter.getData()||mVideoListAdapter.getData().size()<=0){
                    mListsBeanList= (List<FollowVideoList.DataBean.ListsBean>)   ApplicationManager.getInstance().getCacheExample().getAsObject(mAuthorID+"_video_list");
                    if(null==mListsBeanList) mListsBeanList=new ArrayList<>();
                    if(null!=mListsBeanList&&mListsBeanList.size()>0){

                        showContentView();
                        if(null!=mVideoListAdapter){
                            mVideoListAdapter.setNewData(mListsBeanList);
                        }
                    }else{
                        showLoadingView();
                    }
                }
                loadUserInfo();
            }
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),3,GridLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new RecyclerViewSpacesItem(ScreenUtils.dpToPxInt(0.9f)));
        bindingView.recyerView.setHasFixedSize(true);
        mVideoListAdapter = new UserVideoListAdapter(null,3,this);
        MineAuthorRecylerviewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.mine_author_recylerview_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
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



    @Override
    public void onLoadMoreRequested() {
        if(null!=mVideoListAdapter){
            if(null!=mListsBeanList&&mListsBeanList.size()>=10){
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
    }

    /**
     * 初始化用户信息
     */
    private void initUserData() {
        if(null==mInfoBean){
            baseBinding.tvTitleUserName.setText("火星人");
            bindingView.tvSubtitleUserName.setText("火星人");
            bindingView.tvUserDesp.setText("宝宝暂时没有个性签名");
            bindingView.tvUserGrade.setText("Lv1");
            bindingView.tvFansCount.setText("0粉丝");
            bindingView.tvFollowCount.setText("0关注");
            bindingView.userVideoCount.setText("0作品");
            bindingView.ivUserSex.setImageResource(R.drawable.iv_icon_sex_women);
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
            bindingView.tvFansCount.setText((TextUtils.isEmpty(mInfoBean.getFans())?"0": Utils.changeNumberFormString(mInfoBean.getFans()))+"粉丝");
            bindingView.tvFollowCount.setText((TextUtils.isEmpty(mInfoBean.getFollows())?"0":mInfoBean.getFollows())+"关注");
            bindingView.userVideoCount.setText((TextUtils.isEmpty(mInfoBean.getVideo_count())?"0":mInfoBean.getVideo_count())+"作品");
            bindingView.ivUserSex.setImageResource(TextUtils.isEmpty(mInfoBean.getGender())?R.drawable.iv_icon_sex_women:TextUtils.equals("女",mInfoBean.getGender())?R.drawable.iv_icon_sex_women:R.drawable.iv_icon_sex_man);
        }
        switchIsFollow();
        //设置背景封面和用户头像
        if(!getActivity().isFinishing()){
            //设置背景封面
            Glide.with(this)
                    .load(null==mInfoBean?R.drawable.iv_mine_bg:TextUtils.isEmpty(mInfoBean.getImage_bg())?R.drawable.iv_mine_bg:Utils.imageUrlChange(mInfoBean.getImage_bg()))
                    .error(R.drawable.iv_mine_bg)
                    .placeholder(R.drawable.iv_mine_bg)
                    .crossFade()//渐变
                    .thumbnail(0.1f)
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .skipMemoryCache(true)//跳过内存缓存
                    .into(bindingView.ivHeaderBg);
            //作者头像
            Glide.with(this)
                    .load(null==mInfoBean?R.drawable.iv_mine:TextUtils.isEmpty(mInfoBean.getLogo())?R.drawable.iv_mine:Utils.imageUrlChange(mInfoBean.getLogo()))
                    .error(R.drawable.iv_mine)
                    .placeholder(R.drawable.iv_mine)
                    .crossFade()//渐变
                    .thumbnail(0.1f)
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .centerCrop()//中心点缩放
                    .skipMemoryCache(true)//跳过内存缓存
                    .transform(new GlideCircleTransform(getActivity()))
                    .into(bindingView.ivUserIcon);
        }
    }

    /**
     * 是否对该作者已关注
     */
    private void switchIsFollow() {
        if(null==mInfoBean){
            bindingView.reAdd.setBackgroundResource(R.drawable.text_bg_round_app_style_pressed_true_selector);
            bindingView.ivAdd.setImageResource(R.drawable.iv_follow_true_white);
            bindingView.tvAdd.setText("关 注");
            bindingView.tvAdd.setTextColor(CommonUtils.getColor(R.color.white));
        }else{
            bindingView.reAdd.setBackgroundResource(R.drawable.text_bg_round_app_style_pressed_true_selector);
            bindingView.ivAdd.setImageResource(1==mInfoBean.getIs_follow()?R.drawable.iv_follow_true_white:R.drawable.ic_min_add_white);
            bindingView.tvAdd.setText(TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())?"关 注":1==mInfoBean.getIs_follow()?"已关注":"关 注");
            bindingView.tvAdd.setTextColor(CommonUtils.getColor(R.color.white));
        }
        if(!TextUtils.isEmpty(mAuthorID)&&TextUtils.equals(mAuthorID,VideoApplication.getLoginUserID())){
            bindingView.reAdd.setBackgroundResource(R.drawable.bg_item_follow_gray_transpent_selector);
            bindingView.ivAdd.setImageResource(R.drawable.ic_min_add_gray);
            bindingView.tvAdd.setTextColor(CommonUtils.getColor(R.color.common_h2));
        }
    }


    /**
     * 加载用户信息
     */
    private void loadUserInfo() {
        if(null!=mAuthorDetailPresenter&&!mAuthorDetailPresenter.isLoadUserInfo()){
            mAuthorDetailPresenter.getUserInfo(mAuthorID);
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
     * 分享用户的主页
     */
    private void shareMineHome() {
        if(!TextUtils.isEmpty(mAuthorID)&&null!=mActivityWeakReference){
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
            if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()){
                mActivityWeakReference.get().shareMineHome(shareInfo);
            }
        }
    }


    /**
     * 显示详细信息
     */
    private void showUserDetailsDataDialog() {

        if(TextUtils.isEmpty(mAuthorID)) return;
        if(null==mInfoBean) return;

        if(null!=getActivity()&&!getActivity().isFinishing()){
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
                videoDetailsMenu7.setTextColor("#FFFF5000");
                videoDetailsMenu7.setItemName("举报此用户");
                list.add(videoDetailsMenu7);
            }
            CommonMenuDialog commonMenuDialog =new CommonMenuDialog((AppCompatActivity) getActivity());
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
                                if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()){
                                    ToastUtils.shoCenterToast("该操作需要登录！");
                                    mActivityWeakReference.get().login();
                                }
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
            if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()){
                mActivityWeakReference.get().login();
            }
        }
    }


    private void onBackPressed(){
        if(null!=mActivityWeakReference&&null!=mActivityWeakReference.get()&&!mActivityWeakReference.get().isFinishing()){
            mActivityWeakReference.get().setCureenItem(0);
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

        CommonMenuDialog commonMenuDialog =new CommonMenuDialog((AppCompatActivity) getActivity());
        commonMenuDialog.setData(list);
        commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
            @Override
            public void onItemClick(int itemID) {
                //取消关注
                switch (itemID) {
                    case 1:
                        if(!Utils.isCheckNetwork()){
                            ToastUtils.shoCenterToast("没有网络连接");
                            return;
                        }
                        onFollow();
                        break;
                }
            }
        });
        commonMenuDialog.show();
    }



    /**
     * 关注用户
     */
    private void onFollow() {
        if(null!=mAuthorDetailPresenter&&!mAuthorDetailPresenter.isFollow()){
            showProgressDialog("关注中，请稍后...",true);
            mAuthorDetailPresenter.onFollowUser(mInfoBean.getId(), VideoApplication.getLoginUserID());
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
        Intent intent=new Intent(getActivity(), ContentFragmentActivity.class);
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

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    /**
     * 订阅播放结果，以刷新界面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ChangingViewEvent event) {
        if(null!=event&&Constant.FRAGMENT_TYPE_VERTICAL_AUTHOR!=event.getFragmentType())return;
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

    /**
     * 接收播放器界面的通知,还原所有数据
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(VerticalPlayMessageEvent event) {
        if(null!=event){
            this.mAuthorID=event.getAuthorID();
            mInfoBean=null;
            if(null!=mListsBeanList) mListsBeanList.clear();
            mListsBeanList=null;
            mPage=0;//还原页数
            initUserData();//还原所有View数据为初始状态
            if(null!=mVideoListAdapter){
                mVideoListAdapter.setNewData(mListsBeanList);//还原视频列表为空
            }
            showLoadingView();
            //最后设置用户昵称、头像基本信息
            baseBinding.tvTitleUserName.setText(TextUtils.isEmpty(event.getUserName()) ? "火星人" : event.getUserName());
            bindingView.tvSubtitleUserName.setText(TextUtils.isEmpty(event.getUserName()) ? "火星人" : event.getUserName());
            //作者头像
            Glide.with(this)
                    .load(TextUtils.isEmpty(event.getUserCover())?R.drawable.iv_mine:Utils.imageUrlChange(event.getUserCover()))
                    .error(R.drawable.iv_mine)
                    .placeholder(R.drawable.iv_mine)
                    .crossFade()//渐变
                    .thumbnail(0.1f)
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .centerCrop()//中心点缩放
                    .skipMemoryCache(true)//跳过内存缓存
                    .transform(new GlideCircleTransform(getActivity()))
                    .into(bindingView.ivUserIcon);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(null!=mAuthorDetailPresenter){
            mAuthorDetailPresenter.detachView();
        }
        if(null!=mListsBeanList){
            mListsBeanList.clear();
        }
        if(null!=mActivityWeakReference){
            mActivityWeakReference.clear();
        }
        if(null!=mDrawableWeakReference){
            mDrawableWeakReference.clear();
        }
        mListsBeanList=null;mVideoListAdapter=null;mAuthorID=null;mInfoBean=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    //======================================点击事件的监听============================================

    @Override
    public void onItemClick(int poistion) {
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

                        if(!TextUtils.isEmpty(json)) {
                            Intent intent=new Intent(getActivity(),VerticalVideoPlayActivity.class);
                            intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.FRAGMENT_TYPE_VERTICAL_AUTHOR);
                            intent.putExtra(Constant.KEY_POISTION,poistion);
                            intent.putExtra(Constant.KEY_PAGE,mPage);
                            intent.putExtra(Constant.KEY_AUTHOE_ID,VideoApplication.getLoginUserID());
                            intent.putExtra(Constant.KEY_JSON,json);
                            startActivity(intent);
                        }
                    }catch (Exception e){

                    }
                    //单个
                }else{
                    FollowVideoList.DataBean.ListsBean listsBean = data.get(poistion);
                    if(null!=listsBean&&!TextUtils.isEmpty(listsBean.getVideo_id())){
                        saveLocationHistoryList(listsBean);
                        VideoDetailsActivity.start(getActivity(),listsBean.getVideo_id(),listsBean.getUser_id(),false);
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


    //========================================网络请求回调===========================================

    @Override
    public void showErrorView() {
        if(null==mInfoBean){
            showLoadingErrorView();
        }
    }

    @Override
    public void complete() {

    }

    /**
     * 显示用户基本信息
     * @param data
     */
    @Override
    public void showUserInfo(MineUserInfo data,String userID) {
        //防止因为网络问题加载延缓导致数据与用户比匹配问题
        if(null!=mAuthorID&&!TextUtils.equals(mAuthorID,userID)){
            return;
        }
        mInfoBean = data.getData().getInfo();
        if(null!=mInfoBean)   ApplicationManager.getInstance().getCacheExample().remove(mAuthorID);
        ApplicationManager.getInstance().getCacheExample().put(mAuthorID,mInfoBean);
        String videoCount = TextUtils.isEmpty(mInfoBean.getVideo_count()) ? "0" : mInfoBean.getVideo_count();
        bindingView.userVideoCount.setText(videoCount+"作品");
        initUserData();
        mPage=0;
        loadVideoList();
    }

    /**
     * 关注用户结果
     * @param isFollow
     * @param text
     */
    @Override
    public void showFollowUser(Boolean isFollow, String text) {
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
     * 举报用户结果
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
     * 加载用户视频列表成功
     * @param data
     */
    @Override
    public void showUpLoadVideoList(FollowVideoList data) {
        showContentView();
        if(null!=mVideoListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mVideoListAdapter.loadMoreComplete();//加载完成
                }
            });
        }
        //替换为全新数据
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            mListsBeanList=data.getData().getLists();
            ApplicationManager.getInstance().getCacheExample().remove(mAuthorID+"_video_list");
            ApplicationManager.getInstance().getCacheExample().put(mAuthorID+"_video_list", (Serializable) mListsBeanList, Constant.CACHE_TIME);
            if(null!=mVideoListAdapter) mVideoListAdapter.setNewData(mListsBeanList);
            //添加数据
        }else{
            mListsBeanList=data.getData().getLists();
            if(null!=mVideoListAdapter) mVideoListAdapter.addData(mListsBeanList);
        }
    }

    /**
     * 加载用户视频列表为空
     * @param data
     */
    @Override
    public void showUpLoadVideoListEmpty(String data) {
        showContentView();
        if(null!=mVideoListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mVideoListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
        }

        //如果当前用户在第一页的时候获取视频为空，表示该用户没有关注用户
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            ApplicationManager.getInstance().getCacheExample().remove(mAuthorID+"_video_list");
            ApplicationManager.getInstance().getCacheExample().put(mAuthorID+"_video_list", (Serializable) mListsBeanList, Constant.CACHE_TIME);
            if(null!=mVideoListAdapter) mVideoListAdapter.setNewData(mListsBeanList);
        }
        //还原当前的页数
        if (mPage > 1) {
            mPage--;
        }
    }

    /**
     * 加载用户视频列表失败
     * @param data
     */
    @Override
    public void showUpLoadVideoListError(String data) {
        if(null!=mVideoListAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mVideoListAdapter.loadMoreFail();
                }
            });

        }

        if(1==mPage&&null==mListsBeanList||mListsBeanList.size()<=0){
            showLoadingErrorView();
        }

        if(mPage>0){
            mPage--;
        }
    }

    //===========================================界面状态============================================

    /**
     * 显示加载中
     */
    protected void showLoadingView(){
        if(bindingView.recyerView.getVisibility()!=View.GONE){
            bindingView.recyerView.setVisibility(View.GONE);
        }
        if(null!=bindingView.llErrorView&&bindingView.llErrorView.getVisibility()!=View.GONE){
            bindingView.llErrorView.setVisibility(View.GONE);
        }
        if(null!=bindingView.llLoadingView&&bindingView.llLoadingView.getVisibility()!=View.VISIBLE){
            bindingView.llLoadingView.setVisibility(View.VISIBLE);
        }
        if(null!=mDrawableWeakReference&&null!=mDrawableWeakReference.get()&&!getActivity().isFinishing()&&!mDrawableWeakReference.get().isRunning()){
            mDrawableWeakReference.get().start();
        }
    }


    /**
     * 显示界面内容
     */
    protected void showContentView() {
        if(null!=mDrawableWeakReference&&null!=mDrawableWeakReference.get()&&!getActivity().isFinishing()&&mDrawableWeakReference.get().isRunning()){
            mDrawableWeakReference.get().stop();
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

        if(null!=mDrawableWeakReference&&null!=mDrawableWeakReference.get()&&!getActivity().isFinishing()&&mDrawableWeakReference.get().isRunning()){
            mDrawableWeakReference.get().stop();
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
}
