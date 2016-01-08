package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.ErrorHandler;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseUser;

public class VerityEmailActivity extends AuthenticationActivity {

    ParseUser currentUser = ParseUser.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        if(currentUser == null) {
            finish();
            Intent intent = new Intent(VerityEmailActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        checkEmailVerified(null);
    }

    public void checkEmailVerified(View view) {

        if(currentUser.getBoolean("emailVerified"))
            Toast.makeText(getApplicationContext(), "Email verified: true", Toast.LENGTH_SHORT).show();

        try {
            currentUser = ParseUser.getCurrentUser().fetch();
        }
        catch (ParseException e) {
            handleParseError(e);
        }
        boolean isVerified = currentUser.getBoolean("emailVerified");
        Toast.makeText(getApplicationContext(), "Email verified: " + isVerified, Toast.LENGTH_SHORT).show();
        if(isVerified)
            navigateToNewDestination(); //Will almost always go to ChooseDisplayNameActivity
    }

    public void resendConfirmationEmail(View view) {
        currentUser.setEmail("");
        currentUser.saveInBackground();
        currentUser.setEmail(currentUser.getString("username"));
        currentUser.saveInBackground();
    }

    public void logout(View view) {
        ParseUser.logOut();
        navigateToRegistrationActivity();
    }
}
