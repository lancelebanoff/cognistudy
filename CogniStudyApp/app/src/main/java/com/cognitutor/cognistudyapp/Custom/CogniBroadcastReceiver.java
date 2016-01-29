package com.cognitutor.cognistudyapp.Custom;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.parse.ParseBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Kevin on 1/28/2016.
 */
public class CogniBroadcastReceiver extends ParseBroadcastReceiver {

    private static ArrayList<CogniReceiverHandler> handlers = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive", "got Notification");
        try {
            JSONObject data = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            final String title = data.getString("title");
            final String alert = data.getString("alert");

            for(CogniReceiverHandler handler : handlers) {
                if(handler == null) {
                    Log.d("onReceive", "a handler is null");
                    unregisterReceiverHandler(handler);
                    continue;
                }
                JSONObject conditions = handler.getConditions();
                if(satisfiesCondition(data, conditions))
                    handler.onReceiveHandler();
            }
        } catch (JSONException e) {
            Log.e("onReceive", e.getMessage());
        }
        super.onReceive(context, intent);
    }

    public static void registerReceiverHandler(CogniReceiverHandler handler) {
        handlers.add(handler);
    }

    public static void unregisterReceiverHandler(CogniReceiverHandler handler) {
        handlers.remove(handler);
    }

    private static boolean satisfiesCondition(JSONObject data, JSONObject handlerConditions) {

        try {
            Iterator<String> keys = handlerConditions.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!data.has(key))
                    return false;
                if (!data.getString(key).equals(handlerConditions.getString(key)))
                    return false;
            }
        } catch(JSONException e) { e.printStackTrace(); return false; }
        return true;
    }
}