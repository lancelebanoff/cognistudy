package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentSubjectDayStats")
public class StudentSubjectDayStats extends StudentSubjectBlockStats {

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentDayStats(getClassQuery(category));
    }

    @Override
    public void setBlockNum() {
        setDayBlockNum();
    }
}
