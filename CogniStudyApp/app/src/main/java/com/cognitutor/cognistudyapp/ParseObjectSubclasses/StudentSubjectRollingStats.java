package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseQuery;

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

    public static StudentSubjectRollingStats findBySubjectFromCache(final String subject, final String baseUserId) {
        ParseQuery<StudentSubjectRollingStats> query = ParseQuery.getQuery(StudentSubjectRollingStats.class)
                        .whereEqualTo(SuperColumns.baseUserId, baseUserId)
                        .whereEqualTo(Columns.subject, subject)
                        .fromLocalDatastore();
        try {
            return query.getFirst();
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("%-8s", getSubject()) + " | " + super.toString();
    }
}
