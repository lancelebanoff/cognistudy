package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("Student")
public class Student extends ParseObject{

    public Student() {}
    public Student(ParseUser user, PublicUserData publicUserData, PrivateStudentData privateStudentData) {
        ParseACL acl = new ParseACL(user);
        acl.setPublicReadAccess(true);
        setACL(acl);

        put("PublicUserData", publicUserData);
        put("privateStudentData", privateStudentData);
        put("achievements", new ArrayList<ParseObject>());
        put("shopItemsBought", new ArrayList<ParseObject>());
        put("skinSelections", new ArrayList<ParseObject>());
        setRandomEnabled(true);
        setPublicAnalytics(true);
    }

    //public ArrayList<Achievement> getAchievements() { return getList("achievements"); }

    public void setRandomEnabled(boolean val) { put("randomEnabled", val); }
    public boolean getRandomEnabled() { return getBoolean("randomEnabled"); }
    public void setPublicAnalytics(boolean val) { put("publicAnalytics", val); }
    public boolean getPublicAnalytics() { return getBoolean("publicAnalytics"); }

    public PublicUserData getPublicUserData() throws ParseException {
        return getParseObject("publicUserData").fetchIfNeeded();
    }
}
