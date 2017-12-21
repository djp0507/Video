package com.video.newqu.view.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.video.newqu.util.Logger;

/**
 * TinyHung@Outlook.com
 * 2017/10/22
 */

public class ViewDragLayout extends FrameLayout{


    private static final String TAG = ViewDragLayout.class.getSimpleName();
    private ViewDragHelper mViewDragHelper;
    private ViewGroup mBgViewGroup;
    private ViewGroup mTopControllerViewGroup;

    public ViewDragLayout(@NonNull Context context) {
        this(context,null);
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ViewDragLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViewDragHelper = ViewDragHelper.create(this,callBack);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;//这里必须消费事件才有效
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    private final ViewDragHelper.Callback callBack  =new ViewDragHelper.Callback() {

        /**
         * 尝试拖拽一个View
         * @param child 被拖拽的View
         * @param pointerId 多点触控的数量，第一个手指按下是0，第二个累加
         * @return 表明是否能被拖拽
         */
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //如果是表面层，可以拖动，如果是底层不允许拖动
            Logger.d(TAG,"child="+child);
            return child==mTopControllerViewGroup;
        }


        /**
         *  重写这个方法表明可以水平滑动
         * @param child
         * @param left
         * @param dx
         * @return
         */
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }


        /**
         * 重写这个方法表明可以垂直滑动
         * @param child
         * @param top
         * @param dy
         * @return
         */
//        @Override
//        public int clampViewPositionVertical(View child, int top, int dy) {
//            return super.clampViewPositionVertical(child, top, dy);
//        }
    };

    /**
     * 所有布局加载完成调用
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if(getChildCount()!=2){
            throw new IllegalStateException("你必须制定两个子View");
        }
        if(!(getChildAt(0) instanceof ViewGroup)||!(getChildAt(1) instanceof ViewGroup)){
            throw new IllegalStateException("两个子View必须是ViewGroup类型");
        }
        mBgViewGroup = (ViewGroup) getChildAt(0);
        mTopControllerViewGroup = (ViewGroup) getChildAt(1);
    }
}
