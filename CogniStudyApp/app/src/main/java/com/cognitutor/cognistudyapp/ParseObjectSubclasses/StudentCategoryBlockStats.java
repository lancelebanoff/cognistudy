package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentCategoryBlockStats extends StudentBlockStats {

    public static class Columns {
        public static final String category = "category";
    }

    public static void incrementAll(final String category, final boolean correct) {
        //1. Create or get StudentCategoryDayStats and increment
        QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<StudentCategoryDayStats>() {
            @Override
            public ParseQuery<StudentCategoryDayStats> buildQuery() {
                return StudentCategoryDayStats.getCurrentUserCurrentDayStats(category);
            }
        })
        .continueWith(new Continuation<StudentCategoryDayStats, Object>() {
            @Override
            public Object then(Task<StudentCategoryDayStats> task) throws Exception {
                StudentCategoryDayStats dayStats = task.getResult();
                if(dayStats == null) {
                    //TODO: Create object
                }
                else {
                    dayStats.increment(correct);
                }
                return null;
            }
        });
        //2. " " StudentCategoryTridayStats " "
        //3. " " StudentCategoryMonthStats " "
        SubclassUtils.saveAllInBackground();
    }

    protected static <T extends StudentCategoryBlockStats> ParseQuery<T> getCurrentUserQuery(Class<T> className, String category) {
        return getCurrentUserQuery(className)
                .whereEqualTo(Columns.category, category);
    }

    public abstract ParseQuery<? extends StudentCategoryBlockStats> getCurrentUserCurrentStats(String category);
}
