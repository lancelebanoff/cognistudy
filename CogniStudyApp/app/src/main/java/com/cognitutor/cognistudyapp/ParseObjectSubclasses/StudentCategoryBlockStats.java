package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

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

    protected static ParseQuery getCurrentUserSuperQuery(ParseRelation relation, String category) {
        return relation.getQuery()
                .whereEqualTo(Columns.category, category);
    }

//    protected static ParseQuery getCurrentUserQuery(String className, String category) {
//        return getCurrentUserQuery(className)
//                .whereEqualTo(Columns.category, category);
//    }

    @Override
    public void setSubjectOrCategory(String category) {
        put(Columns.category, category);
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getCategory();
    }
}
