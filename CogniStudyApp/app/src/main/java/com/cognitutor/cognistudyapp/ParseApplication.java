package com.cognitutor.cognistudyapp;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Lance on 12/30/2015.
 */
public class ParseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Data Store.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, Constants.Parse.APPLICATION_ID, Constants.Parse.CLIENT_KEY);
    }
}
