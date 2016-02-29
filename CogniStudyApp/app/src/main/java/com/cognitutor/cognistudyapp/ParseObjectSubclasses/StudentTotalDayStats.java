package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/29/2016.
 */
@ParseClassName("StudentTotalDayStats")
public class StudentTotalDayStats extends StudentTotalBlockStats {

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentDayStats(getClassQuery());
    }

    @Override
    public void setBlockNum() {
        setDayBlockNum();
    }
}
