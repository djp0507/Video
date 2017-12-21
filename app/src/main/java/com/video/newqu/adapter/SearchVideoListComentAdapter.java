package com.video.newqu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.video.newqu.R;
import com.video.newqu.bean.SearchResultInfo;

import java.util.List;


/**
 * @time 2017/5/22 22:10
 * @des $视频列表评论列表
 * 搜索列表视频评论列表
 */
public class SearchVideoListComentAdapter extends BaseAdapter {
    private final Context context;
    private final List<SearchResultInfo.DataBean.VideoListBean.CommentListBean> comment_list;
    private LayoutInflater mInflater;


    public SearchVideoListComentAdapter(Context context, List<SearchResultInfo.DataBean.VideoListBean.CommentListBean> comment_list) {
        this.context=context;
        this.comment_list=comment_list;
        mInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return comment_list==null?0:comment_list.size()>=2?2:comment_list.size();
    }

    @Override
    public Object getItem(int position) {
        return comment_list==null?null:comment_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuGridViewHolder holder;
        if(null==convertView){
            convertView=mInflater.inflate(R.layout.video_list_coment_item,null);
            holder=new MenuGridViewHolder();
            holder.tv_item_name= (TextView) convertView.findViewById(R.id.tv_item_name);
            holder.tv_item_content= (TextView) convertView.findViewById(R.id.tv_item_content);
            convertView.setTag(holder);
        }else{
            holder= (MenuGridViewHolder) convertView.getTag();
        }
        try {
            SearchResultInfo.DataBean.VideoListBean.CommentListBean commentListBean = comment_list.get(position);
            if(null!=commentListBean){
                holder.tv_item_name.setText(commentListBean.getNickname());
                holder.tv_item_content.setText("："+commentListBean.getComment());
            }
        }catch (Exception e){

        }

        return convertView;
    }
    private class MenuGridViewHolder{
        private TextView tv_item_name;
        private TextView tv_item_content;
    }
}
