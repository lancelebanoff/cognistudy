package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;

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
        saveInBackground();
    }
}

