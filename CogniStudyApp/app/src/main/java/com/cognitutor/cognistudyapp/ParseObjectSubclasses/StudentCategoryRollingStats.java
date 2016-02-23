package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseRelation;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentCategoryRollingStats")
public class StudentCategoryRollingStats extends StudentTRollingStats {

    public class Columns {
        public static final String category = "category";
        public static final String answeredQuestionIds = "answeredQuestionIds";
    }

    public StudentCategoryRollingStats() {}
    public StudentCategoryRollingStats(String baseUserId, String category) {
        super(baseUserId);
        put(Columns.category, category);
        saveInBackground();
    }
}
