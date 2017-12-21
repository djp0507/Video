package com.video.newqu.camera.adapter;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.MediaMusicInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.contants.Constant;
import com.video.newqu.listener.OnMediaMusicListener;
import com.video.newqu.util.FileUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.view.widget.CircleProgressView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TinyHung@Outlook.com
 * 2017/9/12.
 * 视频编辑界面音乐适配器
 */

public class MediaEditMusicAdapter extends BaseQuickAdapter<MediaMusicInfo.DataBean,MediaEditMusicAdapter.MusicViewHolder> {

    private final File mMusicOutPaht;
    private final OnMediaMusicListener mOnMediaMusicListener;
    private final ExecutorService mThreadPool;
    private int curentIndex=-1;
    private boolean isDownload=true;

    public MediaEditMusicAdapter(List<MediaMusicInfo.DataBean> data, OnMediaMusicListener onMediaMusicListener) {
        super(R.layout.re_media_music_item, data);
        this.mOnMediaMusicListener=onMediaMusicListener;
        //将素材存储在内部缓存
        mMusicOutPaht = new File(Constant.PATH_DATA+"/Music");
        if(!mMusicOutPaht.exists()){
            mMusicOutPaht.mkdirs();
        }
        mThreadPool = Executors.newCachedThreadPool();
    }

    @Override
    protected void convert(MusicViewHolder helper, MediaMusicInfo.DataBean item) {

        helper.setText(R.id.tv_item_title,item.getMaterial_title());
        //设置缩略图
        Glide.with(mContext)
                .load(item.getThumb())
                .error(R.drawable.load_err)
                .thumbnail(0.1f)
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存结果
                .skipMemoryCache(true)//跳过内存缓存
                .into(helper.iv_item_icon);

        helper.circle_progressbar.setMaxProgress(100);
        helper.circle_progressbar.setProgress(0);

        //是否已下载
        File file =new File(mMusicOutPaht, item.getMaterial_id()+"_"+ FileUtils.getFileName(item.getMaterial_url()));
        //已经下载了该素材
        if(file.exists()&&file.isFile()){
            Logger.d(TAG,"文件缓存地址:--"+file.getAbsolutePath()+"，id="+item.getMaterial_id());
            helper.setVisible(R.id.iv_download_icon,false);
        }else{
            Logger.d(TAG,"文件预缓存地址:--"+file.getAbsolutePath()+"，id="+item.getMaterial_id());
            helper.setVisible(R.id.iv_download_icon,item.isDownloading()?false:true);
        }
        helper.setVisible(R.id.re_item_selector,item.isSelector()?true:false);
        helper.re_progress.setVisibility(item.isDownloading()?View.VISIBLE:View.GONE);
        //下载的点击事件
        helper.itemView.setOnClickListener(new OnItemClickListener(helper,file,item,helper.getAdapterPosition()));
    }

    /**
     * 是否暂停下载
     * @param isPause
     */
    public void pause(boolean isPause) {
        if(isPause){
            this.isDownload=false;
        }else{
            this.isDownload=true;
        }
    }


    /**
     * 处理点击素材的点击事件
     */
    private class OnItemClickListener implements View.OnClickListener{
        private final MediaMusicInfo.DataBean data;
        private final MusicViewHolder itemView;
        private final File filePath;
        private final int adapterPosition;

        public OnItemClickListener(MusicViewHolder helper, File filePath,  MediaMusicInfo.DataBean item, int adapterPosition) {
            this.itemView=helper;
            this.data=item;
            this.filePath=filePath;
            this.adapterPosition=adapterPosition;
        }

        @Override
        public void onClick(View v) {

            if(null!=filePath&&filePath.exists()&&filePath.isFile()){
                //刚才选中的，反选
                if(curentIndex==adapterPosition){
                    data.setSelector(false);
                    notifyItemChanged(adapterPosition);
                    curentIndex=-1;
                    if(null!=mOnMediaMusicListener){
                        mOnMediaMusicListener.onRemoveAllMusic();
                    }
                }else{
                    //先取消上次选中的状态
                    if(-1!=curentIndex){
                        MediaMusicInfo.DataBean dataBean = getData().get(curentIndex);
                        if(null!=dataBean){
                            dataBean.setSelector(false);
                            notifyItemChanged(curentIndex);
                        }
                    }
                    //设置新选中的
                    data.setSelector(true);
                    notifyItemChanged(adapterPosition);
                    curentIndex=adapterPosition;
                    if(null!=mOnMediaMusicListener){
                        mOnMediaMusicListener.onMusicItemClick(filePath.getAbsolutePath());
                    }
                }
            }else{
                //本地不存在，下载
                new DownloadFileTask(itemView,data).executeOnExecutor(mThreadPool,data.getMaterial_url());
            }
        }
    }


    /**
     * 音乐下载
     */
    private class DownloadFileTask extends AsyncTask<String,Integer,File> {

        private int laterate = 0;//当前已读字节
        private final MusicViewHolder itemView;
        private final MediaMusicInfo.DataBean data;

        public DownloadFileTask(MusicViewHolder itemView,MediaMusicInfo.DataBean data) {
            this.itemView=itemView;
            this.data=data;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(null!=data){
                data.setDownloading(true);
            }
        }

        @Override
        protected File doInBackground(String... params) {
            String name=data.getMaterial_id()+"_"+FileUtils.getFileName(params[0]);
            try {
                URL url = new URL(params[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(900000);
                conn.setConnectTimeout(900000);
                conn.setDoInput(true);
                conn.setRequestMethod("GET");
                if(conn.getResponseCode()==200){
                    int length = conn.getContentLength();
                    int count = 0;
                    File outPutPath = new File(mMusicOutPaht.getAbsolutePath());
                    if (!outPutPath.exists()) {
                        outPutPath.mkdirs();
                    }
                    File apkDownloadPath = new File(outPutPath, name);
                    InputStream in = conn.getInputStream();
                    FileOutputStream os = new FileOutputStream(apkDownloadPath);
                    byte[] buffer = new byte[1024];
                    do {
                        int numread = in.read(buffer);
                        count += numread;
                        int progress = (int) (((float) count / length) * 100);// 得到当前进度
                        if (progress >= laterate + 1) {// 只有当前进度比上一次进度大于等于1，才可以更新进度
                            laterate = progress;
                            this.publishProgress(progress);
                        }
                        if (numread <= 0) {//下载完毕
                            break;
                        }
                        os.write(buffer, 0, numread);
                    } while (isDownload);
                    in.close();
                    os.close();
                    return apkDownloadPath;
                }else{
                    Log.d("下载更新", "doInBackground: conn.getResponseCode()="+conn.getResponseCode());
                    return null;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.toString();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Logger.d(TAG,"onProgressUpdate="+values[0]);
            if(null!=itemView&&null!=itemView.circle_progressbar){
                itemView.circle_progressbar.setProgressNotInUiThread(values[0]);
            }
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            Logger.d(TAG,"下载完成，状态:"+isDownload);
            if(null!=data){
                data.setDownloading(false);
            }
            //下载成功
            if(null!=file&&file.exists()){
                Logger.d(TAG,"文件下载完成地址file="+file.getAbsolutePath()+"，id="+data.getMaterial_id());

                if(null!=itemView){
                    itemView.circle_progressbar.setVisibility(View.GONE);
                    itemView.iv_download_icon.setVisibility(View.GONE);
                    itemView.re_progress.setVisibility(View.GONE);
                }
            }else{
                Logger.d(TAG,"文件下载失败id="+data.getMaterial_id());
                if(null!=itemView){
                    itemView.re_progress.setVisibility(View.GONE);
                    itemView.circle_progressbar.setVisibility(View.GONE);
                    itemView.iv_download_icon.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void stopDownload(){
        this.isDownload=false;
    }


    /**
     * 条目复用View
     */
    public class MusicViewHolder extends BaseViewHolder{


        private ImageView iv_item_icon;
        private ImageView iv_download_icon;
        private TextView tv_item_title;
        private RelativeLayout re_item_icon;
        private RelativeLayout re_progress;
        private CircleProgressView circle_progressbar;

        public MusicViewHolder(View view) {
            super(view);
            iv_item_icon= (ImageView) view.findViewById(R.id.iv_item_icon);
            iv_download_icon= (ImageView) view.findViewById(R.id.iv_download_icon);
            tv_item_title= (TextView) view.findViewById(R.id.tv_item_title);
            re_progress= (RelativeLayout) view.findViewById(R.id.re_progress);
            re_item_icon= (RelativeLayout) view.findViewById(R.id.re_item_icon);
            circle_progressbar= (CircleProgressView) view.findViewById(R.id.circle_progressbar);
        }
    }
}
