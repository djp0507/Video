package com.video.newqu.ui.pager;

import android.app.Activity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import com.video.newqu.R;
import com.video.newqu.base.BasePager;
import com.video.newqu.bean.StickerNetInfo;
import com.video.newqu.camera.adapter.MediaEditStickerAdapter;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.databinding.MediaStickerLayoutBinding;
import com.video.newqu.ui.contract.MediaStickerContract;
import com.video.newqu.ui.presenter.MediaStickerPresenter;
import com.video.newqu.util.Utils;
import com.video.newqu.model.GridSpaceItemDecorationComent;
import java.util.List;


/**
 * TinyHung@Outlook.com
 * 2017/9/11
 * 贴纸的列表片段
 */

public class MediaStickerPager extends BasePager<MediaStickerLayoutBinding> implements MediaStickerContract.View, BaseQuickAdapter.RequestLoadMoreListener {

    private final String mStickerID;
    private MediaEditStickerAdapter mMediaEditStickerAdapter;
    private MediaStickerPresenter mMediaStickerPresenter;
    private int page=0;
    private int pageSize=10;

    public interface OnStickerItemClickListener{
        void onItemClick(String path);
    }

    private OnStickerItemClickListener mOnStickerItemClickListener;

    public void setOnStickerItemClickListener(OnStickerItemClickListener onStickerItemClickListener) {
        mOnStickerItemClickListener = onStickerItemClickListener;
    }

    public MediaStickerPager(Activity context, String stickerID) {
        super(context);
        this.mStickerID=stickerID;
        setContentView(R.layout.media_sticker_layout);
        showLoadingView();
        initAdapter();
        mMediaStickerPresenter = new MediaStickerPresenter(mContext);
        mMediaStickerPresenter.attachView(this);
        loadStickerList();
    }


    private void loadStickerList() {
        if(null!=mMediaStickerPresenter&& TextUtils.isEmpty(mStickerID)){
            pageSize++;
            mMediaStickerPresenter.getStickerTypeList(mStickerID,page,pageSize);
        }
    }



    /**
     * 初始化贴纸适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(mContext,5, LinearLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new GridSpaceItemDecorationComent(Utils.dip2px(6)));
        mMediaEditStickerAdapter = new MediaEditStickerAdapter(null);
        mMediaEditStickerAdapter.setOnLoadMoreListener(this);
        bindingView.recyerView.setAdapter(mMediaEditStickerAdapter);
        bindingView.recyerView.addOnItemTouchListener(new com.video.newqu.comadapter.listener.OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {

            }
        });
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
            showLoadErrorView();
        }
    }

    @Override
    public void complete() {

    }



    @Override
    public void showStickerList(List<StickerNetInfo.DataBean> data) {

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
        if (page > 1) {
            page--;
        }
    }

    /**
     * 加载贴纸列表错误
     */
    @Override
    public void showStickerError(String data) {

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

        if(1==page){
            showLoadErrorView();
        }

        //还原当前的页数
        if (page > 1) {
            page--;
        }
    }
}
