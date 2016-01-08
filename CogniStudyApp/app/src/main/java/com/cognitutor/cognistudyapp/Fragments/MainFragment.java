package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.RegistrationActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.cognitutor.cognistudyapp.Custom.ErrorHandler;

import java.util.HashMap;

/**
 * Created by Lance on 12/27/2015.
 */

public class MainFragment extends CogniFragment implements View.OnClickListener {

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

        b = (Button) rootView.findViewById(R.id.btnDeleteUser);
        b.setOnClickListener(this);

        if(ParseUser.getCurrentUser().getBoolean("fbLinked") ) {
            ImageView imageView = (ImageView) rootView.findViewById(R.id.imgProfile);
            ParseFile parseFile;
            byte[] data;
            try {
                parseFile = UserUtils.getPublicUserData().getParseFile("profilePic");
                data = parseFile.getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageView.setImageBitmap(bitmap);
            } catch (ParseException e) {
                handleParseError(e);
            }
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        ParseObject student;
        try {
            student = UserUtils.getStudent();
        } catch (ParseException e) { handleParseError(e); return; }
        switch(view.getId()) {
            case R.id.btnStartChallenge:
                navigateToNewChallengeActivity();
                break;
            case R.id.btnLogout:
                ParseUser.logOut();
                navigateToRegistrationActivity();
                break;
            case R.id.btnDeleteUser:
                ParseUser.logOut();
                final HashMap<String, Object> params = new HashMap<>();
                params.put("studentId", student.getObjectId());
                ParseCloud.callFunctionInBackground("deleteStudent", params);
                navigateToRegistrationActivity();
        }
    }

    private void navigateToNewChallengeActivity() {
        Intent intent = new Intent(getActivity(), NewChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.OpponentId.OPPONENT_ID, Constants.IntentExtra.OpponentId.UNKNOWN);
        startActivity(intent);
    }

    public void navigateToRegistrationActivity() {
        Intent intent = new Intent(getActivity(), RegistrationActivity.class);
        startActivity(intent);
        getActivity().finish();
    }
}
