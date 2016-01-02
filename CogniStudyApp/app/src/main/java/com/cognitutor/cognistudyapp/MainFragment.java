package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.parse.ParseUser;

/**
 * Created by Lance on 12/27/2015.
 */

public class MainFragment extends Fragment implements View.OnClickListener {

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button b = (Button) rootView.findViewById(R.id.btnStartChallenge);
        b.setOnClickListener(this);

        b = (Button) rootView.findViewById(R.id.btnLogout);
        b.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnStartChallenge:
                navigateToNewChallengeActivity();
                break;
            case R.id.btnLogout:
                ((AuthenticationActivity) getActivity()).logoutUser();
                navigateToRegistrationActivity();
                break;
        }
    }

    public void navigateToNewChallengeActivity() {
        Intent intent = new Intent(getActivity(), NewChallengeActivity.class);
        startActivity(intent);
    }

    public void navigateToRegistrationActivity() {
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
