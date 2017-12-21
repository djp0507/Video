package com.video.newqu.ui.fragment;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.danikula.videocache.HttpProxyCacheServer;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.MediaMusicRecommendAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.MediaMusicCategoryList;
import com.video.newqu.bean.MusicInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentRecylerBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.listener.OnMediaMusicClickListener;
import com.video.newqu.manager.DownloadFileUtilTask;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.contract.MediaMusicCategoryListContract;
import com.video.newqu.ui.dialog.RecordProgressDialog;
import com.video.newqu.ui.presenter.MediaMusicCategoryListPresenter;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.refresh.SwipePullRefreshLayout;
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
 * TinyHung@outlook.com
 * 2017/11/10 14:44
 * 音乐分类下的列表
 */

public class MediaMusicCategroyListFragment extends BaseFragment<FragmentRecylerBinding> implements  BaseQuickAdapter.RequestLoadMoreListener,OnMediaMusicClickListener, MediaMusicCategoryListContract.View {

    private int mPage=0;
    private int mPageSize=10;
    private List<MediaMusicCategoryList.DataBean> mCategoryMusicList;
    private String mMusicCatrgoryID;
    private MediaMusicCategoryListPresenter mMediaMusicCategoryListPresenter;
    private MediaMusicRecommendAdapter mMusicCategoryListAdapter;
    private ContentFragmentActivity mActivity;
    private RecordProgressDialog mRecordProgressDialog;
    private MediaMusicCategoryList.DataBean mData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (ContentFragmentActivity) context;
    }

    /**
     * 创造实例
     * @param musicCategoryID 音乐分类ID
     * @return
     */
    public static MediaMusicCategroyListFragment newInstance(String musicCategoryID){
        MediaMusicCategroyListFragment fansListFragment=new MediaMusicCategroyListFragment();
        Bundle bundle=new Bundle();
        bundle.putString(Constant.MEDIA_KEY_MUSIC_CATEGORY_ID,musicCategoryID);
        fansListFragment.setArguments(bundle);
        return fansListFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取出参数
        Bundle arguments = getArguments();
        if(null!=arguments) {
            mMusicCatrgoryID = arguments.getString(Constant.MEDIA_KEY_MUSIC_CATEGORY_ID);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_recyler;
    }


    @Override
    protected void initViews() {

        bindingView.swiperefreshLayout.setOnRefreshListener(new SwipePullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPage=0;
                loadCategoryMusicListList();
            }
        });
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMediaMusicCategoryListPresenter = new MediaMusicCategoryListPresenter(getActivity());
        mMediaMusicCategoryListPresenter.attachView(this);
        if(!TextUtils.isEmpty(mMusicCatrgoryID)){

            mCategoryMusicList = (List<MediaMusicCategoryList.DataBean>)  ApplicationManager.getInstance().getCacheExample().getAsObject("category_"+mMusicCatrgoryID);
            if(null== mCategoryMusicList) mCategoryMusicList =new ArrayList<>();
            initAdapter();
            if(null== mCategoryMusicList || mCategoryMusicList.size()<=0){
                showLoadingView("获取音乐中..");
                mPage=0;
                loadCategoryMusicListList();
            }else{
                showContentView();
                bindingView.recyerView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bindingView.swiperefreshLayout.setRefreshing(true);
                        mPage=0;
                        loadCategoryMusicListList();
                    }
                },500);
            }
        }else{
            ToastUtils.shoCenterToast("错误!");
            getActivity().finish();
        }
    }


    /**
     * 加载音乐分类下的音乐列表
     */
    private void loadCategoryMusicListList() {
        if(null!= mMediaMusicCategoryListPresenter &&!mMediaMusicCategoryListPresenter.isHomeLoading()){
            mPage++;
            mMediaMusicCategoryListPresenter.getCategoryMusicList(mMusicCatrgoryID,mPage,mPageSize);
        }
    }


    @Override
    protected void onRefresh() {
        super.onRefresh();
        showLoadingView("获取音乐中..");
        mPage=0;
        loadCategoryMusicListList();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        WindowVideoPlayer.releaseAllVideos();
        super.onDestroyView();
        if(null!=mMediaMusicCategoryListPresenter){
            mMediaMusicCategoryListPresenter.detachView();
        }
    }


    /**
     *初始化适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.VERTICAL,false));
        bindingView.recyerView.setHasFixedSize(true);
        mMusicCategoryListAdapter = new MediaMusicRecommendAdapter(mCategoryMusicList,this);
        mMusicCategoryListAdapter.setOnLoadMoreListener(this);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mMusicCategoryListAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("该分类下暂无数据");
        bindingView.recyerView.setAdapter(mMusicCategoryListAdapter);
    }

    /**
     * 只新增数据
     */
    private void updataAddNewDataAdapter() {
        mMusicCategoryListAdapter.addData(mCategoryMusicList);
    }


    /**
     * 刷新全新适配器
     */
    private void updataNewDataAdapter() {
        showContentView();
        WindowVideoPlayer.releaseAllVideos();
        mMusicCategoryListAdapter.setNewData(mCategoryMusicList);
    }


    /**
     * 加载更多数据
     */
    @Override
    public void onLoadMoreRequested() {

        if(null!= mCategoryMusicList && mCategoryMusicList.size()>=10&&null!= mMusicCategoryListAdapter){
            bindingView.swiperefreshLayout.setRefreshing(false);
            mMusicCategoryListAdapter.setEnableLoadMore(true);
            loadCategoryMusicListList();
        }else{
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(!Utils.isCheckNetwork()){
                        mMusicCategoryListAdapter.loadMoreFail();//加载失败
                    }else{
                        mMusicCategoryListAdapter.loadMoreEnd();//加载为空
                    }
                }
            });
        }
    }


    //==========================================点击事件=============================================
    /**
     * 条目点击事件
     * @param position
     */
    @Override
    public void onItemClick(int position) {

    }

    @Override
    public void onLikeClick(MediaMusicCategoryList.DataBean data) {
        if(!getActivity().isFinishing()&&null!=mMediaMusicCategoryListPresenter&&!mMediaMusicCategoryListPresenter.isLikeIng()){
            showProgressDialog("操作中...",true);
            mMediaMusicCategoryListPresenter.likeMusic(data.getId(),data.getIs_collect());
        }
    }

    @Override
    public void onDetailsClick(String musicID) {

    }

    @Override
    public void onSubmitMusic(MediaMusicCategoryList.DataBean musicPath) {
        //先检查缓存文件是否是完整文件
        HttpProxyCacheServer proxy = VideoApplication.getProxy();
        File cacheFile = proxy.getCacheFile(musicPath.getUrl());
        if(null!=cacheFile&&cacheFile.exists()&&cacheFile.isFile()){
            //完整的音乐文件
            if(Utils.isFileToMp3(cacheFile.getAbsolutePath())){
                if(null!=mActivity&&!mActivity.isFinishing()){
                    WindowVideoPlayer.releaseAllVideos();
                    mActivity.onResultFilish(null==musicPath?"0":musicPath.getId(),cacheFile.getAbsolutePath());
                }
                return;
                //需要下载
            }else{
                showDownloadProgress();
                downloadFile(musicPath);
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
                        if(null!=mActivity&&!mActivity.isFinishing()&&null!=mRecordProgressDialog&&mRecordProgressDialog.isShowing()){
                            mRecordProgressDialog.dismiss();
                        }
                        ToastUtils.shoCenterToast(e);
                    }
                });
            }

            @Override
            public void onDownloadProgress(int progress) {
                if(null!=mActivity&&!mActivity.isFinishing()&&null!=mRecordProgressDialog&&mRecordProgressDialog.isShowing()){
                    mRecordProgressDialog.setProgress(progress);
                }
            }

            @Override
            public void onWownloadFilish(File file) {
                WindowVideoPlayer.releaseAllVideos();
                if(null!=mActivity&&!mActivity.isFinishing()){
                    if(null!=mRecordProgressDialog&&mRecordProgressDialog.isShowing()){
                        if(null!=file&&file.exists()&&file.isFile()){
                            mRecordProgressDialog.setProgress(100);
                            mRecordProgressDialog.setTipsMessage("下载完成");
                            mRecordProgressDialog.dismiss();
                            mRecordProgressDialog=null;
                            if(Utils.isFileToMp3(file.getAbsolutePath())){
                                mActivity.onResultFilish(null==mData?"0":mData.getId(),file.getAbsolutePath());
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




    //======================================加载数据回调==============================================

    @Override
    public void showErrorView() {
        closeProgressDialog();
    }


    @Override
    public void complete() {

    }

    @Override
    public void showCategoryMusicList(List<MediaMusicCategoryList.DataBean> data) {
        if(null!= mMusicCategoryListAdapter &&null!=getActivity()&&!getActivity().isFinishing()){
            showContentView();
            bindingView.swiperefreshLayout.setRefreshing(false);
            mMusicCategoryListAdapter.loadMoreComplete();
            if(1==mPage){
                if(null!= mCategoryMusicList){
                    mCategoryMusicList.clear();
                }
                mCategoryMusicList =data;
                ApplicationManager.getInstance().getCacheExample().remove("category_"+mMusicCatrgoryID);
                ApplicationManager.getInstance().getCacheExample().put("category_"+mMusicCatrgoryID, (Serializable) mCategoryMusicList, Constant.CACHE_TIME);
                updataNewDataAdapter();
            }else {
                mCategoryMusicList =data;
                updataAddNewDataAdapter();
            }
        }
    }

    @Override
    public void showCategoryMusicEmpty(String data) {
        if(null!= mMusicCategoryListAdapter &&null!=getActivity()&&!getActivity().isFinishing()){
            showContentView();
            bindingView.swiperefreshLayout.setRefreshing(false);
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMusicCategoryListAdapter.loadMoreEnd();//没有更多的数据了
                }
            });
            //没有关注用户，替换空的缓存
            if(mPage==1){
                if(null!= mCategoryMusicList){
                    mCategoryMusicList.clear();
                }
                ApplicationManager.getInstance().getCacheExample().remove("category_"+mMusicCatrgoryID);
                ApplicationManager.getInstance().getCacheExample().put("category_"+mMusicCatrgoryID, (Serializable) mCategoryMusicList, Constant.CACHE_TIME);
                updataNewDataAdapter();
            }
            if(mPage>0){
                mPage--;
            }
        }
    }

    @Override
    public void showCategoryMusicError(String data) {
        if(null!= mMusicCategoryListAdapter &&null!=getActivity()&&!getActivity().isFinishing()){
            if(1==mPage&&null== mCategoryMusicList || mCategoryMusicList.size()<=0){
                showLoadingErrorView();
            }

            if(mPage>0){
                mPage--;
            }
            bindingView.swiperefreshLayout.setRefreshing(false);
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMusicCategoryListAdapter.loadMoreFail();
                }
            });
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
                            if(null!=mMusicCategoryListAdapter){
                                List<MediaMusicCategoryList.DataBean> musicList = mMusicCategoryListAdapter.getData();
                                if(null!=musicList&&musicList.size()>0){
                                    int poistion=0;
                                    for (int i = 0; i < musicList.size(); i++) {
                                        if(TextUtils.equals(music_id,musicList.get(i).getId())){
                                            musicList.get(i).setIs_collect(Integer.parseInt(jsonObject.getString("res")));
                                            poistion=i;
                                            break;
                                        }
                                    }
                                    //刷新单个条目
                                    mMusicCategoryListAdapter.notifyItemChanged(poistion);
                                    //替换最新缓存
                                    ApplicationManager.getInstance().getCacheExample().remove("category_"+mMusicCatrgoryID);
                                    ApplicationManager.getInstance().getCacheExample().put("category_"+mMusicCatrgoryID, (Serializable) musicList, Constant.CACHE_TIME);
                                }
                            }
                        }
                    }else{
                        ToastUtils.shoCenterToast("收藏失败");
                    }
                }else{
                    ToastUtils.shoCenterToast("收藏异常");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                ToastUtils.shoCenterToast("收藏异常"+e.getMessage());
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
     * 订阅播放结果，以刷新界面
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        if(null!=event&&TextUtils.equals(Constant.EVENT_UPDATA_MUSIC_PLAYER,event.getMessage())&&-1==event.getType()){
            WindowVideoPlayer.releaseAllVideos();
            if(null!=mMusicCategoryListAdapter){
                mMusicCategoryListAdapter.initialListItem();
            }
        }
    }
}
