package com.cognitutor.cognistudyapp.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.Custom.CogniBroadcastReceiver;
import com.cognitutor.cognistudyapp.Custom.CogniReceiverHandler;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;

import org.json.JSONObject;

/**
 * Created by Kevin on 1/29/2016.
 */
public abstract class CogniPushListenerFragment extends CogniFragment implements CogniReceiverHandler {

    @Override
    public void onResume() {
        CogniBroadcastReceiver.registerReceiverHandler(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        CogniBroadcastReceiver.unregisterReceiverHandler(this);
        super.onPause();
    }
}
