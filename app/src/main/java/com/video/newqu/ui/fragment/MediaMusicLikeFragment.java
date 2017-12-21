package com.video.newqu.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.danikula.videocache.HttpProxyCacheServer;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.MediaMusicRecommendAdapter;
import com.video.newqu.base.BaseMineFragment;
import com.video.newqu.bean.MediaMusicCategoryList;
import com.video.newqu.bean.MusicInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentMediaLikeHomeBinding;
import com.video.newqu.databinding.MineAuthorRecylerviewEmptyLayoutBinding;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.listener.OnMediaMusicClickListener;
import com.video.newqu.manager.DownloadFileUtilTask;
import com.video.newqu.ui.activity.MediaRecordMusicActivity;
import com.video.newqu.ui.contract.MediaMusicLikeContract;
import com.video.newqu.ui.dialog.RecordProgressDialog;
import com.video.newqu.ui.presenter.MediaMusicLikePresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.xinqu.videoplayer.full.WindowVideoPlayer;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/11/9.
 * 音乐选择-收藏列表
 */

public class MediaMusicLikeFragment extends BaseMineFragment<FragmentMediaLikeHomeBinding> implements BaseQuickAdapter.RequestLoadMoreListener
        ,OnMediaMusicClickListener, MediaMusicLikeContract.View {

    private static final String TAG = MediaMusicLikeFragment.class.getSimpleName();
    private MediaRecordMusicActivity mMusicActivity;
    private MediaMusicRecommendAdapter mMediaMusicRecommendAdapter;
    private int page=0;
    private int pageSize=10;
    List<MediaMusicCategoryList.DataBean> mMediaMusicInfoList=null;
    private MediaMusicLikePresenter mMediaMusicLikePresenter;
    private RecordProgressDialog mRecordProgressDialog;
    private MediaMusicCategoryList.DataBean mData;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMusicActivity = (MediaRecordMusicActivity) context;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMediaMusicInfoList= (List<MediaMusicCategoryList.DataBean>)ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC);
        if(null==mMediaMusicInfoList) mMediaMusicInfoList=new ArrayList<>();
        mMediaMusicLikePresenter = new MediaMusicLikePresenter(getActivity());
        mMediaMusicLikePresenter.attachView(this);
        initAdapter();
    }

    @Override
    protected void initViews() {
        bindingView.swiperefreshLayout.setColorSchemeColors(CommonUtils.getColor(R.color.colorTabText));
        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page=0;
                loadMusicData();
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_media_like_home;
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(null!=bindingView&&null!=mMediaMusicRecommendAdapter&&null!=mMediaMusicLikePresenter&&!mMediaMusicLikePresenter.isHomeLoading()){
            if(null==mMediaMusicInfoList||mMediaMusicInfoList.size()<=0){
                showLoadingView("获取我收藏的音乐中...");
                //获取最新的数据
                page=0;
                loadMusicData();
            }else{
                showContentView();
                bindingView.swiperefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        //获取最新的数据
                        page=0;
                        loadMusicData();
                    }
                },500);
            }
        }
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        bindingView.recyerView.setLayoutManager(linearLayoutManager);
        mMediaMusicRecommendAdapter = new MediaMusicRecommendAdapter(mMediaMusicInfoList,this);
        mMediaMusicRecommendAdapter.setOnLoadMoreListener(this);
        MineAuthorRecylerviewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.mine_author_recylerview_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mMediaMusicRecommendAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有收藏的音乐");
        emptyViewbindView.viewEmpty.setVisibility(View.VISIBLE);
        bindingView.recyerView.setAdapter(mMediaMusicRecommendAdapter);
    }


    private void loadMusicData() {
        if(null!= mMediaMusicLikePresenter &&!mMediaMusicLikePresenter.isHomeLoading()){
            page++;
            mMediaMusicLikePresenter.getLikeMusicList(null,page,pageSize);
        }else{
            bindingView.swiperefreshLayout.setRefreshing(false);
        }
    }



    /**
     * 为适配器刷新新数据
     */
    private void upDataNewDataAdapter() {
        WindowVideoPlayer.releaseAllVideos();
        if(null!= mMediaMusicRecommendAdapter) mMediaMusicRecommendAdapter.setNewData(mMediaMusicInfoList);
    }

    /**
     * 为适配器增加数据
     */
    private void updataAddDataAdapter() {
        if(null!= mMediaMusicRecommendAdapter) mMediaMusicRecommendAdapter.addData(mMediaMusicInfoList);
    }



    /**
     * 尝试刷新
     */
    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingView("加载音乐列表中...");
        page=0;
        loadMusicData();
    }

    /**
     * 加载更多列表
     */
    @Override
    public void onLoadMoreRequested() {
        mMediaMusicRecommendAdapter.setEnableLoadMore(true);
        loadMusicData();
    }


    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }


    @Override
    public void showLikeMusicList(List<MediaMusicCategoryList.DataBean> data) {
        if(null!= mMediaMusicRecommendAdapter &&null!=getActivity()&&!getActivity().isFinishing()){
            bindingView.swiperefreshLayout.setRefreshing(false);
            showContentView();
            mMediaMusicRecommendAdapter.loadMoreComplete();
            //更新适配器数据为全新
            if(1==page){//替换最新缓存
                if(null!=mMediaMusicInfoList){
                    mMediaMusicInfoList.clear();
                }
                mMediaMusicInfoList=data;

                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC);
                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC, (Serializable) mMediaMusicInfoList, Constant.CACHE_TIME);
                upDataNewDataAdapter();
                //仅增加新数据
            }else{
                mMediaMusicInfoList=data;
                updataAddDataAdapter();
            }
        }
    }

    @Override
    public void showLikeMusicEmpty(String data) {
        if(null!= mMediaMusicRecommendAdapter &&null!=getActivity()&&!getActivity().isFinishing()){
            bindingView.swiperefreshLayout.setRefreshing(false);
            showContentView();
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMediaMusicRecommendAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
            //没有关注用户，替换空的缓存
            if(page==1){
                if(null!= mMediaMusicInfoList){
                    mMediaMusicInfoList.clear();
                }
                ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC);
                ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC, (Serializable) mMediaMusicInfoList, Constant.CACHE_TIME);
                upDataNewDataAdapter();
            }
            if(page>0){
                page--;
            }
        }
    }

    @Override
    public void showLikeMusicError(String data) {
        if(null!= mMediaMusicRecommendAdapter &&null!=getActivity()&&!getActivity().isFinishing()){
            bindingView.swiperefreshLayout.setRefreshing(false);
            if(1==page&&null== mMediaMusicInfoList || mMediaMusicInfoList.size()<=0){
                showLoadingErrorView();
            }
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMediaMusicRecommendAdapter.loadMoreFail();
                }
            });
            if(page>0){
                page--;
            }
        }
    }

    @Override
    public void showLikeResultResult(String data) {
        closeProgressDialog();
        if(!TextUtils.isEmpty(data)){
            try {
                JSONObject jsonObject=new JSONObject(data);
                if(null!=jsonObject&&jsonObject.length()>0){
                    if(1==jsonObject.getInt("code")){
                        String music_id = jsonObject.getString("music_id");
                        if(null!=music_id){
                            if(null!=mMediaMusicRecommendAdapter){
                                List<MediaMusicCategoryList.DataBean> musicList = mMediaMusicRecommendAdapter.getData();
                                if(null!=musicList&&musicList.size()>0){
                                    int poistion=0;
                                    for (int i = 0; i < musicList.size(); i++) {
                                        if(TextUtils.equals(music_id,musicList.get(i).getId())){
                                            musicList.get(i).setIs_collect(Integer.parseInt(jsonObject.getString("res")));
                                            poistion=i;
                                            break;
                                        }
                                    }
                                    //直接删除取消收藏成功的那个音乐条目
                                    mMediaMusicRecommendAdapter.remove(poistion);
                                    //替换最新缓存
                                    ApplicationManager.getInstance().getCacheExample().remove(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC);
                                    ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_MEDIA_RECORED_LIKE_MUSIC, (Serializable)  mMediaMusicRecommendAdapter.getData(), Constant.CACHE_TIME);
                                }
                            }
                        }
                    }else{
                        ToastUtils.shoCenterToast("取消收藏失败");
                    }
                }else{
                    ToastUtils.shoCenterToast("取消收藏异常");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ToastUtils.shoCenterToast("取消收藏异常"+e.getMessage());
            }
        }
    }

    @Override
    public void showLikeResultError(String data) {
        closeProgressDialog();
        ToastUtils.shoCenterToast(data);
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
     * 注册刷新界面事件，通常是结束了播放，需要还原列表状态
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        com.video.newqu.util.Logger.d(TAG,"onMessageEvent=="+event.getType());
        //只处理与自己相关的事件
        if(null!=event&&TextUtils.equals(Constant.EVENT_UPDATA_MUSIC_PLAYER,event.getMessage())&&1==event.getType()){
            WindowVideoPlayer.releaseAllVideos();
            if(null!=mMediaMusicRecommendAdapter){
                mMediaMusicRecommendAdapter.initialListItem();
            }
        }
    }

    @Override
    public void onDestroy() {
        if(null!=mMediaMusicLikePresenter){
            mMediaMusicLikePresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(null!= mMediaMusicLikePresenter){
            mMediaMusicLikePresenter.detachView();
        }
        WindowVideoPlayer.releaseAllVideos();
    }

    /**
     * 仅处理刷新UI事件
     * @param poistion
     */
    @Override
    public void onItemClick(int poistion) {

    }

    /**
     * 收藏事件
     * @param data
     */
    @Override
    public void onLikeClick(MediaMusicCategoryList.DataBean data) {
        if(!getActivity().isFinishing()&&null!=mMediaMusicLikePresenter&&!mMediaMusicLikePresenter.isLikeIng()){
            showProgressDialog("操作中...",true);
            mMediaMusicLikePresenter.likeMusic(data.getId(),data.getIs_collect());
        }
    }

    /**
     * 查看关于本歌曲的详情事件
     * @param musicID
     */
    @Override
    public void onDetailsClick(String musicID) {
        lookMusicDetailsList(musicID);
    }

    /**
     * 选中了某首音乐
     * @param data
     */
    @Override
    public void onSubmitMusic(MediaMusicCategoryList.DataBean data) {
        //先检查缓存文件是否是完整文件
        HttpProxyCacheServer proxy = VideoApplication.getProxy();
        File cacheFile = proxy.getCacheFile(data.getUrl());
        if(null!=cacheFile&&cacheFile.exists()&&cacheFile.isFile()){
            //完整的音乐文件
            if(Utils.isFileToMp3(cacheFile.getAbsolutePath())){
                if(null!=mMusicActivity&&!mMusicActivity.isFinishing()){
                    WindowVideoPlayer.releaseAllVideos();
                    mMusicActivity.onResultFilish(null==data?"0":data.getId(),cacheFile.getAbsolutePath());
                    return;
                }
            //需要下载
            }else{
                showDownloadProgress();
                downloadFile(data);
            }
        }
    }

    @Override
    public void onSubmitLocationMusic(MusicInfo musicPath) {

    }

    private void downloadFile(MediaMusicCategoryList.DataBean data) {
        if(!Utils.isCheckNetwork()){
            return;
        }
        this.mData=data;
        File file=new File(Constant.DOWNLOAD_PATH);
        if(!file.exists()){
            file.mkdirs();
        }
        new DownloadFileUtilTask(Constant.DOWNLOAD_PATH, new DownloadFileUtilTask.OnDownloadListener() {
            @Override
            public void onStartDownload() {

            }

            @Override
            public void onDownloadError(final String e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(null!=mMusicActivity&&!mMusicActivity.isFinishing()&&null!=mRecordProgressDialog&&mRecordProgressDialog.isShowing()){
                            mRecordProgressDialog.dismiss();
                        }
                        ToastUtils.shoCenterToast(e);
                    }
                });
            }

            @Override
            public void onDownloadProgress(int progress) {
                if(null!=mMusicActivity&&!mMusicActivity.isFinishing()&&null!=mRecordProgressDialog&&mRecordProgressDialog.isShowing()){
                    mRecordProgressDialog.setProgress(progress);
                }
            }

            @Override
            public void onWownloadFilish(File file) {
                WindowVideoPlayer.releaseAllVideos();
                if(null!=mMusicActivity&&!mMusicActivity.isFinishing()){
                    if(null!=mRecordProgressDialog&&mRecordProgressDialog.isShowing()){
                        if(null!=file&&file.exists()&&file.isFile()){
                            mRecordProgressDialog.setProgress(100);
                            mRecordProgressDialog.setTipsMessage("下载完成");
                            mRecordProgressDialog.dismiss();
                            mRecordProgressDialog=null;
                            if(Utils.isFileToMp3(file.getAbsolutePath())){
                                mMusicActivity.onResultFilish(null==mData?"0":mData.getId(),file.getAbsolutePath());
                            }
                            return;
                        }
                        mRecordProgressDialog.dismiss();
                        mRecordProgressDialog=null;
                    }
                }
            }
        }).execute(data.getUrl());
    }

    /**
     * 显示下载对话框
     */
    private void showDownloadProgress() {
        if(null== mRecordProgressDialog){
            mRecordProgressDialog = new RecordProgressDialog(getActivity());
            mRecordProgressDialog.setMode(RecordProgressDialog.SHOW_MODE1);
            mRecordProgressDialog.setOnDialogBackListener(new RecordProgressDialog.OnDialogBackListener() {
                @Override
                public void onBack() {

                }
            });
        }
        mRecordProgressDialog.setTipsMessage("下载中，请稍后...");
        mRecordProgressDialog.setProgress(0);
        mRecordProgressDialog.show();
    }
}
