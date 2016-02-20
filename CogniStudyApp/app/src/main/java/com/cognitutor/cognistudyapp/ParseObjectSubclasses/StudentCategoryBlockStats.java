package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentCategoryBlockStats extends StudentBlockStats {

    public static class Columns {
        public static final String category = "category";
    }

    public String getCategory() { return getString(Columns.category); }

    protected static ParseQuery getCurrentUserQuery(String className, String category) {
        return getCurrentUserQuery(className)
                .whereEqualTo(Columns.category, category);
    }

    @Override
    public void setSubjectOrCategory(String category) {
        put(Columns.category, category);
    }

    @Override
    public boolean equals(Object other) {
        if(!super.equals(other))
            return false;
        return this.getCategory().equals(((StudentCategoryBlockStats) other).getCategory());
    }
}
