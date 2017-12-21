package com.video.newqu.ui.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.bean.NumberCountryInfo;
import com.video.newqu.bean.SerMap;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.ActivityRegisterBinding;
import com.video.newqu.manager.ActivityLoginCollectorManager;
import com.video.newqu.manager.StatusBarManager;
import com.video.newqu.ui.contract.RegisterContract;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.RegisterPresenter;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.FileUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.widget.GlideCircleTransform;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.smssdk.OnSendMessageHandler;
import cn.smssdk.SMSSDK;

/**
 * TinyHung@outlook.com
 * 2017/6/20 16:00
 * 用户注册/只针对手机号码用户
 */

public class RegisterActivity extends BaseActivity<ActivityRegisterBinding> implements View.OnClickListener, RegisterContract.View {

    private Animation mInputAnimation;
    private cn.smssdk.EventHandler mEventHandler;
    private NumberCountryInfo numberCountryInfo=new NumberCountryInfo();//国区号，电话号码匹配规则
    private RegisterPresenter mRegisterPresenter;
    private File mFilePath=null;//图像的存储路径


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        bindingView.viewStateBar.setVisibility(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?View.GONE:View.VISIBLE);
        StatusBarManager.getInstance().init(this,  CommonUtils.getColor(R.color.white), 0,true);
        ActivityLoginCollectorManager.addActivity(this);
        mInputAnimation = AnimationUtils.loadAnimation(RegisterActivity.this, R.anim.shake);
        //初始化短信接收
        initSMS();
        mRegisterPresenter = new RegisterPresenter(RegisterActivity.this);
        mRegisterPresenter.attachView(this);
    }


    @Override
    public void initViews() {
        //先测量目标组件的宽高
        int width =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        int height =View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);
        bindingView.llNikenameView.measure(width,height);
        int viewHeight=bindingView.llNikenameView.getMeasuredHeight();

        //设置性别选择的高度
        LinearLayout.LayoutParams reSexLayoutParams = (LinearLayout.LayoutParams) bindingView.reSexView.getLayoutParams();
        reSexLayoutParams.height=viewHeight;bindingView.reSexView.setLayoutParams(reSexLayoutParams);
        //设置性别标题的高度
        LinearLayout.LayoutParams textLayoutParams = (LinearLayout.LayoutParams) bindingView.tvSex.getLayoutParams();
        textLayoutParams.height=viewHeight; bindingView.tvSex.setLayoutParams(textLayoutParams);

        bindingView.ivBack.setOnClickListener(this);
        bindingView.tvTitle.setText("新用户注册");
        bindingView.ivSubmit.setOnClickListener(this);
        bindingView.tvGetCode.setOnClickListener(this);
        bindingView.reSexView.setOnClickListener(this);
        bindingView.tvNumberCountry.setOnClickListener(this);
        bindingView.llSetUserLogo.setOnClickListener(this);
        bindingView.ivAccountCancel.setOnClickListener(this);
        bindingView.ivPasswordCancel.setOnClickListener(this);
        bindingView.ivCodeCancel.setOnClickListener(this);
        bindingView.ivNicknameCancel.setOnClickListener(this);
        bindingView.etAccount.addTextChangedListener(accountChangeListener);
        bindingView.etPassword.addTextChangedListener(passwordChangeListener);
        bindingView.etCode.addTextChangedListener(codeChangeListener);
        bindingView.etNickname.addTextChangedListener(nicknameChangeListener);
        //监听焦点获悉情况
        bindingView.etAccount.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etPassword.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etCode.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etNickname.setOnFocusChangeListener(onFocusChangeListener);
    }

    @Override
    public void initData() {
        //默认的中国区电话号码正则
        numberCountryInfo.setRule("^1(3|5|7|8|4)\\d{9}");
        numberCountryInfo.setZone("86");//电话号码及区号
        setHeaderImage(R.drawable.iv_mine);
        setCountry();
    }


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
            //获取支持的国家列表成功
                case 103:
                    HashMap<String, String> stringStringHashMap = (HashMap<String, String>) msg.obj;
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
     * 初始化短信监听
     */
    private void initSMS() {

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
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        Log.d("RegisterActivity", "afterEvent:获取验证码成功 ");
                        mHandler.sendEmptyMessage(102);
                        //返回支持发送验证码的国家列表
                    }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //国家列表回调成功
                        ArrayList<HashMap<String,Object>> countryList= (ArrayList<HashMap<String, Object>>) data;
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
                    Log.d("RegisterActivity", "afterEvent:错误 ");
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
     * 设置国家区号
     */
    private void setCountry() {
        bindingView.tvNumberCountry.setText("+"+numberCountryInfo.getZone());
    }

    /**
     * 显示错误信息
     * @param message
     */
    private void showErrorMessage(String message) {
        if(!TextUtils.isEmpty(message)){
            try {
                JSONObject jsonObject=new JSONObject(message);
                if(jsonObject.length()>0){
                    ToastUtils.shoCenterToast(TextUtils.isEmpty(jsonObject.getString("detail"))?"验证错误":jsonObject.getString("detail"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 设置头部背景封面
     */
    private void setHeaderImage(Object imageUrl) {
        //设置LOGO
        Glide.with(this)
                .load(imageUrl)
                .error(R.drawable.iv_mine)
                .placeholder(R.drawable.iv_mine)
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                .centerCrop()//中心点缩放
                .skipMemoryCache(true)//跳过内存缓存
                .transform(new GlideCircleTransform(this))
                .into(bindingView.logUserHead);
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
     * 准备注册用户
     */
    private void cureateRegisterUser() {

        String account = bindingView.etAccount.getText().toString().trim();
        String password = bindingView.etPassword.getText().toString().trim();
        String code = bindingView.etCode.getText().toString().trim();
        String nickName = bindingView.etNickname.getText().toString().trim();
        String sex = bindingView.tvUserSex.getText().toString().trim();

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

        if(TextUtils.isEmpty(nickName)){
            bindingView.etNickname.startAnimation(mInputAnimation);
            ToastUtils.shoCenterToast("请填写昵称");
            return;
        }
        if(TextUtils.isEmpty(sex)){
            ToastUtils.shoCenterToast("性别错误");
            return;
        }
        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
        }
        registerUser(account,password,code,nickName,sex);
    }

    /**
     * 提交注册
     */
    private void registerUser(String account, String password, String code, String nickName,String sex) {
        showProgressDialog("提交注册中...",true);
        // TODO: 2017/6/20 用户注册
        mRegisterPresenter.register(VideoApplication.mUuid,account,password,code,numberCountryInfo.getZone(),nickName,sex,mFilePath);
    }



    /**
     * 获取手机号码的国家区号
     */
    private void getCountryCode() {
        if(locationHasNumberList()){
            Intent intent = new Intent(RegisterActivity.this, CountryCodeSelectorActivity.class);
            startActivityForResult(intent,110);
        }else{
            ToastUtils.shoCenterToast("区号列表正在加载中,稍后再试~");
            getNumberList();
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //返回
            case R.id.iv_back:
                onBackPressed();
                break;
            //提交
            case R.id.iv_submit:
                cureateRegisterUser();
                break;

            //区号
            case R.id.tv_number_country:
                getCountryCode();
                break;

            //获取验证码
            case R.id.tv_get_code:
                cureateGetNumberCode();
                break;

            //性别选择
            case R.id.re_sex_view:
                sexSelector();
                break;
            //选择用户头像
            case R.id.ll_set_user_logo:
                showPictureSelectorPop();
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
            //清除输入框昵称
            case R.id.iv_nickname_cancel:
                bindingView.etNickname.setText("");
                break;
        }
    }

    /**
     * 性别选择
     */
    private void sexSelector() {

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(RegisterActivity.this)
                .setTitle("性别选择")
                .setSingleChoiceItems(getResources().getStringArray(R.array.setting_dialog_sex_choice),
                        TextUtils.equals("女", bindingView.tvUserSex.getText()) ? 0 : 1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                bindingView.tvUserSex.setText(getResources().getStringArray(R.array.setting_dialog_sex_choice)[which]);
                                dialog.dismiss();
                            }
                        })
                .create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bindingView.ivUserSex.setImageResource(TextUtils.equals("女", bindingView.tvUserSex.getText())?R.drawable.iv_icon_sex_women:R.drawable.iv_icon_sex_man);
            }
        });
        alertDialog.show();
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
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    /**
     * 昵称输入框监听
     */
    private TextWatcher nicknameChangeListener=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            bindingView.ivNicknameCancel.setVisibility(!TextUtils.isEmpty(s)&&s.length()>0?View.VISIBLE:View.GONE);
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
                case R.id.et_nickname:
                    if(hasFocus){
                        if(bindingView.etNickname.getText().toString().length()>0){
                            bindingView.ivNicknameCancel.setVisibility(View.VISIBLE);
                        }
                    }else{
                        bindingView.ivNicknameCancel.setVisibility(View.GONE);
                    }
                    break;
            }
        }
    };



    @Override
    public void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mEventHandler);
        if(null!=taskRunnable){
            mHandler.removeCallbacks(taskRunnable);
        }
//        unregisterReceiver(mSmsReceiver);
        if(null!=mFilePath&&mFilePath.exists()){
            FileUtils.deleteFile(mFilePath);
            mFilePath=null;
        }
//        if(null!=mLoadingProgressedView){
//            mLoadingProgressedView.dismiss();
//            mLoadingProgressedView=null;
//        }
//        mInputAnimation=null;mEventHandler=null;numberCountryInfo=null;mRegisterPresenter=null;mFilePath=null;
        ActivityLoginCollectorManager.removeActivity(this);
        Runtime.getRuntime().gc();
    }


    /**
     * 照片选择弹窗
     */
    private void showPictureSelectorPop() {

        try {
            //初始化
            if(null==mOutFilePath)  mOutFilePath = new File(Constant.IMAGE_PATH + IMAGE_DRR_PATH);

            //删除前面的缓存
            if(mOutFilePath.exists()&&mOutFilePath.isFile()){
                FileUtils.deleteFile(mOutFilePath);
            }
            mTempFile = new File(Constant.IMAGE_PATH + IMAGE_DRR_PATH_TEMP);
            if(mTempFile.exists()&&mTempFile.isFile()){
                FileUtils.deleteFile(mTempFile);
            }

        }catch (Exception e){
            ToastUtils.shoCenterToast(e.getMessage());
        }finally {
            List<VideoDetailsMenu> list=new ArrayList<>();
            VideoDetailsMenu videoDetailsMenu1=new VideoDetailsMenu();
            videoDetailsMenu1.setItemID(1);
            videoDetailsMenu1.setTextColor("#FF576A8D");
            videoDetailsMenu1.setItemName("从相册选择");
            list.add(videoDetailsMenu1);

            VideoDetailsMenu videoDetailsMenu2=new VideoDetailsMenu();
            videoDetailsMenu2.setItemID(2);
            videoDetailsMenu2.setTextColor("#FF576A8D");
            videoDetailsMenu2.setItemName("拍一张");
            list.add(videoDetailsMenu2);

            CommonMenuDialog commonMenuDialog =new CommonMenuDialog(RegisterActivity.this);
            commonMenuDialog.setData(list);
            commonMenuDialog.setOnItemClickListener(new CommonMenuDialog.OnItemClickListener() {
                @Override
                public void onItemClick(int itemID) {
                    //取消关注
                    switch (itemID) {
                        case 1:
                            headImageFromGallery();
                            break;
                        case 2:
                            headImageFromCameraCap();
                            break;
                    }
                }
            });
            commonMenuDialog.show();
        }
    }


    //====================================拍摄图片And图片选择=========================================
    private File mTempFile;
    private File mOutFilePath;
    private static final String IMAGE_DRR_PATH = "photo_image.jpg";//最终输出图片
    private static final String IMAGE_DRR_PATH_TEMP = "photo_image_temp.jpg";//临时图片
    private static final int INTENT_CODE_GALLERY_REQUEST = 0xa0;//相册
    private static final int INTENT_CODE_CAMERA_REQUEST = 0xa1;//相册


    // 从本地相册选取图片作为头像
    private void headImageFromGallery() {
        Intent intentFromGallery = new Intent();
        // 设置文件类型
        intentFromGallery.setType("image/*");//选择图片
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentFromGallery, INTENT_CODE_GALLERY_REQUEST);
    }

    // 启动相机拍摄照片
    private void headImageFromCameraCap() {
        // 判断存储卡是否可用，存储照片文件
        if (Utils.hasSdCard()) {
            //判断相机是否可用
            PackageManager pm =getPackageManager();
            boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                    || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD
                    || Camera.getNumberOfCameras() > 0;
            //调用系统相机拍摄
            if(hasACamera){
                Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
                startActivityForResult(intentFromCapture, INTENT_CODE_CAMERA_REQUEST);
                //使用自定义相机拍摄
            }else{
                Intent intent=new Intent(RegisterActivity.this,MediaPictruePhotoActivity.class);
                intent.putExtra("output",mOutFilePath.getAbsolutePath());
                intent.putExtra("output-max-width",800);
                startActivityForResult(intent,Constant.REQUEST_TAKE_PHOTO);
            }
        }else{
            ToastUtils.shoCenterToast("请检查SD卡状态");
        }
    }



    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //选择国家区号，结果
        if(110==requestCode&&89==resultCode){
            if(null!=data){
                numberCountryInfo= (NumberCountryInfo) data.getSerializableExtra("numberCountryInfo");
                setCountry();
            }
            //拍照
        }else{
            if(resultCode== Activity.RESULT_CANCELED){
                return;
            }
            try {
                //拍照和裁剪返回
                if (resultCode == Activity.RESULT_OK && data != null && (requestCode == Constant.REQUEST_CLIP_IMAGE || requestCode == Constant.REQUEST_TAKE_PHOTO)) {

                    String  imagePath = ClipImageActivity.ClipOptions.createFromBundle(data).getOutputPath();
                    if (imagePath != null) {
                        mFilePath=new File(imagePath);
                        if(mFilePath.exists()&&mFilePath.isFile()){
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            if(mFilePath.exists()&&mFilePath.isFile()) setHeaderImage(mFilePath); bindingView.tvSetLogo.setTextColor(CommonUtils.getColor(R.color.white));
                        }
                    }else{
                        ToastUtils.shoCenterToast("操作错误");
                    }
                    //本地相册选取的图片,转换为Path路径后再交给裁剪界面处理
                }else if(requestCode== INTENT_CODE_GALLERY_REQUEST){
                    if(null!=data){
                        ContentResolver resolver =getContentResolver();
                        Uri originalUri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                            if(null!=bitmap){
                                String filePath = FileUtils.saveBitmap(bitmap, Constant.IMAGE_PATH + IMAGE_DRR_PATH_TEMP);
                                startClipActivity(filePath,mOutFilePath.getAbsolutePath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            ToastUtils.shoCenterToast("操作错误"+e.getMessage());
                        }
                    }
                    //系统照相机拍照完成回调
                }else if(requestCode==INTENT_CODE_CAMERA_REQUEST){
                    startClipActivity(mTempFile.getAbsolutePath(),mOutFilePath.getAbsolutePath());
                }
            }catch (Exception e){
                ToastUtils.shoCenterToast("操作错误"+e.getMessage());
            }
        }
    }

    /**
     * 去裁剪
     * @param inputFilePath
     * @param outputFilePath
     */
    private void startClipActivity(String inputFilePath, String outputFilePath) {
        Intent intent = new Intent(RegisterActivity.this, ClipImageActivity.class);
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 2);
        intent.putExtra("maxWidth", 800);
        intent.putExtra("tip", "");
        intent.putExtra("inputPath", inputFilePath);
        intent.putExtra("outputPath", outputFilePath);
        intent.putExtra("clipCircle",true);
        startActivityForResult(intent, Constant.REQUEST_CLIP_IMAGE);
    }



    /**
     * 调用这个方法即表示账号注册成功，携带账号密码返回界面自动登录
     */
    private void close() {
        Intent intent=new Intent();
        intent.putExtra("account",bindingView.etAccount.getText().toString().trim());
        intent.putExtra("password",bindingView.etPassword.getText().toString().trim());
        intent.putExtra("zone",numberCountryInfo.getZone());
        setResult(98,intent);
        finish();
    }



    /**
     * 注册失败
     * @param data
     */
    @Override
    public void registerResultError(String data) {
        closeProgressDialog();
        if(!TextUtils.isEmpty(data)) ToastUtils.shoCenterToast(data);
    }

    /**
     * 注册结果成功
     * @param data
     */
    @Override
    public void registerResultFinlish(String data) {
        try {
            final JSONObject jsonObject=new JSONObject(data);
            final String msg = jsonObject.getString("msg");
            if(jsonObject.length()>0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(null!=mLoadingProgressedView){
                            mLoadingProgressedView.setResultsCompletes(msg,CommonUtils.getColor(R.color.app_style),true,Constant.PROGRESS_CLOSE_DELYAED_TIE);
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
                });
            }
        } catch (JSONException e) {
            ToastUtils.shoCenterToast(e.toString());
            e.printStackTrace();
        }
    }


    @Override
    public void registerError() {
        closeProgressDialog();
        ToastUtils.shoCenterToast("注册失败");
    }

    /**
     * 上传头像失败
     */
    @Override
    public void imageUploadError() {
        closeProgressDialog();
        ToastUtils.shoCenterToast("上传头像失败");
    }

    /**
     * 上传头像成功
     */
    @Override
    public void imageUploadFinlish(final String data) {
        if(null!=mLoadingProgressedView){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLoadingProgressedView.setMessage(data);
                    mLoadingProgressedView.setResultsCompletes("注册账号成功",CommonUtils.getColor(R.color.app_style),true,Constant.PROGRESS_CLOSE_DELYAED_TIE);
                    mLoadingProgressedView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            close();
                        }
                    });
                }
            });
        }else{
            close();
        }
    }

    /**
     * 需要上传头像
     */
    @Override
    public void needIploadImageLogo() {
        setProgressDialogMessage("上传头像中...");
    }

    /**
     * 注册失败
     */
    @Override
    public void showErrorView() {
        closeProgressDialog();
        ToastUtils.shoCenterToast("注册失败");
    }


    @Override
    public void complete() {
        closeProgressDialog();
    }


//    public class SmsReceiver extends BroadcastReceiver {
//        public  final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
//        private String code;
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            //判断传进来的意图，如果相等，拦截到了短信
//            if(intent.getAction().equals(SMS_RECEIVED_ACTION)){
//                Log.d("SmsReceiver", "拦截到了短信");
//                SmsMessage[] smsMessages = getMessagesFromIntent(intent);
//                for (SmsMessage smsMessage : smsMessages) {
////                    smsMessage.getDisplayOriginatingAddress() ;//获取发件人的IP
//                    code=smsMessage.getDisplayMessageBody();//短信内容
////                    smsMessage.getTimestampMillis();//发送时间
//                }
//                if(!TextUtils.isEmpty(code)){
//                    bindingView.etCode.setText(Utils.getAuthCodeFromSms(code));
//                }
//            }
//        }
//        public final SmsMessage[] getMessagesFromIntent(Intent intent) {
//
//            Object[] messages = (Object[]) intent.getSerializableExtra("pdus");//获取实例
//            byte[][] pduObjs = new byte[messages.length][];//创建接收数组
//            for (int i = 0; i < messages.length; i++) {
//                pduObjs[i] = (byte[]) messages[i];
//            }
//            byte[][] pdus = new byte[pduObjs.length][];
//            int pduCount = pdus.length;
//            SmsMessage[] msgs = new SmsMessage[pduCount];
//            for (int i = 0; i < pduCount; i++) {
//                pdus[i] = pduObjs[i];
//                msgs[i] = SmsMessage.createFromPdu(pdus[i]);
//            }
//            return msgs;
//        }
//    }

    @Override
    public void finish() {
        super.finish();
//        overridePendingTransition(0, R.anim.screen_zoom_out);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
