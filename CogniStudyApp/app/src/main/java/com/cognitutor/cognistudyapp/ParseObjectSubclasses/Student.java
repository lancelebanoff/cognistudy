package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QS_ShopItemInfo;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Task;

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

    public List<Achievement> getAchievements() { return getList(Columns.achievements); }
    public List<String> getShopItemsBought() { return getList(Columns.shopItemsBought); }
    public List<SkinSelection> getSkinSelections() { return getList(Columns.skinSelections); }
    public void setSkinSelection() {
        //TODO: Write this method
    }
    public void setRandomEnabled(boolean val) { put(Columns.randomEnabled, val); }
    public boolean getRandomEnabled() { return getBoolean(Columns.randomEnabled); }
    public void setPublicAnalytics(boolean val) { put(Columns.publicAnalytics, val); }
    public boolean getPublicAnalytics() { return getBoolean(Columns.publicAnalytics); }
    public String getBaseUserId() { return getString(Columns.baseUserId); }

    public static ParseQuery<Student> getQuery() {
        return ParseQuery.getQuery(Student.class);
    }

    public static Student getStudent() {
        return getStudent(ParseUser.getCurrentUser().getObjectId());
    }

    public static Student getStudent(String baseUserId) {

        try { return getLocalDataQuery(baseUserId).getFirst(); }
        catch (ParseException e) { e.printStackTrace(); return null; }
    }

    public static Task<Student> getStudentInBackground() {
        return getStudentInBackground(ParseUser.getCurrentUser().getObjectId());
    }

    public static Task<Student> getStudentInBackground(String baseUserId) {
        return getLocalDataQuery(baseUserId).getFirstInBackground();
    }

    private static ParseQuery<Student> getLocalDataQuery(String baseUserId) {

        return Student.getQuery()
                .fromLocalDatastore()
                .whereEqualTo(Columns.baseUserId, baseUserId);
    }
}
