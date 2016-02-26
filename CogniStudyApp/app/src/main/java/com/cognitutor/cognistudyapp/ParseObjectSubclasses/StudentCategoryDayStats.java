package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryDayStats")
public class StudentCategoryDayStats extends StudentCategoryBlockStats {

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentDayStats(getClassQuery(category));
    }

    @Override
    public void setBlockNum() {
        setDayBlockNum();
    }
}