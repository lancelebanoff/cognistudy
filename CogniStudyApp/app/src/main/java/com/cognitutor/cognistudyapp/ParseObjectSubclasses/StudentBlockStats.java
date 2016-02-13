package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentBlockStats extends ParseObject{

    public static class Columns {
        public static final String baseUserId = "baseUserId";
        public static final String startDate = "startDate";
        public static final String total = "total";
        public static final String correct = "correct";
    }

    public StudentBlockStats() {}
    public StudentBlockStats(Date startDate) {
        put(Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
        put(Columns.startDate, startDate);
        put(Columns.total, 0);
        put(Columns.correct, 0);
        SubclassUtils.addToSaveQueue(this);
    }

    protected void increment(boolean correct) {
        increment(Columns.total);
        if(correct)
            increment(Columns.correct);
        SubclassUtils.addToSaveQueue(this);
    }
}
