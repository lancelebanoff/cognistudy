package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.cognitutor.cognistudyapp.Activities.MainActivity;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;

/**
 * Created by Kevin on 2/12/2016.
 */
public class CogniViewPager extends ViewPager {

    private Activity activity;

    public CogniViewPager(Context context) {
        super(context);
    }

    public CogniViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);

        View v = activity.getCurrentFocus();
        if(v != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void setActivityRef(Activity activity) {
        this.activity = activity;
    }
}
