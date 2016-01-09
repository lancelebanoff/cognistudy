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

    public class Columns {
        public static final String achievements = "achievements";
        public static final String shopItemsBought = "shopItemsBought";
        public static final String skinSelections = "skinSelections";
        public static final String randomEnabled = "randomEnabled";
        public static final String publicAnalytics = "publicAnalytics";
        public static final String privateStudentData = "privateStudentData";
        public static final String baseUserId = "baseUserId";
    }

    public Student() {}
    public Student(ParseUser user, PrivateStudentData privateStudentData) {
        ParseACL acl = new ParseACL(user);
        acl.setPublicReadAccess(true);
        setACL(acl);

        put(Columns.privateStudentData, privateStudentData);
        put(Columns.achievements, new ArrayList<ParseObject>());
        put(Columns.shopItemsBought, new ArrayList<ParseObject>());
        put(Columns.skinSelections, new ArrayList<ParseObject>());
        setRandomEnabled(true);
        setPublicAnalytics(true);
        put(Columns.baseUserId, user.getObjectId());
    }

    //public ArrayList<Achievement> getAchievements() { return getList("achievements"); }

    public void setRandomEnabled(boolean val) { put(Columns.randomEnabled, val); }
    public boolean getRandomEnabled() { return getBoolean(Columns.randomEnabled); }
    public void setPublicAnalytics(boolean val) { put(Columns.publicAnalytics, val); }
    public boolean getPublicAnalytics() { return getBoolean(Columns.publicAnalytics); }

    public PrivateStudentData getPrivateStudentData() throws ParseException {
        return getParseObject(Columns.privateStudentData).fetchIfNeeded();
    }
}
