package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentSubjectBlockStats extends StudentBlockStats{

    public static class Columns {
        public static final String subject = "subject";
    }

    private static <T extends StudentCategoryBlockStats> ParseQuery<T> getCurrentUserQuery(Class<T> className, String subject) {
        return getCurrentUserQuery(className)
                .whereEqualTo(Columns.subject, subject);
    }
}
