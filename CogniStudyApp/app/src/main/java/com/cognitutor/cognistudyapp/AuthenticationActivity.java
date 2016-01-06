package com.cognitutor.cognistudyapp;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Kevin on 1/1/2016.
 */
class AuthenticationActivity extends AppCompatActivity {

    protected void navigateToNewDestination() {

        Class dest = LoadingActivity.getDestination();
        if(dest == getClass())
            return;

        doNavigate(dest);
    }

    private void doNavigate(Class dest) {
        //finish();
        Intent intent = new Intent(this, dest);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logoutUser() {
        ParseUser.logOut();
        navigateToNewDestination();
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

    private void handleError(Exception e, String tag) {

        CharSequence text = "Error processing request";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(getApplicationContext(), text, duration).show();
        e.printStackTrace();
    }

    protected void setUpStudentObjects(final ParseUser user, final ParseObject publicUserData, boolean fbLinked, final SaveCallback callback) {

        user.put("fbLinked", fbLinked);
        ParseACL acl = new ParseACL(user);
        acl.setPublicReadAccess(false);
        user.setACL(acl);
        //TODO: Give admins access as well (Tutor access will be added when a student is linked to a tutor)

        ParseACL publicDataACL = new ParseACL(user);
        publicDataACL.setPublicReadAccess(true);
        publicUserData.setACL(publicDataACL);

        publicUserData.put("userType", Constants.UserType.STUDENT);
        publicUserData.put("baseUserId", user.getObjectId());
        publicUserData.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                user.put("publicUserData", publicUserData);
                user.saveInBackground(callback);
            }
        });

        //user.put("publicUserData", ParseObject.createWithoutData("PublicUserData", publicUserData.getObjectId()));

        //TODO: Set up Student object
    }
}
