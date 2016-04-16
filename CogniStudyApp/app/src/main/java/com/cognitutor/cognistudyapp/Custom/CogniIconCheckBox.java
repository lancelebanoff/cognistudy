package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Kevin on 4/15/2016.
 */
public abstract class CogniIconCheckBox extends CogniCheckBox {

    abstract int getCheckedString();
    abstract int getUncheckedString();
    abstract int getDrawableChecked();
    abstract int getDrawableUnchecked();

    public CogniIconCheckBox(Context context) {
        super(context);
        init();
    }

    public CogniIconCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CogniIconCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //Sets the width of the button to be the width of the widest character (M) x (strLen - 1)
        // -1 found by trial and error
        String checked = getResources().getString(getCheckedString());
        String unchecked = getResources().getString(getUncheckedString());
        int length = Math.max(checked.length(), unchecked.length()) - 1;
        setMinEms(length);
    }

    @Override
    protected void switchChecked() {
        super.switchChecked();
        if(isChecked()) {
            setText(getCheckedString());
            setDrawableLeft(getDrawableChecked());
        }
        else {
            setText(getUncheckedString());
            setDrawableLeft(getDrawableUnchecked());
        }
    }

    public void setDrawableLeft(int resId) {
        setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
    }
}
