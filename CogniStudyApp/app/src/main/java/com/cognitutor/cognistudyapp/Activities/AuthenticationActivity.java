package com.cognitutor.cognistudyapp.Activities;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.FacebookUtils;
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

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/1/2016.
 */
class AuthenticationActivity extends CogniActivity {

    protected void navigateToNewDestination() {

        Class dest = LoadingActivity.getDestination();
        if(dest == getClass())
            return;

        if(dest == MainActivity.class) {
            try {
                UserUtils.pinTest();
            } catch (ParseException e) { handleParseError(e); ParseUser.logOut(); navigateToRegistrationActivity(); return; }

        }

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

    protected void setUpStudentObjects(final ParseUser user, final String facebookId, final String displayName,
                                       final ParseFile profilePic, final byte[] profilePicData, final SaveCallback callback) {

        final String TAG = "setUpStudentObjects";
        final boolean fbLinked = facebookId != null;
        final PrivateStudentData privateStudentData = new PrivateStudentData(user);
        final Student student = new Student(user, privateStudentData);
        final PublicUserData publicUserData = new PublicUserData(user, student, facebookId, displayName, profilePic, profilePicData);
        finalizeUser(user, publicUserData, fbLinked);

        publicUserData.pinInBackground("CurrentUser", new SaveCallback() {
            @Override
            public void done(ParseException e) {

                if(e != null) {
                    Log.e("pinInBackground", e.getMessage());
                }
                //noinspection ConstantConditions
                FacebookUtils.getFriendsInBackground(fbLinked).continueWith(new Continuation<Void, Void>() {
                    @Override
                    public Void then(Task<Void> task) throws Exception {

                        if(task.isFaulted()) {
                            Log.e("getFriendsInBackground", task.getError().getMessage());
                        }
                        ArrayList < ParseObject > objects = new ArrayList<ParseObject>();
                        objects.add(privateStudentData);
                        objects.add(student);
                        objects.add(publicUserData);
                        objects.add(user);
                        ParseObject.saveAllInBackground(objects, callback);
                        return null;
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
