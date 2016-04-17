package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Lance on 3/13/2016.
 */
public class CogniCheckBox extends CogniButton {

    private boolean checked = false;

    public CogniCheckBox(Context context) {
        super(context);
    }

    public CogniCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CogniCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean newCheckedValue) {
        if (checked != newCheckedValue) {
            switchChecked();
        }
    }

    @Override
    public void setOnClickListener(final OnClickListener givenListener) {
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchChecked();
                if(givenListener != null)
                    givenListener.onClick(v);
            }
        });
    }

    protected void switchChecked() {
        checked = !checked;
        int color = checked ? R.color.green : R.color.colorPrimaryLight;
        setColor(getContext(), color);
    }
}
