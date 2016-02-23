package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseQuery;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentSubjectBlockStats extends StudentBlockStats{

    public static class Columns {
        public static final String subject = "subject";
    }

    public String getSubject() { return getString(Columns.subject); }

    protected static ParseQuery getCurrentUserQuery(String className, String category) {
        return getCurrentUserQuery(className)
                .whereEqualTo(Columns.subject, getSubjectFromCategory(category));
    }

    @Override
    public void setSubjectOrCategory(String category) {
        put(Columns.subject, getSubjectFromCategory(category));
    }

    public static String getSubjectFromCategory(String category) {
        for(String subject : Constants.getAllConstants(Constants.Subject.class)) {
            List<String> categoriesInSubject = Arrays.asList(Constants.SubjectToCategory.get(subject));
            if(categoriesInSubject.contains(category)) {
                return subject;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getSubject();
    }
}