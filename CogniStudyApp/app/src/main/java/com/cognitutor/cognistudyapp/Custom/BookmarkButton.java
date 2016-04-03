package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 3/26/2016.
 */
public class BookmarkButton extends CogniCheckBox {

    public BookmarkButton(Context context) {
        super(context);
        init();
    }

    public BookmarkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BookmarkButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //Sets the width of the button to be the width of the widest character (M) x (strLen - 1)
        // -1 found by trial and error
        int length = getResources().getString(R.string.bookmark_button_bookmarked).length() - 1;
        setMinEms(length);
    }

    @Override
    protected void switchChecked() {
        super.switchChecked();
        if(isChecked()) {
            setText(R.string.bookmark_button_bookmarked);
            setDrawableLeft(R.drawable.ic_action_icon_bookmarked);
        }
        else {
            setText(R.string.bookmark_button_bookmark);
            setDrawableLeft(R.drawable.ic_action_icon_bookmark);
        }
    }

    private void setDrawableLeft(int resId) {
        setCompoundDrawablesWithIntrinsicBounds(resId, 0, 0, 0);
    }
}
