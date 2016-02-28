package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

import bolts.Task;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentSubjectRollingStats")
public class StudentSubjectRollingStats extends StudentTRollingStats {

    public class Columns {
        public static final String subject = "subject";
    }

    public String getSubject() { return getString(Columns.subject); }

    public StudentSubjectRollingStats() {}

    /**
     * Calls saveInBackground() after creation
     * @param baseUserId
     * @param subject
     */
    public StudentSubjectRollingStats(String baseUserId, String subject) {
        super(baseUserId);
        put(Columns.subject, subject);
        saveInBackground();
    }

    @Override
    public String toString() {
        return String.format("%-8s", getSubject()) + " | " + super.toString();
    }
}
