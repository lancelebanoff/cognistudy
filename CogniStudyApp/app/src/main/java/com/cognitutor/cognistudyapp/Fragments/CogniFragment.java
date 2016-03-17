package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.app.Fragment;

import com.cognitutor.cognistudyapp.Activities.CogniActivity;
import com.cognitutor.cognistudyapp.Activities.RegistrationActivity;
import com.parse.ParseException;

/**
 * Created by Kevin on 1/7/2016.
 */
public class CogniFragment extends Fragment {

    public void handleParseError(ParseException e) {
        ((CogniActivity) getActivity()).handleParseError(e);
    }

    public void logout() throws ParseException {
        ((CogniActivity) getActivity()).logout();
    }

    public void navigateToRegistrationActivity() {
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
