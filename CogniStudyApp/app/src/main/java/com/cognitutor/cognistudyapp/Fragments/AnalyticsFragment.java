package com.cognitutor.cognistudyapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Lance on 12/27/2015.
 */
public class AnalyticsFragment extends CogniFragment {

    public AnalyticsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_analytics, container, false);
        return rootView;
    }
}
