package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentBlockStats extends ParseObject{

    public static class SuperColumns {
        public static final String baseUserId = "baseUserId";
        public static final String startDate = "startDate";
        public static final String total = "total";
        public static final String correct = "correct";
    }

    public StudentBlockStats() {}
    public StudentBlockStats(Date startDate) {
        put(SuperColumns.baseUserId, ParseUser.getCurrentUser().getObjectId());
        put(SuperColumns.startDate, startDate);
        put(SuperColumns.total, 0);
        put(SuperColumns.correct, 0);
        SubclassUtils.addToSaveQueue(this);
    }

    protected void increment(boolean correct) {
        increment(SuperColumns.total);
        if(correct)
            increment(SuperColumns.correct);
        SubclassUtils.addToSaveQueue(this);
    }

    protected static <T extends StudentCategoryBlockStats> ParseQuery<T> getCurrentUserQuery(Class<T> className) {
        return ParseQuery.getQuery(className)
                .whereEqualTo(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
    }
}
