package com.video.newqu.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.adapter.PhotoItemAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.PhotoInfo;
import com.video.newqu.databinding.FragmentServiceMessageBinding;
import com.video.newqu.databinding.ReImageAddLayoutBinding;
import com.video.newqu.ui.dialog.UploadProgressView;
import com.video.newqu.ui.activity.MediaImageListActivity;
import com.video.newqu.ui.activity.MediaImagePreviewActivity;
import com.video.newqu.ui.contract.ServiceMessageContract;
import com.video.newqu.ui.presenter.ServiceMessagePresenter;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import com.video.newqu.model.GridSpaceItemDecorationComent;
import java.util.ArrayList;
import java.util.List;

/**
 * TinyHung@outlook.com
 * 2017-06-09 19:21
 * 意见反馈
 */

public class ServiceMessageFragment extends BaseFragment<FragmentServiceMessageBinding> implements View.OnClickListener, ServiceMessageContract.View {

    private Animation mLoadAnimation;
    private PhotoItemAdapter mAdapter;
    private List<String> urls=new ArrayList<>();
    private CharSequence content_temp;//监听前的文本
    private int content_editStart;//光标开始位置
    private int content_editEnd;//光标结束位置
    private final int content_charMaxNum = 200;
    private ReImageAddLayoutBinding mReImageAddLayoutBinding;
    private ServiceMessagePresenter mServiceMessagePresenter;
    private UploadProgressView mUploadProgressView;


    @Override
    protected void initViews() {
        mLoadAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        bindingView.btSubmit.setOnClickListener(this);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) bindingView.recyerView.getLayoutParams();
        layoutParams.height=(ScreenUtils.getScreenWidth()- ScreenUtils.dpToPxInt(26))/3+ScreenUtils.dpToPxInt(8);
        bindingView.recyerView.setLayoutParams(layoutParams);

        bindingView.etContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                content_temp = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bindingView.tvTextNum.setText(content_charMaxNum - s.length()+"/200");
            }

            @Override
            public void afterTextChanged(Editable s) {
                content_editStart = bindingView.etContent.getSelectionStart();
                content_editEnd = bindingView.etContent.getSelectionEnd();
                if (content_temp.length() > content_charMaxNum) {
                    bindingView.etContent.setError("字数过长");
                    s.delete(content_editStart - 1, content_editEnd);
                    int tempSelection = content_editEnd;
                    bindingView.etContent.setText(s);
                    bindingView.etContent.setSelection(tempSelection);
                }
            }
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_service_message;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showContentView();
        mServiceMessagePresenter = new ServiceMessagePresenter();
        mServiceMessagePresenter.attachView(this);
        initPhotoAdapter();
    }

    /**
     * 显示已选择的图片
     */
    private void initPhotoAdapter() {

        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(),1, LinearLayoutManager.HORIZONTAL,false));
        bindingView.recyerView.addItemDecoration(new GridSpaceItemDecorationComent(Utils.dip2px(2)));
        mAdapter=new PhotoItemAdapter(null, ScreenUtils.getScreenWidth());
        bindingView.recyerView.setAdapter(mAdapter);
        ReImageAddLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.re_image_add_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mAdapter.setEmptyView(emptyViewbindView.getRoot());

        emptyViewbindView.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChoosePicture();
            }
        });

        mAdapter.setOnItemClickListener(new PhotoItemAdapter.OnItemClickListener() {
            //删除
            @Override
            public void onDelete(int poistion) {
                List<PhotoInfo> photoInfoList = mAdapter.getData();

                if(null!=photoInfoList&&photoInfoList.size()>0){
                    mAdapter.remove(poistion);
                    initFooterAddPhotoView();
                }
            }
            //预览
            @Override
            public void onPreviewImage(PhotoInfo data) {
                if(null!=data){
                    if(urls.size()>0){
                        urls.clear();
                    }
                    urls.add(data.getImagePath());
                    if(urls!=null&&urls.size()>0){
                        Intent intent=new Intent(getActivity(), MediaImagePreviewActivity.class);
                        intent.putExtra("local",true);
                        intent.putStringArrayListExtra("pic", (ArrayList<String>) urls);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.zoomin, 0);
                    }
                }
            }
        });
        initFooterAddPhotoView();
    }


    /**
     * 添加图片按钮
     */
    private void initFooterAddPhotoView() {

        List<PhotoInfo> photoInfoList = mAdapter.getData();
        //图片数量达到上限，去除脚步Item
        if(null!=photoInfoList&&photoInfoList.size()>=3){

            if(null!=mReImageAddLayoutBinding&&null!=mAdapter&&mAdapter.getFooterViewsCount()>0){
                mAdapter.removeFooterView(mReImageAddLayoutBinding.getRoot());
                mReImageAddLayoutBinding=null;
            }
        //移除脚步
        }else if(null!=photoInfoList&&photoInfoList.size()<=0){

            if(null!=mReImageAddLayoutBinding&&null!=mAdapter&&mAdapter.getFooterViewsCount()>0){
                mAdapter.removeFooterView(mReImageAddLayoutBinding.getRoot());
                mReImageAddLayoutBinding=null;
            }
        }else if(null!=photoInfoList&&photoInfoList.size()>0){
            if(null!=mAdapter&&mAdapter.getFooterViewsCount()<=0){

                mReImageAddLayoutBinding = DataBindingUtil.inflate(getActivity().getLayoutInflater(), R.layout.re_image_add_layout, (ViewGroup) bindingView.recyerView.getParent(), false);
                mReImageAddLayoutBinding.ivAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ChoosePicture();
                    }
                });
                mAdapter.addFooterView(mReImageAddLayoutBinding.getRoot());
            }
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_submit:
                sendMessage();
                break;
        }
    }

    /**
     * 选择本机所有照片
     */

    private void ChoosePicture() {
        List<PhotoInfo> photoInfos = mAdapter.getData();
        if(null!=photoInfos&&photoInfos.size()>=3){
            showErrorToast(null,null,"最多只能选三张");
            return;
        }
        Logger.d("图片选择","已选中的"+photoInfos.size()+"");
        Intent intent=new Intent(getActivity(),MediaImageListActivity.class);
        intent.putParcelableArrayListExtra("photoinfos",(ArrayList<? extends Parcelable>) photoInfos);
        intent.putExtra("max_num",3);
        startActivityForResult(intent,0x1005);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(0x1005==requestCode&&0x1006==resultCode){
            if(null!=data){
                ArrayList<PhotoInfo> photoInfos=data.getParcelableArrayListExtra("image_list");
                if(null!=photoInfos&&photoInfos.size()>0){
                    mAdapter.setNewData(photoInfos);
                    initFooterAddPhotoView();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    /**
     * 发送消息到服务器
     */
    private void sendMessage() {

        String content = bindingView.etContent.getText().toString().trim();
        if(TextUtils.isEmpty(content)){
            bindingView.etContent.startAnimation(mLoadAnimation);
            showErrorToast(null,null,"请输入反馈内容");
            return;
        }

        if(!Utils.isCheckNetwork()){
            showNetWorkTips();
            return;
        }

        if(null==mUploadProgressView) mUploadProgressView = new UploadProgressView(getActivity(),true);
        mUploadProgressView.setOnDialogBackListener(new UploadProgressView.OnDialogBackListener() {
            @Override
            public void onBack() {
                ToastUtils.shoCenterToast("请等待上传完成");
            }
        });
        mUploadProgressView.initProgressBar();
        mUploadProgressView.setMax(100);
        mUploadProgressView.setMessage("上传准备中...");
        mUploadProgressView.show();
        Logger.d("图片选择","已选中大小"+mAdapter.getData().size()+"");
        String contact = bindingView.etPhoneNumber.getText().toString().trim();
        mServiceMessagePresenter.sendMessage(VideoApplication.getLoginUserID(),content,contact,mAdapter.getData());
    }


    @Override
    public void onDestroy() {
        if(null!=mServiceMessagePresenter){
            mServiceMessagePresenter.detachView();
        }
        if(mAdapter!=null) mAdapter=null;
        super.onDestroy();
    }

    //==========================================发送留言结果=========================================

    @Override
    public void showErrorView() {

    }

    @Override
    public void complete() {

    }

    @Override
    public void showSendMessageError(String data) {
        if(null!=mUploadProgressView) mUploadProgressView.dismiss();
        showErrorToast(null,null,data);
    }

    @Override
    public void showSendMessageResult(String data) {
        if(null!=mUploadProgressView) {
            mUploadProgressView.setMessage(data);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null!=mUploadProgressView) mUploadProgressView.dismiss();
                mUploadProgressView=null;
                getActivity().onBackPressed();
            }
        },600);
    }

    @Override
    public void onUpdataProgress(float progress) {
        if(null!=mUploadProgressView) mUploadProgressView.setProgressNotInUiThread((int) (progress*100));
    }

    @Override
    public void showCutImageIng(final String data) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(null!=mUploadProgressView) mUploadProgressView.setMessage(data);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(null!=mUploadProgressView) mUploadProgressView.dismiss();
    }

    @Override
    public void showStartUpdata(String data) {
        if(null!=mUploadProgressView) mUploadProgressView.setMessage(data);
    }
}
