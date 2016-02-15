package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryDayStats")
public class StudentCategoryDayStats extends StudentCategoryBlockStats {

    static CurrentUserCurrentBlockStats inter;
    static {
        inter = new CurrentUserCurrentBlockStats() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getCurrentUserQuery("StudentCategoryDayStats", category);
            }
        };
    }
}
