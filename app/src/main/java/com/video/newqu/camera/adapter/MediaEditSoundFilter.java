package com.video.newqu.camera.adapter;

import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.MediaSoundFilter;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.Utils;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/12
 * 变声/混响 适配器
 */

public class MediaEditSoundFilter extends BaseQuickAdapter<MediaSoundFilter,BaseViewHolder> {

    private final int mItemWidth;

    public MediaEditSoundFilter(List<MediaSoundFilter> data) {
        super(R.layout.media_filter_item_layout,data);
        mItemWidth = (ScreenUtils.getScreenWidth()- Utils.dip2px(80))/4;
    }

    @Override
    protected void convert(BaseViewHolder helper, MediaSoundFilter item) {
        if(item==null)return;

        RelativeLayout relativeLayout = helper.getView(R.id.re_item_filter);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) relativeLayout.getLayoutParams();
        layoutParams.width= FrameLayout.LayoutParams.MATCH_PARENT;
        layoutParams.height=mItemWidth;
        relativeLayout.setLayoutParams(layoutParams);

        TextView tv_item_title = helper.getView(R.id.tv_item_title);
        tv_item_title.setText(item.getName());

        //无变声/混响
        if(0==helper.getAdapterPosition()){
            tv_item_title.setBackgroundColor(CommonUtils.getColor(R.color.media_text_bg));
            helper.setBackgroundColor(R.id.re_item_selector,CommonUtils.getColor(R.color.media_text_bg));
        }else{
            tv_item_title.setBackgroundColor(CommonUtils.getColor(R.color.media_selector_bg));
            helper.setBackgroundColor(R.id.re_item_selector,CommonUtils.getColor(R.color.media_selector_bg));
        }
        helper.setVisible(R.id.re_item_selector,item.isSelector()?true:false);

        //变声/混响封面
        Glide.with(mContext)
                .load(item.getIcon())
                .error(R.drawable.filter_original)
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.RESULT)//缓存结果
                .skipMemoryCache(true)//跳过内存缓存
                .into((ImageView) helper.getView(R.id.iv_item_filter));
    }
}
