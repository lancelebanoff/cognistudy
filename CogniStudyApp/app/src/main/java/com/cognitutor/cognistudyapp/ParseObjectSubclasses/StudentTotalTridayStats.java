package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/29/2016.
 */
@ParseClassName("StudentTotalTridayStats")
public class StudentTotalTridayStats extends StudentTotalBlockStats {

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentTridayStats(getClassQuery());
    }

    @Override
    public void setBlockNum() {
        setTridayBlockNum();
    }
}
