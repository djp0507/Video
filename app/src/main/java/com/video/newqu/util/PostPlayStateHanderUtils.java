package com.video.newqu.util;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.kk.securityhttp.engin.HttpCoreEngin;
import com.video.newqu.VideoApplication;
import com.video.newqu.bean.PlayCountInfo;
import com.video.newqu.contants.Constant;
import com.video.newqu.contants.NetContants;
import com.video.newqu.listener.OnPostPlayStateListener;
import com.video.newqu.manager.ThreadManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@outlook.com
 * 2017/7/8 11:02
 * 上传播放记录
 */
public class PostPlayStateHanderUtils {

    private static final String TAG = PostPlayStateHanderUtils.class.getSimpleName();

    /**
     * 上传播放的记录
     * @param videoID 视频ID
     * @param currentPosition 播放位置
     * @param state 播放的状态 1：播放完成 0:未播放完成
     */
    public static void postVideoPlayState(String videoID, int currentPosition, int state, final OnPostPlayStateListener onPostPlayStateListener) {


        if(!Utils.isCheckNetwork()){
            return;
        }


        double newNouble = Utils.changeDouble(((float)currentPosition)/1000);
        Map<String,String> params=new HashMap<>();
        params.put("video_id", videoID);
        params.put("imeil", VideoApplication.mUuid);
        params.put("user_id", VideoApplication.getLoginUserID());
        params.put("play_durtaion",newNouble+"");
        params.put("play_state",state+"");

        HttpCoreEngin.get(VideoApplication.getInstance().getApplicationContext()).rxpost(NetContants.BASE_VIDEO_HOST + "play_record", String.class, params,true,true,true).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String data) {
                if(TextUtils.isEmpty(data)){
                    return;
                }
                try {
                    JSONObject jsonObject=new JSONObject(data);
                    if(1==jsonObject.getInt("code")&&TextUtils.equals(Constant.PLAY_COUNT_SUCCESS,jsonObject.getString("msg"))){
                        PlayCountInfo playCountInfo = new Gson().fromJson(data, PlayCountInfo.class);
                        Log.d(TAG, "call: 统计成功");
                        PlayCountInfo.DataBean.InfoBean info = playCountInfo.getData().getInfo();
                        if(null!=onPostPlayStateListener){
                            onPostPlayStateListener.onPostPlayStateComple(info.getPlaty_times()+"");
                        }
                    }else{
                        if(null!=onPostPlayStateListener){
                            onPostPlayStateListener.onPostPlayStateError();
                        }
                        Log.d(TAG, "call: 统计失败");
                    }
                } catch (JSONException e) {
                    Log.d(TAG, "call: 统计异常--->="+e.getMessage());
                    e.printStackTrace();
                    if(null!=onPostPlayStateListener){
                        onPostPlayStateListener.onPostPlayStateError();
                    }
                }
            }
        });
    }
}
