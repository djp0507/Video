package com.video.newqu.adapter;

import com.video.newqu.R;
import com.video.newqu.bean.MediaPainInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/12.
 * 画笔颜色选择适配器
 */

public class MediaPainAdapter extends BaseQuickAdapter<MediaPainInfo,BaseViewHolder>{
    public MediaPainAdapter(List<MediaPainInfo> data) {
        super(R.layout.re_media_pain_item,data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MediaPainInfo item) {
        if(null==item) return;
        helper.setImageResource(R.id.iv_item_icon,item.getColor());
        helper.setBackgroundRes(R.id.iv_item_icon,item.isSelector()?R.drawable.media_btn_bg_shape_white:0);
    }
}
