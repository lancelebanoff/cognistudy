package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

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
}
