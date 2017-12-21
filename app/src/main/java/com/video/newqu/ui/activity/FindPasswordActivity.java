package com.video.newqu.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.bean.NumberCountryInfo;
import com.video.newqu.bean.SerMap;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityFindPasswordBinding;
import com.video.newqu.manager.ActivityLoginCollectorManager;
import com.video.newqu.manager.StatusBarManager;
import com.video.newqu.ui.contract.MakePasswordContract;
import com.video.newqu.ui.presenter.MikePresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;

/**
 * TinyHung@outlook.com
 * 2017/6/21 21:14
 * 找回密码
 */

public class FindPasswordActivity extends BaseActivity<ActivityFindPasswordBinding> implements View.OnClickListener, MakePasswordContract.View {


    private Animation mInputAnimation;
    private cn.smssdk.EventHandler mEventHandler;
    private NumberCountryInfo numberCountryInfo=new NumberCountryInfo();//国区号，电话号码匹配规则
    private MikePresenter mMikePresenter;


    public static void start(Context context) {
        context.startActivity(new Intent(context,FindPasswordActivity.class));
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_password);
        findViewById(R.id.view_state_bar).setVisibility(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?View.GONE:View.VISIBLE);
        StatusBarManager.getInstance().init(this,  CommonUtils.getColor(R.color.white), 0,true);
        ActivityLoginCollectorManager.addActivity(this);
        initSMS();
        mMikePresenter = new MikePresenter(FindPasswordActivity.this);
        mMikePresenter.attachView(this);
        //短信验证
        mInputAnimation = AnimationUtils.loadAnimation(FindPasswordActivity.this, R.anim.shake);
    }

    @Override
    public void initViews() {
        bindingView.ivBack.setOnClickListener(this);
        bindingView.tvTitle.setText("修改密码");
        bindingView.tvGetCode.setOnClickListener(this);
        bindingView.tvNumberCountry.setOnClickListener(this);
        bindingView.ivAccountCancel.setOnClickListener(this);
        bindingView.ivPasswordCancel.setOnClickListener(this);
        bindingView.ivCodeCancel.setOnClickListener(this);
        bindingView.btnSubmit.setOnClickListener(this);
        bindingView.etAccount.addTextChangedListener(accountChangeListener);
        bindingView.etPassword.addTextChangedListener(passwordChangeListener);
        bindingView.etCode.addTextChangedListener(codeChangeListener);
        //监听焦点获悉情况
        bindingView.etAccount.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etPassword.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etCode.setOnFocusChangeListener(onFocusChangeListener);
    }

    @Override
    public void initData() {
        numberCountryInfo.setRule("^1(3|5|7|8|4)\\d{9}");
        numberCountryInfo.setZone("86");//电话号码及区号
        setCountry();
    }


    /**
     * 初始化短信监听
     */
    private void initSMS() {

        mEventHandler = new cn.smssdk.EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                //回调完成
                if (result == SMSSDK.RESULT_COMPLETE) {
                    Log.d("FindPasswordActivity", "afterEvent:回调完成 ");
                    mHandler.sendEmptyMessage(100);
                    //验证码正确
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        Log.d("FindPasswordActivity", "afterEvent:提交验证码成功 ");
                        mHandler.sendEmptyMessage(101);
                        //获取验证码成功
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        Log.d("FindPasswordActivity", "afterEvent:获取验证码成功 ");
                        mHandler.sendEmptyMessage(102);
                        //返回支持发送验证码的国家列表
                    }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        Log.d("FindPasswordActivity", "afterEvent:返回支持发送验证码的国家列表 ");
                        ArrayList<HashMap<String,Object>> countryList= (ArrayList<HashMap<String, Object>>) data;
//                        //解析国家列表
//                        for (int i = 0; i < countryList.size(); i++) {
//                            String zone = (String) countryList.get(i).get("zone");
//                            String rule = (String) countryList.get(i).get("rule");
//                        }
                        //解析国家列表
                        HashMap<String, String> stringStringHashMap = onCountryListGot(countryList);
                        if(null!=stringStringHashMap&&stringStringHashMap.size()>0){
                            Message message=Message.obtain();
                            message.what=103;
                            message.obj=stringStringHashMap;
                            mHandler.sendMessage(message);
                        }
                    }
                }else{
                    Log.d("FindPasswordActivity", "afterEvent:错误 ");
                    Message message=Message.obtain();
                    message.what=99;
                    message.obj=data.toString();
                    mHandler.sendMessage(message);
                }
            }
        };
        SMSSDK.registerEventHandler(mEventHandler); //注册短信回调
        getNumberList();
    }






    /**
     * 检查缓存有没有国际区号列表，没有就下载
     */
    private void getNumberList() {
        if(!locationHasNumberList()){
            Log.d("FindPasswordActivity", "initData: 获取最新国际区号列表");
            SMSSDK.getSupportedCountries();//获取支持的国家列表
        }
    }


    /**
     * 读取本地是否存在缓存
     * @return
     */
    private boolean locationHasNumberList(){
        SerMap serMap= (SerMap) ApplicationManager.getInstance().getCacheExample().getAsObject(Constant.CACHE_COUNTRY_NUMBER_LIST);
        //取缓存
        if(null!=serMap&&serMap.getMap().size()>0){
            return true;
        }
        return false;
    }



    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                //获取支持的国家列表成功
                case 103:
                    HashMap<String,String> stringStringHashMap = (HashMap<String, String>) msg.obj;
                    //保存至本地
                    SerMap serMap=new SerMap();
                    serMap.setMap(stringStringHashMap);
                    ApplicationManager.getInstance().getCacheExample().put(Constant.CACHE_COUNTRY_NUMBER_LIST,serMap,Constant.CACHE_TIME);
                    break;
                //短信验证码验证成功
                case 100:

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
                    initGetCodeBtn();
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
                    break;
            }
        }
    };



    /**
     * 改变获取验证码按钮状态
     */
    private void showGetCodeDisplay() {
        totalTime=60;
        bindingView.tvGetCode.setOnClickListener(null);
        bindingView.tvGetCode.setTextColor(CommonUtils.getColor(R.color.coment_color));
        bindingView.tvGetCode.setBackgroundResource(R.drawable.btn_find_password_bg_gray);
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
        bindingView.tvGetCode.setText("重新获取");
        bindingView.tvGetCode.setOnClickListener(this);
        bindingView.tvGetCode.setTextColor(CommonUtils.getColor(R.color.white));
        bindingView.tvGetCode.setBackgroundResource(R.drawable.bt_bg_orange_noradius_selector);
    }


    /**
     * 定时任务，模拟倒计时广告
     */
    private int totalTime=60;

    Runnable taskRunnable=new Runnable() {
        @Override
        public void run() {

            bindingView.tvGetCode.setText(totalTime+"S后重试");
            totalTime--;
            if(totalTime<0){
                //还原
                initGetCodeBtn();
                return;
            }
            mHandler.postDelayed(this,1000);
        }
    };



    /**
     * 显示错误信息
     * @param message
     */
    private void showErrorMessage(String message) {
        if(!TextUtils.isEmpty(message)){
            try {
                org.json.JSONObject jsonObject=new org.json.JSONObject(message);
                if(jsonObject.length()>0){
                    ToastUtils.shoCenterToast(jsonObject.getString("detail"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解析国家和列表
     * @param countries
     * @return
     */
    private  HashMap<String, String> onCountryListGot(ArrayList<HashMap<String,Object>> countries) {

        HashMap<String, String> countryRules =null;
        // 解析国家列表
        for (HashMap<String, Object> country : countries) {
            String code = (String) country.get("zone");
            String rule = (String) country.get("rule");
            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
                continue;
            }
            if(null==countryRules){
                countryRules=new HashMap<>();
            }
            countryRules.put(code, rule);
        }
        return countryRules;
    }




    /**
     * 获取手机号码的国家区号
     */
    private void getCountryCode() {

        if(locationHasNumberList()){
            Intent intent = new Intent(FindPasswordActivity.this, CountryCodeSelectorActivity.class);
            startActivityForResult(intent,110);
        }else{
            ToastUtils.shoCenterToast("区号列表正在加载中,稍后再试~!");
            getNumberList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //选择国家区号，结果
        if(110==requestCode&&89==resultCode){
            if(null!=data){
                numberCountryInfo= (NumberCountryInfo) data.getSerializableExtra("numberCountryInfo");
                setCountry();
            }
        }
    }


    /**
     * 设置国家区号
     */
    private void setCountry() {
        bindingView.tvNumberCountry.setText("+"+numberCountryInfo.getZone());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //返回
            case R.id.iv_back:
                onBackPressed();
                break;
            //获取验证码
            case R.id.tv_get_code:
                cureateGetNumberCode();
                break;
            //获取国际区号
            case R.id.tv_number_country:
                getCountryCode();
                break;
            //确定修改
            case R.id.btn_submit:
                cureateSubmit();
                break;
            //清除输入框账号
            case R.id.iv_account_cancel:
                bindingView.etAccount.setText("");
                break;
            //清除输入框密码
            case R.id.iv_password_cancel:
                bindingView.etPassword.setText("");
                break;
            //清除输入框验证码
            case R.id.iv_code_cancel:
                bindingView.etCode.setText("");
                break;
        }
    }



    /**
     * 准备获取验证码
     */
    private void cureateGetNumberCode() {

        String account = bindingView.etAccount.getText().toString().trim();
        if(TextUtils.isEmpty(account)){
            bindingView.etAccount.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("手机号码不能为空");
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            ToastUtils.shoCenterToast("手机号码格式不正确");
            return;
        }
        getCode(numberCountryInfo.getZone(),account);
    }

    /**
     * 获取验证码
     * @param country 区号
     * @param account 手机号码
     */
    private void getCode(String country, String account) {

        showProgressDialog("获取验证码中，请稍后...",true);
        SMSSDK.getVerificationCode(country, account, new OnSendMessageHandler() {
            @Override
            public boolean onSendMessage(String country, String account) {
                return false;//发送短信之前调用，返回TRUE表示无需真正发送验证码
            }
        });
    }

    /**
     * 准备提交新密码
     */
    private void cureateSubmit() {

        String account = bindingView.etAccount.getText().toString().trim();
        String password = bindingView.etPassword.getText().toString().trim();
        String code = bindingView.etCode.getText().toString().trim();

        if(TextUtils.isEmpty(account)){
            bindingView.etAccount.startAnimation(mInputAnimation);
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            bindingView.etAccount.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("手机号码格式不正确");
            return;
        }

        if(TextUtils.isEmpty(password)){
            bindingView.etPassword.startAnimation(mInputAnimation);
            return;
        }

        if(!Utils.isPassword(password)){
            bindingView.etPassword.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("密码格式不正确");
            return;
        }

        if(TextUtils.isEmpty(code)){
            bindingView.etCode.startAnimation(mInputAnimation);
            return;
        }
        if(!Utils.isNumberCode(code)){
            bindingView.etCode.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("验证码格式不正确");
            return;
        }

        submitNewPassword(account,password,code);
    }

    /**
     * 提交新密码
     * @param account
     * @param password
     * @param code
     */
    private void submitNewPassword(String account, String password, String code) {
        showProgressDialog("修改密码中...",true);
        mMikePresenter.makePassword(VideoApplication.mUuid,account,password,code,numberCountryInfo.getZone());
    }

    /**
     * 修改密码成功
     * @param data
     */

    @Override
    public void makePasswordFinlish(String data) {

        if(null!=mLoadingProgressedView){
            mLoadingProgressedView.setResultsCompletes(data,CommonUtils.getColor(R.color.app_style),true, Constant.PROGRESS_CLOSE_DELYAED_TIE);
            mLoadingProgressedView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    close();
                }
            });
        }else{
            close();
        }
    }

    /**
     * 修改密码失败
     * @param data
     */
    @Override
    public void makePasswordError(String data) {
        closeProgressDialog();
        ToastUtils.shoCenterToast(data);
    }

    /**
     * 错误
     */
    @Override
    public void errorView() {
        closeProgressDialog();
        ToastUtils.shoCenterToast("错误,请重试!");
    }

    @Override
    public void showErrorView() {
        closeProgressDialog();
        ToastUtils.shoCenterToast("错误,请重试!");
    }

    @Override
    public void complete() {

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
            bindingView.ivAccountCancel.setVisibility(!TextUtils.isEmpty(s)&&s.length()>0?View.VISIBLE:View.GONE);
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
            bindingView.ivPasswordCancel.setVisibility(!TextUtils.isEmpty(s)&&s.length()>0?View.VISIBLE:View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };


    /**
     * 二维码输入框监听
     */
    private TextWatcher codeChangeListener=new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            bindingView.ivCodeCancel.setVisibility(!TextUtils.isEmpty(s)&&s.length()>0?View.VISIBLE:View.GONE);
            bindingView.btnSubmit.setBackgroundResource(!TextUtils.isEmpty(s)&&s.length()>0?R.drawable.btn_login_app_style_selector :R.drawable.bt_shape_gray_login);
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
                    if(hasFocus){
                        if(bindingView.etAccount.getText().toString().length()>0){
                            bindingView.ivAccountCancel.setVisibility(View.VISIBLE);
                        }
                    }else{
                        bindingView.ivAccountCancel.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_password:
                    if(hasFocus){
                        if(bindingView.etPassword.getText().toString().length()>0){
                            bindingView.ivPasswordCancel.setVisibility(View.VISIBLE);
                        }
                    }else{
                        bindingView.ivPasswordCancel.setVisibility(View.GONE);
                    }
                    break;
                case R.id.et_code:
                    if(hasFocus){
                        if(bindingView.etCode.getText().toString().length()>0){
                            bindingView.ivCodeCancel.setVisibility(View.VISIBLE);
                        }
                    }else{
                        bindingView.ivCodeCancel.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };




    /**
     * 调用这个方法即表示账号修改成功,
     */
    private void close() {
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mEventHandler);
        ActivityLoginCollectorManager.removeActivity(this);
        Runtime.getRuntime().gc();

    }
}
