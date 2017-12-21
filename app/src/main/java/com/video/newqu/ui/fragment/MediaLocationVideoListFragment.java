package com.video.newqu.ui.fragment;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import com.video.newqu.R;
import com.video.newqu.adapter.MoivesListAdapter;
import com.video.newqu.base.BaseFragment;
import com.video.newqu.bean.WeiXinVideo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.listener.OnItemClickListener;
import com.video.newqu.contants.Constant;
import com.video.newqu.databinding.FragmentLocationVideoListBinding;
import com.video.newqu.databinding.RecylerViewEmptyLayoutBinding;
import com.video.newqu.manager.ThreadManager;
import com.video.newqu.model.RecyclerViewSpacesItem;
import com.video.newqu.ui.activity.MediaEditActivity;
import com.video.newqu.ui.activity.MediaVideoCatActivity;
import com.video.newqu.util.MediaStoreUtil;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.ToastUtils;
import com.video.newqu.util.Utils;
import java.io.File;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/7.
 * 本机视频列表
 */

public class MediaLocationVideoListFragment extends BaseFragment<FragmentLocationVideoListBinding> implements BaseQuickAdapter.RequestLoadMoreListener {

    private static final String TAG = MediaLocationVideoListFragment.class.getCanonicalName();
    private MoivesListAdapter mMoivesListAdapter;

    @Override
    protected void initViews() {

    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_location_video_list;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showLoadingView("加载本机视频中...");
        initAdapter();
        loadVideo();
    }

    /**
     * 初始化适配器
     */
    private void initAdapter() {

        if(null==bindingView) return;
        bindingView.recyerView.setLayoutManager(new GridLayoutManager(getActivity(), 3, LinearLayoutManager.VERTICAL, false));
        bindingView.recyerView.addItemDecoration(new RecyclerViewSpacesItem(ScreenUtils.dpToPxInt(1.5f)));
        mMoivesListAdapter = new MoivesListAdapter(null);
        RecylerViewEmptyLayoutBinding emptyViewbindView= DataBindingUtil.inflate(getActivity().getLayoutInflater(),R.layout.recyler_view_empty_layout, (ViewGroup) bindingView.recyerView.getParent(),false);
        mMoivesListAdapter.setEmptyView(emptyViewbindView.getRoot());
        mMoivesListAdapter.setOnLoadMoreListener(this);
        emptyViewbindView.ivItemIcon.setImageResource(R.drawable.ic_list_empty_icon);
        emptyViewbindView.tvItemName.setText("在相册中未找到视频！试试右上角的相册列表吧~");
        bindingView.recyerView.setAdapter(mMoivesListAdapter);

        bindingView.recyerView.addOnItemTouchListener(new OnItemClickListener() {
            @Override
            public void onSimpleItemClick(BaseQuickAdapter adapter, View view, int position) {
                List<WeiXinVideo> data = mMoivesListAdapter.getData();
                if(null!=data&&data.size()>0){
                    WeiXinVideo item = data.get(position);
                    if(null!=item&&null!=item.getVideoPath()&&new File(item.getVideoPath()).isFile()){
                        if(MediaStoreUtil.isSupport(item.getVideoPath(),"mp4","mov","3gp")){
                            if(item.getVideoDortion()>Constant.MEDIA_VIDEO_EDIT_MAX_DURTION){
                                Intent intent=new Intent(getActivity(),MediaVideoCatActivity.class);
                                intent.putExtra(Constant.KEY_MEDIA_RECORD_PRAMER_VIDEO_PATH,item.getVideoPath());
                                intent.putExtra(Constant.KEY_MEDIA_RECORD_PRAMER_SOURCETYPE,2);//如果视频的时间超过限制，先裁剪时长
                                startActivity(intent);
                                return;
                            }else{
                                if(item.getVideoDortion()<Constant.MEDIA_VIDEO_EDIT_MIN_DURTION){
                                    showErrorToast(null,null,"视频长度小于5秒！");
                                    return;
                                }
                                Intent intent=new Intent(getActivity(),MediaEditActivity.class);
                                intent.putExtra(Constant.KEY_MEDIA_RECORD_PRAMER_VIDEO_PATH,item.getVideoPath());
                                intent.putExtra(Constant.KEY_MEDIA_RECORD_PRAMER_SOURCETYPE,2);//选择视频上传
                                startActivity(intent);
                                return;
                            }
                        }else{
                            showErrorToast(null,null,"抱歉，该视频格式不受支持，请换个视频重试");
                            return;
                        }
                    }else{
                        showErrorToast(null,null,"视频不存在，请重新扫描重试！");
                        return;
                    }
                }
            }
        });
    }


    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
           if(10011==msg.what){
                showContentView();
                List<WeiXinVideo> videoInfo = (List<WeiXinVideo>) msg.obj;
                if(null!=mMoivesListAdapter){
                    if(null!=videoInfo){
                        mMoivesListAdapter.setNewData(videoInfo);
                        mMoivesListAdapter.loadMoreEnd();
                    }
                }
            }
            super.handleMessage(msg);
        }
    };


    /**
     * 扫描本机相册的所有视频
     */
    private void loadVideo() {
        String status = Environment.getExternalStorageState();

        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            closeProgressDialog();
            ToastUtils.shoCenterToast("SD存储卡准备中");
            return;
        }
        if (status.equals(Environment.MEDIA_SHARED)) {
            closeProgressDialog();
            ToastUtils.shoCenterToast("您的设备没有链接到USB位挂载");
            return;
        }
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            closeProgressDialog();
            ToastUtils.shoCenterToast("无法读取SD卡，请检查SD卡授予本软件的使用权限！");
            return;
        }

        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                List<WeiXinVideo> videoInfos = MediaStoreUtil.getVideoInfo(getActivity(),"mp4","mov","3gp");
                if(null!=mHandler){
                    Message message=Message.obtain();
                    message.what=10011;
                    message.obj=videoInfos;
                    mHandler.sendMessage(message);
                }

            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        if(null!=mMoivesListAdapter){
            mMoivesListAdapter.setEnableLoadMore(true);
        }
    }

    public void updataAdapter(List<WeiXinVideo> weiXinVideos) {
        if(null!=mMoivesListAdapter){
            mMoivesListAdapter.setNewData(weiXinVideos);
            bindingView.recyerView.post(new Runnable() {
                @Override
                public void run() {
                    mMoivesListAdapter.loadMoreEnd();
                }
            });
        }
    }
}
