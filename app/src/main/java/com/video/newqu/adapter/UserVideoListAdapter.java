package com.video.newqu.adapter;

import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.contants.Cheeses;
import com.video.newqu.listener.OnUserVideoListener;
import com.video.newqu.util.AnimationUtil;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.TextViewSamllTopicSpan;
import com.video.newqu.util.Utils;
import com.video.newqu.model.GlideRoundTransform;
import com.video.newqu.view.widget.GlideCircleTransform;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017-05-22 21:33
 * 一个针对于自己 可以编辑列表的适配器,用户相关的额视频列表，适用于我发布的作品、收藏列表、用户中心列表
 */

public class UserVideoListAdapter extends BaseQuickAdapter<FollowVideoList.DataBean.ListsBean, BaseViewHolder> {

    public static final String TAG = UserVideoListAdapter.class.getSimpleName();
    private final int mFragmentType;//1：我的作品 2：喜欢 3：用户中心 4：关注列表
    private final OnUserVideoListener onUserVideoListener;
    private int mItemHeight;

    public UserVideoListAdapter(List<FollowVideoList.DataBean.ListsBean> listsBeanList, int fragmentType, OnUserVideoListener onUserVideoListener) {
        super(R.layout.re_user_video_list_item, listsBeanList);
        this.mFragmentType = fragmentType;
        this.onUserVideoListener=onUserVideoListener;
        int screenHeight = ScreenUtils.getScreenHeight();
        mItemHeight = ScreenUtils.dpToPxInt(150);
        if(screenHeight>=1280){
            mItemHeight =ScreenUtils.dpToPxInt(170);
        }
    }

    @Override
    protected void convert(final BaseViewHolder helper, final FollowVideoList.DataBean.ListsBean item) {
        try {
            RelativeLayout re_item_video = helper.getView(R.id.re_item_video);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) re_item_video.getLayoutParams();
            layoutParams.height=mItemHeight;
            re_item_video.setLayoutParams(layoutParams);
            if (null != item&&!TextUtils.isEmpty(item.getVideo_id())) {
                LinearLayout ll_user_data = helper.getView(R.id.ll_user_data);
                helper.setVisible(R.id.ll_item_menu_view,item.isSelector()?true:false);
                helper.setVisible(R.id.ll_content_view,item.isSelector()?false:true);
                helper.setImageResource(R.id.btn_item_menu,item.isSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);

                if(1==mFragmentType){
                    ll_user_data.setVisibility(View.INVISIBLE);
                    helper.setVisible(R.id.iv_private,TextUtils.isEmpty(item.getIs_private())?false:TextUtils.equals("0",item.getIs_private())?false:true);//该视频是否已公开
                    helper.setVisible(R.id.tv_item_public_video,TextUtils.isEmpty(item.getIs_private())?false:TextUtils.equals("1",item.getIs_private())?true:false);//是否显示公开视频按钮
                    helper.setVisible(R.id.tv_item_delete_video,true);
                    helper.setVisible(R.id.tv_item_unfollow_video,false);
                    helper.setVisible(R.id.btn_item_menu,true);
                }else if(2==mFragmentType){
                    ll_user_data.setVisibility(View.VISIBLE);
                    helper.setVisible(R.id.iv_private,false);
                    helper.setVisible(R.id.tv_item_unfollow_video,true);
                    helper.setVisible(R.id.tv_item_public_video,false);
                    helper.setVisible(R.id.tv_item_delete_video,false);
                    helper.setVisible(R.id.btn_item_menu,true);
                }else{
                    ll_user_data.setVisibility(View.INVISIBLE);
                    helper.setVisible(R.id.iv_private,false);
                    helper.setVisible(R.id.tv_item_unfollow_video,false);
                    helper.setVisible(R.id.tv_item_public_video,false);
                    helper.setVisible(R.id.tv_item_delete_video,false);
                    helper.setVisible(R.id.btn_item_menu,false);
                }

                helper.setText(R.id.tv_item_commend_count,TextUtils.isEmpty(item.getComment_times())?"0":item.getComment_times());
                helper.setText(R.id.tv_item_author_name,TextUtils.isEmpty(item.getNickname())?"火星人":item.getNickname());
                TextView tv_item_desp = helper.getView(R.id.tv_item_desp);
                try {
                    String decode = URLDecoder.decode(TextUtils.isEmpty(item.getDesp())?"":item.getDesp(), "UTF-8");
                    //设置视频介绍，需要单独处理
                    SpannableString topicStyleContent = TextViewSamllTopicSpan.getTopicStyleContent(decode, CommonUtils.getColor(R.color.white), tv_item_desp,null,null);
                    tv_item_desp.setText(topicStyleContent);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //视频封面
                Glide.with(mContext)
                        .load(item.getCover())
                        .thumbnail(0.1f)
                        .placeholder(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0, 5)])
                        .error(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0, 5)])
                        .crossFade()//渐变
                        .animate(R.anim.item_alpha_in)//加载中动画
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .centerCrop()//中心点缩放
                        .skipMemoryCache(true)//跳过内存缓存
                        .transform(new GlideRoundTransform(mContext))
                        .into((ImageView) helper.getView(R.id.iv_item_icon));
                //作者封面
                Glide.with(mContext)
                        .load(item.getLogo())
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
                //公开视频
                helper.setOnClickListener(R.id.tv_item_public_video,new OnItemClickListener(item,helper));
                //取消收藏视频
                helper.setOnClickListener(R.id.tv_item_unfollow_video,new OnItemClickListener(item,helper));
                //条目点击
                helper.itemView.setOnClickListener(new OnItemClickListener(item,helper));

                helper.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        //只拦截发布的作品界面和收藏界面长按事件
                        if(mFragmentType<=0|mFragmentType>=3){
                            return false;
                        }
                        if(item.isSelector()){
                            Logger.d(TAG,"onLongClick未拦截");
                            return false;
                        }
                        Logger.d(TAG,"onLongClick已拦截");
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),true);
                        item.setSelector(true);
                        helper.setImageResource(R.id.btn_item_menu,item.isSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);
                        return true;
                    }
                });
            }
        } catch (Exception e) {

        }
    }

    /**
     * 处理点击事件
     */
    private class OnItemClickListener implements View.OnClickListener{

        private final FollowVideoList.DataBean.ListsBean item;
        private final BaseViewHolder helper;

        public OnItemClickListener(FollowVideoList.DataBean.ListsBean item, BaseViewHolder helper) {
            this.item=item;
            this.helper=helper;
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                //编辑菜单
                case R.id.btn_item_menu:
                    if(item.isSelector()){
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),false);
                        item.setSelector(false);
                    }else{
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),true);
                        item.setSelector(true);
                    }
                    helper.setImageResource(R.id.btn_item_menu,item.isSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);
                    break;
                //编辑面板
                case R.id.ll_item_menu_view:
                    if(item.isSelector()){
                        showView(helper.getView(R.id.ll_item_menu_view),helper.getView(R.id.ll_content_view),false);
                        item.setSelector(false);
                    }
                    helper.setImageResource(R.id.btn_item_menu,item.isSelector()?R.drawable.btn_video_edit_close_selector:R.drawable.btn_video_edit_menu_selector);
                    break;
                //删除视频
                case R.id.tv_item_delete_video:
                    if(null!=item&&null!=onUserVideoListener){
                        onUserVideoListener.onDeleteVideo(item.getVideo_id());
                    }
                    break;
                //公开视频
                case R.id.tv_item_public_video:
                    if(null!=item&&null!=onUserVideoListener){
                        onUserVideoListener.onPublicVideo(item.getVideo_id());
                    }
                    break;
                //点击了用户头像
                case R.id.iv_item_author_icon:
                    if(null!=item&&null!=onUserVideoListener){
                        onUserVideoListener.onHeaderIcon(item.getUser_id());
                    }
                    break;
                //取消收藏视频
                case R.id.tv_item_unfollow_video:
                    if(null!=item&&null!=onUserVideoListener){
                        onUserVideoListener.onUnFollowVideo(item.getVideo_id());
                    }
                    break;
                //默认点击了条目
                default:
                    if(null!=helper&&null!=onUserVideoListener){
                        onUserVideoListener.onItemClick(helper.getAdapterPosition());
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
