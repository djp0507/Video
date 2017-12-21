
package com.video.newqu.ui.presenter;

import android.content.Context;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.bean.NotifactionMessageInfo;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.ui.contract.MessageLocationContract;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017/5/23 10:53
 * 只读取本地数据库的消息列表
 */

public class MessageLocationPresenter extends RxPresenter<MessageLocationContract.View> implements MessageLocationContract.Presenter<MessageLocationContract.View> {

    private static final String TAG = MessageLocationPresenter.class.getSimpleName();

    private final Context context;
    private boolean isLoading;


    public boolean isLoading() {
        return isLoading;
    }



    public MessageLocationPresenter(Context context){
        this.context=context;
    }

    /**
     * 分页获取历史消息
     */
    @Override
    public void getMessageList() {
        if(isLoading) return;
        isLoading=true;
//        try {
////            List<MessageListInfo> messageListInfos = messageManager.getMessageList();
//            //分页查询
//            List<NotifactionMessageInfo> messageListInfos = VideoApplication.mMessageManager.queryMessageListOfPage(page, pageCount);
//            Log.d(TAG, "getMessageList: messageListInfos.size()="+messageListInfos.size());
//            if(null!=messageListInfos&&messageListInfos.size()>0){
//
//                Collections.sort(messageListInfos, new Comparator<NotifactionMessageInfo>() {
//                    @Override
//                    public int compare(NotifactionMessageInfo lhs, NotifactionMessageInfo rhs) {
//                        return rhs.getAdd_time().compareTo(lhs.getAdd_time());
//                    }
//                });
//                mView.showMessageList(messageListInfos);
//            }else if(null!=messageListInfos&&messageListInfos.size()<=0){
//                mView.getMessageEmpty("没有更多消息了");
//            }else{
//                mView.getMessageError("查询消息失败");
//            }
//        }catch (Exception e){
//            mView.getMessageError("查询消息失败");
//        }
        List<NotifactionMessageInfo> messageInfos= (List<NotifactionMessageInfo>)  ApplicationManager.getInstance().getCacheExample().getAsObject(VideoApplication.getLoginUserID()+ Constant.CACHE_USER_MESSAGE);

        isLoading=false;
            if(null!=messageInfos&&messageInfos.size()>0){
                Collections.sort(messageInfos, new Comparator<NotifactionMessageInfo>() {
                    @Override
                    public int compare(NotifactionMessageInfo lhs, NotifactionMessageInfo rhs) {
                        return rhs.getAdd_time().compareTo(lhs.getAdd_time());
                    }
                });
                mView.showMessageList(messageInfos);
            }else if(null!=messageInfos&&messageInfos.size()<=0){
                mView.getMessageEmpty("没有更多消息了");
            }else{
                mView.getMessageEmpty("没有更多消息了");
            }
    }


}
