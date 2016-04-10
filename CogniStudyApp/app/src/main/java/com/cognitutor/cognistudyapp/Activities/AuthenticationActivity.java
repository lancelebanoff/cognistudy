package com.cognitutor.cognistudyapp.Activities;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.FacebookUtils;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/1/2016.
 */
class AuthenticationActivity extends CogniActivity {

    protected void navigateToNewDestination() {

        final Class dest = LoadingActivity.getDestination();
        if(dest == getClass())
            return;

        if(dest == MainActivity.class) {
            try {
                UserUtils.pinCurrentUser().continueWithTask(new Continuation<Void, Task<Void>>() {
                    @Override
                    public Task<Void> then(Task<Void> task) throws Exception {
                        doNavigate(dest, true);
                        return null;
                    }
                });
            } catch (ParseException e) {
                handleParseError(e);
                ParseUser.logOut();
                navigateToRegistrationActivity();
            }
        } else {
            doNavigate(dest, true);
        }
    }

    private void doNavigate(Class dest, boolean finish) {

        Intent intent = new Intent(this, dest);
        if(finish)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void navigateToMainActivity() {
        try {
            UserUtils.pinCurrentUser().waitForCompletion();
        } catch (Exception e) { e.printStackTrace(); }
        doNavigate(MainActivity.class, true);
    }

    public void navigateToMainActivity(boolean pinCurrentUser) {
        if(pinCurrentUser) {
            try {
                UserUtils.pinCurrentUser().waitForCompletion();
            } catch (Exception e) { e.printStackTrace(); }
        }
        doNavigate(MainActivity.class, true);
    }

    public void navigateToLoginActivity(View view) {
        doNavigate(LoginActivity.class, false);
    }

    public void navigateToRegistrationActivity() {
        UserUtils.setUserLoggedIn(false);
        doNavigate(RegistrationActivity.class, true);
    }

    protected void setUpStudentObjects(final ParseUser user, final String facebookId, final String displayName,
                                       final ParseFile profilePic, final byte[] profilePicData, final SaveCallback callback) {

        final String TAG = "setUpStudentObjects";
        final boolean fbLinked = facebookId != null;
        final PrivateStudentData privateStudentData = new PrivateStudentData(user);
        final Student student = new Student(user, privateStudentData);
        final PublicUserData publicUserData = new PublicUserData(user, student, facebookId, displayName, profilePic, profilePicData);
        //TODO: Remove this
        finalizeUser(user, publicUserData, fbLinked);

        //TODO: This does not add a PinnedObject instance for current user since the user has not been saved to Parse yet
        privateStudentData.pinInBackground(Constants.PinNames.CurrentUser)
//        ParseObjectUtils.pinInBackground(Constants.PinNames.CurrentUser, privateStudentData) //Only privateStudentData is needed for FB
                        .continueWith(new Continuation<Void, Void>() {
                            @Override
                            public Void then(Task<Void> task) throws Exception {
                                if (task.isFaulted()) {
                                    Log.e("pinInBackground", task.getError().getMessage());
                                }
                                //noinspection ConstantConditions
                                if (fbLinked) {
                                    FacebookUtils.getFriendsInBackground().continueWith(new Continuation<Void, Void>() {
                                        @Override
                                        public Void then(Task<Void> task) throws Exception {

                                            if (task.isFaulted()) {
                                                Log.e("getFriendsInBackground", task.getError().getMessage());
                                            }
                                            saveObjects(privateStudentData, student, publicUserData, user, callback);
                                            return null;
                                        }
                                    });
                                } else {
                                    saveObjects(privateStudentData, student, publicUserData, user, callback);
                                }
                                return null;
                            }
                        });
    }

    private void saveObjects(PrivateStudentData privateStudentData, Student student, PublicUserData publicUserData,
                             ParseUser user, SaveCallback callback) {
        ArrayList < ParseObject > objects = new ArrayList<ParseObject>();
        objects.add(privateStudentData);
        objects.add(student);
        objects.add(publicUserData);
        objects.add(user);
        ParseObject.saveAllInBackground(objects, callback);
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
