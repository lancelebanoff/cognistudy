package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.App;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.HashMap;
import java.util.Map;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentBlockStats extends ParseObject{

    public abstract void setSubjectOrCategory(String category);
    public abstract void setBlockNum();

    private static Map<Class<? extends StudentBlockStats>, StudentBlockStatsSubclassInterface> subclasses;

    interface StudentBlockStatsSubclassInterface {
        ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(String category);
        String getClassName();
    }

    public static class SuperColumns {
        public static final String baseUserId = "baseUserId";
        public static final String blockNum = "blockNum";
        public static final String total = "total";
        public static final String correct = "correct";
    }

    public StudentBlockStats() {
        if(!App.isInitFinished() || !UserUtils.isUserLoggedIn())
            return;
        put(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
        put(SuperColumns.total, 0);
        put(SuperColumns.correct, 0);
        SubclassUtils.addToSaveQueue(this);
    }

    public static void incrementAll(final String category, final boolean correct) {

        Map<Class<? extends StudentBlockStats>, StudentBlockStatsSubclassInterface> subclasses = getSubclassesMap();

        for(final Class clazz : subclasses.keySet()) {
            final StudentBlockStatsSubclassInterface inter = subclasses.get(clazz);
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

    private static Map<Class<? extends StudentBlockStats>, StudentBlockStatsSubclassInterface> getSubclassesMap() {
        if(subclasses != null)
            return subclasses;
        subclasses = new HashMap<>();
        subclasses.put(StudentCategoryDayStats.class, StudentCategoryDayStats.getInterface());
        subclasses.put(StudentCategoryTridayStats.class, StudentCategoryTridayStats.getInterface());
        subclasses.put(StudentCategoryMonthStats.class, StudentCategoryMonthStats.getInterface());
        subclasses.put(StudentSubjectDayStats.class, StudentSubjectDayStats.getInterface());
        subclasses.put(StudentSubjectTridayStats.class, StudentSubjectTridayStats.getInterface());
        subclasses.put(StudentSubjectMonthStats.class, StudentSubjectMonthStats.getInterface());
        return subclasses;
    }

    protected void increment(boolean correct) {
        increment(SuperColumns.total);
        if(correct)
            increment(SuperColumns.correct);
        Log.i("total", Integer.toString(getInt(SuperColumns.total)));
        SubclassUtils.addToSaveQueue(this);
    }

    private static void createIfNecessaryAndIncrement(StudentBlockStats blockStats, Class clazz,
                                                      String className, String category, boolean correct) {
        if(blockStats == null) {
            blockStats = createInstance(clazz);
            blockStats.setSubjectOrCategory(category);
            blockStats.setBlockNum();
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

    protected static ParseQuery<StudentBlockStats> getDayStats(ParseQuery<StudentBlockStats> query) {
        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentDayBlockNum());
    }

    protected static ParseQuery<StudentBlockStats> getMonthStats(ParseQuery<StudentBlockStats> query) {
        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentMonthBlockNum());
    }

    protected static ParseQuery<StudentBlockStats> getTridayStats(ParseQuery<StudentBlockStats> query) {
        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentTridayBlockNum());
    }

    protected void setDayBlockNum() {
        doSetBlockNum(DateUtils.getCurrentDayBlockNum());
    }

    protected void setTridayBlockNum() {
        doSetBlockNum(DateUtils.getCurrentTridayBlockNum());
    }

    protected void setMonthBlockNum() {
        doSetBlockNum(DateUtils.getCurrentMonthBlockNum());
    }

    private void doSetBlockNum(int num) {
        put(SuperColumns.blockNum, num);
    }
}