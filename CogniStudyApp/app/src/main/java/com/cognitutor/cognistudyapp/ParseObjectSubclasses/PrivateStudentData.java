package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

import bolts.Task;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("PrivateStudentData")
public class PrivateStudentData extends ParseObject{

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
        put(Columns.friends, new ArrayList<ParseObject>());
        put(Columns.tutors, new ArrayList<ParseObject>());
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
        return getPrivateStudentData(ParseUser.getCurrentUser().getObjectId());
    }

    private static PrivateStudentData getPrivateStudentData(String baseUserId) {

        try {
            return PrivateStudentData.getQuery()
                    .fromLocalDatastore()
                    .whereEqualTo(Columns.baseUserId, baseUserId)
                    .getFirst();
        }
        catch (ParseException e) { e.printStackTrace(); return null; }
    }

    public static Task<PrivateStudentData> getPrivateStudentDataInBackground() {
        return getPrivateStudentDataInBackground(ParseUser.getCurrentUser().getObjectId());
    }

    private static Task<PrivateStudentData> getPrivateStudentDataInBackground(String baseUserId) {

        return PrivateStudentData.getQuery()
                .fromLocalDatastore()
                .whereEqualTo(Columns.baseUserId, baseUserId)
                .getFirstInBackground();
    }
}
