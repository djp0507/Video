package com.video.newqu.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.MineUserInfo;
import com.video.newqu.bean.SMSEventMessage;
import com.video.newqu.bean.UserData;
import com.video.newqu.databinding.FragmentLoginBinding;
import com.video.newqu.ui.activity.LoginGroupActivity;
import com.video.newqu.ui.contract.LoginXinQuContract;
import com.video.newqu.ui.presenter.LoginXinQuPresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * TinyHung@Outlook.com
 * 2017/11/28.
 * 用户账号密码登录
 */

public class LoginFragment extends BaseFragment<FragmentLoginBinding> implements LoginXinQuContract.View {

    private static final String TAG = LoginFragment.class.getSimpleName();
    private Animation mInputAnimation;
    private LoginXinQuPresenter mLoginXinQuPresenter;
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
                    //登录
                    case R.id.btn_login:
                        createAccountLogin();
                        break;
                    //忘记密码
                    case R.id.tv_retrieve_password:
                        if(null!=mLoginGroupActivity&&!mLoginGroupActivity.isFinishing()){
                            mLoginGroupActivity.addReplaceFragment(new LoginEditPasswordFragment(),"修改密码","登录");//打开修改密码界面
                            mLoginGroupActivity.showOthreLoginView(false);
                        }
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
        bindingView.tvRetrievePassword.setOnClickListener(onClickListener);
        bindingView.ivAccountCancel.setOnClickListener(onClickListener);
        bindingView.ivPasswordCancel.setOnClickListener(onClickListener);
        bindingView.btnLogin.setOnClickListener(onClickListener);
        bindingView.etAccount.addTextChangedListener(accountChangeListener);
        bindingView.etPassword.addTextChangedListener(passwordChangeListener);
        //监听焦点获悉情况
        bindingView.etAccount.setOnFocusChangeListener(onFocusChangeListener);
        bindingView.etPassword.setOnFocusChangeListener(onFocusChangeListener);
        mInputAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        //设置密码属性
        bindingView.etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showContentView();
        mLoginXinQuPresenter = new LoginXinQuPresenter(getActivity());
        mLoginXinQuPresenter.attachView(this);
    }


    /**
     * 用户使用账号登录
     */
    private void createAccountLogin() {
        String account = bindingView.etAccount.getText().toString().trim();
        String password = bindingView.etPassword.getText().toString().trim();
        if(TextUtils.isEmpty(account)){
            ToastUtils.shoCenterToast("手机号码不能为空");
            bindingView.etAccount.startAnimation(mInputAnimation);
            return;
        }
        if(TextUtils.isEmpty(password)){
            ToastUtils.shoCenterToast("密码不能为空");
            bindingView.etPassword.startAnimation(mInputAnimation);
            return;
        }
        if(!Utils.isPhoneNumber(account)){
            ToastUtils.shoCenterToast("手机号码格式不正确");
            return;
        }
        if(null!=mLoginXinQuPresenter&&!mLoginXinQuPresenter.isLogin()){
            showProgressDialog("登录中,请稍后...",true);
            mLoginXinQuPresenter.userLogin("86",account,password);
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
            if(98==event.getSmsCode()&&null!=bindingView) {
                bindingView.etAccount.setText( event.getAccount());
                bindingView.etPassword.setText(event.getPassword());
                bindingView.etAccount.setSelection(event.getAccount().length());
                bindingView.etPassword.setSelection(event.getPassword().length());
                if (null != mLoginXinQuPresenter && !mLoginXinQuPresenter.isLogin()) {
                    showProgressDialog("登录中,请稍后...", true);
                    mLoginXinQuPresenter.userLogin("86", event.getAccount(), event.getPassword());
                }
            }else if(99==event.getSmsCode()&&null!=bindingView){
                bindingView.etAccount.setText(event.getAccount());
                bindingView.etAccount.setSelection(event.getAccount().length());
                bindingView.etPassword.setText("");
            }
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
                        if(null!=bindingView)  bindingView.ivAccountCancel.setVisibility(View.INVISIBLE);
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
    public void showErrorView() {
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }

    @Override
    public void showLoginError(String data) {
        closeProgressDialog();
        ToastUtils.shoCenterToast(data);
    }

    @Override
    public void showLoginFinlish(MineUserInfo data) {
        closeProgressDialog();
        MineUserInfo.DataBean.InfoBean info = data.getData().getInfo();
        UserData.DataBean.InfoBean infoBean =new UserData.DataBean.InfoBean();
        infoBean.setCity(info.getCity());
        infoBean.setGender(info.getGender());
        infoBean.setId(info.getId());
        infoBean.setImeil(info.getImeil());
        infoBean.setLogin_type(info.getLogin_type());
        infoBean.setLogo(info.getLogo());
        infoBean.setNickname(info.getNickname());
        infoBean.setOpen_id(info.getOpen_id());
        infoBean.setProvince(info.getProvince());
        infoBean.setSignature(info.getSignature());
        infoBean.setLogin_type(info.getLogin_type());
        infoBean.setStatus(info.getStatus());
        infoBean.setPhone(info.getPhone());
        VideoApplication.getInstance().setUserData(infoBean,true);
        if(null!=mLoginGroupActivity&&!mLoginGroupActivity.isFinishing()){
            mLoginGroupActivity.closeForResult(info);
        }
    }

    @Override
    public void onDestroy() {
        if(null!=mLoginXinQuPresenter){
            mLoginXinQuPresenter.detachView();
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        closeProgressDialog();
        bindingView.etAccount.setText("");
        bindingView.etPassword.setText("");
        mInputAnimation=null;
        mLoginXinQuPresenter=null;
        mLoginGroupActivity=null;
    }
}
