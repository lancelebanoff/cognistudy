package com.cognitutor.cognistudyapp;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Kevin on 1/1/2016.
 */
class AuthenticationActivity extends AppCompatActivity {

    private Class getDestination() {

        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null)
            return RegistrationActivity.class;
        if(!currentUser.getBoolean("emailVerified"))
            return VerityEmailActivity.class;
        if( ((ParseObject) currentUser.get("publicUserData")).getString("displayName").isEmpty() )
            return ChooseDisplayNameActivity.class;
        return MainActivity.class;
    }

    protected void navigateToNewDestination() {

        Class dest = getDestination();
        if(dest == getClass())
            return;

        doNavigate(dest);
    }

    private void doNavigate(Class dest) {
        finish();
        Intent intent = new Intent(this, dest);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //TODO: Remove this
    public void navigateToMainActivity(View view) {
        doNavigate(MainActivity.class);
    }

    public void navigateToLoginActivity(View view) {
        doNavigate(LoginActivity.class);
    }

    public void navigateToRegistrationActivity() {
        doNavigate(RegistrationActivity.class);
    }
}
