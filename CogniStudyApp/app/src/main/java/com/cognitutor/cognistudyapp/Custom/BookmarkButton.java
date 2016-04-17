package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 3/26/2016.
 */
public class BookmarkButton extends CogniIconCheckBox {

    @Override
    int getCheckedString() {
        return R.string.bookmark_button_bookmarked;
    }

    @Override
    int getUncheckedString() {
        return R.string.bookmark_button_bookmark;
    }

    @Override
    int getDrawableChecked() {
        return R.drawable.ic_action_icon_bookmarked;
    }

    @Override
    int getDrawableUnchecked() {
        return R.drawable.ic_action_icon_bookmark;
    }

    public BookmarkButton(Context context) {
        super(context);
    }

    public BookmarkButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BookmarkButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
