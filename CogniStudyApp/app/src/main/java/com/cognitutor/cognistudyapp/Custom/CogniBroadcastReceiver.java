package com.cognitutor.cognistudyapp.Custom;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cognitutor.cognistudyapp.Activities.ChatActivity;
import com.cognitutor.cognistudyapp.Activities.MainActivity;
import com.cognitutor.cognistudyapp.Activities.SuggestedQuestionsListActivity;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Kevin on 1/28/2016.
 */
public class CogniBroadcastReceiver extends ParseBroadcastReceiver {

    private static ArrayList<CogniReceiverHandler> handlers = new ArrayList<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("onReceive", "got Notification");
        JSONObject data = getData(intent);
        final Map<String, String> intentExtras = getIntentExtras(data); //intentExtras will not always be given
        //Title, alert, and activityConstant are required
        try {
            final String title = data.getString(Constants.NotificationData.title);
            final String alert = data.getString(Constants.NotificationData.alert);
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
                generateNotification(context, title, alert, getActivityClass(activityConstant), intentExtras);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onReceive(context, intent);
    }

    private JSONObject getData(Intent intent) {
        try {
            return new JSONObject(intent.getExtras().getString("com.parse.Data"));
        } catch (JSONException e) { e.printStackTrace(); return null; }
    }

    private Map<String, String> getIntentExtras(JSONObject data) {

        Map<String, String> map = new HashMap<>();
        try {
            JSONObject intentExtras = data.getJSONObject(Constants.NotificationData.intentExtras);
            Iterator<String> keys = intentExtras.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                map.put(key, intentExtras.getString(key));
            }
        } catch (JSONException e) { }
        return map;
    }

    private Class getActivityClass(String activityConstant) {
        switch(activityConstant) {
            case Constants.NotificationData.Activity.CHAT_ACTIVITY:
                return ChatActivity.class;
            case Constants.NotificationData.Activity.SUGGESTED_QUESTIONS_LIST_ACTIVITY:
                return SuggestedQuestionsListActivity.class;
            default:
                return MainActivity.class;
        }
    }

    private void generateNotification(Context context, String title, String text, Class activityClass,
                                      Map<String, String> intentExtras) {

        int id = 0;

        Intent resultIntent = new Intent(context, activityClass);
        for(String key : intentExtras.keySet()) {
            resultIntent.putExtra(key, intentExtras.get(key));
        }
        //TODO: Add task stack (in xml?)
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(activityClass);
        stackBuilder.addNextIntent(resultIntent);

        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
//                .setSmallIcon(R.drawable.icon_cognitutor)
                .setSmallIcon(R.drawable.ic_stat_icon_pencil_in_gear)
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