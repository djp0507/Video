package com.video.newqu.adapter;

import android.view.View;
import com.video.newqu.R;
import com.video.newqu.bean.VideoPainColorInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/8/21.
 */

public class VideoColorAdapter extends BaseQuickAdapter<VideoPainColorInfo,BaseViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(int poistion);
    }
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public VideoColorAdapter(List<VideoPainColorInfo> videoChartletInfos) {
        super(R.layout.video_paincolor_item,videoChartletInfos);
    }

    @Override
    protected void convert(final BaseViewHolder helper, VideoPainColorInfo item) {
        if(null==item) return;
        helper.setBackgroundRes(R.id.pain_back_color,item.isSelector()?R.drawable.pain_back_white:0);
        helper.setBackgroundRes(R.id.pain_color,item.getPainColor());

        helper.setOnClickListener(R.id.pain_back_color, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnItemClickListener){
                    mOnItemClickListener.onItemClick(helper.getAdapterPosition());
                }
            }
        });
    }
}
