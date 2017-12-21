package com.video.newqu.adapter;

import android.text.SpannableString;
import android.text.TextUtils;
import android.widget.TextView;
import com.video.newqu.R;
import com.video.newqu.bean.ComentList;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.TextViewTopicSpan;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/12/13.
 * 视频播放界面自动滚动留言列表
 */

public class AutoPollCommentAdapter extends BaseQuickAdapter<ComentList.DataBean.CommentListBean,BaseViewHolder> {

    public AutoPollCommentAdapter(List<ComentList.DataBean.CommentListBean> data) {
        super(R.layout.auto_poll_comment_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, ComentList.DataBean.CommentListBean item) {
        helper.setText(R.id.tv_item_name,item.getNickname()+"：");
        try {
            String decode = URLDecoder.decode(item.getComment(), "utf-8");
            String coment=null;
            //回复留言
            if(!TextUtils.isEmpty(item.getTo_nickname())&&!TextUtils.isEmpty(item.getTo_user_id())){
                coment="回复@"+item.getTo_nickname()+" :"+decode;
                //回复视频
            }else{
                coment=decode;
            }
            TextView view = helper.getView(R.id.tv_item_content);
            SpannableString topicStyleContent = TextViewTopicSpan.getTopicStyleContent(coment, CommonUtils.getColor(R.color.app_text_style), view, null,item.getTo_user_id());
            helper.setText(R.id.tv_item_content,topicStyleContent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
