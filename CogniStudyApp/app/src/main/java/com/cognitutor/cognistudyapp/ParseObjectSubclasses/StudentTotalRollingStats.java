package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 1/31/2016.
 */
@ParseClassName("StudentTotalRollingStats")
public class StudentTotalRollingStats extends StudentTRollingStats {

    public class Columns {
    }

    public StudentTotalRollingStats() {}

    /**
     * Calls saveInBackground() after creation
     *
     * @param baseUserId
     */
    public StudentTotalRollingStats(String baseUserId) {
        super(baseUserId);
    }

    public static StudentTotalRollingStats findByBaseUserIdFromCache(final String baseUserId) {
        ParseQuery<StudentTotalRollingStats> query = ParseQuery.getQuery(StudentTotalRollingStats.class)
                .whereEqualTo(SuperColumns.baseUserId, baseUserId)
                .fromLocalDatastore();

        try {
            return query.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}

