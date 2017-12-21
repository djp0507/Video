package com.video.newqu.view.layout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import com.video.newqu.util.Logger;

/**
 * /**
 * TinyHung@Outlook.com
 * 2017/8/19
 * 监听手势滑动
 */

public class DetectTouchGestureLayout extends FrameLayout{
	private int mTouchSlop, mDownX, mDownY, mTempX, totalMoveX, viewWidth;
	private boolean isSilding;
	private onSwipeGestureListener swipeListener;
	private int mAbs;
	private boolean isInterceptClickEvent;

	public DetectTouchGestureLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		totalMoveX = 0;
	}

	/**
	 * 滑动的距离占屏幕宽度的绝对值
	 * @param abs
	 */
	public void setMintouchabs(int abs) {
		this.mAbs=abs;
	}

	/**
	 *
	 * @param isInterceptClickEvent 实际否拦截单击事件
	 */
	public void setInterceptClickEvent(boolean isInterceptClickEvent) {
		this.isInterceptClickEvent=isInterceptClickEvent;
	}

	public interface onSwipeGestureListener{
		void onLeftSwipe();
		void onRightSwipe();
		void onClick();

	}
	
	public void setSwipeGestureListener(onSwipeGestureListener listener) {
		this.swipeListener = listener;
	}
	
	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		viewWidth = this.getWidth();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			try {
				mDownX = mTempX = (int) ev.getRawX();
				mDownY = (int) ev.getRawY();
			}catch (IllegalArgumentException e){
				Logger.d("DetectTouchGestureLayout","e="+e);
			}
			break;
		case MotionEvent.ACTION_MOVE:
			try {
				int moveX = (int) ev.getRawX();
				// 满足此条件屏蔽SildingFinishLayout里面子类的touch事件
				if (Math.abs(moveX - mDownX) > mTouchSlop && Math.abs((int) ev.getRawY() - mDownY) < mTouchSlop) {
					return true;
				}
			}catch (IllegalArgumentException e){
				Logger.d("DetectTouchGestureLayout","e="+e);
			}
			break;
		}
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		//点击事件
		case MotionEvent.ACTION_DOWN:

			if(null!=swipeListener) swipeListener.onClick();
				break;
		case MotionEvent.ACTION_MOVE:

			try {
				int moveX = (int) event.getRawX();
				int deltaX = mTempX - moveX;
				Logger.d("debug", "deltaX:" + deltaX + "mTouchSlop:" + mTouchSlop);
				mTempX = moveX;
				if (Math.abs(moveX - mDownX) > mTouchSlop
						&& Math.abs((int) event.getRawY() - mDownY) < mTouchSlop) {
					isSilding = true;
				}

				if (Math.abs(moveX - mDownX) >= 0 && isSilding) {
//				mContentView.scrollBy(deltaX, 0);
					totalMoveX += deltaX;
				}
			}catch (IllegalArgumentException e){
				Logger.d("DetectTouchGestureLayout","e="+e);
			}

			break;
		case MotionEvent.ACTION_UP:
			isSilding = false;
			try {
				Log.i("debug", "TotoalMoveX:" + totalMoveX + "viewVidth:" + viewWidth);//Math.abs(totalMoveX) >= viewWidth / 3  是否滑动了屏幕的1/6
				if(Math.abs(totalMoveX) >= viewWidth /mAbs){
					Logger.d("debug","Math.abs(totalMoveX)="+Math.abs(totalMoveX));
					if(totalMoveX>0){
						if(null!=swipeListener) swipeListener.onLeftSwipe();
					}else{
						if(null!=swipeListener) swipeListener.onRightSwipe();
					}
				}

			}catch (IllegalArgumentException e){
				Logger.d("debug","e="+e);
			}
			totalMoveX = 0;
			break;
		}

		return isInterceptClickEvent;
	}
	
}
