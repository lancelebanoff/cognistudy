package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/29/2016.
 */
@ParseClassName("StudentTotalMonthStats")
public class StudentTotalMonthStats extends StudentTotalBlockStats {

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentMonthStats(getClassQuery());
    }

    @Override
    public void setBlockNum() {
        setMonthBlockNum();
    }
}
