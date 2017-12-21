package com.video.newqu.base;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.bean.FollowVideoList;
import com.video.newqu.bean.UserPlayerVideoHistoryList;
import com.video.newqu.contants.ApplicationManager;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentBaseVideoPlayBinding;
import com.video.newqu.listener.SnackBarListener;
import com.video.newqu.ui.activity.ContentFragmentActivity;
import com.video.newqu.ui.dialog.LoadingProgressView;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ToastUtils;

/**
 * TinyHung@outlook.com
 * 2017/3/17 15:46
 * 视频播放父类
 */

public abstract class BaseVideoPlayFragment<VS extends ViewDataBinding> extends Fragment {

    // 子布局view
    protected VS bindingView;
    protected AppCompatActivity context;
    protected LoadingProgressView mLoadingProgressedView;
    protected FragmentBaseVideoPlayBinding baseBindingView;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context= (AppCompatActivity) context;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        baseBindingView=DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.fragment_base_video_play,null,false);
        bindingView = DataBindingUtil.inflate(getActivity().getLayoutInflater(), getLayoutId(), null, false);
        if(null!=bindingView){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            bindingView.getRoot().setLayoutParams(params);
            baseBindingView.reConttentView.addView(bindingView.getRoot());
        }
        return baseBindingView.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews();
    }

    /**
     * 根据Targe打开新的界面
     * @param title
     * @param fragmentTarge
     */
    protected void startTargetActivity(int fragmentTarge,String title,String authorID,int authorType) {
        Intent intent=new Intent(getActivity(), ContentFragmentActivity.class);
        intent.putExtra(Constant.KEY_FRAGMENT_TYPE,fragmentTarge);
        intent.putExtra(Constant.KEY_TITLE,title);
        intent.putExtra(Constant.KEY_AUTHOR_ID,authorID);
        intent.putExtra(Constant.KEY_AUTHOR_TYPE,authorType);
        getActivity().startActivity(intent);
    }

    protected void saveLocationHistoryList(final FollowVideoList.DataBean.ListsBean data) {
        if(null==data) return;
        new Thread(){
            public static final String TAG ="saveLocationHistoryList" ;

            @Override
            public void run() {
                super.run();
                Logger.d(TAG,"");
                UserPlayerVideoHistoryList userLookVideoList=new UserPlayerVideoHistoryList();
                userLookVideoList.setUserName(TextUtils.isEmpty(data.getNickname())?"火星人":data.getNickname());
                userLookVideoList.setUserSinger("该宝宝没有个性签名");
                userLookVideoList.setUserCover(data.getLogo());
                userLookVideoList.setVideoDesp(data.getDesp());
                userLookVideoList.setVideoLikeCount(TextUtils.isEmpty(data.getCollect_times())?"0":data.getCollect_times());
                userLookVideoList.setVideoCommendCount(TextUtils.isEmpty(data.getComment_count())?"0":data.getComment_count());
                userLookVideoList.setVideoShareCount(TextUtils.isEmpty(data.getShare_times())?"0":data.getShare_times());
                userLookVideoList.setUserId(data.getUser_id());
                userLookVideoList.setVideoId(data.getVideo_id());
                userLookVideoList.setVideoCover(data.getCover());
                userLookVideoList.setUploadTime(data.getAdd_time());
                userLookVideoList.setAddTime(System.currentTimeMillis());
                userLookVideoList.setIs_interest(data.getIs_interest());
                userLookVideoList.setIs_follow(data.getIs_follow());
                userLookVideoList.setVideoPath(data.getPath());
                userLookVideoList.setVideoPlayerCount(TextUtils.isEmpty(data.getPlay_times())?"0":data.getPlay_times());
                userLookVideoList.setVideoType(TextUtils.isEmpty(data.getType())?"2":data.getType());
                ApplicationManager.getInstance().getUserPlayerDB().insertNewPlayerHistoryOfObject(userLookVideoList);
            }
        }.start();
    }



    protected abstract void initViews();

    /**
     * 在这里实现Fragment数据的缓加载.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            onVisible();
        } else {
            onInvisible();
        }
    }

    protected void onInvisible() {

    }

    protected void onVisible() {

    }



    /**
     * 子类可实现此登录方法
     */
    protected void onLogin() {

    }

    protected <T extends View> T getView(int id) {
        if(null==getView()) return null;
        return (T) getView().findViewById(id);
    }

    /**
     * 布局
     */
    public abstract int getLayoutId();

    /**
     * 加载失败后点击后的操作
     */
    protected void onRefresh() {

    }

    /**
     * 显示进度框
     * @param message
     * @param isProgress
     */
    protected void showProgressDialog(String message,boolean isProgress){
        if(null==mLoadingProgressedView){
            mLoadingProgressedView = new LoadingProgressView(getActivity(),isProgress);
        }
        if(!getActivity().isFinishing()){
            mLoadingProgressedView.setMessage(message);
            mLoadingProgressedView.show();
        }
    }

    /**
     * 关闭进度框
     */
    protected void closeProgressDialog(){
        if(null!=mLoadingProgressedView&&!getActivity().isFinishing()&&mLoadingProgressedView.isShowing()){
            mLoadingProgressedView.dismiss();
            mLoadingProgressedView=null;
        }
    }

    /**
     * 失败吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    protected void showErrorToast(String action, SnackBarListener snackBarListener, String message){
        if(null!=getActivity()){
            ToastUtils.showSnackebarStateToast(getActivity().getWindow().getDecorView(),action,snackBarListener, R.drawable.snack_bar_error_white, Constant.SNACKBAR_ERROR,message);
        }
    }

    /**
     * 统一的网络设置入口
     */
    protected void showNetWorkTips(){

        showErrorToast("网络设置", new SnackBarListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);//直接进入网络设置
                startActivity(intent);
            }
        }, "没有可用的网络链接");
    }

    /**
     * 成功吐司
     * @param action
     * @param snackBarListener
     * @param message
     */
    protected void showFinlishToast(String action, SnackBarListener snackBarListener, String message){
        if(null!=getActivity()){
            ToastUtils.showSnackebarStateToast(getActivity().getWindow().findViewById(Window.ID_ANDROID_CONTENT),action,snackBarListener, R.drawable.snack_bar_done_white, Constant.SNACKBAR_DONE,message);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context=null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();
    }
}
