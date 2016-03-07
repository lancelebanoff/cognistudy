package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
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

        setButtonColors();

        checkEmailVerified(null);
    }

    private void setButtonColors() {
        CogniButton btnContinue = (CogniButton) findViewById(R.id.btnContinue);
        btnContinue.setColor(this, R.color.green);

        CogniButton btnLogout = (CogniButton) findViewById(R.id.btnLogout);
        btnLogout.setColor(this, R.color.red);
    }

    public void checkEmailVerified(View view) {

        try {
            currentUser = ParseUser.getCurrentUser().fetch();
        }
        catch (ParseException e) { handleParseError(e); return; }

        boolean isVerified = currentUser.getBoolean("emailVerified");
        if (!isVerified && view != null) {
            Toast.makeText(getApplicationContext(), "Email verified: " + isVerified, Toast.LENGTH_SHORT).show();
        }

        if(isVerified)
            navigateToNewDestination(); //Should almost always go to ChooseDisplayNameActivity
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
