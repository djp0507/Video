package com.video.newqu.contants;

import android.os.Environment;
import com.video.newqu.VideoApplication;
import com.video.newqu.listener.OnPostPlayStateListener;
import com.video.newqu.manager.DBBatchVideoUploadManager;
import com.video.newqu.manager.DBUserPlayerVideoHistoryManager;
import com.video.newqu.manager.DBVideoUploadManager;
import com.video.newqu.manager.ThreadManager;
import com.video.newqu.upload.bean.UploadParamsConfig;
import com.video.newqu.util.ACache;
import com.video.newqu.util.DeviceUtils;
import com.video.newqu.util.FileUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.PostPlayStateHanderUtils;
import com.video.newqu.util.SharedPreferencesUtil;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * TinyHung@Outlook.com
 * 2017/12/12.
 * 持有数据库管理者对象，缓存管理者对象等。。当内存不足时释放这些对象
 */

public class ApplicationManager {

    private static final String TAG =ApplicationManager.class.getSimpleName();
    private static WeakReference<ApplicationManager> mInstanceWeakReference=null;//自己
    public  WeakReference<DBVideoUploadManager> mUploadManagerWeakReference=null;//视频上传记录
    public  WeakReference<DBBatchVideoUploadManager> mDBBatchVideoUploadManagerWeakReference=null;//微信视频上传记录
    public  WeakReference<DBUserPlayerVideoHistoryManager> mUserPlayerVideoHistoryManagerWeakReference=null;//本地视频播放记录管理者
    public WeakReference<ACache> mACacheWeakReference=null;//缓存
    public WeakReference<UploadParamsConfig> mUploadParamsConfigWeakReference=null;//上传配置

    public static synchronized ApplicationManager getInstance(){
        synchronized (ApplicationManager.class){
            if(null==mInstanceWeakReference||null==mInstanceWeakReference.get()){
                ApplicationManager applicationManager=new ApplicationManager();
                mInstanceWeakReference=new WeakReference<ApplicationManager>(applicationManager);
            }
        }
        return mInstanceWeakReference.get();
    }

    /**
     * 上传记录的数据库管理者
     * @return
     */
    public DBVideoUploadManager getVideoUploadDB(){
        if(null==mUploadManagerWeakReference||null==mUploadManagerWeakReference.get()){
            DBVideoUploadManager dbVideoUploadManager=new DBVideoUploadManager(VideoApplication.getInstance().getApplicationContext());
            mUploadManagerWeakReference=new WeakReference<DBVideoUploadManager>(dbVideoUploadManager);
        }
        return mUploadManagerWeakReference.get();
    }

    /**
     * 微信上传记录的数据库管理者
     * @return
     */
    public DBBatchVideoUploadManager getWeiXinVideoUploadDB(){
        if(null==mDBBatchVideoUploadManagerWeakReference||null==mDBBatchVideoUploadManagerWeakReference.get()){
            DBBatchVideoUploadManager dbVideoUploadManager=new DBBatchVideoUploadManager(VideoApplication.getInstance().getApplicationContext());
            mDBBatchVideoUploadManagerWeakReference=new WeakReference<DBBatchVideoUploadManager>(dbVideoUploadManager);
        }
        return mDBBatchVideoUploadManagerWeakReference.get();
    }

    /**
     * 本地视频记录
     * @return
     */
    public DBUserPlayerVideoHistoryManager getUserPlayerDB(){
        if(null==mUserPlayerVideoHistoryManagerWeakReference||null==mUserPlayerVideoHistoryManagerWeakReference.get()){
            DBUserPlayerVideoHistoryManager userPlayerVideoHistoryManager=new DBUserPlayerVideoHistoryManager(VideoApplication.getInstance().getApplicationContext());
            mUserPlayerVideoHistoryManagerWeakReference=new WeakReference<DBUserPlayerVideoHistoryManager>(userPlayerVideoHistoryManager);
        }
        return mUserPlayerVideoHistoryManagerWeakReference.get();
    }

    public UploadParamsConfig getUploadConfig(){
        if(null==mUploadParamsConfigWeakReference||null==mUploadParamsConfigWeakReference.get()){
            mUploadParamsConfigWeakReference=new WeakReference<UploadParamsConfig>(new UploadParamsConfig());
        }
        return mUploadParamsConfigWeakReference.get();
    }
    /**
     * 全局初始化需要传入
     * @param mACache
     */
    public void setCacheExample(ACache mACache) {
        if(null==mACacheWeakReference||null==mACacheWeakReference.get()){
            mACacheWeakReference=new WeakReference<ACache>(mACache);
        }
    }

    /**
     * 缓存的对象实例
     * @return
     */
    public ACache getCacheExample(){
        if(null==mACacheWeakReference||null==mACacheWeakReference.get()){
            ACache aCache=ACache.get(VideoApplication.getInstance().getApplicationContext());
            mACacheWeakReference=new WeakReference<ACache>(aCache);
        }
        return mACacheWeakReference.get();
    }


    /**
     * 统计播放次数
     * @param video_id
     * @param currentPositionWhenPlaying
     * @param i
     * @param onPostPlayStateListener
     */
    public  void postVideoPlayState(final String video_id, final int currentPositionWhenPlaying, final int i, final OnPostPlayStateListener onPostPlayStateListener) {
        ThreadManager.getInstance().createLongPool().execute(new Runnable() {
            @Override
            public void run() {
                PostPlayStateHanderUtils.postVideoPlayState(video_id,currentPositionWhenPlaying,i,onPostPlayStateListener);
            }
        });
    }


    /**
     * 获取视频缓存的目录
     * @return
     */
    public String getVideoCacheDir() {
        String cachePath = FileUtils.getFileDir(VideoApplication.getInstance().getApplicationContext());
        Logger.d(TAG,"内部缓存");
        if(null==cachePath){
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                File file= new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/.XinQu/Cache/Video/");
                if(!file.exists()){
                    file.mkdirs();
                }
                cachePath=file.getAbsolutePath();
                //使用内部缓存
                Logger.d(TAG,"SD卡缓存");
            }
        }
        return cachePath;
    }

    /**
     * 获取SD卡文件缓存路径
     * @return
     */
    public String  getSdPath(){
        if (FileUtils.getDiskCacheDir(VideoApplication.getInstance().getApplicationContext()) != null) {
            return FileUtils.getDiskCacheDir(VideoApplication.getInstance().getApplicationContext());
        }
        return null;
    }

    /**
     * 初始化SD卡路径
     */
    public void initSDPath() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File file = new File(Constant.IMAGE_PATH);
            if(!file.exists()&&!file.isDirectory()){
                file.mkdirs();
            }
            try {
                if(0== SharedPreferencesUtil.getInstance().getInt(Constant.IS_DELETE_PHOTO_DIR)){
                    boolean fileOrDirectory = FileUtils.deleteFileOrDirectory(file);
                    SharedPreferencesUtil.getInstance().putInt(Constant.IS_DELETE_PHOTO_DIR,1);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            File cacheFile=new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/XinQu/");
            if(!cacheFile.exists()&&!file.isDirectory()){
                cacheFile.mkdirs();
            }

            //初始化拍摄视频缓存路径
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            String path;
            String comperPath;
            if (DeviceUtils.isZte()) {
                if (dcim.exists()) {
                    path=dcim + "/XinQu/Video";
                    comperPath=dcim + "/XinQu/Video/Comple";
                } else {
                    path=dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/XinQu/Video";
                    comperPath=dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/XinQu/Video/Comple";
                }
            } else {
                path=dcim + "/XinQu/Video";
                comperPath=dcim + "/XinQu/Video/Comple";
            }
            File videoPath = new File(path);
            if (!videoPath.exists()) {
                videoPath.mkdirs();
            }
            File complePath=new File(comperPath);
            if (!complePath.exists()) {
                complePath.mkdirs();
            }
        }
    }

    /**
     * 获取APP的各种缓存目录
     * @param MODE 0：视频录制输出目录，1：视频合成输出目录
     * @return
     */
    public String getOutPutPath(int MODE){
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            String recordOutPath;//视频录制输出目录
            String composeOutPath;//视频合成输出目录
            if (DeviceUtils.isZte()) {
                if (dcim.exists()) {
                    recordOutPath=dcim + "/XinQu/Video";
                    composeOutPath=dcim + "/XinQu/Video/Comple";
                } else {
                    recordOutPath=dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/XinQu/Video";
                    composeOutPath=dcim.getPath().replace("/sdcard/", "/sdcard-ext/") + "/XinQu/Video/Comple";
                }
            } else {
                recordOutPath=dcim + "/XinQu/Video";
                composeOutPath=dcim + "/XinQu/Video/Comple";
            }

            File videoPath = new File(recordOutPath);
            if (!videoPath.exists()) {
                videoPath.mkdirs();
            }

            File complePath=new File(composeOutPath);
            if (!complePath.exists()) {
                complePath.mkdirs();
            }
            if(0==MODE){
                return recordOutPath;
            }else if(1==MODE){
                return composeOutPath;
            }
        }
        return null;
    }


    public void onDestory(){
        if(null!=mUploadManagerWeakReference){
            mUploadManagerWeakReference.clear();
            mUploadManagerWeakReference=null;
        }

        if(null!=mDBBatchVideoUploadManagerWeakReference){
            mDBBatchVideoUploadManagerWeakReference.clear();
            mDBBatchVideoUploadManagerWeakReference=null;
        }

        if(null!=mUserPlayerVideoHistoryManagerWeakReference){
            mUserPlayerVideoHistoryManagerWeakReference.clear();
            mUserPlayerVideoHistoryManagerWeakReference=null;
        }

        if(null!=mACacheWeakReference){
            mACacheWeakReference.clear();
            mACacheWeakReference=null;
        }

        if(null!=mUploadParamsConfigWeakReference){
            mUploadParamsConfigWeakReference.clear();
            mUploadParamsConfigWeakReference=null;
        }

        if(null!=mInstanceWeakReference){
            mInstanceWeakReference.clear();
            mInstanceWeakReference=null;
        }
    }
}
