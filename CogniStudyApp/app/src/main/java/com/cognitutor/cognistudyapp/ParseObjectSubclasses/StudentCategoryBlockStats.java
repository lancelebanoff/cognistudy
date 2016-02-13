package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.text.format.DateUtils;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseQuery;

import java.util.Date;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentCategoryBlockStats extends StudentBlockStats {

    public class Columns {
        public static final String category = "category";
    }

    public static void increment(String category, boolean correct) {
        //1. Create or get StudentCategoryDayStats and increment
        ParseQuery query = getQuery(StudentCategoryDayStats.class, category);
        //2. " " StudentCategoryTridayStats " "
        //3. " " StudentCategoryMonthStats " "
    }

    private static ParseQuery<StudentCategoryBlockStats> getQuery(Class subClass, String category) {
        return ParseQuery.getQuery(subClass)
                .whereEqualTo(Columns.category, category);
    }
}
