package com.video.newqu.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseMediaFragment;
import com.video.newqu.bean.StickerNetInfo;
import com.video.newqu.camera.adapter.MediaEditNetStickerListAdapter;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentStickerLayoutBinding;
import com.video.newqu.databinding.RecylerMediaEditEmptyLayoutBinding;
import com.video.newqu.listener.OnMediaStickerListener;
import com.video.newqu.ui.contract.MediaStickerContract;
import com.video.newqu.ui.presenter.MediaStickerPresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.Utils;
import com.video.newqu.model.GridSpaceItemDecorationComent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/12.
 * 贴纸列表片段
 */

public class MediaStickerFragment extends BaseMediaFragment<FragmentStickerLayoutBinding> implements MediaStickerContract.View, BaseQuickAdapter.RequestLoadMoreListener {

    private static final String TAG = "MediaStickerFragment";
    private static OnMediaStickerListener mOnMediaStickerListener;
    private String mStickerID;
    private MediaEditNetStickerListAdapter mMediaEditStickerAdapter=null;
    private MediaStickerPresenter mMediaStickerPresenter;
    private int page=0;
    private int pageSize=10;

    public static MediaStickerFragment newInstance(String typeID, OnMediaStickerListener onMediaStickerListener){
        MediaStickerFragment mediaStickerFragment=new MediaStickerFragment();
        Bundle bundle=new Bundle();
        bundle.putString("type_id",typeID);
        mediaStickerFragment.setArguments(bundle);
        mOnMediaStickerListener=onMediaStickerListener;
        return mediaStickerFragment;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //取出参数
        Bundle arguments = getArguments();
        if(null!=arguments) {
            mStickerID = arguments.getString("type_id");
        }
    }


    @Override
    protected void initViews() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_sticker_layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMediaStickerPresenter = new MediaStickerPresenter(getActivity());
        mMediaStickerPresenter.attachView(this);
        initAdapter();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(null!=bindingView&&null!=mMediaEditStickerAdapter&&(null==mMediaEditStickerAdapter.getData()||mMediaEditStickerAdapter.getData().size()<=0)){
            showLoadingView("获取贴纸列表中");
            page=0;
            loadStickerList();
        }
    }

    @Override
    protected void onRefresh() {
        super.onRefresh();
        page=0;
        showLoadingView("获取贴纸列表中");
        loadStickerList();
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }


    private void loadStickerList() {
        if(null!=mMediaStickerPresenter&&!TextUtils.isEmpty(mStickerID)){
            page++;
            mMediaStickerPresenter.getStickerTypeList(mStickerID,page,pageSize);
        }
    }

    /**
     * 初始化贴纸适配器
     */
    private void initAdapter() {

        List<StickerNetInfo.DataBean> list= (List<StickerNetInfo.DataBean>)  ApplicationManager.getInstance().getCacheExample().getAsObject(mStickerID+Constant.STICKER_STICKERID_LIST);
        if(null==list) list=new ArrayList<>();
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),5, LinearLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new GridSpaceItemDecorationComent(Utils.dip2px(6)));
        bindingView.recyerView.setHasFixedSize(true);
        mMediaEditStickerAdapter = new MediaEditNetStickerListAdapter(list,mOnMediaStickerListener);
        mMediaEditStickerAdapter.setOnLoadMoreListener(this);
        bindingView.recyerView.setAdapter(mMediaEditStickerAdapter);
        RecylerMediaEditEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_media_edit_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mMediaEditStickerAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有找到素材列表~");
    }
    /**
     * 加载更多
     */
    @Override
    public void onLoadMoreRequested() {
        if(null!=mMediaEditStickerAdapter){
            mMediaEditStickerAdapter.setEnableLoadMore(true);
            loadStickerList();
        }
    }

    /**
     * 网络失败
     */
    @Override
    public void showErrorView() {
        if(1==page){
            showLoadingError();
        }
    }

    @Override
    public void complete() {

    }

    /**
     * 获取分类下列表成功
     * @param data
     */
    @Override
    public void showStickerList(List<StickerNetInfo.DataBean>  data) {
        showContentView();
        bindingView.recyerView.post(new Runnable() {
            @Override
            public void run() {
                if(null!=mMediaEditStickerAdapter){
                    mMediaEditStickerAdapter.loadMoreComplete();//加载完成
                }
            }
        });

        if(1==page){
            ApplicationManager.getInstance().getCacheExample().remove(mStickerID+Constant.STICKER_STICKERID_LIST);
            ApplicationManager.getInstance().getCacheExample().put(mStickerID+Constant.STICKER_STICKERID_LIST, (Serializable) data);
            mMediaEditStickerAdapter.setNewData(data);
        }else{
            mMediaEditStickerAdapter.addData(data);
        }
    }

    /**
     * 加载贴纸列表为空
     */
    @Override
    public void showStickerEmpty(String data) {
        showContentView();
        if(null!=mMediaEditStickerAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(null!=mMediaEditStickerAdapter){
                        mMediaEditStickerAdapter.loadMoreEnd();//没有更多的数据了
                    }
                }
            });
        }
        //还原当前的页数
        if (page > 0) {
            page--;
        }
    }

    /**
     * 加载贴纸列表错误
     */
    @Override
    public void showStickerError(String data) {


        if(1==page&&null==mMediaEditStickerAdapter.getData()||mMediaEditStickerAdapter.getData().size()<=0){
            showLoadingError();
        }

        if(null!=mMediaEditStickerAdapter){
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    if(null!=mMediaEditStickerAdapter){
                        mMediaEditStickerAdapter.loadMoreFail();//加载失败
                    }
                }
            });
        }

        //还原当前的页数
        if (page > 0) {
            page--;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume");
        if(null!=mMediaEditStickerAdapter){
            mMediaEditStickerAdapter.pause(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG,"onPause");
        if(null!=mMediaEditStickerAdapter){
            mMediaEditStickerAdapter.pause(true);
        }
    }

    @Override
    public void onDestroy() {
        if(null!=mMediaStickerPresenter){
            mMediaStickerPresenter.detachView();
        }
        if(null!=mMediaEditStickerAdapter){
            mMediaEditStickerAdapter.stopDownload();
        }
        super.onDestroy();
    }
}
