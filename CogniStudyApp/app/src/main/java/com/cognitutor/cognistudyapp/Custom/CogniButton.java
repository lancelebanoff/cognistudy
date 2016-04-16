package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.Button;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Lance on 2/21/2016.
 */
public class CogniButton extends Button {

    public CogniButton(Context context) {
        super(context);
        setColor(context);
    }

    public CogniButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setColor(context);
    }

    public CogniButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setColor(context);
    }

    private void setColor(Context context) {
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.LOLLIPOP) {
            setColor(context, R.color.colorPrimaryLight);
        } else {
            setColor(context, R.color.colorPrimary);
        }
    }

    public void setColor(Context context, int color) {
        getBackground().setColorFilter(ContextCompat.getColor(context, color), PorterDuff.Mode.MULTIPLY);
        setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    public void setDrawableLeft(int resId) {
        setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
    }
}
