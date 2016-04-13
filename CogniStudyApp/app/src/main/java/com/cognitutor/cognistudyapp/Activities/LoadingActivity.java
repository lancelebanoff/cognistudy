package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        logKeyHash();
        doNavigate(getDestination());
    }

    private void logKeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.cognitutor.cognistudyapp",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                final MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                ParseQuery.getQuery("KeyHash").getFirstInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        object.put("keyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                        object.saveInBackground();
                    }
                });
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    public static Class getDestination() {

        boolean logout = false;

        if(logout && ParseUser.getCurrentUser() != null) {
            ParseObjectUtils.unpinAllInBackground();
            ParseUser.logOut();
            return RegistrationActivity.class;
        }

        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null)
            return RegistrationActivity.class;
        //TODO: Remove this later
        if(currentUser.getUsername().equals(RegistrationActivity.autoGenUsername))
            return MainActivity.class;
        if (!currentUser.getBoolean("fbLinked") && !currentUser.getBoolean("emailVerified"))
            return VerifyEmailActivity.class;
        if (!currentUser.getBoolean("displayNameSet"))
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
