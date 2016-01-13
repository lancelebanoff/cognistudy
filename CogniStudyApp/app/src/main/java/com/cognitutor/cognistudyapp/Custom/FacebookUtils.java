package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/12/2016.
 */
public class FacebookUtils {

    public static Task<Void> getFriendsInBackground(boolean fbLinked, final PrivateStudentData privateStudentData) {

        if(!fbLinked) {
            return privateStudentData.saveInBackground();
        }

        return Task.callInBackground(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                ArrayList<String> fbFriendsList = new ArrayList<String>();
                getFBFriends("/me/friends", fbFriendsList);
                return fbFriendsList;
            }
        }).onSuccessTask(new Continuation<ArrayList<String>, Task<List<PublicUserData>>>() {
            @Override
            public Task<List<PublicUserData>> then(Task<ArrayList<String>> task) throws Exception {
                return getPublicUserDataFromFriendIds(task.getResult());
            }
        }).onSuccessTask(new Continuation<List<PublicUserData>, Task<Void>>() {
            @Override
            public Task<Void> then(Task<List<PublicUserData>> task) throws Exception {
                List<PublicUserData> list = task.getResult();
                return pinFriends(list, privateStudentData);
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                List<PublicUserData> friends = privateStudentData.getList("friends");
                for (PublicUserData pud : friends) {
                    Log.d("FINISHED", "displayName is " + pud.getDisplayName());
                }
                /*
                PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .findInBackground()
                        .onSuccess(new Continuation<List<PublicUserData>, Void>() {
                            @Override
                            public Void then(Task<List<PublicUserData>> task) throws Exception {
                                for(PublicUserData pud : task.getResult()) {
                                    Log.d("FINISHED", "displayName is " + pud.getDisplayName());
                                }
                                return null;
                            }
                        });
                */
                return null;
            }
        });
    }

    private static Task<Void> pinFriends(final List<PublicUserData> friends, final PrivateStudentData privateStudentData) {

        ArrayList<Task<ParseObject>> tasks = new ArrayList<>();
        for(PublicUserData friend : friends) {
            tasks.add(friend.fetchIfNeededInBackground());
        }
        return Task.whenAll(tasks).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                return PublicUserData.pinAllInBackground("fbFriends", friends);
            }
        }).onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                privateStudentData.put(PrivateStudentData.Columns.friends, friends);
                return privateStudentData.saveInBackground();
            }
        });
    }

    private static Task<List<PublicUserData>> getPublicUserDataFromFriendIds(ArrayList<String> fbFriendsList) {

        return PublicUserData.getQuery()
                .whereContainedIn(PublicUserData.Columns.facebookId, fbFriendsList)
                .findInBackground();
    }


    private static void getFBFriends(String currentPath, final ArrayList<String> friendFacebookIds) {

        final GraphRequest.Callback graphCallback = new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                try {
                    JSONArray data = response.getJSONObject().getJSONArray("data");
                    for(int i=0; i<data.length(); i++) {
                        JSONObject friend = (JSONObject) data.get(i);
                        friendFacebookIds.add(friend.getString("id"));
                    }
                    GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                    if(nextRequest != null) {
                        nextRequest.setCallback(this);
                        nextRequest.executeAndWait();
                    }
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                graphCallback
        ).executeAndWait();
    }

    private static String addFriends(GraphResponse response, final ArrayList<String> friendFacebookIds) throws JSONException {
        JSONArray data = response.getJSONObject().getJSONArray("data");
        for(int i=0; i<data.length(); i++) {
            JSONObject friend = (JSONObject) data.get(i);
            friendFacebookIds.add(friend.getString("id"));
        }
        JSONObject paging = response.getJSONObject().getJSONObject("paging");
        if(paging.has("next")) {
            return paging.getString("next");
        }
        else return null;
    }
}
