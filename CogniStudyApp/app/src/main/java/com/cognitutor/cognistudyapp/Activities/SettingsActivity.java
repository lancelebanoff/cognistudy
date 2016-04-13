package com.cognitutor.cognistudyapp.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.R;

import bolts.Continuation;
import bolts.Task;

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

        Student.getStudentInBackground().continueWith(new Continuation<Student, Void>() {
            @Override
            public Void then(Task<Student> task) throws Exception {
                mStudent = task.getResult();
                return null;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case "random_matching":
                final boolean enabled = sharedPreferences.getBoolean(key, false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(mStudent == null) {}
                        mStudent.setRandomEnabled(enabled);
                    }
                }).start();
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
}
