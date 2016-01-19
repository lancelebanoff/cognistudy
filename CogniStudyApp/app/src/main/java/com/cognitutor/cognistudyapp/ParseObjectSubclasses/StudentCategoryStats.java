package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentCategoryStats")
public class StudentCategoryStats extends StudentTStats {

    public class Columns {
        public static final String category = "category";
    }

    public StudentCategoryStats() {}
    public StudentCategoryStats(String baseUserId, String category) {
        super(baseUserId);
        put(Columns.category, category);
        saveInBackground();
    }
}
