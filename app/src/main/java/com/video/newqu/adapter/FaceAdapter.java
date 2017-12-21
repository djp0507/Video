package com.video.newqu.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.video.newqu.R;
import com.video.newqu.bean.ChatEmoji;

import java.util.List;


/**
 * TinyHung@outlook.com
 * 2017/6/13 19:46
 * 表情输入框的辅助
 */
public class FaceAdapter extends BaseAdapter {

    private List<ChatEmoji> data;

    private LayoutInflater inflater;

    private int size=0;

    public FaceAdapter(Context context, List<ChatEmoji> list) {
        this.inflater=LayoutInflater.from(context);
        this.data=list;
        this.size=list.size();
    }

    @Override
    public int getCount() {
        return this.size;
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatEmoji emoji=data.get(position);
        ViewHolder viewHolder=null;
        if(convertView == null) {
            viewHolder=new ViewHolder();
            convertView=inflater.inflate(R.layout.list_item_face, null);
            viewHolder.iv_face=(ImageView)convertView.findViewById(R.id.item_iv_face);
            convertView.setTag(viewHolder);
        } else {
            viewHolder=(ViewHolder)convertView.getTag();
        }
        try {
            if(emoji.getId() == R.drawable.face_del_icon) {
                convertView.setBackgroundDrawable(null);
                viewHolder.iv_face.setImageResource(emoji.getId());
            } else if(TextUtils.isEmpty(emoji.getCharacter())) {
                convertView.setBackgroundDrawable(null);
                viewHolder.iv_face.setImageDrawable(null);
            } else {
                viewHolder.iv_face.setTag(emoji);
                viewHolder.iv_face.setImageResource(emoji.getId());
            }
        }catch (Exception e){

        }

        return convertView;
    }

    class ViewHolder {

        public ImageView iv_face;
    }
}