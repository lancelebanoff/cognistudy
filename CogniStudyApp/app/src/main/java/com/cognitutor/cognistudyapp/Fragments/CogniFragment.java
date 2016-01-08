package com.cognitutor.cognistudyapp.Fragments;

import android.support.v4.app.Fragment;

import com.cognitutor.cognistudyapp.Activities.CogniActivity;
import com.parse.ParseException;

/**
 * Created by Kevin on 1/7/2016.
 */
public class CogniFragment extends Fragment {

    public void handleParseError(String errorMsg, ParseException e) {
        ((CogniActivity) getActivity()).handleParseError(errorMsg, e);
    }
}
