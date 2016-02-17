package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryMonthStats")
public class StudentCategoryMonthStats extends StudentCategoryBlockStats{

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getMonthStats(getCurrentUserQuery(getClassName(), category));
            }

            @Override
            public String getClassName() {
                return "StudentCategoryMonthStats";
            }
        };
    }

    @Override
    public void setBlockNum() {
        setMonthBlockNum();
    }
}
