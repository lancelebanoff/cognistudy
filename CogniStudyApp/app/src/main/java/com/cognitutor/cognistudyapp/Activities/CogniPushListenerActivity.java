package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.cognitutor.cognistudyapp.Activities.CogniActivity;
import com.cognitutor.cognistudyapp.Custom.CogniBroadcastReceiver;
import com.cognitutor.cognistudyapp.Custom.CogniReceiverHandler;

/**
 * Created by Kevin on 1/29/2016.
 */
public abstract class CogniPushListenerActivity extends CogniActivity implements CogniReceiverHandler {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        CogniBroadcastReceiver.registerReceiverHandler(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        CogniBroadcastReceiver.unregisterReceiverHandler(this);
        super.onDestroy();
    }
}

