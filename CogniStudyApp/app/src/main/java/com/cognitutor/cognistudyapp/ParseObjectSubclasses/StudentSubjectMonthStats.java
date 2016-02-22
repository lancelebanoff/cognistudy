package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentSubjectMonthStats")
public class StudentSubjectMonthStats extends StudentSubjectBlockStats {

    public static StudentBlockStatsSubclassInterface getInterface() {
        return new StudentBlockStatsSubclassInterface() {
            @Override
            public ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category) {
                return getMonthStats(getCurrentUserQuery(getClassName(), category));
            }

            @Override
            public String getClassName() {
                return Constants.ClassName.StudentSubjectMonthStats;
            }
        };
    }

    @Override
    public void setBlockNum() {
        setMonthBlockNum();
    }
}
