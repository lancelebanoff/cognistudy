package com.cognitutor.cognistudyapp.Custom;

import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cognitutor.cognistudyapp.Activities.ConversationActivity;
import com.cognitutor.cognistudyapp.Activities.MainActivity;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.R;
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
            final String activityConstant = data.getString(Constants.NotificationData.ACTIVITY);

            for(CogniReceiverHandler handler : handlers) {
                if(handler == null) {
                    Log.d("onReceive", "a handler is null");
                    unregisterReceiverHandler(handler);
                    continue;
                }
                JSONObject conditions = handler.getConditions();
                if(satisfiesCondition(data, conditions)) {
                    handler.onReceiveHandler();
                }
            }

            if(Foreground.get().isBackground()) {
                generateNotification(context, title, alert, getActivityClass(activityConstant));
            }
        } catch (JSONException e) {
            Log.e("onReceive", e.getMessage());
        }
        super.onReceive(context, intent);
    }

    private Class getActivityClass(String activityConstant) {
        switch(activityConstant) {
            case Constants.NotificationData.Activity.CONVERSATION_ACTIVITY:
                return ConversationActivity.class;
            default:
                return MainActivity.class;
        }
    }

    private void generateNotification(Context context, String title, String text, Class activityClass) {

        int id = 0;

        Intent resultIntent = new Intent(context, activityClass);
        //TODO: Add task stack (in xml?)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.icon_cognitutor)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(resultPendingIntent);

        Notification notification = builder.build();
        notification.defaults |= Notification.DEFAULT_LIGHTS; //TODO: Default is no lights???
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);
    }

    public static void registerReceiverHandler(CogniReceiverHandler handler) {
        handlers.add(handler);
    }

    public static void unregisterReceiverHandler(CogniReceiverHandler handler) {
        handlers.remove(handler);
    }

    private boolean satisfiesCondition(JSONObject data, JSONObject handlerConditions) {

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