package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentSubjectRollingStats")
public class StudentSubjectRollingStats extends StudentTRollingStats {

    public class Columns {
        public static final String subject = "subject";
    }

    public StudentSubjectRollingStats() {}
    public StudentSubjectRollingStats(String baseUserId, String subject) {
        super(baseUserId);
        put(Columns.subject, subject);
        saveInBackground();
    }
}
