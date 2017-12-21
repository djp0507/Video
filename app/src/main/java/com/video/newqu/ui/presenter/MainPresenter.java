
package com.video.newqu.ui.presenter;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSONArray;
import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.RxPresenter;
import com.video.newqu.bean.DeviceInfo;
import com.video.newqu.contants.Constant;
import com.video.newqu.contants.NetContants;
import com.video.newqu.ui.contract.MainContract;
import com.video.newqu.util.Logger;
import com.video.newqu.util.SharedPreferencesUtil;
import com.video.newqu.util.SystemUtils;
import com.video.newqu.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/6/1 15:00
 * 版本更新
 */
public class MainPresenter extends RxPresenter<MainContract.View> implements MainContract.Presenter<MainContract.View> {

    private static final String TAG = MainPresenter.class.getSimpleName();
    private final Context context;
    public MainPresenter(Context context){
        this.context=  context;
    }

    /**
     * 注册用户信息
     */
    @Override
    public void register() {

    }

    /**
     * 统计安装信息
     */
    public void registerApp() {
        DeviceInfo deviceInfo=new DeviceInfo();
        deviceInfo.setLocation_longitude("0");//经度
        deviceInfo.setLocation_latitude("0");//纬度
        deviceInfo.setApp_ini(Utils.getVersionCode()+"");
        deviceInfo.setBrand(Build.BRAND);//手机品牌
        deviceInfo.setImeil(VideoApplication.mUuid);/// /设备号
        deviceInfo.setModel(Build.MODEL);//手机型号
        deviceInfo.setSdk_ini(Build.VERSION.RELEASE);

        try {
            String[] locationID = SystemUtils.getLocationID();
            Logger.d(TAG,"locationID="+locationID);
            if(null!=locationID&&locationID.length>0){
                deviceInfo.setLocation_longitude(locationID[0]);
                deviceInfo.setLocation_latitude(locationID[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            final String json = JSONArray.toJSON(deviceInfo).toString();
            Log.d("MainPresenter", "run: json="+json);
            Map<String,String> params=new HashMap<>();
            params.put("imeil", VideoApplication.mUuid);
            params.put("brand", deviceInfo.getBrand());
            params.put("location",TextUtils.isEmpty(deviceInfo.getLocation_longitude())?"0,0": deviceInfo.getLocation_longitude()+","+deviceInfo.getLocation_latitude());
            params.put("app_ini", deviceInfo.getApp_ini());
            params.put("model", deviceInfo.getModel());
            params.put("sdk_ini", deviceInfo.getModel());
            HttpCoreEngin.get(context).rxpost(NetContants.BASE_HOST + "open_app",String.class,params,false,false,false).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
                @Override
                public void call(String data) {
                    if(!TextUtils.isEmpty(data)){
                        try {
                            JSONObject jsonObject=new JSONObject(data);
                            if(jsonObject.length()>0){
                                if(1==jsonObject.getInt("code")){
                                    SharedPreferencesUtil.getInstance().putBoolean(Constant.REGISTER_OPEN_APP,true);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }
}
