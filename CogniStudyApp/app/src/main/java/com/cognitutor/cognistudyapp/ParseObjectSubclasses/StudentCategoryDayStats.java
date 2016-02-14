package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryDayStats")
public class StudentCategoryDayStats extends StudentCategoryBlockStats {

    public static ParseQuery<StudentCategoryDayStats> getQuery() { return ParseQuery.getQuery(StudentCategoryDayStats.class); }

    @Override
    public ParseQuery<StudentCategoryDayStats> getCurrentUserCurrentStats(String category) {
        return getCurrentUserQuery(StudentCategoryDayStats.class, category);
        //TODO: Where day == current day
    }

    public static ParseQuery<StudentCategoryDayStats> getCurrentUserCurrentDayStats(String category) {
        return getCurrentUserQuery(StudentCategoryDayStats.class, category);
        //TODO: Where day == current day
    }
}
