package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

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

    static final Map<Class<? extends StudentBlockStats>, CurrentUserCurrentBlockStats> subclasses;
    static {
        Map<Class<? extends StudentBlockStats>, CurrentUserCurrentBlockStats> map = new HashMap<>();

        map.put(StudentCategoryDayStats.class, StudentCategoryDayStats.inter);

        subclasses = Collections.unmodifiableMap(map);
    }

    public static class SuperColumns {
        public static final String baseUserId = "baseUserId";
        public static final String startDate = "startDate";
        public static final String total = "total";
        public static final String correct = "correct";
    }

    public StudentBlockStats() {
        put(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
//        put(SuperColumns.startDate, startDate); //TODO: Get current date
        put(SuperColumns.total, 0);
        put(SuperColumns.correct, 0);
        SubclassUtils.addToSaveQueue(this);
    }

    public static void incrementAll(final String category, final boolean correct) {

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
    }

    protected void increment(boolean correct) {
        increment(SuperColumns.total);
        if(correct)
            increment(SuperColumns.correct);
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
}
