
package com.video.newqu.ui.presenter;

import android.content.Context;
import android.text.TextUtils;

import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.bean.FansInfo;
import com.video.newqu.bean.FollowUserList;
import com.video.newqu.contants.NetContants;
import com.video.newqu.ui.contract.FansContract;
import com.video.newqu.util.ToastUtils;

import java.util.HashMap;
import java.util.Map;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/6/1 15:00
 * 获取粉丝列表
 */
public class FansPresenter extends RxPresenter<FansContract.View> implements FansContract.Presenter<FansContract.View> {

    private static final String TAG = FansPresenter.class.getSimpleName();
    private final Context context;

    public FansPresenter(Context context){
        this.context=context;
    }

    /**
     * 获取粉丝列表
     * @param userID
     * @param page
     * @param pageSize
     */

    @Override
    public void getFanslist(String userID, String page, String pageSize) {

        Map<String,String> params=new HashMap<>();

        params.put("user_id",userID);
        params.put("page",page);
        params.put("page_size",pageSize);

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.BASE_HOST + "fans_list", FansInfo.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<FansInfo>() {
            @Override
            public void call(FansInfo data) {
                if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().getList().size()>0){
                    mView.showFansList(data);
                }else if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().getList().size()<=0){
                    mView.showFansListEmpty("没有更多数据了");
                }else{
                    mView.showFansListError("加载粉丝列表失败");
                }
            }
        });
        addSubscrebe(subscribe);
    }

    /**
     * 关注
     * @param userID
     * @param fansUserID
     */
    @Override
    public void onFollowUser(String fansUserID, String userID) {

        Map<String,String> params=new HashMap<>();

        params.put("fans_user_id",fansUserID);
        params.put("user_id",userID);

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.BASE_HOST + "follow", String.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String data) {
                if(!TextUtils.isEmpty(data)){
                    mView.showFollowUser(data);
                }else{
                    ToastUtils.shoCenterToast("关注失败");
                }
            }
        });
        addSubscrebe(subscribe);
    }

    /**
     * 获取关注列表
     * @param userID
     * @param page
     * @param pageSize
     */
    @Override
    public void getFollowUserList(String userID, String page, String pageSize) {

        Map<String,String> params=new HashMap<>();

        params.put("user_id",userID);
        params.put("page",page);
        params.put("page_size",pageSize);

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(NetContants.BASE_HOST + "follow_list", FollowUserList.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<FollowUserList>() {
            @Override
            public void call(FollowUserList data) {
                if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().getList().size()>0){
                    mView.showFollowUserList(data);
                }else if(null!=data&&1==data.getCode()&&null!=data.getData()&&data.getData().getList().size()<=0){
                    mView.showFollowUserListEmpty("没有更多数据");
                }else{
                    mView.showFollowUserListError("获取关注者失败");
                }
            }
        });
        addSubscrebe(subscribe);
    }
}
