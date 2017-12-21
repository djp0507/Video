package com.video.newqu.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.FindVideoListInfo;
import com.video.newqu.contants.Cheeses;
import com.video.newqu.util.Utils;
import java.util.List;


/**
 * @time 2017/5/24 10:20
 * @des $发现列表子适配器
 */
public class FindVideoListItemAdapter extends BaseAdapter {


    private static final String TAG = FindVideoListItemAdapter.class.getSimpleName();
    private final Context context;
    private final List<FindVideoListInfo.DataBean.VideosBean> videoListInfos;
    private final int count;
    private final int mScreenWidth;
    private LayoutInflater mInflater;

    public FindVideoListItemAdapter(Context context, List<FindVideoListInfo.DataBean.VideosBean> videoListInfos, int count,int screenWidth) {
        this.context=context;
        this.videoListInfos=videoListInfos;
        this.count=count;
        mInflater = LayoutInflater.from(context);
        this.mScreenWidth=screenWidth;
    }


    @Override
    public int getCount() {
        return videoListInfos==null?0:videoListInfos.size()>4?count:videoListInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return videoListInfos==null?null:videoListInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuGridViewHolder holder;
        if(null==convertView){
            convertView=mInflater.inflate(R.layout.video_find_list_item,null);
            holder=new MenuGridViewHolder();
            holder.iv_item_icon= (ImageView) convertView.findViewById(R.id.iv_item_icon);
            convertView.setTag(holder);
        }else{
            holder= (MenuGridViewHolder) convertView.getTag();
        }
        try {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.iv_item_icon.getLayoutParams();
            layoutParams.width=RelativeLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height= (((mScreenWidth- Utils.dip2px(context,2))/2)-2)/2;
            holder.iv_item_icon.setLayoutParams(layoutParams);

            FindVideoListInfo.DataBean.VideosBean videosBean = videoListInfos.get(position);
            if(null!=videosBean){
                //视频封面
                Glide.with(context)
                        .load(videosBean.getCover())
                        .error(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0,5)])
                        .placeholder(Cheeses.IMAGE_EMPTY_COLOR[Utils.getRandomNum(0,5)])
                        .crossFade()//渐变
                        .thumbnail(0.1f)
                        .animate(R.anim.item_alpha_in)//加载中动画
                        .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                        .centerCrop()//中心点缩放
                        .skipMemoryCache(true)//跳过内存缓存
                        .into(holder.iv_item_icon);
            }
        }catch (Exception e){

        }

        return convertView;
    }

    public List<FindVideoListInfo.DataBean.VideosBean> getData() {
        return videoListInfos;
    }

    private class MenuGridViewHolder{
        private ImageView iv_item_icon;
    }
}
