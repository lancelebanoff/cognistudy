package com.cognitutor.cognistudyapp;

import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Kevin on 1/7/2016.
 */
public class UserUtils {

    public static ParseObject getPublicUserData() {
        return getObjectFromPointer(ParseUser.getCurrentUser(), "publicUserData");
    }

    public static ParseObject getStudent() {
        return getObjectFromPointer(getPublicUserData(), "student");
    }

    public static ParseObject getPrivateStudentData() {
        return getObjectFromPointer(getStudent(), "privateStudentData");
    }

    //Should these use fetchIfNeeded? Or local datastore, etc.????????
    ///////////////////////////////
    public static ParseObject getPublicUserDataFromStudentOrTutor(ParseObject object) {
        return getObjectFromPointer(object, "publicUserData");
    }

    public static ParseObject getStudentFromPublicUserData(ParseObject publicUserData) {
        return getObjectFromPointer(publicUserData, "student");
    }

    public static ParseObject getTutorFromPublicUserData(ParseObject publicUserData) {
        return getObjectFromPointer(publicUserData, "tutor");
    }
    //////////////////////////////

    private static ParseObject getObjectFromPointer(ParseObject parent, String pointerClass) {
        ParseObject returnObject = null;
        try {
            returnObject = parent.getParseObject(pointerClass).fetchIfNeeded();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return returnObject;
    }
}
