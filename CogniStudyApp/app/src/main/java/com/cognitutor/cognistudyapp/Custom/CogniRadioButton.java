package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Lance on 3/13/2016.
 */
public class CogniRadioButton extends CogniButton {

    private CogniRadioGroup group = null;
    private boolean checked = false;

    public CogniRadioButton(Context context) {
        super(context);
    }

    public CogniRadioButton(Context context, int drawableLeftId) {
        super(context, drawableLeftId);
    }

    public CogniRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CogniRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setGroup(CogniRadioGroup group) {
        this.group = group;
    }

    @Override
    public void setOnClickListener(final OnClickListener givenListener) {
        if (group == null) {
            try {
                throw new Exception("CogniRadioButton must be added to a CogniRadioGroup before setting an OnClickListener.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        final CogniRadioButton thisButton = this;
        super.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                group.setChosenButton(thisButton);
                givenListener.onClick(v);
            }
        });
    }

    public void setChecked(boolean newCheckedValue) {
        if (checked != newCheckedValue) {
            switchChecked();
        }
    }

    private void switchChecked() {
        checked = !checked;
        int color = checked ? R.color.green : R.color.colorPrimaryLight;
        setColor(getContext(), color);
    }
}
