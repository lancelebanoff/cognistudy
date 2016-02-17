package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryDayStats")
public class StudentCategoryDayStats extends StudentCategoryBlockStats {

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getDayStats(getCurrentUserQuery(getClassName(), category));
            }

            @Override
            public String getClassName() {
                return "StudentCategoryDayStats";
            }
        };
    }

    @Override
    public void setBlockNum() {
        setDayBlockNum();
    }
}