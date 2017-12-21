package com.video.newqu.ui.presenter;

import android.content.Context;
import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.bean.StickerNetInfo;
import com.video.newqu.contants.NetContants;
import com.video.newqu.ui.contract.MediaStickerContract;
import com.video.newqu.util.Logger;
import java.util.HashMap;
import java.util.Map;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/5/23 10:53
 * 获取贴纸列表
 */

public class MediaStickerPresenter extends RxPresenter<MediaStickerContract.View> implements MediaStickerContract.Presenter<MediaStickerContract.View> {

    private final Context context;
    public static final String TAG = "MediaStickerPresenter";

    public MediaStickerPresenter(Context context){
        this.context=context;
    }


    @Override
    public void getStickerTypeList(String typeID,int page,int pageSize) {

        Map<String,String> params=new HashMap<>();
        params.put("type_id",typeID);
        params.put("page",page+"");
        params.put("page_size",pageSize+"");

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.MEDIA_EDIT_HOST + "sticker_lists",StickerNetInfo.class, params, false,false,false).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<StickerNetInfo>() {
            @Override
            public void call(StickerNetInfo data) {

                Logger.d(TAG,"data="+data);
                if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().size()>0){
                    mView.showStickerList(data.getData());
                }else if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().size()<=0){
                    mView.showStickerEmpty("获取贴纸为空");
                }else{
                    mView.showStickerError("获取贴纸失败");
                }
            }
        });
        addSubscrebe(subscribe);
    }
}
