package com.video.newqu.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import com.video.newqu.contants.Constant;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.dialog.GradeDialog;

/**
 * TinyHung@Outlook.com
 * 2017/11/28
 */

public class GradeUtil {

    private static final String TAG = GradeUtil.class.getSimpleName();

    public static void init(final Context context) {
        //用户未选择不再提示
        if(0==SharedPreferencesUtil.getInstance().getInt(Constant.GRADE_IS_SHOW)){
            //这个版本用户未选择不再询问
            if(Utils.getVersionCode()!= SharedPreferencesUtil.getInstance().getInt(Constant.TIPS_GEADE_CODE)){
                //用户连续两天登录
                if(SharedPreferencesUtil.getInstance().getInt(Constant.GRADE_LOGIN_COUNT)>=2){
                    //今天播放视频大于等于3个
                    if(SharedPreferencesUtil.getInstance().getInt(Constant.GRADE_PLAYER_VIDEO_COUNT)>=5){
                        //今天还未弹出评分窗口
                        if(!SharedPreferencesUtil.getInstance().getBoolean(Constant.GRADE_TODAY_TIPS)){
                            GradeDialog gradeDialog=new GradeDialog(context);
                            gradeDialog.setOnItemClickListener(new GradeDialog.OnItemClickListener() {
                                /**
                                 * 去打分
                                 */
                                @Override
                                public void onGoToTheStore() {
                                    Logger.d(TAG,"onCancel--用户选择了评分");
                                    try {
                                        Uri uri = Uri.parse("market://details?id="+ Utils.getAppProcessName(context));
                                        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(intent);
                                    }catch (Exception e){
                                        Log.d("SettingsFragment",e.toString());
                                    }
                                }

                                /**
                                 * 去反馈
                                 */
                                @Override
                                public void onGoToTheServicer() {
                                    Intent intent=new Intent(context, ContentFragmentActivity.class);
                                    intent.putExtra(Constant.KEY_FRAGMENT_TYPE,Constant.KEY_FRAGMENT_SERVICES);
                                    intent.putExtra(Constant.KEY_TITLE,"反馈中心");
                                    context.startActivity(intent);
                                }

                                /**
                                 * 用户拒绝了
                                 */
                                @Override
                                public void onCancel() {
                                    //这个版本都不再提示
                                    SharedPreferencesUtil.getInstance().putInt(Constant.TIPS_GEADE_CODE,Utils.getVersionCode());
                                    Logger.d(TAG,"onCancel--用户拒绝了评分");
                                }
                            });
                            gradeDialog.show();
                            SharedPreferencesUtil.getInstance().putInt(Constant.GRADE_LOGIN_COUNT,0);//还原连续登录天数
                            SharedPreferencesUtil.getInstance().putBoolean(Constant.GRADE_TODAY_TIPS,true);//当天不会再提示了
                        }
                    }
                }
            }
        }
    }
}
