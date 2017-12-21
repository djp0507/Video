package com.video.newqu.camera.adapter;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.bean.CaptionsInfo;
import com.video.newqu.comadapter.BaseMultiItemQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.contants.Constant;
import com.video.newqu.listener.OnMediaCaptionsListener;
import com.video.newqu.util.FileUtils;
import com.video.newqu.util.Logger;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.Utils;
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
 * 2017/9/11.
 * 视频编辑界面字幕素材适配器
 */

public class MediaEditCaptionsAdapter extends BaseMultiItemQuickAdapter<CaptionsInfo.DataBean,MediaEditCaptionsAdapter.StickerViewHolder> {

    private final int mItemWidth;
    private final File mStickerImagePath;
    private final ExecutorService mThreadPool;
    private final OnMediaCaptionsListener mOnMediaCaptionsListener;
    private boolean isDownload=true;

    public MediaEditCaptionsAdapter(List<CaptionsInfo.DataBean> data, OnMediaCaptionsListener onMediaCaptionsListener) {
        super(data);

        addItemType(0,R.layout.re_media_netsticker_item_layout);
        addItemType(1,R.layout.re_sticker_header_layout);
        addItemType(2,R.layout.re_sticker_header_layout);//空背景
        mItemWidth = (ScreenUtils.getScreenWidth()- Utils.dip2px(60))/5;
        this.mOnMediaCaptionsListener=onMediaCaptionsListener;
        mThreadPool = Executors.newCachedThreadPool();
        //将素材存储在内部缓存
        mStickerImagePath = new File(Constant.PATH_DATA+"/Captions");
        if(!mStickerImagePath.exists()){
            mStickerImagePath.mkdirs();
        }
    }

    @Override
    protected void convert(StickerViewHolder helper, CaptionsInfo.DataBean item) {
        Logger.d(TAG,"item.getItemType()="+item.getItemType());
        switch (item.getItemType()) {
            //正常的
            case 0:
                setItemData(helper,item);
                break;
            //清除所有的
            case 1:
                setCanelAllCaptionsItemData(helper,item);
                break;
            //空的
            case 2:
                setEmptyCaptionsItemData(helper,item);
                break;
        }
    }

    /**
     * 设置空的头部
     * @param helper
     * @param item
     */
    private void setEmptyCaptionsItemData(StickerViewHolder helper, CaptionsInfo.DataBean item) {

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) helper.ll_header.getLayoutParams();
        int mItemWidth = (ScreenUtils.getScreenWidth()- Utils.dip2px(60))/5;
        layoutParams.width=mItemWidth;
        layoutParams.height=mItemWidth;
        helper.ll_header.setLayoutParams(layoutParams);
        helper.iv_item_empty_icon.setImageResource(R.drawable.iv_captions_text);
        helper.ll_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnMediaCaptionsListener){
                    mOnMediaCaptionsListener.onEmptyCaptions();
                }
            }
        });
    }

    /**
     * 设置清除所有的头部
     * @param helper
     * @param item
     */
    private void setCanelAllCaptionsItemData(StickerViewHolder helper, CaptionsInfo.DataBean item) {

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) helper.ll_header.getLayoutParams();
        int mItemWidth = (ScreenUtils.getScreenWidth()- Utils.dip2px(60))/5;
        layoutParams.width=mItemWidth;
        layoutParams.height=mItemWidth;
        helper.ll_header.setLayoutParams(layoutParams);
        helper.iv_item_empty_icon.setImageResource(R.drawable.close_effect);
        helper.ll_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=mOnMediaCaptionsListener){
                    mOnMediaCaptionsListener.onRemoveAllCaptions();
                }
            }
        });
    }

    /**
     * 设置Item
     * @param helper
     * @param item
     */
    private void setItemData(StickerViewHolder helper,CaptionsInfo.DataBean item) {
        if(item==null||null==helper) return;

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) helper.re_item_sticker.getLayoutParams();
        layoutParams.height=mItemWidth;
        layoutParams.width= LinearLayout.LayoutParams.MATCH_PARENT;
        helper.re_item_sticker.setLayoutParams(layoutParams);
//        helper.re_item_sticker.setBackgroundResource(item.isSelector()?R.drawable.media_btn_bg_shape_orgin:0);//是否选中了

        //设置缩略图
        Glide.with(mContext)
                .load(item.getCaption_url())
                .error(R.drawable.iv_media_sticker_min_error)
                .thumbnail(0.1f)
                .crossFade()//渐变
                .animate(R.anim.item_alpha_in)//加载中动画
                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存结果
                .skipMemoryCache(true)//跳过内存缓存
                .into(helper.iv_item_sticker);

        helper.circle_progressbar.setMaxProgress(100);
        helper.circle_progressbar.setProgress(0);


        //是否已下载
        File file =new File(mStickerImagePath, item.getCaption_id()+"_"+FileUtils.getFileName(item.getCaption_url()));
        //已经下载了该素材
        if(file.exists()&&file.isFile()){
            Logger.d(TAG,"文件缓存地址:--"+file.getAbsolutePath()+"，id="+item.getCaption_id());
            helper.setVisible(R.id.iv_download_icon,false);
        }else{
            Logger.d(TAG,"文件预缓存地址:--"+file.getAbsolutePath()+"，id="+item.getCaption_id());
            helper.setVisible(R.id.iv_download_icon,item.isDownloading()?false:true);
        }

        //是否正在下载
        helper.re_progress.setVisibility(item.isDownloading()?View.VISIBLE:View.GONE);
        //下载的点击事件
        helper.itemView.setOnClickListener(new OnItemClickListener(helper,file,item,helper.getAdapterPosition()));
    }

    public void stopDownload() {
        this.isDownload=false;
    }

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

        private final CaptionsInfo.DataBean data;
        private final StickerViewHolder itemView;
        private final File filePath;
        private final int adapterPosition;

        public OnItemClickListener(StickerViewHolder helper, File filePath, CaptionsInfo.DataBean item, int adapterPosition) {
            this.itemView=helper;
            this.data=item;
            this.filePath=filePath;
            this.adapterPosition=adapterPosition;
        }

        @Override
        public void onClick(View v) {

            if(null!=filePath&&filePath.exists()&&filePath.isFile()){
                if(null!=mOnMediaCaptionsListener){
                    mOnMediaCaptionsListener.onCapsionsItemClick(filePath.getAbsolutePath(),data);
                }
            }else{
                //本地不存在，下载
                new DownloadFileTask(itemView,data).executeOnExecutor(mThreadPool,data.getCaption_url());
            }
        }
    }


    /**
     * 素材下载
     */
    private class DownloadFileTask extends AsyncTask<String,Integer,File>{

        private int laterate = 0;//当前已读字节
        private final StickerViewHolder itemView;
        private final CaptionsInfo.DataBean data;

        public DownloadFileTask(StickerViewHolder itemView, CaptionsInfo.DataBean data) {
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
            String name=data.getCaption_id()+"_"+FileUtils.getFileName(params[0]);
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
                    File outPutPath = new File(mStickerImagePath.getAbsolutePath());
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
            //下载成功
            if(null!=file&&file.exists()){
                Logger.d(TAG,"文件下载完成地址file="+file.getAbsolutePath()+"，id="+data.getCaption_id());
                if(null!=data){
                    data.setDownloading(false);
                }
                if(null!=itemView){
                    itemView.circle_progressbar.setVisibility(View.GONE);
                    itemView.iv_download_icon.setVisibility(View.GONE);
                    itemView.re_progress.setVisibility(View.GONE);
                }
            }else{
                Logger.d(TAG,"文件下载失败id="+data.getCaption_id());
                if(null!=itemView){
                    itemView.re_progress.setVisibility(View.GONE);
                    itemView.circle_progressbar.setVisibility(View.GONE);
                    itemView.iv_download_icon.setVisibility(View.VISIBLE);
                }
            }
        }
    }


    /**
     * 条目复用View
     */
    public class StickerViewHolder extends BaseViewHolder{

        private RelativeLayout re_item_sticker;
        private RelativeLayout re_progress;
        private ImageView iv_item_sticker;
        private ImageView iv_download_icon;
        private CircleProgressView circle_progressbar;
        private LinearLayout ll_header;
        private ImageView iv_item_empty_icon;

        public StickerViewHolder(View view) {
            super(view);
            re_item_sticker= (RelativeLayout) view.findViewById(R.id.re_item_sticker);
            re_progress= (RelativeLayout) view.findViewById(R.id.re_progress);
            iv_item_sticker= (ImageView) view.findViewById(R.id.iv_item_sticker);
            iv_download_icon= (ImageView) view.findViewById(R.id.iv_download_icon);
            iv_item_empty_icon= (ImageView) view.findViewById(R.id.iv_item_empty_icon);
            circle_progressbar= (CircleProgressView) view.findViewById(R.id.circle_progressbar);
            ll_header= (LinearLayout) view.findViewById(R.id.ll_header);
        }
    }
}
