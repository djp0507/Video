package com.video.newqu.helper;

import android.content.Context;
import android.util.Log;
import com.video.newqu.util.Logger;
import com.video.newqu.util.SharedPreferencesUtil;
import cn.jpush.android.api.JPushMessage;

/**
 * 处理tag alias相关的逻辑
 *
 */
public class TagAliasOperatorHelper {

    private static final String TAG = TagAliasOperatorHelper.class.getSimpleName();
    private static TagAliasOperatorHelper mInstance;

    public interface  OnAliasChangeListener{
        void onChange(JPushMessage jPushMessage);
    }

    private OnAliasChangeListener mOnAliasChangeListener;

    public void setOnAliasChangeListener(OnAliasChangeListener onAliasChangeListener) {
        mOnAliasChangeListener = onAliasChangeListener;
    }

    private TagAliasOperatorHelper(){

    }

    public static TagAliasOperatorHelper getInstance(){
        if(mInstance == null){
            synchronized (TagAliasOperatorHelper.class){
                if(mInstance == null){
                    mInstance = new TagAliasOperatorHelper();
                }
            }
        }
        return mInstance;
    }


    /**
     * 对TAG操作的回调
     * @param context
     * @param jPushMessage
     */
    public void onTagOperatorResult(Context context, JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        Logger.i(TAG,"action - onTagOperatorResult, sequence:"+sequence+",tags:"+jPushMessage.getTags());

        if(jPushMessage.getErrorCode() == 0){
            Logger.i(TAG,"action - modify tag Success,sequence:"+sequence);

        }else{
            if(jPushMessage.getErrorCode() == 6018){
                //tag数量超过限制,需要先清除一部分再add
                Log.d(TAG, "onTagOperatorResult: tag超过限制");
            }
        }
    }

    /**
     * 检查TAG回调
     * @param context
     * @param jPushMessage
     */
    public void onCheckTagOperatorResult(Context context, JPushMessage jPushMessage){
        int sequence = jPushMessage.getSequence();
        Logger.i(TAG,"action - onCheckTagOperatorResult, sequence:"+sequence+",checktag:"+jPushMessage.getCheckTag());

        if(jPushMessage.getErrorCode() == 0){

        }else{

        }
    }

    /**
     * 对别名操作的回调
     * @param context
     * @param jPushMessage
     */
    public void onAliasOperatorResult(Context context, JPushMessage jPushMessage) {
        int sequence = jPushMessage.getSequence();
        Log.d(TAG, "onAliasOperatorResult: jPushMessage="+jPushMessage);
        if(null!=mOnAliasChangeListener){
            mOnAliasChangeListener.onChange(jPushMessage);
        }
//        SharedPreferencesUtil.getInstance().putBoolean(Constant.JG_ISREGISTER_PLUSH,false);
        Log.d(TAG, "onAliasOperatorResult: sequence="+sequence);
        Logger.i(TAG,"action - onAliasOperatorResult, sequence:"+sequence+",alias:"+jPushMessage.getAlias());

        if(jPushMessage.getErrorCode() == 0){
            Logger.i(TAG,"action - modify alias Success,sequence:"+sequence);
        }else{

        }
    }
}
