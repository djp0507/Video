package com.video.newqu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;

import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.bean.UserData;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityBindingPhoneBinding;
import com.video.newqu.manager.StatusBarManager;
import com.video.newqu.ui.contract.BindingPhoneContract;
import com.video.newqu.ui.presenter.BindingPhonePresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.smssdk.EventHandler;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;


/**
 * TinyHung@Outlook.com
 * 2017/11/19
 */

public class BindingPhoneActivity extends BaseActivity<ActivityBindingPhoneBinding> implements BindingPhoneContract.View {

    private static final String TAG = BindingPhoneActivity.class.getSimpleName();
    private  BindingPhonePresenter mBindingPhonePresenter;
    private  Animation mLoadAnimation;
    private EventHandler mEventHandler;
    private String phone;

    @Override
    public void initViews() {
        bindingView.tvTitle.setText("验证手机");
        String tips = getIntent().getStringExtra(Constant.INTENT_BINDING_TIPS);
        bindingView.tvTips.setText(tips);
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_close:
                        onBackPressed();
                        break;
                    case R.id.btn_submit:
                        bindingPhonrBumber();
                        break;
                    case R.id.btn_get_code:
                        getCode();
                        break;
                }
            }
        };
        bindingView.btnClose.setOnClickListener(onClickListener);
        bindingView.btnSubmit.setOnClickListener(onClickListener);
        bindingView.btnGetCode.setOnClickListener(onClickListener);
    }

    @Override
    public void initData() {
        mEventHandler = new cn.smssdk.EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                //回调完成
                if (result == SMSSDK.RESULT_COMPLETE) {
                    Log.d("RegisterActivity", "afterEvent:回调完成 ");
                    mHandler.sendEmptyMessage(100);
                    //验证码正确
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        Log.d("RegisterActivity", "afterEvent:提交验证码成功 ");
                        mHandler.sendEmptyMessage(101);
                        //获取验证码成功
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        Log.d("RegisterActivity", "afterEvent:获取验证码成功 ");
                        mHandler.sendEmptyMessage(102);
                        //返回支持发送验证码的国家列表
                    }
                }else{
                    Log.d("RegisterActivity", "afterEvent:错误 ");
                    Message message=Message.obtain();
                    message.what=99;
                    message.obj=data.toString();
                    mHandler.sendMessage(message);
                }
            }
        };
        SMSSDK.registerEventHandler(mEventHandler); //注册短信回调
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_binding_phone);
        bindingView.viewStateBar.setVisibility(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?View.GONE:View.VISIBLE);
        StatusBarManager.getInstance().init(this,  CommonUtils.getColor(R.color.white), 0,true);
        mBindingPhonePresenter = new BindingPhonePresenter(BindingPhoneActivity.this);
        mBindingPhonePresenter.attachView(this);
        mLoadAnimation = AnimationUtils.loadAnimation(BindingPhoneActivity.this, R.anim.shake);
    }

    private void getCode() {
        String phoneBumber = bindingView.etPhone.getText().toString().trim();
        if(TextUtils.isEmpty(phoneBumber)){
            bindingView.etPhone.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("手机号码不能为空!");
            return;
        }
        if(!Utils.isPhoneNumber(phoneBumber)){
            bindingView.etPhone.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("手机号码格式不正确！");
            return;
        }

        showProgressDialog("获取验证码中，请稍后...",true);
        SMSSDK.getVerificationCode("86", phoneBumber, new OnSendMessageHandler() {
            @Override
            public boolean onSendMessage(String country, String account) {
                return false;//发送短信之前调用，返回TRUE表示无需真正发送验证码
            }
        });
    }

    private void bindingPhonrBumber() {
        String phoneBumber = bindingView.etPhone.getText().toString().trim();
        String phoneCode = bindingView.etCode.getText().toString().trim();
        if(TextUtils.isEmpty(phoneBumber)){
            bindingView.etPhone.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("手机号码不能为空!");
            return;
        }
        if(!Utils.isPhoneNumber(phoneBumber)){
            bindingView.etPhone.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("手机号码格式不正确！");
            return;
        }
        if(TextUtils.isEmpty(phoneCode)){
            bindingView.etCode.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("请输入接收到的验证码！");
            return;
        }

        if(!Utils.isNumberCode(phoneCode)){
            bindingView.etCode.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("验证码格式不正确！");
            return;
        }

        if(null!= mBindingPhonePresenter &&!mBindingPhonePresenter.isSanLogin()){
            showProgressDialog("绑定手机号中",true);
            phone=phoneBumber;
            mBindingPhonePresenter.bindingPhone(phoneBumber,phoneCode);
        }
    }


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                //短信验证码验证成功
                case 100:
                    Logger.d(TAG,"验证成功");
                    break;
                //获取验证码成功
                case 102:
                    closeProgressDialog();
                    showGetCodeDisplay();
                    ToastUtils.shoCenterToast("已成功发送验证码");
                    break;
                //短信验证码已提交完成
                case 101:
                    //请求后台服务器验证

                    break;
                //失败
                case 99:
                    closeProgressDialog();
                    try {
                        String data = (String) msg.obj;
                        if(!TextUtils.isEmpty(data)){
                            try {
                                JSONObject jsonObject=new JSONObject(data);
                                if(null!=jsonObject&&jsonObject.length()>0){
                                    ToastUtils.shoCenterToast(jsonObject.getString("detail"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (Exception e){

                    }
                    initGetCodeBtn();
                    break;
            }
        }
    };



    /**
     * 改变获取验证码按钮状态
     */
    private void showGetCodeDisplay() {
        totalTime=60;
        bindingView.btnGetCode.setClickable(false);
        bindingView.btnGetCode.setTextColor(CommonUtils.getColor(R.color.coment_color));
        bindingView.btnGetCode.setBackgroundResource(R.drawable.btn_find_password_bg_gray);
        mHandler.postDelayed(taskRunnable,0);
    }


    /**
     * 还原获取验证码按钮状态
     */
    private void initGetCodeBtn() {
        totalTime=0;
        if(null!=taskRunnable){
            mHandler.removeCallbacks(taskRunnable);
        }
        bindingView.btnGetCode.setText("重新获取");
        bindingView.btnGetCode.setClickable(true);
        bindingView.btnGetCode.setTextColor(CommonUtils.getColor(R.color.white));
        bindingView.btnGetCode.setBackgroundResource(R.drawable.square_login_background_orgin);
    }


    /**
     * 定时任务，模拟倒计时广告
     */
    private int totalTime=60;

    Runnable taskRunnable=new Runnable() {
        @Override
        public void run() {
            bindingView.btnGetCode.setText(totalTime+"S后重试");
            totalTime--;
            if(totalTime<0){
                //还原
                initGetCodeBtn();
                return;
            }
            mHandler.postDelayed(this,1000);
        }
    };

    @Override
    public void onDestroy() {
        SMSSDK.unregisterEventHandler(mEventHandler);
        super.onDestroy();
        ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.menu_exit);//出场动画
    }

    @Override
    public void showBindingPhoneResult(String data) {
        closeProgressDialog();
        Logger.d(TAG,"data"+data);
        if(!TextUtils.isEmpty(data)){
            try {
                JSONObject jsonObject=new JSONObject(data);
                if(null!=jsonObject&&jsonObject.length()>0){
                    if(1==jsonObject.getInt("code")){
                        ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                        if(null!= VideoApplication.getInstance().getUserData()){
                            UserData.DataBean.InfoBean userData = VideoApplication.getInstance().getUserData();
                            if(!TextUtils.isEmpty(phone))userData.setPhone(phone);
                            VideoApplication.getInstance().setUserData(userData,true);
                            bindingPhoneFinlish();
                            return;
                        }
                    }else if(0==jsonObject.getInt("code")){
                        ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            ToastUtils.shoCenterToast("绑定失败");
        }
    }

    private void bindingPhoneFinlish() {
        Intent intent=new Intent();
        setResult(Constant.MEDIA_BINDING_PHONE_RESULT,intent);
        onBackPressed();
    }

    @Override
    public void showBindingPhoneError(String data) {
        closeProgressDialog();
        ToastUtils.shoCenterToast(data);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
