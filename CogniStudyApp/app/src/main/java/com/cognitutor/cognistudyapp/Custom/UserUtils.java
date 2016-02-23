package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Kevin on 1/7/2016.
 */
public class UserUtils {

    private static boolean userLoggedIn;

    public static void setUserLoggedIn(boolean val) {
        userLoggedIn = val;
    }

    public static boolean isUserLoggedIn() { return userLoggedIn; }

    public static PublicUserData getPublicUserData() throws ParseException {
        return (PublicUserData) ParseUser.getCurrentUser().getParseObject("publicUserData").fetchIfNeeded();
    }

    public static Student getStudent() throws ParseException {
        return getPublicUserData().getStudent();
    }

    public static void pinTest() throws  ParseException {
        String TAG = "pinStudentObjects";
        /*
        Log.d("pinSutdentObjects", "publicUserData " +
                (ParseUser.getCurrentUser().getParseObject("publicUserData").isDataAvailable() ? "is available" : "is not available"));
                */
        PublicUserData publicUserData = (PublicUserData) ParseUser.getCurrentUser().getParseObject("publicUserData");
//        Log.d(TAG, "publicUserData is " + (publicUserData.isDataAvailable() ? "" : "not ") + "available");
        publicUserData.fetchIfNeeded();

        Student student = (Student) publicUserData.getParseObject("student");
        Log.d(TAG, "student is " + (student.isDataAvailable() ? "" : "not ") + "available");
        student.fetchIfNeeded();
        //TODO: Get and pin all Student_RollingStats objects from student

        PrivateStudentData privateStudentData = (PrivateStudentData) student.getParseObject("privateStudentData");
        Log.d(TAG, "privateStudentData is " + (privateStudentData.isDataAvailable() ? "" : "not ") + "available");
        privateStudentData.fetchIfNeeded();

        ParseObjectUtils.unpinAll("CurrentUser");
        ParseObjectUtils.pin("CurrentUser", publicUserData);
    }

    public static void getPinTest() throws ParseException {

        PublicUserData publicUserDataFromPin = ParseQuery.getQuery(PublicUserData.class)
                //.fromPin("CurrentUser")
                .fromLocalDatastore()
                .whereEqualTo("baseUserId", ParseUser.getCurrentUser().getObjectId())
                .include("student")
                .include("privateStudentData")
                .getFirst();

        PrivateStudentData privateStudentData = (PrivateStudentData) publicUserDataFromPin
                .getParseObject("student")
                .getParseObject("privateStudentData");

        PrivateStudentData privateStudentData1 = ParseQuery.getQuery(PrivateStudentData.class)
                .fromLocalDatastore()
                .whereEqualTo(PrivateStudentData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId())
                .getFirst();
        /*
        Log.d("After client fetch", "publicUserData objectId is " + publicUserDataFromPin.getObjectId());
        Log.d("After client fetch", "student objectId is " + publicUserDataFromPin.getStudent().getObjectId());
        Log.d("After client fetch", "privateStudentData objectId is " + publicUserDataFromPin.getStudent().getPrivateStudentData().getObjectId());
        */
        Log.d("After client fetch", "privateStudentData objectId is " + privateStudentData.getObjectId());
        Log.d("After client fetch", "privateStudentData1 objectId is " + privateStudentData1.getObjectId());

        /*
        List<PublicUserData> list = ParseQuery.getQuery(PublicUserData.class)
                .fromPin("CurrentUser")
                .include("student")
                .include("privateStudentData")
                .find();

        Log.d("After client fetch", "Here are the users");
        for(int i=1; i<=list.size(); i++) {
            PublicUserData p = list.get(i-1);
            Log.d("User " + i, "publicUserData objectId is " + p.getObjectId());
            Log.d("User " + i, "student objectId is " + p.getStudent().getObjectId());
            Log.d("User " + i, "privateStudentData objectId is " + p.getStudent().getPrivateStudentData().getObjectId());
        }
        */

        //Log.d("getPinTest", "Skipping method");
    }

    public static String getCurrentUserId() {
        return ParseUser.getCurrentUser().getObjectId();
    }
}
