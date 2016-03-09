package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentSubjectBlockStats extends StudentBlockStats{

    public static class Columns {
        public static final String subject = "subject";
    }

    public String getSubject() { return getString(Columns.subject); }

    protected ParseQuery<ParseObject> getClassQuery(String category) {
        return super.getClassQuery().whereEqualTo(Columns.subject, CommonUtils.getSubjectFromCategory(category));
    }

    @Override
    public void setSubjectOrCategory(String category) {
        put(Columns.subject, CommonUtils.getSubjectFromCategory(category));
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getSubject();
    }
}