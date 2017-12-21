package com.video.newqu.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.video.newqu.R;
import com.video.newqu.base.BaseActivity;
import com.video.newqu.databinding.ActivityPreviewImageBinding;
import com.video.newqu.view.widget.PinchImageView;
import com.video.newqu.view.widget.PinchImageViewPager;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author TinyHung@Outlook.com
 * @des $查看多张图片的大图模式
 */
public class MediaImagePreviewActivity extends BaseActivity<ActivityPreviewImageBinding> {

    private static final String TAG = "ShowBigImageListActivity";
    private PinchImageViewPager mViewPager;
    private ArrayList<String> mPics;
    private boolean mLocal;
    private PopupWindow mPopupWindow;
    private String imageUrl=null;//图片下载地址
    private boolean isNet;

    @Override
    public void initViews() {
        ((ImageView) findViewById(R.id.ivBack)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void initData() {

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置为全屏幕模式，去除标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_preview_image);
        Intent intent = getIntent();
        mPics = intent.getStringArrayListExtra("pic");
        mLocal = intent.getBooleanExtra("local", false);//是否是本地图片
        isNet=intent.getBooleanExtra("isNet",false);
        if(mPics!=null&&mPics.size()>0){
            showImage();
        }
    }

    /**
     * 显示图片
     */
    private void showImage() {
        mViewPager = ((PinchImageViewPager) findViewById(R.id.view_pager));
        mViewPager.setAdapter(mImagePagerAdapter);
        mViewPager.setOnPageChangeListener(onPageChangeListener);
    }
    //缓存
    LinkedList<PinchImageView> cache = new LinkedList<>();
    /**
     * 图片适配器
     */
    private PagerAdapter mImagePagerAdapter=new PagerAdapter() {

        @Override
        public int getCount() {
            return mPics == null ? 0 : mPics.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            PinchImageView piv;
            if (cache.size() > 0) {
                piv = cache.remove();
                piv.reset();
            } else {
                piv = new PinchImageView(MediaImagePreviewActivity.this);
            }
            container.addView(piv);
            piv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            piv.setOnLongClickListener(new OnImageLongClickListener());
            return piv;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            PinchImageView piv = (PinchImageView) object;
            container.removeView(piv);
            cache.add(piv);
        }

        /**
         * 设置图片
         * @param container
         * @param position
         * @param object
         */
        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            PinchImageView piv = (PinchImageView) object;
            String url="";
            if(mLocal){
                url="file://"+mPics.get(position);
            }else{
                url= mPics.get(position);
            }
            if(isNet){
                url=mPics.get(position);
            }
            if(!TextUtils.isEmpty(url)){
                Glide.with(container.getContext())
                        .load(url)
                        .placeholder(R.drawable.loading_12)
                        .error(R.drawable.load_err)
                        .fitCenter()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .into(piv);
                mViewPager.setMainPinchImageView(piv);
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mViewPager!=null){
            mViewPager.removeAllViews();
        }
        mPopupWindow=null;
        Runtime.getRuntime().gc();
    }



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.zoomout);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    /**
     * 对当前显示的角标进行监听
     */
    private PinchImageViewPager.OnPageChangeListener onPageChangeListener=new PinchImageViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if(mLocal){
                imageUrl="file://"+mPics.get(position);
            }else{
                imageUrl=mPics.get(position);
            }
        }

        @Override
        public void onPageSelected(int position) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 图片的长按点击事件.
     */
    private class OnImageLongClickListener implements View.OnLongClickListener{

        @Override
        public boolean onLongClick(View v) {
            if(!TextUtils.isEmpty(imageUrl)){
                showPopupWindown();
                return true;
            }else{
                showErrorToast(null,null,"图片下载地址错误");
            }
            return false;
        }
    }

    /**
     * 显示弹窗
     */
    private void showPopupWindown() {

        //打开整震动模式
        Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        long [] pattern = {100,100}; // 停止 开启
        vibrator.vibrate(pattern,-1); //重复两次上面的pattern 如果只想震动一次，index设为-1
        View conentView= View.inflate(this,R.layout.popupwindown_copy_image_layout,null);
        conentView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        if(mPopupWindow==null){
            mPopupWindow = new PopupWindow(conentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
            mPopupWindow.setClippingEnabled(false);
            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            mPopupWindow.setAnimationStyle(R.style.LoadingProgressDialogStyle);
            mPopupWindow.setFocusable(true);
        }

        conentView.findViewById(R.id.tv_save_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
                if (mLocal) {
                    showErrorToast(null,null,"本地相册已存在此图片");
                } else {
                    // TODO: 2017/8/15 保存相片
                }
            }
        });

        conentView.findViewById(R.id.tv_canale).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.showAtLocation(conentView, Gravity.CENTER, 0, 0);
    }
}
