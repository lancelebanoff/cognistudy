package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentSubjectDayStats")
public class StudentSubjectDayStats extends StudentSubjectBlockStats {

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getDayStats(getCurrentUserQuery(getClassName(), category));
            }

            @Override
            public String getClassName() {
                return "StudentSubjectDayStats";
            }
        };
    }

    @Override
    public void setBlockNum() {
        setDayBlockNum();
    }
}
