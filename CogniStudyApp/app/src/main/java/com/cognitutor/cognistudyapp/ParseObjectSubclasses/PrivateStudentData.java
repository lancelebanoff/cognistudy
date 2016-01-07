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

    public PrivateStudentData() {}
    public PrivateStudentData(ParseUser user) {
        ParseACL acl = new ParseACL(user);
        acl.setPublicReadAccess(false);
        setACL(acl);
        put("numCoins", 0);
        put("friends", new ArrayList<ParseObject>());
        put("tutors", new ArrayList<ParseObject>());
        put("blocked", new ArrayList<ParseObject>());
        put("recentChallenges", new ArrayList<ParseObject>());
        put("requestsFromTutors", new ArrayList<ParseObject>());
        put("totalResponses", 0);
        put("correctResponses", 0);
        put("suggestedQuestions", new ArrayList<ParseObject>());
    }
}
