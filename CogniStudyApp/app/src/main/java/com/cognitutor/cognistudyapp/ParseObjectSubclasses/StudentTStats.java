package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseObject;

/**
 * Created by Kevin on 1/18/2016.
 */
public abstract class StudentTStats extends ParseObject{

    public class Columns {
        public static final String baseUserId = "baseUserId";
        public static final String totalResponses = "totalResponses";
        public static final String correctResponses = "correctResponses";
    }

    public StudentTStats() {}
    public StudentTStats(String baseUserId) {
        put(Columns.baseUserId, baseUserId);
        put(Columns.totalResponses, 0);
        put(Columns.correctResponses, 0);
    }
}
