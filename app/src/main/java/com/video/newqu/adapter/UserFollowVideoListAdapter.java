package com.video.newqu.adapter;

import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.contants.Cheeses;
import com.video.newqu.listener.VideoComentClickListener;
import com.video.newqu.util.CommonUtils;
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

public class UserFollowVideoListAdapter extends BaseQuickAdapter<FollowVideoList.DataBean.ListsBean, BaseViewHolder> {

    public static final String TAG = UserFollowVideoListAdapter.class.getSimpleName();
    private final int mFragmentType;//1：我的作品 2：喜欢 3：用户中心 4：关注列表
    private final VideoComentClickListener videoComentClickListener;
    private  int mItemHeight;


    public UserFollowVideoListAdapter(List<FollowVideoList.DataBean.ListsBean> listsBeanList, int fragmentType, VideoComentClickListener videoComentClickListener) {
        super(R.layout.re_follow_video_list_item, listsBeanList);
        this.mFragmentType = fragmentType;
        this.videoComentClickListener=videoComentClickListener;
        int screenHeight = ScreenUtils.getScreenHeight();
        mItemHeight = ScreenUtils.dpToPxInt(250);
        if(screenHeight>=1280){
            mItemHeight =ScreenUtils.dpToPxInt(266);
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
                helper.setText(R.id.tv_item_follow_count, item.getCollect_times());
                if (1 == item.getIs_interest()) {
                    helper.setImageResource(R.id.iv_item_follow, R.drawable.ic_follow_red);
                    helper.setTextColor(R.id.tv_item_follow_count, CommonUtils.getColor(R.color.tips_color));
                } else {
                    helper.setImageResource(R.id.iv_item_follow, R.drawable.ic_follow_white);
                    helper.setTextColor(R.id.tv_item_follow_count, CommonUtils.getColor(R.color.white));
                }
                helper.setText(R.id.tv_item_author_name,item.getNickname());

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

                helper.setOnClickListener(R.id.iv_item_author_icon, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(null!=videoComentClickListener){
                            videoComentClickListener.onAuthorClick(item.getUser_id());
                        }
                    }
                });

                helper.setOnClickListener(R.id.iv_item_icon, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(null!=videoComentClickListener){
                            videoComentClickListener.onItemClick(helper.getPosition());
                        }
                    }
                });
            }
        } catch (Exception e) {

        }
    }
}
