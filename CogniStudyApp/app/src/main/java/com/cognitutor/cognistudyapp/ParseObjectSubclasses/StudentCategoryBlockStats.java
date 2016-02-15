package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

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

    public interface CurrentStatsInterface {
//        public ParseQuery<StudentCategoryBlockStats> getCurrentUserCurrentStats(Class type, String category);
        public ParseQuery<? extends StudentCategoryBlockStats> getCurrentUserCurrentStats(String category);
        public StudentCategoryBlockStats createObject();
    }

    public static void blah() {
        List<CurrentStatsInterface> list = new ArrayList<>();
        list.add(new CurrentStatsInterface() {
//            @Override
//            public <T extends StudentCategoryBlockStats> ParseQuery<T> getCurrentUserCurrentStats(Class<T> type, String category) {
//                return getCurrentUserQuery(StudentCategoryDayStats.class, category);
//                //TODO: where day == currentDay
//                return null;
//            }

            @Override
            public ParseQuery<? extends StudentCategoryBlockStats> getCurrentUserCurrentStats(String category) {
                return getCurrentUserQuery(StudentCategoryDayStats.class, category);
                //TODO: where day == currentDay
            }

            @Override
            public StudentCategoryBlockStats createObject() {
                return new StudentCategoryDayStats();
            }
        });

        for(final CurrentStatsInterface inter : list) {
            ParseQuery<? extends StudentCategoryBlockStats> query = inter.getCurrentUserCurrentStats("category");
            final String category = "cat";
            final boolean correct = true;

            ParseQuery<? extends StudentCategoryBlockStats> query2;

            new QueryUtils.ParseQueryBuilderAbstract<StudentCategoryBlockStats>() {
                @Override
                public ParseQuery<? extends StudentCategoryBlockStats> buildQuery() {
                    return inter.getCurrentUserCurrentStats(category);
                }
            };

//            QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilderAbstract<StudentCategoryBlockStats>() {
//                @Override
//                public ParseQuery<? extends StudentCategoryBlockStats> buildQuery() {
//                    return inter.getCurrentUserCurrentStats(category);
//                }
//            })
            QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<StudentCategoryBlockStats>() {
                @Override
                public ParseQuery<StudentCategoryBlockStats> buildQuery() {
                    return (ParseQuery<StudentCategoryBlockStats>) inter.getCurrentUserCurrentStats(category);
                }
            })
            .continueWith(new Continuation<StudentCategoryBlockStats, Object>() {
                @Override
                public Object then(Task<StudentCategoryBlockStats> task) throws Exception {
                    StudentCategoryBlockStats dayStats = task.getResult();
                    if (dayStats == null) {
                        //TODO: Create object
                    } else {
                        dayStats.increment(correct);
                    }
                    return null;
                }
            });
        }
    }
}
