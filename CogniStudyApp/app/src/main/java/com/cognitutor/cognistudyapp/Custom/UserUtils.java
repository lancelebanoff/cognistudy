package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Kevin on 1/7/2016.
 */
public class UserUtils {

    public static PublicUserData getPublicUserData() throws ParseException {
        if (ParseUser.getCurrentUser().getParseObject("publicUserData").isDataAvailable()) {
            Log.d("UserUtil getPubUserData", "Data is not available");
        }
        else {
            Log.d("UserUtil getPubUserData", "Data is available");
        }
        return (PublicUserData) ParseUser.getCurrentUser().getParseObject("publicUserData").fetchIfNeeded();
    }

    public static Student getStudent() throws ParseException {
        return getPublicUserData().getStudent();
    }

    public static PrivateStudentData getPrivateStudentData() throws ParseException {
        return getStudent().getPrivateStudentData();
    }

    public static void pinTest() throws  ParseException {
        String TAG = "pinStudentObjects";
        /*
        Log.d("pinSutdentObjects", "publicUserData " +
                (ParseUser.getCurrentUser().getParseObject("publicUserData").isDataAvailable() ? "is available" : "is not available"));
                */
        PublicUserData publicUserData = (PublicUserData) ParseUser.getCurrentUser().getParseObject("publicUserData");
        Log.d(TAG, "publicUserData is " + (publicUserData.isDataAvailable() ? "" : "not ") + "available");
        publicUserData.fetchIfNeeded();

        Student student = (Student) publicUserData.getParseObject("student");
        Log.d(TAG, "student is " + (student.isDataAvailable() ? "" : "not ") + "available");
        student.fetchIfNeeded();

        PrivateStudentData privateStudentData = (PrivateStudentData) student.getParseObject("privateStudentData");
        Log.d(TAG, "privateStudentData is " + (privateStudentData.isDataAvailable() ? "" : "not ") + "available");
        privateStudentData.fetchIfNeeded();

        publicUserData.pin("CurrentUser");
    }

    public static void getPinTest() throws ParseException {

        /*
        PublicUserData publicUserDataFromPin = ParseQuery.getQuery(PublicUserData.class)
                .fromPin("CurrentUser")
                .include("student")
                .include("privateStudentData")
                .getFirst();

        Log.d("After client fetch", "publicUserData objectId is " + publicUserDataFromPin.getObjectId());
        Log.d("After client fetch", "student objectId is " + publicUserDataFromPin.getStudent().getObjectId());
        Log.d("After client fetch", "privateStudentData objectId is " + publicUserDataFromPin.getStudent().getPrivateStudentData().getObjectId());
        */

        Log.d("getPinTest", "Skipping method");
    }

    public static void unpinTest() throws ParseException {

        ParseObject.unpinAll("CurrentUser");
    }
}
