package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentSubjectMonthStats")
public class StudentSubjectMonthStats extends StudentSubjectBlockStats {

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentMonthStats(getClassQuery(category));
    }

    @Override
    public void setBlockNum() {
        setMonthBlockNum();
    }
}
