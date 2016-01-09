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

    private class Columns {
        private static final String numCoins = "numCoins";
        private static final String responses = "responses";
        private static final String friends = "friends";
        private static final String tutors = "tutors";
        private static final String blocked = "blocked";
        private static final String recentChallenges = "recentChallenges";
        private static final String requestsFromTutors = "requestsFromTutors";
        private static final String totalResponses = "totalResponses";
        private static final String correctResponses = "correctResponses";
        private static final String suggestedQuestions = "suggestedQuestions";
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
    }
}
