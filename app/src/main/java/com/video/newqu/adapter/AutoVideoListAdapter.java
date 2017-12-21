package com.video.newqu.adapter;

import android.view.animation.ScaleAnimation;
import com.video.newqu.R;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.holder.VideoItem;
import com.video.newqu.holder.VideoListViewHolder;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/5/26 11:55
 * 自动播放视频列表适配器
 */

public class AutoVideoListAdapter extends BaseQuickAdapter<VideoItem,VideoListViewHolder> {


    private final ScaleAnimation followScaleAnimation;
    private List<FollowVideoList.DataBean.ListsBean> listsBeanList;

    public AutoVideoListAdapter(List<VideoItem> videoItems, List<FollowVideoList.DataBean.ListsBean> listsBeanList, ScaleAnimation followScaleAnimation) {
        super(R.layout.follow_video_list_item, videoItems);
        this.listsBeanList=listsBeanList;
        this.followScaleAnimation=followScaleAnimation;
    }

    @Override
    protected void convert(VideoListViewHolder helper, VideoItem item) {
        try {
            FollowVideoList.DataBean.ListsBean followVideoInfo = listsBeanList.get(helper.getPosition());
            if(null!=followVideoInfo){
                item.onBindViewHolder(helper,followVideoInfo,helper.getAdapterPosition(),followScaleAnimation);
            }
        }catch (Exception e){

        }
    }

    /**
     * 为适配器添加新的数据
     * @param videoItems
     * @param videoListInfos
     */
    public void addListData(List<VideoItem> videoItems,List<FollowVideoList.DataBean.ListsBean> videoListInfos){

        if(null!=videoListInfos&&videoListInfos.size()>0){
            listsBeanList.addAll(videoListInfos);
        }
        addData(videoItems);
    }


    /**
     * 设置全新数据
     * @param videoItemList
     * @param followVideoList
     */
    public void setNewListData(List<VideoItem> videoItemList, List<FollowVideoList.DataBean.ListsBean> followVideoList) {
        if(null!=listsBeanList) listsBeanList.clear();
        this.listsBeanList=followVideoList;
        setNewData(videoItemList);
    }

    /**
     * 返回最新的数据集合
     * @return
     */
    public List<FollowVideoList.DataBean.ListsBean> getVideoList() {
        return listsBeanList;
    }
}
