package com.cognitutor.cognistudyapp.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    Student mStudent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        try {
            mStudent = PublicUserData.getPublicUserData().getStudent();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case Constants.Settings.RANDOM_MATCHING:
                boolean enabled = sharedPreferences.getBoolean(key, false);
                mStudent.setRandomEnabled(enabled);
                break;
            case Constants.Settings.SKIP_BUNDLES:
                boolean skipBundlesEnabled = sharedPreferences.getBoolean(key, false);
                mStudent.setSkipBundles(skipBundlesEnabled);
                break;
            case Constants.Settings.ENABLE_NOTIFICATIONS:
                boolean notificationsEnabled = sharedPreferences.getBoolean(key, true);
                mStudent.setNotificationsEnabled(notificationsEnabled);
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);

        if (mStudent != null) {
            mStudent.saveInBackground();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout)findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        bar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
