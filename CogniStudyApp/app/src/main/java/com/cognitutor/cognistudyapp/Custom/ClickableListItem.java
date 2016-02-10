package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Kevin on 1/24/2016.
 */
public class ClickableListItem extends RelativeLayout {

    public ClickableListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
