package com.cognitutor.cognistudyapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.parse.ParseUser;

public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        doNavigate(getDestination());
    }

    public static Class getDestination() {

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null)
            return RegistrationActivity.class;
        if(!currentUser.getBoolean("fbLinked") && !currentUser.getBoolean("emailVerified"))
            return VerityEmailActivity.class;
        String displayName = UserUtils.getPublicUserData().getString("displayName");
        if(displayName == null || displayName.isEmpty())
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
