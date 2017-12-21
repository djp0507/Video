package com.video.newqu.adapter;

import com.video.newqu.R;
import com.video.newqu.bean.TopicVideoList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.holder.TopicVideoItem;
import com.video.newqu.holder.VideoListViewHolder;
import com.video.newqu.util.AnimationUtil;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/5/26 11:55
 * 话题列表自动播放适配器
 */

public class TopicAutoVideoListAdapter extends BaseQuickAdapter<TopicVideoItem,VideoListViewHolder> {

    private List<TopicVideoList.DataBean.VideoListBean> listsBeanList;

    public TopicAutoVideoListAdapter(List<TopicVideoItem> videoItems, List<TopicVideoList.DataBean.VideoListBean> listsBeanList) {
        super(R.layout.follow_video_list_item, videoItems);
        this.listsBeanList=listsBeanList;
    }

    @Override
    protected void convert(VideoListViewHolder helper, TopicVideoItem item) {
        try {
            TopicVideoList.DataBean.VideoListBean videoListBean = listsBeanList.get(helper.getPosition());
            if(null!=videoListBean){
                item.onBindViewHolder(helper,videoListBean,helper.getAdapterPosition(), AnimationUtil.followAnimation());
            }
        }catch (Exception e){

        }

    }

    /**
     * 为适配器添加新的数据
     * @param videoItems
     * @param videoListInfos
     */
    public void addListData(List<TopicVideoItem> videoItems,List<TopicVideoList.DataBean.VideoListBean> videoListInfos){

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
    public void setNewListData(List<TopicVideoItem> videoItemList, List<TopicVideoList.DataBean.VideoListBean> followVideoList) {
        if(null!=listsBeanList) listsBeanList.clear();
        this.listsBeanList=followVideoList;
        setNewData(videoItemList);
    }

    /**
     * 返回最新的数据集合
     * @return
     */
    public List<TopicVideoList.DataBean.VideoListBean> getVideoList() {
        return listsBeanList;
    }
}
