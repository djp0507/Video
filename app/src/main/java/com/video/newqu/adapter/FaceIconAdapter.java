package com.video.newqu.adapter;

import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.ChatEmoji;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.model.GlideRoundTransform;
import com.video.newqu.util.ScreenUtils;

import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/11/17
 * 表情选择
 */

public class FaceIconAdapter extends BaseQuickAdapter<ChatEmoji,BaseViewHolder>{

    private final int mItemWidth;

    public FaceIconAdapter(List<ChatEmoji> data) {
        super(R.layout.re_item_face, data);
        mItemWidth = ScreenUtils.dpToPxInt(35);
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatEmoji item) {
        RelativeLayout re_item_view = (RelativeLayout) helper.getView(R.id.re_item_view);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) re_item_view.getLayoutParams();
        layoutParams.height=mItemWidth;
        layoutParams.width=mItemWidth;
        re_item_view.setLayoutParams(layoutParams);

        Glide.with(mContext)
                .load(item.getId())
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                .centerCrop()//中心点缩放
                .skipMemoryCache(true)//跳过内存缓存
                .transform(new GlideRoundTransform(mContext))
                .into((ImageView) helper.getView(R.id.item_iv_face));
    }
}
