package com.video.newqu.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.video.newqu.R;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.view.layout.VideoGroupRelativeLayout;
import com.video.newqu.view.layout.WrapContentGrideView;
import com.xinqu.videoplayer.XinQuVideoPlayerStandard;

/**
 * TinyHung@outlook.com
 * 2017/5/26 9:54
 * 视频列表ViewHolder
 */
public class VideoListViewHolder extends BaseViewHolder {

    public final TextView tv_item_type;
    public final TextView tv_item_add;
    public final XinQuVideoPlayerStandard video_player;
    public final ImageView iv_item_user_icon;
    public final TextView tv_item_user_name;
    public final TextView tv_item_time;
    public final TextView tv_item_play_count;
    public final TextView video_item_list_title;
    public final LinearLayout ll_price;
    public final LinearLayout ll_coment;
    public final LinearLayout ll_share;
    public final ImageView iv_item_menu;
    public final ImageView iv_item_follow_icon;
    public final TextView tv_item_follow_count;
    public final TextView tv_item_coment_count;
    public final TextView tv_item_share_count;
    public final WrapContentGrideView grid_view;
    public final LinearLayout ll_more_coment;
    public final RelativeLayout re_follow;
    public final RelativeLayout re_item_video;
    public final RelativeLayout re_item_icon_layout;
    public final ImageView iv_item_icon_layout;
    public final VideoGroupRelativeLayout re_video_group;



    public VideoListViewHolder(View itemView) {
        super(itemView);

            video_player= (XinQuVideoPlayerStandard) itemView.findViewById(R.id.video_player);

        iv_item_user_icon= (ImageView) itemView.findViewById(R.id.iv_item_user_icon);
        iv_item_follow_icon= (ImageView) itemView.findViewById(R.id.iv_item_follow_icon);
        iv_item_menu= (ImageView) itemView.findViewById(R.id.iv_item_menu);

        ll_price= (LinearLayout) itemView.findViewById(R.id.ll_price);
        ll_coment= (LinearLayout) itemView.findViewById(R.id.ll_coment);
        ll_share= (LinearLayout) itemView.findViewById(R.id.ll_share);
        ll_more_coment= (LinearLayout) itemView.findViewById(R.id.ll_more_coment);

        tv_item_user_name=(TextView)itemView.findViewById(R.id.tv_item_user_name);
        tv_item_time=(TextView)itemView.findViewById(R.id.tv_item_time);
        tv_item_play_count=(TextView)itemView.findViewById(R.id.tv_item_play_count);
        video_item_list_title=(TextView)itemView.findViewById(R.id.video_item_list_title);
        tv_item_follow_count=(TextView)itemView.findViewById(R.id.tv_item_follow_count);
        tv_item_coment_count=(TextView)itemView.findViewById(R.id.tv_item_coment_count);
        tv_item_share_count=(TextView)itemView.findViewById(R.id.tv_item_share_count);
        tv_item_type=(TextView)itemView.findViewById(R.id.tv_item_type);//用户类型
        tv_item_add=(TextView)itemView.findViewById(R.id.tv_item_add);//关注

        grid_view= (WrapContentGrideView) itemView.findViewById(R.id.grid_view);
        re_follow= (RelativeLayout) itemView.findViewById(R.id.re_follow);

        re_item_video= (RelativeLayout) itemView.findViewById(R.id.re_item_video);
        re_item_icon_layout= (RelativeLayout) itemView.findViewById(R.id.re_item_icon_layout);
        iv_item_icon_layout= (ImageView) itemView.findViewById(R.id.iv_item_icon_layout);
        re_video_group= (VideoGroupRelativeLayout) itemView.findViewById(R.id.re_video_group);
    }
}
