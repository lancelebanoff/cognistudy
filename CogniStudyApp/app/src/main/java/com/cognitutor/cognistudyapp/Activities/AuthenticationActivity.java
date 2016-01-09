package com.cognitutor.cognistudyapp.Activities;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Kevin on 1/1/2016.
 */
class AuthenticationActivity extends CogniActivity {

    protected void navigateToNewDestination() {

        Class dest = LoadingActivity.getDestination();
        if(dest == getClass())
            return;

        doNavigate(dest, true);
    }

    private void doNavigate(Class dest, boolean finish) {

        Intent intent = new Intent(this, dest);
        if(finish)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void navigateToMainActivity() {
        try {
            UserUtils.pinTest();
        }
        catch (ParseException e) { handleParseError(e); return; }
        doNavigate(MainActivity.class, true);
    }

    public void navigateToLoginActivity(View view) {
        doNavigate(LoginActivity.class, false);
    }

    public void navigateToRegistrationActivity() {
        doNavigate(RegistrationActivity.class, true);
    }

    protected void setUpStudentObjects(final ParseUser user, final boolean fbLinked, final String displayName,
                                       final ParseFile profilePic, final byte[] profilePicData, final SaveCallback callback) {

        final PrivateStudentData privateStudentData = new PrivateStudentData(user);
        privateStudentData.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                final Student student = new Student(user, privateStudentData);
                student.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        final PublicUserData publicUserData = new PublicUserData(user, student, displayName, profilePic, profilePicData);
                        publicUserData.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                finalizeUser(user, publicUserData, fbLinked);
                                user.saveInBackground(callback);
                            }
                        });
                    }
                });
            }
        });
    }

    private void finalizeUser(ParseUser user, PublicUserData publicUserData, boolean fbLinked) {

        user.put("publicUserData", publicUserData);
        user.put("fbLinked", fbLinked);
        user.put("displayNameSet", true);
        ParseACL privateACL = new ParseACL(user);
        privateACL.setPublicReadAccess(false);
        user.setACL(privateACL);
        //TODO: Give admins access as well (Tutor access will be added when a student is linked to a tutor)
    }
}
