package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("PrivateStudentData")
public class PrivateStudentData extends ParseObject{

    private final ArrayList<String> friendFacebookIds = new ArrayList<>();
    private final ArrayList<PublicUserData> friendsList = new ArrayList<>();

    public class Columns {
        public static final String numCoins = "numCoins";
        public static final String responses = "responses";
        public static final String friends = "friends";
        public static final String tutors = "tutors";
        public static final String blocked = "blocked";
        public static final String recentChallenges = "recentChallenges";
        public static final String requestsFromTutors = "requestsFromTutors";
        public static final String totalResponses = "totalResponses";
        public static final String correctResponses = "correctResponses";
        public static final String suggestedQuestions = "suggestedQuestions";
        public static final String baseUserId = "baseUserId";
    }
    public PrivateStudentData() {}
    public PrivateStudentData(ParseUser user) {
        ParseACL acl = new ParseACL(user);
        acl.setPublicReadAccess(false);
        setACL(acl);

        put(Columns.numCoins, 0);
        put(Columns.friends, new ArrayList<PublicUserData>());
        put(Columns.tutors, new ArrayList<PublicUserData>());
        put(Columns.blocked, new ArrayList<ParseObject>());
        put(Columns.recentChallenges, new ArrayList<ParseObject>());
        put(Columns.requestsFromTutors, new ArrayList<ParseObject>());
        put(Columns.totalResponses, 0);
        put(Columns.correctResponses, 0);
        put(Columns.suggestedQuestions, new ArrayList<ParseObject>());
        put(Columns.baseUserId, user.getObjectId());
    }

    public static ParseQuery<PrivateStudentData> getQuery() {
        return ParseQuery.getQuery(PrivateStudentData.class);
    }

    public static PrivateStudentData getPrivateStudentData() {
        return Student.getStudent().getPrivateStudentData();
        //return getPrivateStudentData(ParseUser.getCurrentUser().getObjectId());
    }

    private static PrivateStudentData getPrivateStudentData(String baseUserId) {

        try { return getLocalDataQuery(baseUserId).getFirst(); }
        catch (ParseException e) {
            Log.e("PrivateStudentData", "PrivateStudentData not in local datastore");
            e.printStackTrace();
            return null;
        }
    }

    public static Task<PrivateStudentData> getPrivateStudentDataInBackground() {
        //return getPrivateStudentDataInBackground(ParseUser.getCurrentUser().getObjectId());
        return Student.getStudentInBackground().continueWith(new Continuation<Student, PrivateStudentData>() {
            @Override
            public PrivateStudentData then(Task<Student> task) throws Exception {
                if(task.isFaulted()) {
                    Log.e("PrivateStudentData", "error getting student first");
                }
                Student student = task.getResult();
                return student.getPrivateStudentData();
            }
        });
    }

    private static Task<PrivateStudentData> getPrivateStudentDataInBackground(String baseUserId) {
        return getLocalDataQuery(baseUserId).getFirstInBackground();
    }

    private static ParseQuery<PrivateStudentData> getLocalDataQuery(String baseUserId) {

        return PrivateStudentData.getQuery()
                .setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK)
                .whereEqualTo(Columns.baseUserId, baseUserId);
    }

    public static ArrayList<String> getFriendPublicUserIds() {
        ArrayList<String> friendPublicUserIds = new ArrayList<>();
        try {
            JSONArray pointerArray = getPrivateStudentData().getJSONArray(Columns.friends);
            for (int i = 0; i < pointerArray.length(); i++) {
                JSONObject pointer = pointerArray.getJSONObject(i);
                friendPublicUserIds.add(pointer.getString("objectId"));
                Log.d("getFriendPublicUserIds", "Adding " + friendPublicUserIds.get(i));
            }
        }
        catch (JSONException e) {
            Log.e("PrivateStudentData", "Error in getFriendPublicUserIds");
            e.printStackTrace();
        }
        return friendPublicUserIds;
    }
}
