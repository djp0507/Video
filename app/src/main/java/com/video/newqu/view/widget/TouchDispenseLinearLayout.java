package com.video.newqu.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * TinyHung@Outlook.com
 * 2017/10/22
 * 请求GroupLayout在特定情况下不要拦截触摸事件
 */

public class TouchDispenseLinearLayout extends LinearLayout {


    private boolean isRequestFocusInDescendants=true;//请求父ViewGroup是否拦截触摸焦点


    public void setRequestFocusInDescendants(boolean requestFocusInDescendants) {
        isRequestFocusInDescendants = requestFocusInDescendants;
    }


    public TouchDispenseLinearLayout(Context context) {
        this(context,null);
    }

    public TouchDispenseLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TouchDispenseLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
        if(isRequestFocusInDescendants){
            return super.onRequestFocusInDescendants(direction, previouslyFocusedRect);
        }else{
            return false;
        }
    }
}
