package com.video.newqu.adapter;

import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.contants.Cheeses;
import com.video.newqu.listener.OnUserPlayerHistoryClickListener;
import com.video.newqu.util.AnimationUtil;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.TextViewTopicSpan;
import com.video.newqu.util.Utils;
import com.video.newqu.view.widget.GlideCircleTransform;
import com.video.newqu.view.widget.GlideRoundTransform;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017-05-22 21:33
 * 用户观看视频记录
 */

public class UserHistoryVideoListAdapter extends BaseQuickAdapter<UserPlayerVideoHistoryList, BaseViewHolder> {

    public static final String TAG = UserHistoryVideoListAdapter.class.getSimpleName();
    private final OnUserPlayerHistoryClickListener onUserPlayerHistoryClickListener;
    private  int mItemHeight;

    public UserHistoryVideoListAdapter(List<UserPlayerVideoHistoryList> listsBeanList, OnUserPlayerHistoryClickListener onUserPlayerHistoryClickListener) {
        super(R.layout.re_user_plsyer_video_history_list_item, listsBeanList);
        this.onUserPlayerHistoryClickListener=onUserPlayerHistoryClickListener;
        int screenHeight = ScreenUtils.getScreenHeight();
        mItemHeight = ScreenUtils.dpToPxInt(166);
        if(screenHeight>=1280){
            mItemHeight =ScreenUtils.dpToPxInt(170);
        }
    }

    @Override
    protected void convert(final BaseViewHolder helper, final UserPlayerVideoHistoryList item) {
        try {
            RelativeLayout re_item_video = helper.getView(R.id.re_item_video);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) re_item_video.getLayoutParams();
            layoutParams.height=mItemHeight;
            re_item_video.setLayoutParams(layoutParams);

            if(null!=item){
                helper.setText(R.id.tv_item_author_name, TextUtils.isEmpty(item.getUserName())?"火星人":item.getUserName())
                        .setText(R.id.tv_item_commend_count,TextUtils.isEmpty(item.getVideoCommendCount())?"0":item.getVideoCommendCount());

                TextView tv_item_desp = helper.getView(R.id.tv_item_desp);
                try {
                    String decode = URLDecoder.decode(TextUtils.isEmpty(item.getVideoDesp())?"":item.getVideoDesp(), "UTF-8");
                    //设置视频介绍，需要单独处理
                    SpannableString topicStyleContent = TextViewTopicSpan.getTopicStyleContent(decode, CommonUtils.getColor(R.color.app_style_text_color), tv_item_desp,null,null);
                    tv_item_desp.setText(topicStyleContent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //视频封面
                Glide.with(mContext)
                        .load(item.getVideoCover())
                        .thumbnail(0.1f)
                        .placeholder(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0,5)])
                        .error(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0,5)])
                        .crossFade()//渐变
                        .animate(R.anim.item_alpha_in)//加载中动画
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .centerCrop()//中心点缩放
                        .skipMemoryCache(true)//跳过内存缓存
                        .transform(new GlideRoundTransform(mContext))
                        .into((ImageView) helper.getView(R.id.iv_item_icon));

                //作者封面
                Glide.with(mContext)
                        .load(item.getUserCover())
                        .error(R.drawable.iv_mine)
                        .placeholder(R.drawable.iv_mine)
                        .crossFade()//渐变
                        .animate(R.anim.item_alpha_in)//加载中动画
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .centerCrop()//中心点缩放
                        .skipMemoryCache(true)//跳过内存缓存
                        .transform(new GlideCircleTransform(mContext))
                        .into((ImageView) helper.getView(R.id.iv_item_author_icon));

                //编辑菜单
                helper.setOnClickListener(R.id.btn_item_menu,new OnItemClickListener(item,helper));
                //编辑面板
                helper.setOnClickListener(R.id.ll_item_menu_view,new OnItemClickListener(item,helper));
                //删除视频
                helper.setOnClickListener(R.id.tv_item_delete_video,new OnItemClickListener(item,helper));
                //点击了用户头像
                helper.setOnClickListener(R.id.iv_item_author_icon,new OnItemClickListener(item,helper));
                //条目点击
                helper.itemView.setOnClickListener(new OnItemClickListener(item,helper));

                helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        Logger.d(TAG,"onLongClick已拦截");
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),true);
                        item.setIsSelector(true);
                        helper.setImageResource(R.id.btn_item_menu,item.getIsSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);
                        return true;
                    }
                });
            }
        }catch (Exception e){

        }
    }

    /**
     * 处理点击事件
     */
    private class OnItemClickListener implements View.OnClickListener{

        private final UserPlayerVideoHistoryList item;
        private final BaseViewHolder helper;

        public OnItemClickListener(UserPlayerVideoHistoryList item, BaseViewHolder helper) {
            this.item=item;
            this.helper=helper;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //编辑菜单
                case R.id.btn_item_menu:
                    if(item.getIsSelector()){
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),false);
                        item.setIsSelector(false);
                    }else{
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),true);
                        item.setIsSelector(true);
                    }
                    helper.setImageResource(R.id.btn_item_menu,item.getIsSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);
                    break;
                //编辑面板
                case R.id.ll_item_menu_view:
                    if(item.getIsSelector()){
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),false);
                        item.setIsSelector(false);
                    }
                    helper.setImageResource(R.id.btn_item_menu,item.getIsSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);
                    break;
                //删除视频
                case R.id.tv_item_delete_video:
                    if(null!=item&&null!=onUserPlayerHistoryClickListener){
                        onUserPlayerHistoryClickListener.onDeleteVideo(item,helper.getAdapterPosition());
                    }
                    break;
                //点击了用户头像
                case R.id.iv_item_author_icon:
                    if(null!=item&&null!=onUserPlayerHistoryClickListener){
                        onUserPlayerHistoryClickListener.onUserIcon(item.getUserId());
                    }
                    break;
                //默认点击了条目
                default:
                    if(null!=helper&&null!=onUserPlayerHistoryClickListener){
                        onUserPlayerHistoryClickListener.onItemClick(helper.getAdapterPosition());
                    }
            }
        }
    }
    /**
     * 显示某个View
     * @param menuView
     * @param contentView
     */
    private void showView(final View menuView, final View contentView, boolean isShow){
        if(isShow){
            contentView.setVisibility(View.GONE);
            menuView.setVisibility(View.VISIBLE);
            ScaleAnimation scaleAnimation = AnimationUtil.moveThisScaleViewToBigMenu();
            menuView.startAnimation(scaleAnimation);
        }else{
            ScaleAnimation scaleAnimation = AnimationUtil.moveThisScaleViewToDissmes();
            scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    menuView.setVisibility(View.GONE);
                    contentView.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            menuView.startAnimation(scaleAnimation);
        }
    }
}
