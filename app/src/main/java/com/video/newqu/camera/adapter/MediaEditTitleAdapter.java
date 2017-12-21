package com.video.newqu.camera.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.video.newqu.R;
import com.video.newqu.bean.MediaEditTitleInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.util.ScreenUtils;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/9/9
 * 视频编辑界面底部标题栏
 */

public class MediaEditTitleAdapter extends BaseQuickAdapter<MediaEditTitleInfo,BaseViewHolder> {

    private final int mItemWidth;

    public MediaEditTitleAdapter(List<MediaEditTitleInfo> data) {
        super(R.layout.media_edit_layout_item, data);
        mItemWidth = ScreenUtils.getScreenWidth()/5;
    }

    @Override
    protected void convert(final BaseViewHolder helper, MediaEditTitleInfo item) {

        if(item==null) return;
        TextView tv_item_title = (TextView) helper.getView(R.id.tv_item_title);
        ImageView iv_item_icon = (ImageView) helper.getView(R.id.iv_item_icon);
        //一行显示6个Item，均分屏幕宽度
        LinearLayout re_item_click = (LinearLayout) helper.getView(R.id.re_item_click);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) re_item_click.getLayoutParams();
        layoutParams.width=mItemWidth;
        re_item_click.setLayoutParams(layoutParams);
        tv_item_title.setText(item.getTitle());
        iv_item_icon.setImageResource(item.getIcon());
        tv_item_title.setSelected(item.isSelector());
        iv_item_icon.setSelected(item.isSelector());
        View view = helper.getView(R.id.media_edit_view_line);
        view.setVisibility(item.isSelector()? View.VISIBLE:View.INVISIBLE);
    }
}
