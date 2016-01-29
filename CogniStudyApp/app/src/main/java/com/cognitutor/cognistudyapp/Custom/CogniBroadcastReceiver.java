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

import java.util.List;

/**
 * Created by Kevin on 1/28/2016.
 */
public class CogniBroadcastReceiver extends ParseBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive", "got Notification");
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            final String title = json.getString("title");
            final String alert = json.getString("alert");
            final String activity = json.getString("activity");
            Log.i("activity", activity);

            if(MainFragment.txtChange != null) {
                MainFragment.txtChange.setText("Received!");
            }
        } catch (JSONException e) {
            Log.e("onReceive", e.getMessage());
        }
        super.onReceive(context, intent);
    }
}