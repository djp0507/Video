package com.video.newqu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import com.google.gson.Gson;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.AutoVideoListAdapter;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityVideoListBinding;
import com.video.newqu.holder.VideoItem;
import com.video.newqu.holder.VideoListViewHolder;
import com.video.newqu.listener.ShareFinlishListener;
import com.video.newqu.listener.TopicClickListener;
import com.video.newqu.listener.VideoOnItemClickListener;
import com.video.newqu.manager.StatusBarManager;
import com.video.newqu.ui.contract.FollowContract;
import com.video.newqu.ui.presenter.FollowPresenter;
import com.video.newqu.util.AnimationUtil;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
import com.volokh.danylo.visibility_utils.calculator.DefaultSingleItemCalculatorCallback;
import com.volokh.danylo.visibility_utils.calculator.ListItemsVisibilityCalculator;
import com.volokh.danylo.visibility_utils.calculator.SingleListViewItemActiveCalculator;
import com.volokh.danylo.visibility_utils.scroll_utils.RecyclerViewItemPositionGetter;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import com.xinqu.videoplayer.XinQuVideoPlayer;


/**
 * TinyHung@outlook.com
 * 2017/5/24 11:50
 * 热门界面而来，更多推荐
 */

public class RecomVideoListActivity extends BaseActivity<ActivityVideoListBinding> implements VideoItem.ItemCallback,VideoOnItemClickListener, FollowContract.View ,TopicClickListener, BaseQuickAdapter.RequestLoadMoreListener, ShareFinlishListener {

    private List<FollowVideoList.DataBean.ListsBean> mListsBeanList;
    private AutoVideoListAdapter mAutoVideoListAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private FollowPresenter mFollowPresenter;
    private int mPage=0;
    private int mPageSize=10;
    private List<VideoItem> mVideoItemList=new ArrayList<>();
    private List<VideoItem> mCopyVideoItemList=new ArrayList<>();
    private RecyclerViewItemPositionGetter mItemsPositionGetter;
    private ListItemsVisibilityCalculator mListItemVisibilityCalculator;
    private int mPoistion;


    /**
     * 入口
     * @param context
     * @param position
     */
    public static void start(Context context, String json, int position,int mPage) {
        Intent intent=new Intent(context,RecomVideoListActivity.class);
        intent.putExtra("position",position);
        intent.putExtra("page",mPage);
        intent.putExtra("video_list_json",json);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        findViewById(R.id.view_state_bar).setVisibility(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?View.GONE:View.VISIBLE);
        StatusBarManager.getInstance().init(this,  CommonUtils.getColor(R.color.white), 0,true);
        mFollowPresenter = new FollowPresenter(this);
        mFollowPresenter.attachView(this);
        initAdapter();
        initIntent();
        if (null == mListsBeanList || mListsBeanList.size() <= 0) {
            ToastUtils.shoCenterToast("错误--原因:数据为空");
            finish();
        } else {
            upDataNewDataAdapter();
            //第一次使用弹出使用提示
            if(Utils.getVersionCode()!= SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_VIDEO_LIST_CODE)&&TextUtils.equals("com.video.newqu.ui.activity.RecomVideoListActivity", SystemUtils.getTopActivity())){
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
                        SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_VIDEO_LIST_CODE,Utils.getVersionCode());
                    }
                },1000);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }



    /**
     * 获取传递过来的数据
     */
    private void initIntent() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("video_list_json");
        mPoistion = intent.getIntExtra("position", 0);
        mPage = intent.getIntExtra("page", 0);
        mListsBeanList=new Gson().fromJson(json,FollowVideoList.class).getData().getLists();
    }



    @Override
    public void initViews() {

        bindingView.tvTitle.setText("热门");
        bindingView.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadVideoList();
            }
        });
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        createItemList();
        mLinearLayoutManager = new LinearLayoutManager(RecomVideoListActivity.this);
        mLinearLayoutManager.setAutoMeasureEnabled(true);
        bindingView.recyerView.setLayoutManager(mLinearLayoutManager);
        bindingView.recyerView.setHasFixedSize(false);
        mAutoVideoListAdapter = new AutoVideoListAdapter(mCopyVideoItemList,mListsBeanList, AnimationUtil.followAnimation());
        mAutoVideoListAdapter.setOnLoadMoreListener(this);
        bindingView.recyerView.setAdapter(mAutoVideoListAdapter);

        //初始化手势滑动
        bindingView.recyerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //松手后调用
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                if(null==mCopyVideoItemList||null==bindingView) return;
                if(scrollState == RecyclerView.SCROLL_STATE_IDLE && !mCopyVideoItemList.isEmpty()){

                    if(null!=bindingView.tvTipsMessage&&bindingView.tvTipsMessage.getVisibility()==View.VISIBLE){
                        bindingView.tvTipsMessage.setVisibility(View.GONE);
                    }

                    if(null!=mListItemVisibilityCalculator){
                        mListItemVisibilityCalculator.onScrollStateIdle(
                                mItemsPositionGetter,
                                mLinearLayoutManager.findFirstVisibleItemPosition(),
                                mLinearLayoutManager.findLastVisibleItemPosition());
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
        mItemsPositionGetter = new RecyclerViewItemPositionGetter(mLinearLayoutManager, bindingView.recyerView);
    }


    /**
     * 填充管理滑动的列表Item对象到数组，每次初始化或者刷新必须调用
     */
    private void createItemList() {

        if(null!=mCopyVideoItemList){
            mCopyVideoItemList.clear();
        }
        if(null!=mListsBeanList&&mListsBeanList.size()>0){
            for (int i = 0; i<mListsBeanList.size(); i++) {
                mCopyVideoItemList.add(new VideoItem(RecomVideoListActivity.this,RecomVideoListActivity.this,RecomVideoListActivity.this,RecomVideoListActivity.this,0));
            }
        }
        mListItemVisibilityCalculator = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mCopyVideoItemList);
    }


    /**
     * 往滑动控制器中添加Item,每次添加数据时候添加
     */
    private void addItemList() {
        if(null!=mListsBeanList&&mListsBeanList.size()>0){
            if(null!=mVideoItemList){
                mVideoItemList.clear();
            }
            for (int i = 0; i < mListsBeanList.size(); i++) {
                mVideoItemList.add(new VideoItem(RecomVideoListActivity.this,RecomVideoListActivity.this,RecomVideoListActivity.this,RecomVideoListActivity.this,0));
            }
        }
        mListItemVisibilityCalculator = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), mCopyVideoItemList);
    }


    /**
     * 刷新适配器所有数据
     */
    private void upDataNewDataAdapter() {

        XinQuVideoPlayer.releaseAllVideos();
        createItemList();
        mAutoVideoListAdapter.setNewListData(mCopyVideoItemList,mListsBeanList);
        if(0!=mPoistion){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    //最后一个
                    if(mPoistion==mListsBeanList.size()-1){
                        mLinearLayoutManager.scrollToPosition(mPoistion);
                        //其他的
                    }else{
                        mLinearLayoutManager.scrollToPositionWithOffset(mPoistion,0);
                    }
                    playerVideo(mPoistion);
                    mPoistion=0;
                }
            });
        }else{
            playerVideo(mPoistion);
        }
    }

    /**
     * 一进来直接播放
     * @param poistion
     */
    private void playerVideo(final int poistion) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null==mLinearLayoutManager||null==bindingView.recyerView) return;
                if(!RecomVideoListActivity.this.isFinishing()){
                    //处理一进来自动播放
                    int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                    if(poistion-firstVisibleItemPosition>=0){
                        View childAt = bindingView.recyerView.getChildAt(poistion-firstVisibleItemPosition);
                        if(null!=childAt){
                            VideoListViewHolder childViewHolder = (VideoListViewHolder) bindingView.recyerView.getChildViewHolder(childAt);
                            if(null!=childViewHolder){
                                if(null!=childViewHolder.video_player&& ConfigSet.getInstance().isWifiAuthPlayer()&&Utils.isCheckNetwork()){
                                    if(!RecomVideoListActivity.this.isFinishing()) childViewHolder.video_player.startVideo();
                                }
                            }
                        }
                    }
                }
            }
        },1200);
    }


    /**
     * 为适配器添加新的数据
     */
    private void updataAddDataAdapter() {
        addItemList();
        mAutoVideoListAdapter.addListData(mVideoItemList,mListsBeanList);
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {
        mAutoVideoListAdapter.setEnableLoadMore(true);
        loadVideoList();
    }


    /**
     * 加载数据
     */
    private void loadVideoList() {
        mPage++;
        //加载热门数据的时候，如果当前是未登录用户，则使用游客ID获取数据
        mFollowPresenter.getHotVideoList(mPage+"",VideoApplication.getLoginUserID());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if(null!=mListsBeanList) mListsBeanList.clear(); mListsBeanList=null;
//        if(null!=mVideoItemList) mVideoItemList.clear(); mVideoItemList=null;
//        if(null!=mCopyVideoItemList) mCopyVideoItemList.clear(); mCopyVideoItemList=null;mPoistion=0;mPage=0;
//        mAutoVideoListAdapter=null;mLinearLayoutManager=null;mFollowPresenter=null;mItemsPositionGetter=null;mListItemVisibilityCalculator=null;
        Runtime.getRuntime().gc();
    }

    @Override
    public void initData() {
        if(null!=mAutoVideoListAdapter){
            mAutoVideoListAdapter=null;
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        XinQuVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onBackPressed() {
        if(XinQuVideoPlayer.backPress()){
            return;
        }
        super.onBackPressed();
    }


    @Override
    public void onDeactivate(View currentView, int position) {

    }

    @Override
    public void onActiveViewChangedActive(View newActiveView, int newActiveViewPosition) {

    }

    /**
     * 登录方法
     */
    public void login(){
        Intent intent=new Intent(RecomVideoListActivity.this,LoginGroupActivity.class);
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
                Logger.d("RecomVideoListActivity","登录成功");
                //登录成功,刷新子界面
                if(booleanExtra){
                    //刷新首页
                    VideoApplication.isLogin=true;
//                    updataView();
                }
            }
        }
    }

    /**
     * 刷新界面
     */
    private void updataView() {
        mPage=0;
        loadVideoList();
    }



    //==================================获取网络数据结果处理的回调=====================================


    /**
     * 显示菜单
     * @param data
     */
    private void openMenu(final FollowVideoList.DataBean.ListsBean data) {

    }



    @Override
    public void showloadFollowVideoList(FollowVideoList data) {

    }


    @Override
    public void showloadFollowListEmptry(String response) {

    }

    @Override
    public void showloadFollowListError() {

    }


    /**
     * 加载热门视频列表成功
     * @param data
     */
    @Override
    public void showHotVideoList(FollowVideoList data) {

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mAutoVideoListAdapter.loadMoreComplete();//加载完成
            }
        });
        //替换为全新数据
        if(1==mPage){
            if(null!=mListsBeanList){
                mListsBeanList.clear();
            }
            mListsBeanList=data.getData().getLists();
            bindingView.swiperefreshLayout.setRefreshing(false,mListsBeanList.size());
            upDataNewDataAdapter();
            //添加数据
        }else{
            mListsBeanList=data.getData().getLists();
            updataAddDataAdapter();
        }
    }

    /**
     * 加载热门视频列表为空
     * @param data
     */
    @Override
    public void showHotVideoListEmpty(String data) {

        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mAutoVideoListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //还原当前的页数
        if (mPage > 1) {
            mPage--;
        }
    }

    /**
     * 加载热门视频列表失败
     * @param data
     */
    @Override
    public void showHotVideoListError(String data) {

        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mAutoVideoListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });

        if(1==mPage){
            bindingView.swiperefreshLayout.setRefreshing(false,-1);
        }

        //还原当前的页数
        if (mPage > 1) {
            mPage--;
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
            if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.REPORT_USER_RESULT)){
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
            if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.REPORT_USER_RESULT)){
                showFinlishToast(null,null,jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }


    /**
     * 举报视频
     * @param video_id
     */
    private void onReportVideo(String video_id) {
        showProgressDialog("举报视频中...",true);
        mFollowPresenter.onReportVideo(VideoApplication.getLoginUserID(),video_id);
    }

    /**
     * 举报用户
     * @param accuseUserId
     */
    private void onReportUser(String accuseUserId) {
        showProgressDialog("举报用户中...",true);
        mFollowPresenter.onReportUser(VideoApplication.getLoginUserID(),accuseUserId);
    }


    /**
     * 视频详情界面
     * @param listsBean
     * @param isShowKeyBoard
     */
    private void startVideoDetails(FollowVideoList.DataBean.ListsBean listsBean, boolean isShowKeyBoard) {
        //最后一个参数是否打开键盘
        VideoDetailsActivity.start(RecomVideoListActivity.this,listsBean.getVideo_id(),listsBean.getUser_id(),false);
    }

    /**
     * 分享回调
     * @param videoID
     * @param newShareCount
     */
    @Override
    public void shareFinlish(String videoID, String newShareCount) {
        if(null==mAutoVideoListAdapter) return;
        List<FollowVideoList.DataBean.ListsBean> listsBeanList = mAutoVideoListAdapter.getVideoList();
        if(null!=listsBeanList&&listsBeanList.size()>0){
            int poistion=0;
            for (int i = 0; i < listsBeanList.size(); i++) {
                FollowVideoList.DataBean.ListsBean listsBean = listsBeanList.get(i);
                if(TextUtils.equals(videoID,listsBean.getVideo_id())){
                    listsBean.setShare_times(newShareCount);
                    poistion=i;
                    break;
                }
            }
            mAutoVideoListAdapter.notifyItemChanged(poistion);
        }
    }


    //=========================================UI操作回调============================================

    /**
     * 点击了条目
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        try {
            List<FollowVideoList.DataBean.ListsBean> videoList = mAutoVideoListAdapter.getVideoList();
            if(null!=videoList&&videoList.size()>0){
                FollowVideoList.DataBean.ListsBean listsBean = videoList.get(position);
                if(null!=listsBean){
                    startVideoDetails(listsBean, false);
                }
            }
        }catch (Exception e){

        }
    }

    /**
     * 收藏
     * @param position
     * @param data
     */
    @Override
    public void onItemPrice(int position, FollowVideoList.DataBean.ListsBean data) {
        //去登录
        login();
    }

    /**
     * 评论
     * @param position
     * @param data
     * @param isShowKeyBoard
     */
    @Override
    public void onItemComent(int position, FollowVideoList.DataBean.ListsBean data, boolean isShowKeyBoard) {

        if(null!=data){
            startVideoDetails(data, isShowKeyBoard);
        }
    }

    /**
     * 分享
     * @param position
     * @param data
     */
    @Override
    public void onItemShare(int position, FollowVideoList.DataBean.ListsBean data) {

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }
        XinQuVideoPlayer.releaseAllVideos();

        if(null==VideoApplication.getInstance().getUserData()){
            login();
        }else{
            ShareInfo shareInfo=new ShareInfo();
            shareInfo.setDesp(TextUtils.isEmpty(data.getDesp())?"一起来看 "+data.getNickname()+" 的新趣视频":data.getDesp());
            shareInfo.setTitle("一起来看 "+data.getNickname()+" 的新趣视频!");
            shareInfo.setUrl(data.getPath());
            shareInfo.setVideoID(data.getVideo_id());
            shareInfo.setImageLogo(data.getCover());
            onShare(shareInfo,this);
        }
    }

    /**
     * 菜单
     * @param position
     * @param data
     */
    @Override
    public void onItemMenu(int position, FollowVideoList.DataBean.ListsBean data) {
        openMenu(data);
    }

    /**
     * 用户主页
     * @param position
     * @param data
     */
    @Override
    public void onItemVisitOtherHome(int position, FollowVideoList.DataBean.ListsBean data) {
        AuthorDetailsActivity.start(RecomVideoListActivity.this, data.getUser_id());
    }

    /**
     * 关注
     * @param position
     * @param data
     */
    @Override
    public void onItemFollow(int position, FollowVideoList.DataBean.ListsBean data) {
        //去登录
        if(null==VideoApplication.getInstance().getUserData()) {
           login();
        }
    }

    /**
     * 对子留言进行评论
     * @param position
     * @param data
     * @param isShowKeyBoard
     */
    @Override
    public void onItemChildComent(int position, FollowVideoList.DataBean.ListsBean data, boolean isShowKeyBoard) {
        startVideoDetails(data,isShowKeyBoard);
    }



    @Override
    public void onTopicClick(String topic) {
        TopicVideoListActivity.start(RecomVideoListActivity.this, topic,null,null,0);
    }

    @Override
    public void onUrlClick(String url) {

    }

    @Override
    public void onAuthoeClick(String author) {

    }
}
