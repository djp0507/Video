package com.video.newqu.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseMultiItemQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.contants.Cheeses;
import com.video.newqu.holder.ComendVideoItem;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.util.Utils;

import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/6/2 12:45
 * 我的作品多视图切换和可自动播放的列表适配器
 */

public class FragmentComendVideoListAdapter extends BaseMultiItemQuickAdapter<ComendVideoItem,BaseViewHolder>{

    private int itemType;
    private List<FollowVideoList.DataBean.ListsBean> videoList;
    private final ScaleAnimation followAnimation;
    private final int mItemHeight;

    public FragmentComendVideoListAdapter(List<ComendVideoItem> data, List<FollowVideoList.DataBean.ListsBean> videoList, int itemType, int screenWidth, ScaleAnimation followAnimation) {
        super(data);
        addItemType(0,R.layout.video_list_works_th_item);
        addItemType(1,R.layout.follow_video_list_item);
        this.itemType=itemType;
        this.videoList=videoList;
        mItemHeight = (screenWidth-12)/3;
        this.followAnimation=followAnimation;
    }

    @Override
    protected void convert(BaseViewHolder helper, ComendVideoItem item) {
        if(0==itemType){
            setItem_0Content(helper,item);
        }else if(1==itemType){
            setItem_1Content(helper,item);
        }
    }

    /**
     * 缩略图
     * @param helper
     * @param item
     */
    private void setItem_0Content(BaseViewHolder helper, ComendVideoItem item) {
        if(null==item) return;
        try{
            final FollowVideoList.DataBean.ListsBean listsBean = videoList.get(helper.getPosition());
            if(null!=listsBean){
                RelativeLayout re_itecon_layout = helper.getView(R.id.re_item_icon_layout);
                RelativeLayout.LayoutParams linearParams = (RelativeLayout.LayoutParams)re_itecon_layout.getLayoutParams();
                linearParams.height = mItemHeight;
                re_itecon_layout.setLayoutParams(linearParams);
                ImageView iv_private = (ImageView) helper.getView(R.id.iv_private);
                ImageView iv_item_icon_layout = helper.getView(R.id.iv_item_icon_layout);
                if(null!=listsBean){
                    //封面
                    Glide.with(mContext)
                            .load(listsBean.getCover())
                            .crossFade()//渐变
                            .error(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0,5)])
                            .placeholder(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0,5)])
                            .animate(R.anim.item_alpha_in)//加载中动画
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                            .centerCrop()//中心点缩放
                            .skipMemoryCache(true)//跳过内存缓存
                            .into(iv_item_icon_layout);

                    iv_private.setVisibility(TextUtils.isEmpty(listsBean.getIs_private())?View.GONE:TextUtils.equals("0",listsBean.getIs_private())?View.GONE:View.VISIBLE);

                    iv_item_icon_layout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            VideoDetailsActivity.start(mContext,listsBean.getVideo_id(),listsBean.getUser_id(),false);
                        }
                    });
                }
            }
        }catch (Exception e){

        }
    }

    /**
     * 垂直条目
     * @param helper
     * @param item
     */
    private void setItem_1Content(BaseViewHolder helper, ComendVideoItem item) {
        if(null==item)return;
        try {
            FollowVideoList.DataBean.ListsBean listsBean = videoList.get(helper.getPosition());
            item.onBindViewHolder(helper,listsBean, helper.getAdapterPosition(),followAnimation);
        }catch (Exception e){

        }
    }



    /**
     * 设置新数据
     * @param videoItemList
     * @param listsBeanList
     * @param tabMenuType
     */
    public void setNewData(List<ComendVideoItem> videoItemList, List<FollowVideoList.DataBean.ListsBean> listsBeanList, int tabMenuType) {
        this.itemType=tabMenuType;
        this.videoList=listsBeanList;
        this.setNewData(videoItemList);
    }


    /**
     * 为适配器添加新的数据
     * @param videoItemList
     * @param listsBeanList
     */
    public void addListData(List<ComendVideoItem> videoItemList,List<FollowVideoList.DataBean.ListsBean> listsBeanList){

        if(null!=listsBeanList&&listsBeanList.size()>0){
            videoList.addAll(listsBeanList);
        }

        addData(videoItemList);
    }
    public List<FollowVideoList.DataBean.ListsBean>  getVideoData() {
        return videoList;
    }

    public int getItemType() {
        return itemType;
    }

}
