package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentSubjectTridayStats")
public class StudentSubjectTridayStats extends StudentSubjectBlockStats {

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getTridayStats(getCurrentUserQuery(getClassName(), category));
            }

            @Override
            public String getClassName() {
                return "StudentSubjectTridayStats";
            }
        };
    }

    @Override
    public void setBlockNum() {
        setTridayBlockNum();
    }
}
