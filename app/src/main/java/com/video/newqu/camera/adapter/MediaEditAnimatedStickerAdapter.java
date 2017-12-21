package com.video.newqu.camera.adapter;

import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.AnimatedStickerInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/9.
 * 动态贴纸适配器
 */

public class MediaEditAnimatedStickerAdapter extends BaseQuickAdapter<AnimatedStickerInfo,BaseViewHolder>{

    public MediaEditAnimatedStickerAdapter(List<AnimatedStickerInfo> data) {
        super(R.layout.re_animated_sticker_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, AnimatedStickerInfo item) {
        if(null==item) return;
        //设置缩略图
        Glide.with(mContext)
                .load(item.getLogoPath())
                .error(R.drawable.iv_media_sticker_min_error)
                .thumbnail(0.1f)
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存结果
                .skipMemoryCache(true)//跳过内存缓存
                .into((ImageView) helper.getView(R.id.iv_item_sticker));
    }
}
