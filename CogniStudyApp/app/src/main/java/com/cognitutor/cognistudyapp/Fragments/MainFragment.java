package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.QuestionActivity;
import com.cognitutor.cognistudyapp.Activities.RegistrationActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.cognitutor.cognistudyapp.Custom.ErrorHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

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

        b = (Button) rootView.findViewById(R.id.btnQuestion);
        b.setOnClickListener(this);

        if(ParseUser.getCurrentUser().getBoolean("fbLinked") ) {
            ImageView imageView = (ImageView) rootView.findViewById(R.id.imgProfile);
            ParseFile parseFile;
            byte[] data;
            try {
                Log.d("MainFragment fbLoad", "Before getPublicUserData");
                parseFile = UserUtils.getPublicUserData().getParseFile("profilePic");
                Log.d("MainFragment fbLoad", "After getPublicUserData");
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
        switch(view.getId()) {
            case R.id.btnQuestion:
                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                intent.putExtra(Constants.IntentExtra.QUESTION_ID, "fF4lsHt2iW"); //TODO: Replace with desired questionId
                startActivity(intent);
                break;
            case R.id.btnStartChallenge:
                navigateToNewChallengeActivity();
                break;
            case R.id.btnLogout:
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e); return; }
                navigateToRegistrationActivity();
                break;
            case R.id.btnDeleteUser:
                String userId = ParseUser.getCurrentUser().getObjectId();
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e); return; }
                final HashMap<String, Object> params = new HashMap<>();
                params.put("userId", userId);
                ParseCloud.callFunctionInBackground("deleteStudent", params);
                navigateToRegistrationActivity();
        }
    }

    private void navigateToNewChallengeActivity() {
        Intent intent = new Intent(getActivity(), NewChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.OpponentId.OPPONENT_ID, Constants.IntentExtra.OpponentId.UNKNOWN);
        startActivity(intent);
    }


}
