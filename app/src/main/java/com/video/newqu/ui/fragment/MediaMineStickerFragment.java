package com.video.newqu.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseMediaFragment;
import com.video.newqu.bean.StickerNetInfo;
import com.video.newqu.camera.adapter.MediaEditNetStickerListAdapter;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentStickerLayoutBinding;
import com.video.newqu.databinding.RecylerMediaEditEmptyLayoutBinding;
import com.video.newqu.listener.OnMediaStickerListener;
import com.video.newqu.util.Utils;
import com.video.newqu.model.GridSpaceItemDecorationComent;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/12.
 * 用户用过的贴纸列表片段
 */

public class MediaMineStickerFragment extends BaseMediaFragment<FragmentStickerLayoutBinding>{

    private static OnMediaStickerListener mOnMediaStickerListener;
    private MediaEditNetStickerListAdapter mMediaEditStickerAdapter;
    private List<StickerNetInfo.DataBean> mDataBeanList;

    public static MediaMineStickerFragment newInstance( OnMediaStickerListener onMediaStickerListener){
        MediaMineStickerFragment mediaStickerFragment=new MediaMineStickerFragment();
        mOnMediaStickerListener=onMediaStickerListener;
        return mediaStickerFragment;
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
        mDataBeanList = (List<StickerNetInfo.DataBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINT_MAKE_STICKER_LIST);
        initAdapter();
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if(null!=bindingView&&null!=mMediaEditStickerAdapter){
            if(null!=mDataBeanList) mDataBeanList.clear();
            mDataBeanList = (List<StickerNetInfo.DataBean>) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_MINT_MAKE_STICKER_LIST);
            mMediaEditStickerAdapter.setNewData(mDataBeanList);
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    /**
     * 初始化贴纸适配器
     */
    private void initAdapter() {
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),5, LinearLayoutManager.VERTICAL,false));
        bindingView.recyerView.addItemDecoration(new GridSpaceItemDecorationComent(Utils.dip2px(6)));
        mMediaEditStickerAdapter = new MediaEditNetStickerListAdapter(mDataBeanList,mOnMediaStickerListener);
        bindingView.recyerView.setAdapter(mMediaEditStickerAdapter);
        RecylerMediaEditEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_media_edit_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mMediaEditStickerAdapter.setEmptyView(emptyViewbindView.getRoot());
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("没有使用记录~");
    }
}
