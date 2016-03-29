package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseObject;

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

    public static Task<Void> getFriendsInBackground() {

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
                return pinFriends(list);
            }
        }).onSuccessTask(new Continuation<Void, Task<PrivateStudentData>>() {
            @Override
            public Task<PrivateStudentData> then(Task<Void> task) throws Exception {
                return PrivateStudentData.getPrivateStudentDataInBackground();
            }
        }).continueWith(new Continuation<PrivateStudentData, Void>() {
            @Override
            public Void then(Task<PrivateStudentData> task) throws Exception {
                List<PublicUserData> friends = task.getResult().getList("friends");
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

    private static Task<Void> pinFriends(final List<PublicUserData> friends) {

        ArrayList<Task<ParseObject>> tasks = new ArrayList<>();
        for(PublicUserData friend : friends) {
            tasks.add(friend.fetchIfNeededInBackground());
        }
        return Task.whenAll(tasks).continueWithTask(new Continuation<Void, Task<PrivateStudentData>>() {

//            @Override
//            public Task<Void> then(Task<Void> task) throws Exception {
//                if (task.isFaulted()) {
//                    Log.e("Error in fetching", task.getError().toString());
//                }
////                return ParseObjectUtils.pinAllInBackground("fbFriends", friends); //TODO: What?
//                return ParseObject.pinAllInBackground(friends);
//            }
//        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<PrivateStudentData> then(Task<Void> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e("Error in pinning", task.getError().toString());
                }
                return PrivateStudentData.getPrivateStudentDataInBackground();
            }
        }).continueWith(new Continuation<PrivateStudentData, Task<Void>>() {
            @Override
            public Task<Void> then(Task<PrivateStudentData> task) throws Exception {
                PrivateStudentData privateStudentData = task.getResult();
                privateStudentData.put(PrivateStudentData.Columns.friends, friends);
                return privateStudentData.saveInBackground();
            }
        }).continueWith(new Continuation<Task<Void>, Void>() {
            @Override
            public Void then (Task<Task<Void>> task) {
                if (task.isFaulted()) {
                    String message = task.getError().getMessage();
                    Log.e("Error in saving private", task.getError().getMessage());
                } else {
                    Log.e("saving private", "Ok");
                }
                return null;
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
