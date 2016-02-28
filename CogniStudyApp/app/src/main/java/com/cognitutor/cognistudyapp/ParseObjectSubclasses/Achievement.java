package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("Achievement")
public class Achievement extends ParseObject {
    public static class Columns {
        public static final String numToGain = "numToGain";
        public static final String baseUserId = "baseUserId";
        public static final String test = "test";
    }

    public int getNumToGain() { return getInt(Columns.numToGain); }
    public String getBaseUserId() { return getString(Columns.baseUserId); }
    public int getTest() { return getInt(Columns.test); }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() + " | numToGain: " + getNumToGain() + " | baseUserID: " + getBaseUserId()
                + " | test: " + getTest();
    }
}
