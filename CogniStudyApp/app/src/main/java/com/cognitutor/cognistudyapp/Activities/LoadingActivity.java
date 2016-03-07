package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.R;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import bolts.Continuation;
import bolts.Task;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        doNavigate(getDestination());
    }

    public static Class getDestination() {

        boolean logout = false;

        if(logout && ParseUser.getCurrentUser() != null) {
            ParseObjectUtils.unpinAllInBackground();
            ParseUser.logOut();
            return RegistrationActivity.class;
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null)
            return RegistrationActivity.class;
        //TODO: Remove this later
        if(currentUser.getUsername().equals(RegistrationActivity.autoGenUsername))
            return MainActivity.class;
        if (!currentUser.getBoolean("fbLinked") && !currentUser.getBoolean("emailVerified"))
            return VerityEmailActivity.class;
        if (!currentUser.getBoolean("displayNameSet"))
            return ChooseDisplayNameActivity.class;
        return MainActivity.class;
    }

    private void doNavigate(Class dest) {
        finish();
        Intent intent = new Intent(this, dest);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private static void handleError(Exception e, String tag) {

        if(tag.equals("getPublicUserData")) {
            Log.d(tag, "Error fetching publicUserData");
            Log.d(tag, e.getMessage());
        }
        e.printStackTrace();
    }
}
