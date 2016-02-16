package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.App;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentBlockStats extends ParseObject{

    public abstract void setSubjectOrCategory(String category);

    interface CurrentUserCurrentBlockStats {
        ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category);
        String getClassName();
    }

    static Map<Class<? extends StudentBlockStats>, CurrentUserCurrentBlockStats> subclasses;

    public static class SuperColumns {
        public static final String baseUserId = "baseUserId";
        public static final String startDate = "startDate";
        public static final String total = "total";
        public static final String correct = "correct";
    }

    public StudentBlockStats() {
        if(!App.isInitFinished() || !UserUtils.isUserLoggedIn())
            return;
        put(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
//        put(SuperColumns.startDate, startDate); //TODO: Get current date
        put(SuperColumns.total, 0);
        put(SuperColumns.correct, 0);
        SubclassUtils.addToSaveQueue(this);
    }

    public static void incrementAll(final String category, final boolean correct) {

        Map<Class<? extends StudentBlockStats>, CurrentUserCurrentBlockStats> subclasses = getSubclassesMap();

        for(final Class clazz : subclasses.keySet()) {
            final CurrentUserCurrentBlockStats inter = subclasses.get(clazz);
            final String className = inter.getClassName();
            QueryUtils.getFirstPinElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<StudentBlockStats>() {
                @Override
                public ParseQuery<StudentBlockStats> buildQuery() {
                    return inter.getCurrentUserCurrentStats(category);
                }
            }, className, true)
            .continueWith(new Continuation<StudentBlockStats, Object>() {
                @Override
                public Object then(Task<StudentBlockStats> task) throws Exception {
                    createIfNecessaryAndIncrement(task.getResult(), clazz, inter.getClassName(), category, correct);
                    return null;
                }
            });
        }
        SubclassUtils.saveAllInBackground();
    }

    private static Map<Class<? extends StudentBlockStats>, CurrentUserCurrentBlockStats> getSubclassesMap() {
        if(subclasses != null)
            return subclasses;
        subclasses = new HashMap<>();
        subclasses.put(StudentCategoryDayStats.class, StudentCategoryDayStats.getInterface());
        return subclasses;
    }

    protected void increment(boolean correct) {
        increment(SuperColumns.total);
        if(correct)
            increment(SuperColumns.correct);
        Log.i("total", Integer.toString(getInt(SuperColumns.total)));
        SubclassUtils.addToSaveQueue(this);
    }

    private static void createIfNecessaryAndIncrement(StudentBlockStats blockStats, Class type,
                                                      String className, String category, boolean correct) {
        if(blockStats == null) {
            blockStats = createInstance(type);
            blockStats.setSubjectOrCategory(category);
            ParseObject.unpinAllInBackground(className);
            blockStats.pinInBackground(className);
        }
        blockStats.increment(correct);
    }

    private static <T extends StudentBlockStats> T createInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (Exception e) {e.printStackTrace();}
        return null;
    }

    protected static ParseQuery getCurrentUserQuery(String className) {
        return ParseQuery.getQuery(className)
                .whereEqualTo(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
    }

//    interface DayStats {
//        class Columns {
//            public static final String day = "day";
//        }
//        default <T extends StudentBlockStats> ParseQuery<T> getDayStats(ParseQuery<T> query) {
//            return query.whereEqualTo(Columns.day, 1);
//        }
//    }
}