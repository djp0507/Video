
package com.video.newqu.ui.presenter;

import android.content.Context;
import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.contants.NetContants;
import com.video.newqu.ui.contract.MessageContract;
import com.video.newqu.util.Logger;
import java.util.HashMap;
import java.util.Map;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;


/**
 * TinyHung@outlook.com
 * 2017/5/23 10:53
 * 推送消息获取
 */

public class MessagePresenter extends RxPresenter<MessageContract.View> implements MessageContract.Presenter<MessageContract.View> {

    private static final String TAG = MessagePresenter.class.getSimpleName();

    private final Context context;
    private boolean isLoading;

    public boolean isLoading() {
        return isLoading;
    }

    public MessagePresenter(Context context){
        this.context=context;
    }

    @Override
    public void getMessageList(String userID,String page,String pageSize) {
        if(isLoading) return;
        isLoading=true;
        Map<String,String> params=new HashMap<>();
//        params.put("user_id",userID);
//        params.put("page",page);
//        params.put("page_size",pageSize);

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.BASE_VIDEO_HOST + "recommend", String.class, null, true, true, true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String data) {
                isLoading=false;
//                if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().size()>0){
//                    mView.showMessageInfo(data);
//                }else if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().size()<=0){
//                    mView.getMessageEmpty("没有更多了");
//                }else{
//                    mView.getMessageError("加载失败");
//                }
                Logger.d(TAG,"data="+data);
            }
        });
        addSubscrebe(subscribe);
    }
}