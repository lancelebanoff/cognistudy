package com.cognitutor.cognistudyapp.Fragments;


import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.cognitutor.cognistudyapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment {

    public SettingsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }
}
