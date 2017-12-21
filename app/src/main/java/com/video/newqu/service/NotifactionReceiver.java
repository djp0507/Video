package com.video.newqu.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.google.gson.Gson;
import com.video.newqu.VideoApplication;
import com.video.newqu.bean.NotifactionMessageInfo;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.ui.activity.MainActivity;
import com.video.newqu.ui.activity.VideoDetailsActivity;
import com.video.newqu.ui.activity.WebViewActivity;
import com.video.newqu.util.Logger;
import com.video.newqu.util.NotifactionUtil;
import com.video.newqu.util.Utils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.service.PushReceiver;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * 极光推送自定义接收器
 */

public class NotifactionReceiver extends PushReceiver {


	private static final String TAG = NotifactionReceiver.class.getSimpleName();

	/**
	 * @param context
	 * @param intent
     */
	@Override
	public void onReceive(Context context, Intent intent) {
		try {
			Bundle bundle = intent.getExtras();
			printBundle(bundle);
			//注册成功
			if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
				String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
				Logger.d(TAG, "onReceive: 接收到了接收RegistrationID="+regId);
			//接收到自定义消息
			} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
				String result = bundle.getString(JPushInterface.EXTRA_MESSAGE);//自定义参数
				Logger.d(TAG, "onReceive:自定义参数"+result);
			//接收到通知
			} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
				int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
				String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
				if(!TextUtils.isEmpty(extras)){
                    NotifactionMessageInfo messageInfo = new Gson().fromJson(extras, NotifactionMessageInfo.class);
					if(null!=messageInfo){
						messageInfo.setId((long) notifactionId);
						List<NotifactionMessageInfo> messageList= (List<NotifactionMessageInfo>) ApplicationManager.getInstance().getCacheExample().getAsObject(VideoApplication.getLoginUserID()+Constant.CACHE_USER_MESSAGE);
						int badgeCount=0;
						if(null==messageList) messageList=new ArrayList<>();
						messageList.add(messageInfo);
						ApplicationManager.getInstance().getCacheExample().put(VideoApplication.getLoginUserID()+Constant.CACHE_USER_MESSAGE, (Serializable) messageList);//刷新本地缓存
						if(null!=messageList&&messageList.size()>0){
							for (NotifactionMessageInfo notifactionMessageInfo : messageList) {
								if(!notifactionMessageInfo.isRead()){
									badgeCount++;
								}
							}
							//处理桌面图标
							if(badgeCount>0){
								ShortcutBadger.applyCount(context.getApplicationContext(), badgeCount); //for 1.1.4+
							}
						}
						Logger.d(TAG,"onReceive,未读消息数量"+badgeCount+",消息ID="+notifactionId+",类型="+messageInfo.getItemType()+",消息内容="+extras);
						//发送消息给主界面，有新的消息收到了
						MessageEvent messageEvent = new MessageEvent();
						messageEvent.setMessage(Constant.EVENT_NEW_MESSAGE);
						messageEvent.setExtar(badgeCount);
						EventBus.getDefault().post(messageEvent);
					}
                }
			//用户点击通知
			} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
				Logger.d(TAG, "用户点击打开了通知");

				String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);//标题
				String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
				String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
				if (!NotifactionUtil.isEmpty(extras)) {
				try {
					JSONObject extraJson = new JSONObject(extras);
					if (extraJson.length() > 0) {
						String url = extraJson.getString("url");
						String video_id = extraJson.getString("video_id");
						String msg_type = extraJson.getString("msg_type");
						Intent startIntent=new Intent();
						startIntent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
						//视频类型消息
						if(!TextUtils.isEmpty(msg_type)&&TextUtils.equals("0",msg_type)){
							startIntent.setClass(context,VideoDetailsActivity.class);
							startIntent.putExtra("video_id",video_id);
							startIntent.putExtra("video_author_id", VideoApplication.getLoginUserID());
							//网页类型
						}else if(!TextUtils.isEmpty(msg_type)&&TextUtils.equals("1",msg_type)){
							startIntent.setClass(context,WebViewActivity.class);
							startIntent.putExtra("url",url);
							startIntent.putExtra("title",title);
							//普通的通知类型，打开软件即可
						}else{
							startIntent.setClass(context, MainActivity.class);
						}
						context.startActivity(startIntent);
					}else {
						startMainActivity(context);
					}
				} catch (JSONException e) {
					startMainActivity(context);
				}

			}else{
					startMainActivity(context);
				}

			} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
				com.video.newqu.util.Logger.d(TAG, "用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
				//在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..

			} else if(JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
				boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			} else {
				com.video.newqu.util.Logger.d(TAG, "Unhandled intent  " +intent.getAction());

			}
		} catch (Exception e){

		}

	}

	private void startMainActivity(Context context) {
		//程序未启动
		if(3==Utils.getAppSatus(context,"com.video.newqu")){
			Log.d(TAG, "startMainActivity: 程序未启动");
			//启动开屏页
			String packageName = context.getApplicationContext().getPackageName();
			Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			context.startActivity(launchIntent);
			//程序已经在前台或后台运行
		}else{
			Log.d(TAG, "startMainActivity: 程序已启动");
			Intent startIntent=new Intent();
			startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startIntent.setClass(context, MainActivity.class);
			context.startActivity(startIntent);
		}
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			}else if(key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)){
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else if (key.equals(JPushInterface.EXTRA_EXTRA)) {
				if (TextUtils.isEmpty(bundle.getString(JPushInterface.EXTRA_EXTRA))) {
					Logger.i(TAG, "This message has no Extra data");
					continue;
				}

				try {
					JSONObject json = new JSONObject(bundle.getString(JPushInterface.EXTRA_EXTRA));
					Iterator<String> it =  json.keys();

					while (it.hasNext()) {
						String myKey = it.next().toString();
						sb.append("\nkey:" + key + ", value: [" +
								myKey + " - " +json.optString(myKey) + "]");
					}
				} catch (JSONException e) {
					Logger.e(TAG, "Get message extra JSON error!");
				}

			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		return sb.toString();
	}
}
