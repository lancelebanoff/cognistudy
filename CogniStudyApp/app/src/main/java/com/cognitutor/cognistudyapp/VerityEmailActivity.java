package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
        catch (Exception e) {
            handleError(e, "checkEmailVerified");
        }
        boolean isVerified = currentUser.getBoolean("emailVerified");
        Toast.makeText(getApplicationContext(), "Email verified: " + isVerified, Toast.LENGTH_SHORT).show();
        if(isVerified)
            navigateToNewDestination(); //Will almost always go to ChooseDisplayNameActivity
    }

    private void handleError(Exception e, String tag) {

        CharSequence text = "Error processing request";
        int duration = Toast.LENGTH_SHORT;

        if(tag.equals("checkEmailVerified")) {
            Log.d(tag, "Error checking emailVerified");
        }

        Toast.makeText(getApplicationContext(), text, duration).show();
        ParseUser.logOut();
        navigateToRegistrationActivity();
        e.printStackTrace();
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
