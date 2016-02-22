package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryTridayStats")
public class StudentCategoryTridayStats extends StudentCategoryBlockStats{

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getTridayStats(getCurrentUserQuery(getClassName(), category));
            }

            @Override
            public String getClassName() {
                return Constants.ClassName.StudentCategoryTridayStats;
            }
        };
    }

    @Override
    public void setBlockNum() {
        setTridayBlockNum();
    }
}
