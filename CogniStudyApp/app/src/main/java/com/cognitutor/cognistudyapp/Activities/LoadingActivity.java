package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cognitutor.cognistudyapp.R;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        doNavigate(getDestination());
    }

    public static Class getDestination() {

        try {
            Log.d("LoadingActivity", "Before getCurrentUser");
            ParseUser currentUser = ParseUser.getCurrentUser();
            Log.d("LoadingActivity", "Past getCurrentUser");
            if (currentUser == null)
                return RegistrationActivity.class;
            if (!currentUser.getBoolean("fbLinked") && !currentUser.getBoolean("emailVerified"))
                return VerityEmailActivity.class;
            Log.d("LoadingActivity", "Before getPublicUserData");
            String displayName = UserUtils.getPublicUserData().getString("displayName");
            Log.d("LoadingActivity", "After getPublicUserData");
            if (displayName == null || displayName.isEmpty())
                return ChooseDisplayNameActivity.class;
            return MainActivity.class;
        }
        catch (ParseException e) {
            //TODO: Handle this differently?
            Log.d("LoadingActivity", "Error");
            Log.d("LoadingActivity", "Error code: " + e.getCode());
            e.printStackTrace();
            ParseUser.logOut();
            return RegistrationActivity.class;
        }
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
