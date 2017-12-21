
package com.video.newqu.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.contants.NetContants;
import com.video.newqu.ui.contract.AuthorListContract;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/6/1 15:00
 * 获取用户的上传的视频列表
 */
public class AuthorListPresenter extends RxPresenter<AuthorListContract.View> implements AuthorListContract.Presenter<AuthorListContract.View> {

    private static final String TAG = AuthorListPresenter.class.getSimpleName();
    private final Context context;

    public AuthorListPresenter(Context context){
        this.context=context;
    }

    @Override
    public void getUpLoadVideoList(String userID,String fansID, String page, String pageSize) {

        Map<String,String> params=new HashMap<>();
        params.put("user_id",userID);
        params.put("page",page);
        params.put("page_size",pageSize);
        params.put("visit_user_id",fansID);

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.BASE_VIDEO_HOST + "list_byUserId", FollowVideoList.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<FollowVideoList>() {
            @Override
            public void call(FollowVideoList data) {
                if(null!=data&&null!=data.getData()&&data.getData().getLists().size()>0){
                    mView.showUpLoadVideoList(data);
                }else{
                    mView.showErrorView();
                }
            }
        });
        addSubscrebe(subscribe);
    }

    /**
     * 举报视频
     * @param userID
     * @param videoID
     */
    @Override
    public void onReportVideo(String userID, String videoID) {
        Map<String,String> params=new HashMap<>();
        params.put("user_id",userID);
        params.put("video_id",videoID);

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.BASE_HOST + "accuse_video", String.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String data) {
                if(!TextUtils.isEmpty(data)){
                    mView.showReportVideoResult(data);
                }
            }
        });

        addSubscrebe(subscribe);
    }
}
