package com.cognitutor.cognistudyapp.Activities;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

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
        if(finish)
            finish();
        Intent intent = new Intent(this, dest);
        startActivity(intent);
    }

    public void navigateToMainActivity() {
        doNavigate(MainActivity.class, true);
    }

    public void navigateToLoginActivity(View view) {
        doNavigate(LoginActivity.class, false);
    }

    public void navigateToRegistrationActivity() {
        doNavigate(RegistrationActivity.class, true);
    }

    private void handleError(Exception e, String tag) {

        CharSequence text = "Error processing request";
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(getApplicationContext(), text, duration).show();
        e.printStackTrace();
    }

    protected void setUpStudentObjects(final ParseUser user, final PublicUserData publicUserData, boolean fbLinked, final SaveCallback callback) {

        user.put("fbLinked", fbLinked);
        final ParseACL privateACL = new ParseACL(user);
        privateACL.setPublicReadAccess(false);
        user.setACL(privateACL);
        //TODO: Give admins access as well (Tutor access will be added when a student is linked to a tutor)

        final ParseACL publicDataACL = new ParseACL(user);
        publicDataACL.setPublicReadAccess(true);
        publicUserData.setACL(publicDataACL);

        publicUserData.put("userType", Constants.UserType.STUDENT);
        publicUserData.put("baseUserId", user.getObjectId());
                user.put("publicUserData", publicUserData);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        final PrivateStudentData privateStudentData = setUpPrivateStudentData(privateACL);
                        privateStudentData.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                final ParseObject student = new Student(user, publicUserData, privateStudentData);
                                publicUserData.put("student", student);
                                publicUserData.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        student.saveInBackground(callback);
                                    }
                                });
                            }
                        });
                    }
                });
    }

    private PrivateStudentData setUpPrivateStudentData(ParseACL acl) {
        PrivateStudentData privateStudentData = new PrivateStudentData();
        privateStudentData.setACL(acl);
        privateStudentData.put("numCoins", 0);
        privateStudentData.put("friends", new ArrayList<ParseObject>());
        privateStudentData.put("tutors", new ArrayList<ParseObject>());
        privateStudentData.put("blocked", new ArrayList<ParseObject>());
        privateStudentData.put("recentChallenges", new ArrayList<ParseObject>());
        privateStudentData.put("requestsFromTutors", new ArrayList<ParseObject>());
        privateStudentData.put("totalResponses", 0);
        privateStudentData.put("correctResponses", 0);
        privateStudentData.put("suggestedQuestions", new ArrayList<ParseObject>());
        return privateStudentData;
    }
}
