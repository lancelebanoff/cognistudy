package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.parse.ParseException;
import com.parse.ParseObject;
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

    public static void pinStudentObjects() {
    }
}
