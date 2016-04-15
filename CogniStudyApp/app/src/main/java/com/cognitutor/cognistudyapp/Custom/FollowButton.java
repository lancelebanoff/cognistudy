package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.AttributeSet;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 4/15/2016.
 */
public class FollowButton extends CogniIconCheckBox {

    public FollowButton(Context context) {
        super(context);
        init();
    }

    public FollowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setOnClickListener(null);
        setDrawableLeft(getDrawableUnchecked());
    }

    @Override
    int getCheckedString() {
        return R.string.unfollow;
    }

    @Override
    int getUncheckedString() {
        return R.string.follow;
    }

    @Override
    int getDrawableChecked() {
        return R.drawable.ic_action_icon_follow;
    }

    @Override
    int getDrawableUnchecked() {
        return R.drawable.ic_action_icon_follow;
    }
}
