package com.cognitutor.cognistudyapp;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Kevin on 12/30/2015.
 */
public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Constants.Parse.APPLICATION_ID, Constants.Parse.CLIENT_KEY);
    }
}
