package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Lance on 2/21/2016.
 */
public class CogniImageButton extends ImageButton {

    public CogniImageButton(Context context) {
        super(context);
        setColor(context);
    }

    public CogniImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColor(context);
    }

    public CogniImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setColor(context);
    }

    private void setColor(Context context) {
        getBackground().setColorFilter(ContextCompat.getColor(context, R.color.colorPrimaryLight), PorterDuff.Mode.MULTIPLY);
    }

    private void setWidth() {
        setMaxWidth(getHeight());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setWidth();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
