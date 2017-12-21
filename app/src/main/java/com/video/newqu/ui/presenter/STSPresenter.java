
package com.video.newqu.ui.presenter;

import android.content.Context;
import android.text.TextUtils;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.ui.contract.STSContract;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/6/1 15:00
 * 版本更新
 */
public class STSPresenter extends RxPresenter<STSContract.View> implements STSContract.Presenter<STSContract.View> {


    private final Context context;
    public STSPresenter(Context context){
        this.context=  context;
    }

    /**
     * 获取上传文件的临时Token
     * @param stsServer
     */
    @Override
    public void getFederationToken(String stsServer) {

        Subscription subscribe = HttpCoreEngin.get(context).rxpost(stsServer, String.class,null,false,false,false).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String data) {
                if(!TextUtils.isEmpty(data)){
                    try {
                        JSONObject jsonObject=new JSONObject(data);
                        if(jsonObject.length()>0){
                            String ak=jsonObject.getString("");
                            String sk=jsonObject.getString("");
                            String token=jsonObject.getString("");
                            String expiration=jsonObject.getString("");
                            OSSFederationToken ossFederationToken=new OSSFederationToken(ak,sk,token,expiration);
                            mView.getOSSFederationToken(ossFederationToken);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        addSubscrebe(subscribe);
    }
}
