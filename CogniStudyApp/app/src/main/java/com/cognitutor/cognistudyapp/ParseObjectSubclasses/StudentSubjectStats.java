package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentSubjectStats")
public class StudentSubjectStats extends StudentTStats {

    public class Columns {
        public static final String subject = "subject";
    }

    public StudentSubjectStats() {}
    public StudentSubjectStats(String baseUserId, String subject) {
        super(baseUserId);
        put(Columns.subject, subject);
        saveInBackground();
    }
}
