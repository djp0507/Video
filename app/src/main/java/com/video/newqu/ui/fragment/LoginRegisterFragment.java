package com.video.newqu.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.SMSEventMessage;
import com.video.newqu.databinding.FragmentRegisterBinding;
import com.video.newqu.ui.activity.LoginGroupActivity;
import com.video.newqu.ui.contract.RegisterContract;
import com.video.newqu.ui.presenter.RegisterPresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * TinyHung@Outlook.com
 * 2017/11/28.
 * 用户注册密码
 */

public class LoginRegisterFragment extends BaseFragment <FragmentRegisterBinding> implements RegisterContract.View {

    private static final String TAG = LoginRegisterFragment.class.getSimpleName();
    private Animation mInputAnimation;
    private Handler mHandler;
    private RegisterPresenter mRegisterPresenter;
    private LoginGroupActivity mLoginGroupActivity;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mLoginGroupActivity = (LoginGroupActivity) context;
    }

    @Override
    protected void initViews() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    //注册
                    case R.id.btn_register:
                        cureateRegisterUser();
                        break;
                    //获取验证码
                    case R.id.tv_get_code:
                        cureateGetNumberCode();
                        break;
                    //清除输入框账号
                    case R.id.iv_account_cancel:
                        bindingView.etAccount.setText("");
                        break;
                    //清除输入框密码
                    case R.id.iv_password_cancel:
                        bindingView.etPassword.setText("");
                        break;
                }
            }
        };
        bindingView.btnRegister.setOnClickListener(onClickListener);
        bindingView.ivAccountCancel.setOnClickListener(onClickListener);
        bindingView.ivPasswordCancel.setOnClickListener(onClickListener);
        bindingView.tvGetCode.setOnClickListener(onClickListener);

        bindingView.etAccount.addTextChangedListener(accountChangeListener);
        bindingView.etPassword.addTextChangedListener(passwordChangeListener);

        //监听焦点获悉情况
        bindingView.etAccount.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etPassword.setOnFocusChangeListener(onFocusChangeListener);

        mInputAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_register;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showContentView();
        mHandler=new Handler();
        mRegisterPresenter = new RegisterPresenter(getActivity());
        mRegisterPresenter.attachView(this);
    }


    /**
     * 准备获取验证码
     */
    private void cureateGetNumberCode() {
        String account = bindingView.etAccount.getText().toString().trim();
        if(TextUtils.isEmpty(account)){
            ToastUtils.shoCenterToast("手机号码不能为空");
            bindingView.etAccount.startAnimation(mInputAnimation);
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            ToastUtils.shoCenterToast("手机号码格式不正确");
            return;
        }
        getCode("86",account);
    }

    /**
     * 获取验证码
     * @param country 区号
     * @param account 手机号码
     */
    private void getCode(String country, String account) {
        if(null!=mLoginGroupActivity&&!mLoginGroupActivity.isFinishing()){
            mLoginGroupActivity.getCode(country,account);
            showProgressDialog("获取验证码中，请稍后...",true);
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 刷新通知
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SMSEventMessage event) {
        if(null!=event){
            switch (event.getSmsCode()) {
                //发送验证码失败
                case 99:
                    closeProgressDialog();
                    initGetCodeBtn();
                    try {
                        if(!TextUtils.isEmpty(event.getMessage())){
                            try {
                                JSONObject jsonObject=new JSONObject(event.getMessage());
                                if(null!=jsonObject&&jsonObject.length()>0){
                                    if(null!=jsonObject.getString("detail")){
                                        ToastUtils.shoCenterToast(jsonObject.getString("detail"));
                                    }else{
                                        ToastUtils.shoCenterToast("发送验证码失败");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }catch (Exception e){

                    }
                    break;

                //获取支持的国家列表成功
                case 103:
//                    HashMap<String, String> stringStringHashMap = (HashMap<String, String>) event.getMessage();
//                    //保存至本地
//                    SerMap serMap=new SerMap();
//                    serMap.setMap(stringStringHashMap);
//                    VideoApplication.mACache.put(Constant.CACHE_COUNTRY_NUMBER_LIST,serMap,Constant.CACHE_TIME);
                    break;
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

            }
        }
    }

    /**
     * 改变获取验证码按钮状态
     */
    private void showGetCodeDisplay() {
        if(null==bindingView) return;
        totalTime=60;
        bindingView.tvGetCode.setClickable(false);
        bindingView.tvGetCode.setTextColor(CommonUtils.getColor(R.color.coment_color));
        bindingView.tvGetCode.setBackgroundResource(R.drawable.btn_find_password_bg_gray);
        if(null!=mHandler) mHandler.postDelayed(taskRunnable,0);
    }


    /**
     * 还原获取验证码按钮状态
     */
    private void initGetCodeBtn() {
        if(null==bindingView) return;
        totalTime=0;
        if(null!=taskRunnable&&null!=mHandler){
            mHandler.removeCallbacks(taskRunnable);
        }
        bindingView.tvGetCode.setText("重新获取");
        bindingView.tvGetCode.setClickable(true);
        bindingView.tvGetCode.setTextColor(CommonUtils.getColor(R.color.login_hint));
        bindingView.tvGetCode.setBackgroundResource(R.drawable.square_login_background_orgin);
    }

    /**
     * 定时任务，模拟倒计时广告
     */
    private int totalTime=60;

    Runnable taskRunnable=new Runnable() {
        @Override
        public void run() {
            if(null==bindingView) return;
            bindingView.tvGetCode.setText(totalTime+"S后重试");
            totalTime--;
            if(totalTime<0){
                //还原
                initGetCodeBtn();
                return;
            }
            if(null!=mHandler) mHandler.postDelayed(this,1000);
        }
    };




    /**
     * 准备注册用户
     */
    private void cureateRegisterUser() {

        String account = bindingView.etAccount.getText().toString().trim();
        String password = bindingView.etPassword.getText().toString().trim();
        String code = bindingView.etCode.getText().toString().trim();

        if(TextUtils.isEmpty(account)){
            bindingView.etAccount.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("手机号码不能为空");
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            bindingView.etAccount.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("手机号码格式不正确");
            return;
        }

        if(TextUtils.isEmpty(password)){
            bindingView.etPassword.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("请设置密码");
            return;
        }

        if(!Utils.isPassword(password)){
            bindingView.etPassword.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("密码格式不正确");
            return;
        }

        if(TextUtils.isEmpty(code)){
            bindingView.etCode.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("验证码不能为空");
            return;
        }

        if(!Utils.isNumberCode(code)){
            bindingView.etCode.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("验证码格式不正确");
            return;
        }

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
        }
        if(null!=mRegisterPresenter&&!mRegisterPresenter.isRegister()){
            showProgressDialog("提交注册中...",true);
            // TODO: 2017/6/20 用户注册
            mRegisterPresenter.register(VideoApplication.mUuid,account,password,code);
        }
    }


    /**
     * 账号输入框监听
     */
    private TextWatcher accountChangeListener=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(null!=bindingView) bindingView.ivAccountCancel.setVisibility(!TextUtils.isEmpty(s)&&s.length()>0?View.VISIBLE:View.INVISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * 密码输入框监听
     */
    private TextWatcher passwordChangeListener=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(null!=bindingView) bindingView.ivPasswordCancel.setVisibility(!TextUtils.isEmpty(s)&&s.length()>0?View.VISIBLE:View.INVISIBLE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };



    /**
     * 对个输入框焦点进行监听
     */
    private View.OnFocusChangeListener onFocusChangeListener=new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            switch (v.getId()) {
                case R.id.et_account:
                    if(hasFocus&&null!=bindingView){
                        if(bindingView.etAccount.getText().toString().length()>0){
                            bindingView.ivAccountCancel.setVisibility(View.VISIBLE);
                        }
                    }else{
                        if(null!=bindingView) bindingView.ivAccountCancel.setVisibility(View.INVISIBLE);
                    }
                    break;
                case R.id.et_password:
                    if(hasFocus&&null!=bindingView){
                        if(bindingView.etPassword.getText().toString().length()>0){
                            bindingView.ivPasswordCancel.setVisibility(View.VISIBLE);
                        }
                    }else{
                        if(null!=bindingView) bindingView.ivPasswordCancel.setVisibility(View.INVISIBLE);
                    }
                    break;
            }
        }
    };


    @Override
    public void onDestroy() {
        if(null!=mRegisterPresenter){
            mRegisterPresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        initGetCodeBtn();
        if(null!=mHandler){
            mHandler.removeCallbacks(taskRunnable);
            mHandler=null;
        }
        bindingView=null;
    }


    //==========================================注册结果回调=========================================

    @Override
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }

    @Override
    public void registerResultError(String data) {
        closeProgressDialog();
        ToastUtils.shoCenterToast(data);
    }

    @Override
    public void registerResultFinlish(String data) {
        closeProgressDialog();
        ToastUtils.shoCenterToast(data);
        if(null!=mLoginGroupActivity&&!mLoginGroupActivity.isFinishing()){
            mLoginGroupActivity.login(bindingView.etAccount.getText().toString().trim(),bindingView.etPassword.getText().toString().trim());
        }
    }

    @Override
    public void registerError() {
        closeProgressDialog();
    }

    @Override
    public void imageUploadError() {

    }

    @Override
    public void imageUploadFinlish(String data) {

    }

    @Override
    public void needIploadImageLogo() {

    }
}
