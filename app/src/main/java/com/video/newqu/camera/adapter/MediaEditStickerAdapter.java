package com.video.newqu.camera.adapter;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.video.newqu.R;
import com.video.newqu.VideoApplication;
import com.video.newqu.bean.CaptionsInfo;
import com.video.newqu.comadapter.BaseQuickAdapter;
import com.video.newqu.comadapter.BaseViewHolder;
import com.video.newqu.util.ScreenUtils;
import com.video.newqu.util.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * TinyHung@Outlook.com
 * 2017/9/11.
 * 视频编辑界面贴纸适配器
 */

public class MediaEditStickerAdapter extends BaseQuickAdapter<CaptionsInfo,BaseViewHolder>{

    private final int mItemWidth;

    public MediaEditStickerAdapter(List<CaptionsInfo> data) {
        super(R.layout.re_media_sticker_item_layout, data);
        mItemWidth = (ScreenUtils.getScreenWidth()- Utils.dip2px(60))/5;
    }

    @Override
    protected void convert(BaseViewHolder helper, CaptionsInfo item) {
        if(item==null) return;

        RelativeLayout relativeLayout = helper.getView(R.id.re_item_sticker);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) relativeLayout.getLayoutParams();
        layoutParams.height=mItemWidth;
        layoutParams.width= LinearLayout.LayoutParams.MATCH_PARENT;
        relativeLayout.setLayoutParams(layoutParams);

        ImageView view = helper.getView(R.id.iv_item_sticker);


        //作者封面
//        Glide.with(mContext)
//                .load(item.getIconPath())
//                .error(R.drawable.load_err)
//                .crossFade()//渐变
//                .animate(R.anim.item_alpha_in)//加载中动画
//                .diskCacheStrategy(DiskCacheStrategy.ALL)//缓存结果
//                .skipMemoryCache(true)//跳过内存缓存
//                .into((ImageView) helper.getView(R.id.iv_item_sticker));

    }

    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = VideoApplication.getInstance().getApplicationContext().getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
