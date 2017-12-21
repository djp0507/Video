package com.video.newqu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.google.gson.Gson;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.TopicAutoVideoListAdapter;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.bean.ShareInfo;
import com.video.newqu.bean.TopicVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.ConfigSet;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityVideoListBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.manager.StatusBarManager;
import com.video.newqu.holder.TopicVideoItem;
import com.video.newqu.holder.VideoListViewHolder;
import com.video.newqu.listener.ShareFinlishListener;
import com.video.newqu.listener.TopicClickListener;
import com.video.newqu.listener.TopicVideoOnItemClickListener;
import com.video.newqu.ui.contract.TopicVideoContract;
import com.video.newqu.ui.presenter.TopicVideoPresenter;
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
import com.volokh.danylo.visibility_utils.scroll_utils.ItemsPositionGetter;
import com.volokh.danylo.visibility_utils.scroll_utils.RecyclerViewItemPositionGetter;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.xinqu.videoplayer.XinQuVideoPlayer;

/**
 * TinyHung@outlook.com
 * 2017/6/27 15:55
 * 关于话题列表下的所有视频,两种情况，一种是携带一页前来，一种是携带话题Key而来
 */
public class TopicVideoListActivity extends BaseActivity<ActivityVideoListBinding> implements View.OnClickListener, TopicVideoContract.View,
        TopicVideoOnItemClickListener,TopicVideoItem.ItemCallback, BaseQuickAdapter.RequestLoadMoreListener,TopicClickListener,ShareFinlishListener {

    private String mTopicKey;
    private TopicVideoPresenter mTopicVideoPresenter;
    private int page=1;//默认是从第二页开始
    private int pageSize=5;
    private List<TopicVideoList.DataBean.VideoListBean> mTopicVideoList;
    private LinearLayoutManager mLinearLayoutManager;
    private List<TopicVideoItem> mVideoItemList=new ArrayList<>();
    private List<TopicVideoItem> copyVideoItemList=new ArrayList<>();
    private ListItemsVisibilityCalculator mListItemVisibilityCalculator;
    private ItemsPositionGetter mItemsPositionGetter;
    private TopicAutoVideoListAdapter mAutoVideoListAdapter;
    private int mPoistion;
    private String mVideoID;

    /**
     * 入口
     * @param context
     * @param topic KEY
     * @param videoID VIDEO_ID
     * @param dataBeanTopicjson JSON数组
     * @param poistion 要自动滚动到顶部的poistion
     */
    public static void start(Context context,  String topic,String videoID,String dataBeanTopicjson, int poistion) {
        Intent intent = new Intent(context, TopicVideoListActivity.class);
        intent.putExtra("topic",topic);
        intent.putExtra("topic_json",dataBeanTopicjson);
        intent.putExtra("video_id",videoID);
        intent.putExtra("poistion",poistion);
        context.startActivity(intent);
    }

    /**
     * 获取参数
     */
    private void initIntent() {
        Intent intent = getIntent();
        mTopicKey =Utils.slipTopic(intent.getStringExtra("topic")) ;

        if(TextUtils.isEmpty(mTopicKey)){
            ToastUtils.shoCenterToast("错误");
            finish();
        }
        bindingView.tvTitle.setText(TextUtils.isEmpty(mTopicKey)?"#话题#":"#"+mTopicKey+"#");
        String topicJson = intent.getStringExtra("topic_json");
        mPoistion = intent.getIntExtra("poistion", 0);
        mVideoID = intent.getStringExtra("video_id");
        if(!TextUtils.isEmpty(topicJson)){
            TopicVideoList topicVideoList = new Gson().fromJson(topicJson, TopicVideoList.class);
            if(null!=topicVideoList&&null!=topicVideoList.getData()&&null!=topicVideoList.getData().getVideo_list()){
                mTopicVideoList=topicVideoList.getData().getVideo_list();
            }
        }
    }

    @Override
    public void initViews() {
        bindingView.ivBack.setOnClickListener(this);

        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=0;
                loadTopicVideoList();
            }
        });
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingViews("精彩视频即将呈现...");
        page=0;
        loadTopicVideoList();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        findViewById(R.id.view_state_bar).setVisibility(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?View.GONE:View.VISIBLE);
        StatusBarManager.getInstance().init(this,  CommonUtils.getColor(R.color.white), 0,true);
        mTopicVideoPresenter = new TopicVideoPresenter(TopicVideoListActivity.this);
        mTopicVideoPresenter.attachView(this);
        showLoadingViews("精彩视频即将呈现...");
        initAdapter();
        initIntent();
        setData();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    private void setData() {

        if(!TopicVideoListActivity.this.isFinishing()){
            //携带的数组直接刷新
            if(null!=mTopicVideoList&&mTopicVideoList.size()>0){
                Log.d("TopicVideoListActivity", "setData: 携带的数组");
                upDataNewDataAdapter();

             //读取缓存\网络加载
            }else {

                mTopicVideoList= (List<TopicVideoList.DataBean.VideoListBean>)      ApplicationManager.getInstance().getCacheExample().getAsObject(mTopicKey);
                //读取缓存
                if(null!=mTopicVideoList&&mTopicVideoList.size()>0){
                    Log.d("TopicVideoListActivity", "setData: 缓存");
                    upDataNewDataAdapter();
                //从网络加载
                }else{
                    Log.d("TopicVideoListActivity", "setData: 网络加载的");
                    page=0;
                    loadTopicVideoList();
                }
            }
        }
    }

    /**
     * 根据ID滚动到指定的Poistion
     */
    private void smoveItemToPoistion() {

        Log.d("发现列表", " mVideoID="+mVideoID);
        if(!TextUtils.isEmpty(mVideoID)){
            if(null!=mTopicVideoList&&mTopicVideoList.size()>0){
                for (int i = 0; i < mTopicVideoList.size(); i++) {
                    TopicVideoList.DataBean.VideoListBean videoListBean = mTopicVideoList.get(i);
                    if(TextUtils.equals(mVideoID,videoListBean.getVideo_id())){
                        mPoistion=i;
                        break;
                    }
                }
            }
            mVideoID=null;
        }
        Log.d("发现列表", "smoveItemToPoistion: 对应poistion="+mPoistion);
        //为0不需要自动滚动
        if(0!=mPoistion){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    //最后一个
                    if(mPoistion==mTopicVideoList.size()-1){
                        mLinearLayoutManager.scrollToPosition(mPoistion);
                    //其他
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
                if(!TopicVideoListActivity.this.isFinishing()){
                    //处理一进来自动播放
                    int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();
                    if(poistion-firstVisibleItemPosition>=0){
                        View childAt = bindingView.recyerView.getChildAt(poistion-firstVisibleItemPosition);
                        Log.d("自动播放", "run 要播放的View===="+(poistion-firstVisibleItemPosition));
                        if(null!=childAt){
                            VideoListViewHolder childViewHolder = (VideoListViewHolder) bindingView.recyerView.getChildViewHolder(childAt);
                            if(null!=childViewHolder){
                                if(null!=childViewHolder.video_player&& ConfigSet.getInstance().isWifiAuthPlayer()&&Utils.isCheckNetwork()){
                                    childViewHolder.video_player.startVideo();
                                }
                            }
                        }
                    }
                }
            }
        },1200);
    }


    /**
     * 加载话题视频列表
     */
    private void loadTopicVideoList() {
        page++;
        mTopicVideoPresenter.getTopicVideoList(VideoApplication.getLoginUserID(),mTopicKey,page+"");
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {
        if(null!=mTopicVideoList&&mTopicVideoList.size()>=5){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mAutoVideoListAdapter.setEnableLoadMore(true);
            loadTopicVideoList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mAutoVideoListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
        }
    }


    /**
     * 初始化适配器
     */
    private void initAdapter() {
        createItemList();
        mLinearLayoutManager = new LinearLayoutManager(TopicVideoListActivity.this);
        bindingView.recyerView.setLayoutManager(mLinearLayoutManager);
        bindingView.recyerView.setHasFixedSize(false);
        mAutoVideoListAdapter = new TopicAutoVideoListAdapter(copyVideoItemList,mTopicVideoList);
        //添加加载更多监听
        mAutoVideoListAdapter.setOnLoadMoreListener(this);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mAutoVideoListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有发现与此话题相关的视频~");
        bindingView.recyerView.setAdapter(mAutoVideoListAdapter);

        //添加滑动监听
        bindingView.recyerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            //松手后调用
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
                super.onScrollStateChanged(recyclerView, scrollState);
                if(null==copyVideoItemList||null==bindingView) return;
                if(scrollState == RecyclerView.SCROLL_STATE_IDLE && !copyVideoItemList.isEmpty()){

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
        });
        mItemsPositionGetter = new RecyclerViewItemPositionGetter(mLinearLayoutManager, bindingView.recyerView);
    }

    /**
     * 填充管理滑动的列表Item对象到数组，每次初始化或者刷新必须调用
     */
    private void createItemList() {
        if(null!=copyVideoItemList){
            copyVideoItemList.clear();
        }
        if(null!=mTopicVideoList&&mTopicVideoList.size()>0){
            for (int i = 0; i<mTopicVideoList.size(); i++) {
                copyVideoItemList.add(new TopicVideoItem(TopicVideoListActivity.this,TopicVideoListActivity.this,TopicVideoListActivity.this,TopicVideoListActivity.this,0));
            }
        }
        mListItemVisibilityCalculator = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), copyVideoItemList);
    }



    /**
     * 往滑动控制器中添加Item
     */
    private void addItemList() {
        if(null!=mTopicVideoList&&mTopicVideoList.size()>0){
            if(null!=mVideoItemList){
                mVideoItemList.clear();
            }
            for (int i = 0; i < mTopicVideoList.size(); i++) {
                mVideoItemList.add(new TopicVideoItem(TopicVideoListActivity.this,TopicVideoListActivity.this,TopicVideoListActivity.this,TopicVideoListActivity.this,0));
            }
        }
        mListItemVisibilityCalculator = new SingleListViewItemActiveCalculator(new DefaultSingleItemCalculatorCallback(), copyVideoItemList);
    }


    /**
     * 刷新适配器所有数据
     */
    private void upDataNewDataAdapter() {
        showContentView();
        createItemList();
        XinQuVideoPlayer.releaseAllVideos();
        mAutoVideoListAdapter.setNewListData(copyVideoItemList,mTopicVideoList);
        smoveItemToPoistion();
        //第一次使用弹出使用提示
        if(Utils.getVersionCode()!= SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_VIDEO_LIST_CODE)&&TextUtils.equals("com.video.newqu.ui.activity.TopicVideoListActivity", SystemUtils.getTopActivity())){
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


    /**
     * 为适配器添加新的数据
     */
    private void updataAddDataAdapter() {
        addItemList();
        mAutoVideoListAdapter.addListData(mVideoItemList,mTopicVideoList);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
        }
    }


    /**
     * 举报视频
     * @param video_id
     */
    private void onReportVideo(String video_id) {
        showProgressDialog("举报视频中...",true);
        mTopicVideoPresenter.onReportVideo(VideoApplication.getLoginUserID(),video_id);
    }

    /**
     * 举报用户
     * @param accuseUserId
     */
    private void onReportUser(String accuseUserId) {
        showProgressDialog("举报用户中...",true);
        mTopicVideoPresenter.onReportUser(VideoApplication.getLoginUserID(),accuseUserId);
    }



    //==========================================网络请求结果===========================================

    /**
     * 加载话题列表成功
     * @param data
     */
    @Override
    public void showTopicVideoListFinlish(TopicVideoList data) {
        showContentView();


        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mAutoVideoListAdapter.loadMoreComplete();//加载完成
            }
        });
        //替换为全新数据
        if(1==page){
            bindingView.swiperefreshLayout.setRefreshing(false);
            if(null!=mTopicVideoList){
                mTopicVideoList.clear();
            }
            mTopicVideoList=data.getData().getVideo_list();
            ApplicationManager.getInstance().getCacheExample().remove(mTopicKey);
            ApplicationManager.getInstance().getCacheExample().put(mTopicKey, (Serializable) mTopicVideoList,1200);//20分钟后清除缓存
            upDataNewDataAdapter();
            //添加数据
        }else{
            mTopicVideoList=data.getData().getVideo_list();
            updataAddDataAdapter();
        }
    }

    /**
     * 加载话题列表为空
     * @param data
     */
    @Override
    public void showTopicVideoListEmpty(String data) {
        showContentView();
        bindingView.swiperefreshLayout.setRefreshing(false);
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mAutoVideoListAdapter.loadMoreEnd();//没有更多的数据了
            }
        });
        //还原当前的页数
        if (page > 1) {
            page--;
        }
    }

    /**
     * 加载话题列表失败
     * @param data
     */
    @Override
    public void showTopicVideoListError(String data) {


        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                mAutoVideoListAdapter.loadMoreFail();
            }
        });

        if(page==1){
            bindingView.swiperefreshLayout.setRefreshing(false,-1);
        }
        if(page==1&&null==mTopicVideoList){
            showLoadErrorView();
        }
        if(page>0){
            page--;
        }
    }

    /**
     *  举报用户回调
     * @param data
     */
    @Override
    public void showReportUserResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"), Constant.REPORT_USER_RESULT)){
                ToastUtils.shoCenterToast(jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 举报视频回调
     * @param data
     */
    @Override
    public void showReportVideoResult(String data) {
        closeProgressDialog();
        try {
            JSONObject jsonObject=new JSONObject(data);
            if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.REPORT_USER_RESULT)){
                ToastUtils.shoCenterToast(jsonObject.getString("msg"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showErrorView() {
        bindingView.swiperefreshLayout.setRefreshing(true);
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }

    @Override
    public void onDeactivate(View currentView, int position) {

    }

    @Override
    public void onActiveViewChangedActive(View newActiveView, int newActiveViewPosition) {

    }


    /**
     * 视频详情界面
     * @param videoListBean
     * @param isShowKeyBoard
     */
    private void startVideoDetails(TopicVideoList.DataBean.VideoListBean videoListBean, boolean isShowKeyBoard) {
        if(null==videoListBean) return;
        //最后一个参数是否打开键盘
        VideoDetailsActivity.start(TopicVideoListActivity.this,videoListBean.getVideo_id(),videoListBean.getUser_id(),false);
    }


    /**
     * 登录方法
     */
    public void loginAccount(){

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        Intent intent=new Intent(TopicVideoListActivity.this,LoginGroupActivity.class);
        startActivityForResult(intent,Constant.INTENT_LOGIN_TOPIC);
        overridePendingTransition( R.anim.menu_enter,0);//进场动画
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //登录意图，需进一步确认
        if(Constant.INTENT_LOGIN_TOPIC==requestCode&&resultCode==Constant.INTENT_LOGIN_RESULTCODE){
            if(null!=data){
                boolean booleanExtra = data.getBooleanExtra(Constant.INTENT_LOGIN_STATE, false);
                Logger.d("TopicVideoListActivity","登录成功");
                //登录成功,刷新子界面
                if(booleanExtra){
                    VideoApplication.isLogin=true;
                    VideoApplication.isWorksChange=false;
                    VideoApplication.isFolloUser=false;
//                    page=0;
//                    loadTopicVideoList();
                }
            }
        }
    }



    /**
     * 显示菜单
     * @param videoListInfo
     */
    private void openMenu(final TopicVideoList.DataBean.VideoListBean videoListInfo) {

    }


    //========================================点击事件的回调=========================================

    /**
     * 点击条目
     * @param position
     */
    @Override
    public void onItemClick(int position) {
        try {
            List<TopicVideoList.DataBean.VideoListBean> videoList = mAutoVideoListAdapter.getVideoList();
            if(null!=videoList&&videoList.size()>0){
                TopicVideoList.DataBean.VideoListBean videoListBean = videoList.get(position);
                if(null!=videoListBean){
                    startVideoDetails(videoListBean, false);
                }
            }
        }catch (Exception e){

        }
    }



    /**
     * 收藏视频,这里处理为登录事件
     * @param position
     * @param data
     */
    @Override
    public void onItemPrice(int position, TopicVideoList.DataBean.VideoListBean data) {
        loginAccount();
    }


    /**
     * 评论
     * @param position
     * @param data
     * @param isShowKeyBoard
     */
    @Override
    public void onItemComent(int position, TopicVideoList.DataBean.VideoListBean data, boolean isShowKeyBoard) {
        if(null!=data){
            startVideoDetails(data,false);
        }
    }

    /**
     * 分享
     * @param position
     * @param data
     */
    @Override
    public void onItemShare(int position, TopicVideoList.DataBean.VideoListBean data) {
        try {
            if(!Utils.isCheckNetwork()){
                showNetWorkTips();
                return;
            }
            XinQuVideoPlayer.releaseAllVideos();
            if(null!=VideoApplication.getInstance().getUserData()){
                ShareInfo shareInfo=new ShareInfo();
                shareInfo.setDesp(TextUtils.isEmpty(data.getDesp())?"一起来看 "+data.getNickname()+" 的新趣视频":data.getDesp());
                shareInfo.setTitle("一起来看 "+data.getNickname()+" 的新趣视频!");
                shareInfo.setUrl(data.getPath());
                shareInfo.setVideoID(data.getVideo_id());
                shareInfo.setImageLogo(data.getCover());
                onShare(shareInfo,this);
            }else{
                ToastUtils.shoCenterToast("分享需要登录账号");
                login();
            }
        }catch (Exception e){

        }

    }

    /**
     * 菜单
     * @param position
     * @param data
     */
    @Override
    public void onItemMenu(int position, TopicVideoList.DataBean.VideoListBean data) {
        openMenu(data);
    }

    /**
     * 查看更多评论
     * @param position
     * @param data
     */
    @Override
    public void onItemVisitOtherHome(int position, TopicVideoList.DataBean.VideoListBean data) {
        AuthorDetailsActivity.start(TopicVideoListActivity.this,data.getUser_id());
    }

    /**
     * 关注
     * @param position
     * @param data
     */
    @Override
    public void onItemFollow(int position, TopicVideoList.DataBean.VideoListBean data) {
        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }
        loginAccount();
    }

    /**
     * 点击子留言条目
     * @param position
     * @param data
     * @param isShowKeyBoard
     */
    @Override
    public void onItemChildComent(int position, TopicVideoList.DataBean.VideoListBean data, boolean isShowKeyBoard) {
        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }
        startVideoDetails(data,isShowKeyBoard);
    }

    /**
     * 登录
     */
    @Override
    public void login() {
          loginAccount();
    }

    /**
     * 点击了话题
     * @param topic
     */
    @Override
    public void onTopicClick(String topic) {
        TopicVideoListActivity.start(TopicVideoListActivity.this,topic,null,null,0);
    }

    @Override
    public void onUrlClick(String url) {

    }

    @Override
    public void onAuthoeClick(String author) {

    }

    /**
     * 监听分享回调
     * @param videoID
     * @param newShareCount
     */
    @Override
    public void shareFinlish(String videoID, String newShareCount) {
        if(null==mAutoVideoListAdapter) return;
        try {
            List<TopicVideoList.DataBean.VideoListBean> videoListBeen = mAutoVideoListAdapter.getVideoList();
            if(null!=videoListBeen&&videoListBeen.size()>0){
                int poistion=0;
                for (int i = 0; i < videoListBeen.size(); i++) {
                    TopicVideoList.DataBean.VideoListBean videoListBean = videoListBeen.get(i);
                    if(null!=videoListBean){
                        if(TextUtils.equals(videoID,videoListBean.getVideo_id())){
                            videoListBean.setShare_times(newShareCount);
                            poistion=i;
                            break;
                        }
                    }
                }
                mAutoVideoListAdapter.notifyItemChanged(poistion);
            }
        }catch (Exception e){

        }

    }


    @Override
    public void onBackPressed() {
        if (XinQuVideoPlayer.backPress()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        XinQuVideoPlayer.releaseAllVideos();
    }
}
