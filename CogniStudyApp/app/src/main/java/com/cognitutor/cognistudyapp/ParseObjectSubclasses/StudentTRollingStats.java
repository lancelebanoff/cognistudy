package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseObject;

/**
 * Created by Kevin on 1/18/2016.
 */
public abstract class StudentTRollingStats extends ParseObject{

    public class Columns {
        public static final String baseUserId = "baseUserId";
        public static final String totalAllTime = "totalAllTime";
        public static final String correctAllTime = "correctAllTime";
        public static final String totalPastMonth = "totalPastMonth";
        public static final String correctPastMonth = "correctPastMonth";
        public static final String totalPastWeek = "totalPastWeek";
        public static final String correctPastWeek = "correctPastWeek";
    }

    public StudentTRollingStats() {}
    public StudentTRollingStats(String baseUserId) {
        put(Columns.baseUserId, baseUserId);
        put(Columns.totalAllTime, 0);
        put(Columns.correctAllTime, 0);
        put(Columns.totalPastMonth, 0);
        put(Columns.correctPastMonth, 0);
        put(Columns.totalPastWeek, 0);
        put(Columns.correctPastWeek, 0);
    }
}
