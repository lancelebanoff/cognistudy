package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryTridayStats")
public class StudentCategoryTridayStats extends StudentCategoryBlockStats{

    public ParseQuery<ParseObject> getCurrentBlockStats(String category) {
        return getCurrentTridayStats(getClassQuery(category));
    }

    @Override
    public void setBlockNum() {
        setTridayBlockNum();
    }
}
