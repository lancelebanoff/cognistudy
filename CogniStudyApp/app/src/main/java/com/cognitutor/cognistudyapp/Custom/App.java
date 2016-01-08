package com.cognitutor.cognistudyapp.Custom;

import android.app.Application;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Achievement;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;

/**
 * Created by Kevin on 12/30/2015.
 */
public class App extends Application {

    public static CallbackManager callbackManager;
    public static AccessTokenTracker accessTokenTracker;
    public static ProfileTracker profileTracker;

    @Override public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        registerSubclasses();
        Parse.initialize(this, Constants.Parse.APPLICATION_ID, Constants.Parse.CLIENT_KEY);
        ParseFacebookUtils.initialize(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                //TODO: I don't think we need to do this
            }
        };

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                //TODO: Do something here
            }
        };
    }

    private void registerSubclasses() {
        ParseObject.registerSubclass(Achievement.class);
        ParseObject.registerSubclass(PrivateStudentData.class);
        ParseObject.registerSubclass(PublicUserData.class);
        ParseObject.registerSubclass(Student.class);
    }
}
