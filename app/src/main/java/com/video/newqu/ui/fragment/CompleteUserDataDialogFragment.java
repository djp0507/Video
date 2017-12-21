package com.video.newqu.ui.fragment;

import android.Manifest;
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
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.video.newqu.R;
import com.video.newqu.base.BaseDialogFragment;
import com.video.newqu.bean.MineUserInfo;
import com.video.newqu.bean.VideoDetailsMenu;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentDialogCompleteUserdataBinding;
import com.video.newqu.event.MessageEvent;
import com.video.newqu.ui.activity.ClipImageActivity;
import com.video.newqu.ui.activity.MediaPictruePhotoActivity;
import com.video.newqu.ui.contract.UserEditContract;
import com.video.newqu.ui.dialog.CommonMenuDialog;
import com.video.newqu.ui.presenter.UserEditPresenter;
import com.video.newqu.util.AndroidNFileUtils;
import com.video.newqu.util.CommonUtils;
import com.video.newqu.util.FileUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.view.widget.GlideCircleTransform;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * TinyHung@Outlook.com
 * 2017/11/28.
 * 若是手机号码登录的，补全用户信息，或者任意界面补全用户信息
 */

public class CompleteUserDataDialogFragment extends BaseDialogFragment<FragmentDialogCompleteUserdataBinding> implements UserEditContract.View {

    private static final String TAG = CompleteUserDataDialogFragment.class.getSimpleName();
    private MineUserInfo.DataBean.InfoBean mUserData;
    private UserEditPresenter mUserEditPresenter;
    private Animation mLoadAnimation;
    private File mFilePath=null;
    private static CompleteUserDataDialogFragment mInstance;
    private String mTitle="补全用户资料";
    private int mAction_mode;
    private boolean change=false;
    private CharSequence content_temp;//监听前的文本
    private int content_editStart;//光标开始位置
    private int content_editEnd;//光标结束位置
    private final int content_charMaxNum = 60;


    @Override
    public int getLayoutId() {
        return R.layout.fragment_dialog_complete_userdata;
    }

    public synchronized static CompleteUserDataDialogFragment getInstance(MineUserInfo.DataBean.InfoBean data,String title,int actionMode){
        synchronized(CompleteUserDataDialogFragment.class){
            if(null== mInstance){
                mInstance = new CompleteUserDataDialogFragment();
                Bundle bundle=new Bundle();
                bundle.putSerializable("user_data",data);
                bundle.putString("title",title);
                bundle.putInt("action_mode",actionMode);
                mInstance.setArguments(bundle);
            }
        }
        return mInstance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Black_NoTitleBar);//全屏铺满
        Bundle arguments = getArguments();
        if(null!=arguments){
            mUserData = (MineUserInfo.DataBean.InfoBean) arguments.getSerializable("user_data");
            mTitle = arguments.getString("title");
            mAction_mode = arguments.getInt("action_mode",1);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
        getView().findViewById(R.id.view_state_bar).setVisibility(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M?View.VISIBLE:View.GONE);
        showContentView();
    }



    @Override
    protected void initViews() {
        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_cancel:
                    case R.id.iv_back:
                        CompleteUserDataDialogFragment.this.dismiss();
                        break;

                    case R.id.btn_submit:
                        submitData();
                        break;

                    //头像切换
                    case R.id.re_pictrue:
                        showPictureSelectorPop();
                        break;
                    //性别选择
                    case R.id.ll_sex_selector:
                        showSexSelector();
                        break;
                    //复制ID
                    case R.id.ll_copy_id:
                        Utils.copyString(bindingView.tvUserId.getText().toString().trim());
                        ToastUtils.shoCenterToast("已复制到粘贴板");
                        break;
                }
            }
        };
        bindingView.btnCancel.setOnClickListener(onClickListener);
        bindingView.btnSubmit.setOnClickListener(onClickListener);
        bindingView.rePictrue.setOnClickListener(onClickListener);
        bindingView.llSexSelector.setOnClickListener(onClickListener);
        bindingView.llCopyId.setOnClickListener(onClickListener);
        bindingView.ivBack.setOnClickListener(onClickListener);
        mUserEditPresenter = new UserEditPresenter(getActivity());
        mUserEditPresenter.attachView(this);
        mLoadAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);

        bindingView.tvTitle.setText(mTitle);
        if(mAction_mode==Constant.MODE_USER_COMPLETE){
            bindingView.ivBack.setVisibility(View.GONE);
            bindingView.btnCancel.setVisibility(View.VISIBLE);
        }else if(mAction_mode==Constant.MODE_USER_EDIT){
            bindingView.ivBack.setVisibility(View.VISIBLE);
            bindingView.btnCancel.setVisibility(View.GONE);
        }else{
            bindingView.ivBack.setVisibility(View.GONE);
            bindingView.btnCancel.setVisibility(View.VISIBLE);
        }

        bindingView.etUserDesp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                content_temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                content_editStart = bindingView.etUserDesp.getSelectionStart();
                content_editEnd = bindingView.etUserDesp.getSelectionEnd();
                if (content_temp.length() > content_charMaxNum) {
                    bindingView.etUserDesp.setError("个性签名长度超过限制!");
                    s.delete(content_editStart - 1, content_editEnd);
                    int tempSelection = content_editEnd;
                    bindingView.etUserDesp.setText(s);
                    bindingView.etUserDesp.setSelection(tempSelection);
                }
            }
        });
        setUserData();
    }






    /**
     * 设置用户基本信息
     */
    private void setUserData() {
        if(null==mUserData) return;
        bindingView.etUserName.setHint(TextUtils.isEmpty(mUserData.getNickname())?"火星人":mUserData.getNickname());
        bindingView.tvUserSex.setText(TextUtils.isEmpty(mUserData.getGender())?"女":mUserData.getGender());
        //设置性别
        bindingView.ivUserSex.setImageResource(!TextUtils.isEmpty(mUserData.getGender())&&TextUtils.equals("女",mUserData.getGender())?R.drawable.iv_icon_sex_women:R.drawable.iv_icon_sex_man);

        try {
            String decode = URLDecoder.decode(mUserData.getSignature(), "UTF-8");
            bindingView.etUserDesp.setHint(TextUtils.isEmpty(decode)?"本宝宝暂时没有个性签名":decode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        bindingView.tvUserId.setText(mUserData.getId());
        //作者封面
        Glide.with(this)
                .load(mUserData.getLogo())
                .error(R.drawable.iv_mine)
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存源资源和转换后的资源
                .centerCrop()//中心点缩放
                .skipMemoryCache(true)//跳过内存缓存
                .transform(new GlideCircleTransform(getActivity()))
                .into(bindingView.ivUserHead);
    }



    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        //进入
        if(enter){
            Logger.d(TAG,"enter");
            return AnimationUtils.loadAnimation(getActivity(), R.anim.menu_enter);
            //销毁
        }else{
            Logger.d(TAG,"exit");
            return AnimationUtils.loadAnimation(getActivity(), R.anim.menu_exit);
        }
    }

    /**
     * 提交资料
     */
    private void submitData() {
        if(null==mUserData) return;
        String nikeName = bindingView.etUserName.getText().toString().trim();
        String sex = bindingView.tvUserSex.getText().toString().trim();
        String desp = bindingView.etUserDesp.getText().toString().trim();

        if(TextUtils.isEmpty(sex)){
            bindingView.etUserName.startAnimation(mLoadAnimation);
            ToastUtils.shoCenterToast("性别非法");
            return;
        }
        String encodeDesp=null;
        try {
            encodeDesp= URLEncoder.encode(desp, "UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //只传头像,或者什么都没改
        if(TextUtils.isEmpty(nikeName)&&TextUtils.isEmpty(encodeDesp)&&TextUtils.equals(sex,mUserData.getGender())){
            if(null!=mFilePath&&mFilePath.exists()&&mFilePath.isFile()){
                showProgressDialog("补全用户信息中，请稍后...",true);
                mUserEditPresenter.onPostImagePhoto(mUserData.getId(),mFilePath.getAbsolutePath());
            }else{
                ToastUtils.shoCenterToast("基本信息未作修改~!");
            }
            //有其他信息修改
        }else{
            showProgressDialog("补全用户信息中，请稍后...",true);
            mUserEditPresenter.onPostUserData(mUserData.getId(),nikeName,sex,encodeDesp);
        }
    }


    /**
     * 显示性别选择框
     */
    private void showSexSelector() {

        android.support.v7.app.AlertDialog alertDialog = new android.support.v7.app.AlertDialog.Builder(getActivity())
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
            showErrorToast(null,null,e.getMessage());
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

            CommonMenuDialog commonMenuDialog =new CommonMenuDialog((AppCompatActivity) getActivity());
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

    //====================================拍摄图片And图片选择========================================
    private File mTempFile;
    private File mOutFilePath;
    private static final String IMAGE_DRR_PATH = "photo_image.jpg";//最终输出图片
    private static final String IMAGE_DRR_PATH_TEMP = "photo_image_temp.jpg";//临时图片
    private static final int INTENT_CODE_GALLERY_REQUEST = 100;//相册
    private static final int INTENT_CODE_CAMERA_REQUEST = 200;//拍摄

    // 从本地相册选取图片作为头像
    private void headImageFromGallery() {
        Intent intentFromGallery = new Intent();
        // 设置文件类型
        intentFromGallery.setType("image/*");//选择图片
        intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
        getActivity().startActivityForResult(intentFromGallery, INTENT_CODE_GALLERY_REQUEST);
    }


    // 启动相机拍摄照片
    private void headImageFromCameraCap() {
        //检查SD读写权限
        RxPermissions.getInstance(getActivity()).request(Manifest.permission.CAMERA).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                if(null!=aBoolean&&aBoolean){
                    //判断相机是否可用
                    PackageManager pm = getActivity().getPackageManager();
                    boolean hasACamera = pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                            || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                            || Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD
                            || Camera.getNumberOfCameras() > 0;
                    //调用系统相机拍摄
                    if(hasACamera){
                        AndroidNFileUtils.startActionCapture(getActivity(),mTempFile,INTENT_CODE_CAMERA_REQUEST);
                        //使用自定义相机拍摄
                    }else{
                        Intent intent=new Intent(getActivity(),MediaPictruePhotoActivity.class);
                        intent.putExtra("output",mOutFilePath.getAbsolutePath());
                        intent.putExtra("output-max-width",800);
                        getActivity().startActivityForResult(intent,Constant.REQUEST_TAKE_PHOTO);
                    }
                }else{
                    checkedPermission();
                }
            }
        });
    }


    private final static int PERMISSION_REQUEST_CAMERA = 1;//摄像

    /**
     * 检查拍照权限
     */
    private void checkedPermission() {
        int cameraPerm = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.M) {
                ToastUtils.shoCenterToast("大23，需要检测权限");
                String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_REQUEST_CAMERA);
            }
        } else {
            headImageFromCameraCap();
        }
    }

    /**
     * 获取权限回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    headImageFromCameraCap();
                } else {
                    ToastUtils.shoCenterToast("要正常使用拍摄功能，请务必授予拍照权限！");
                }
                break;
            }
        }
    }


    /**
     * 对图片的拍摄和相册图片的选取，统一在这里进行
     *
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Logger.d(TAG,"onMessageEvent"+event.getRequestCode());
        if(null!=event&&TextUtils.equals("CAMERA_REQUEST",event.getMessage())){
            if(event.getResultState()== Activity.RESULT_CANCELED){
                return;
            }
            try {
                //拍照和裁剪返回
                if ( event.getData()  != null && (event.getRequestCode() == Constant.REQUEST_CLIP_IMAGE || event.getRequestCode()  == Constant.REQUEST_TAKE_PHOTO)) {
                    String imagePath = ClipImageActivity.ClipOptions.createFromBundle(event.getData()).getOutputPath();
                    if (imagePath != null) {
                        mFilePath=new File(imagePath);
                        if(mFilePath.exists()&&mFilePath.isFile()){
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            bindingView.ivUserHead.setImageBitmap(bitmap);
                        }
                    }else{
                        showErrorToast(null,null,"操作错误");
                    }
                    //本地相册选取的图片,转换为Path路径后再交给裁剪界面处理
                }else if(event.getRequestCode() == INTENT_CODE_GALLERY_REQUEST){
                    if(null!=event.getData()){
                        ContentResolver resolver =getActivity().getContentResolver();
                        Uri originalUri = event.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, originalUri);
                            if(null!=bitmap){
                                String filePath = FileUtils.saveBitmap(bitmap, Constant.IMAGE_PATH + IMAGE_DRR_PATH_TEMP);
                                startClipActivity(filePath,mOutFilePath.getAbsolutePath());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            showErrorToast(null,null,"操作错误"+e.getMessage());
                        }
                    }
                    //系统照相机拍照完成回调
                }else if(event.getRequestCode() ==INTENT_CODE_CAMERA_REQUEST){
                    startClipActivity(mTempFile.getAbsolutePath(),mOutFilePath.getAbsolutePath());
                }
            }catch (Exception e){
                showErrorToast(null,null,"操作错误"+e.getMessage());
            }
        }
    }


    /**
     * 去裁剪
     * @param inputFilePath
     * @param outputFilePath
     */
    private void startClipActivity(String inputFilePath, String outputFilePath) {
        Intent intent = new Intent(getActivity(), ClipImageActivity.class);
        intent.putExtra("aspectX", 3);
        intent.putExtra("aspectY", 2);
        intent.putExtra("maxWidth", 800);
        intent.putExtra("tip","");
        intent.putExtra("inputPath", inputFilePath);
        intent.putExtra("outputPath", outputFilePath);
        intent.putExtra("clipCircle",true);
        getActivity().startActivityForResult(intent, Constant.REQUEST_CLIP_IMAGE);
    }



    @Override
    public void showErrorView() {
        change=false;
        closeProgressDialog();
    }

    @Override
    public void complete() {

    }

    @Override
    public void showPostUserDataResult(String data) {
        change=true;
        if(!TextUtils.isEmpty(data)){
            try {
                JSONObject jsonObject=new JSONObject(data);
                //修改基本信息成功
                if(1==jsonObject.getInt("code")){
                    //还需要上传头像
                    if(null!=mFilePath&&mFilePath.exists()&&mFilePath.isFile()){
                        setProgressDialogMessage("基本信息修改成功，正在上传头像中...");
                        mUserEditPresenter.onPostImagePhoto(mUserData.getId(),mFilePath.getAbsolutePath());
                    }else{
                        closeProgressDialog();
                        ToastUtils.shoCenterToast("修改个人信息成功！");
                        CompleteUserDataDialogFragment.this.dismiss();
                    }
                }else{
                    closeProgressDialog();
                    ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                closeProgressDialog();
                ToastUtils.shoCenterToast(e.getMessage());
            }
        }
    }

    @Override
    public void showPostImagePhotoResult(String data) {
        change=true;
        if(!TextUtils.isEmpty(data)){
            try {
                JSONObject jsonObject=new JSONObject(data);
                if(1==jsonObject.getInt("code")&&TextUtils.equals(jsonObject.getString("msg"),Constant.UPLOAD_USER_PHOTO_SUCCESS)){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoadingProgressedView.setResultsCompletes("修改头像成功", CommonUtils.getColor(R.color.app_style),true,Constant.PROGRESS_CLOSE_DELYAED_TIE);
                            mLoadingProgressedView.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    CompleteUserDataDialogFragment.this.dismiss();
                                }
                            });
                        }
                    });

                }else{
                    ToastUtils.shoCenterToast(jsonObject.getString("msg"));
                }
            } catch (JSONException e) {
                ToastUtils.shoCenterToast("上传头像失败");
                e.printStackTrace();
            }
        }
    }


    @Override
    public void dismiss() {
        super.dismiss();
        if(null!=mOnDismissListener){
            mOnDismissListener.onDismiss(change);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if(null!=mFilePath&&mFilePath.exists()&&mFilePath.isFile()){
            FileUtils.deleteFile(mFilePath);
            mFilePath=null;
        }
        mUserData=null;mLoadAnimation=null;mFilePath=null;mUserEditPresenter=null;
    }


    /**
     * 设置消失的监听事件
     * @param onDismissListener
     * @return
     */
    public CompleteUserDataDialogFragment setOnDismissListener(OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
        return mInstance;
    }


    public interface OnDismissListener{
        void onDismiss(boolean change);
    }
    private OnDismissListener mOnDismissListener;

    @Override
    public void onDestroy() {
        if(null!=mUserEditPresenter){
            mUserEditPresenter.detachView();
        }
        super.onDestroy();
    }
}
