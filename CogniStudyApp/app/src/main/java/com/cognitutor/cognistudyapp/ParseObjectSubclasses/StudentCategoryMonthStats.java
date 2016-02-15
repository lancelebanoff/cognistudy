package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("StudentCategoryMonthStats")
public class StudentCategoryMonthStats extends StudentCategoryBlockStats{
    @Override
    public ParseQuery<? extends StudentCategoryBlockStats> getCurrentUserCurrentStats(String category) {
        return null;
    }
}
