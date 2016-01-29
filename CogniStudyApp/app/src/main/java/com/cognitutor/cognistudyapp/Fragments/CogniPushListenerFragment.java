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
