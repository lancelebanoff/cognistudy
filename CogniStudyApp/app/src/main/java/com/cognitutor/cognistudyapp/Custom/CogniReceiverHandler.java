package com.cognitutor.cognistudyapp.Custom;

import org.json.JSONObject;

/**
 * Created by Kevin on 1/29/2016.
 */
public interface CogniReceiverHandler {

    public void onReceiveHandler();
    public JSONObject getConditions();
}
