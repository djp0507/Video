package com.video.newqu.adapter;

import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.NotifactionMessageInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.util.TimeUtils;
import com.video.newqu.view.widget.GlideCircleTransform;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/12/14.
 * 首页消息
 */

public class MessageListAdapter extends BaseQuickAdapter<NotifactionMessageInfo,BaseViewHolder> {
    public MessageListAdapter( List<NotifactionMessageInfo> data) {
        super(R.layout.recyler_message_list_item, data);
    }

    @Override
    protected void convert(final BaseViewHolder helper, final NotifactionMessageInfo item) {
        if(null==item) return;
        if(1==item.getItemType()){
            helper.setVisible(R.id.re_item_icon,false);
            helper.setVisible(R.id.btn_clickevent,false);
            helper.setVisible(R.id.re_icon,false);
        }else if(2==item.getItemType()){
            helper.setVisible(R.id.re_item_icon,false);
            helper.setVisible(R.id.btn_clickevent,true);
            helper.setVisible(R.id.re_icon,true);
        }else if(0==item.getItemType()){
            helper.setVisible(R.id.re_item_icon,true);
            helper.setVisible(R.id.re_icon,true);
            helper.setVisible(R.id.btn_clickevent,false);
        }

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
                .into((ImageView) helper.getView(R.id.iv_item_user_icon));

        if(0==item.getItemType()){
            //封面
            Glide.with(mContext)
                    .load(item.getCover())
                    .crossFade()//渐变
                    .animate(R.anim.item_alpha_in)//加载中动画
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                    .centerCrop()//中心点缩放
                    .skipMemoryCache(true)//跳过内存缓存
                    .into((ImageView) helper.getView(R.id.iv_video_thbum));
        }else{
            helper.setImageResource(R.id.iv_video_thbum,R.drawable.error_big);
        }

        helper.setText(R.id.tv_item_username,item.getNickname())
                .setText(R.id.tv_item_content,item.getDesp());

        String addTime=item.getAdd_time()+"000";
        helper.setText(R.id.tv_time, TimeUtils.getTilmNow(Long.parseLong(addTime)));

        helper.addOnLongClickListener(R.id.ll_item_bg);
        //条目点击事件
        helper.setOnClickListener(R.id.re_item, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnItemClickListener){
                    mOnItemClickListener.onItemClick(helper.getAdapterPosition());
                }
            }
        });
        //头像点击事件
        helper.setOnClickListener(R.id.iv_item_user_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnItemClickListener){
                    mOnItemClickListener.onUserClick(item.getUser_id());
                }
            }
        });
        //其他类型事件
        helper.setOnClickListener(R.id.btn_clickevent, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnItemClickListener){
                    mOnItemClickListener.onOthorClick(item.getTopic());
                }
            }
        });
    }

    public interface OnItemClickListener{
        void onItemClick(int poistion);
        void onUserClick(String userID);
        void onOthorClick(String topic);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }
}
