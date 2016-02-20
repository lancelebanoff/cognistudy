package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.App;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
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
    public abstract void setBlockNum();

    private static Map<Class<? extends StudentBlockStats>, StudentBlockStatsSubclassInterface> subclasses;
    private static SubclassUtils subclassUtils = new SubclassUtils();

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

    public String getBaseUserId() { return getString(SuperColumns.baseUserId); }
    public int getBlockNum() { return getInt(SuperColumns.blockNum); }
    public int getTotal() { return getInt(SuperColumns.total); }
    public int getCorrect() { return getInt(SuperColumns.correct); }

    public StudentBlockStats() {
//        if(!App.isInitFinished() || !UserUtils.isUserLoggedIn())
//            return;
//        put(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
//        put(SuperColumns.total, 0);
//        put(SuperColumns.correct, 0);
//        SubclassUtils.addToSaveQueue(this);
    }

    private void initFields(String category) {
        put(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
        put(SuperColumns.total, 0);
        put(SuperColumns.correct, 0);
        setSubjectOrCategory(category);
        setACL();
        setBlockNum();
//        saveInBackground().continueWith(new Continuation<Void, Object>() {
//            @Override
//            public Object then(Task<Void> task) throws Exception {
//                if(task.isFaulted()) {
//                    Exception e = task.getError();
//                    Log.e("initFields", e.getMessage());
//                    if(e instanceof ParseException) {
//                        ParseException pe = (ParseException) e;
//                        Log.e("initFields error code:", String.valueOf(pe.getCode()));
//                    }
//                }
//                return null;
//            }
//        });
        subclassUtils.addToSaveQueue(this);
    }

    public static Task<Boolean> incrementAll(final String category, final boolean correct) {

        Map<Class<? extends StudentBlockStats>, StudentBlockStatsSubclassInterface> subclasses = getSubclassesMap();

        List<Task<Void>> tasks = new ArrayList<>();

        for(final Class clazz : subclasses.keySet()) {
            final StudentBlockStatsSubclassInterface inter = subclasses.get(clazz);
            final String className = inter.getClassName();
//            try {
//                getOrCreateAndIncrement(inter, category, className, clazz, correct)
//                        .waitForCompletion();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            tasks.add(getOrCreateAndIncrement(inter, category, className, clazz, correct));
        }
//        return SubclassUtils.saveAllInBackground();
        return Task.whenAll(tasks)
            .continueWithTask(new Continuation<Void, Task<Boolean>>() {
                @Override
                public Task<Boolean> then(Task<Void> task) throws Exception {
                    return subclassUtils.saveAllInBackground();
                }
            });
    }

    private static Task<Void> getOrCreateAndIncrement(final StudentBlockStatsSubclassInterface inter, final String category,
                                                       final String className, final Class clazz, final boolean correct) {
        return QueryUtils.getFirstPinElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<StudentBlockStats>() {
            @Override
            public ParseQuery<StudentBlockStats> buildQuery() {
                return inter.getCurrentUserCurrentStats(category);
            }
        }, className, true)
        .continueWith(new Continuation<StudentBlockStats, Void>() {
            @Override
            public Void then(Task<StudentBlockStats> task) throws Exception {
                createIfNecessaryAndIncrement(task.getResult(), clazz, inter.getClassName(), category, correct);
                return null;
            }
        });
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
        subclassUtils.addToSaveQueue(this);
    }

    private static void createIfNecessaryAndIncrement(StudentBlockStats blockStats, Class clazz,
                                                      String className, String category, boolean correct) {
        if(blockStats == null) {
            blockStats = createInstance(clazz);
            blockStats.initFields(category);
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

    private void setACL() {
        //TODO: Implement
    }

    protected static ParseQuery getCurrentUserQuery(String className) {
        return ParseQuery.getQuery(className)
                .whereEqualTo(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
    }

    private static Date testDate;
    public static void setTestDate(Date date) { testDate = date; }

    protected static ParseQuery<StudentBlockStats> getDayStats(ParseQuery<StudentBlockStats> query) {
        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentDayBlockNum());
//        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getDayBlockNum(testDate));
    }

    protected static ParseQuery<StudentBlockStats> getMonthStats(ParseQuery<StudentBlockStats> query) {
        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentMonthBlockNum());
//        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getMonthBlockNum(testDate));
    }

    protected static ParseQuery<StudentBlockStats> getTridayStats(ParseQuery<StudentBlockStats> query) {
        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentTridayBlockNum());
//        return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getTridayBlockNum(testDate));
    }

    protected void setDayBlockNum() {
        doSetBlockNum(DateUtils.getCurrentDayBlockNum());
//        doSetBlockNum(DateUtils.getDayBlockNum(testDate));
    }

    protected void setTridayBlockNum() {
        doSetBlockNum(DateUtils.getCurrentTridayBlockNum());
//        doSetBlockNum(DateUtils.getTridayBlockNum(testDate));
    }

    protected void setMonthBlockNum() {
        doSetBlockNum(DateUtils.getCurrentMonthBlockNum());
//        doSetBlockNum(DateUtils.getMonthBlockNum(testDate));
    }

    private void doSetBlockNum(int num) {
        put(SuperColumns.blockNum, num);
    }

    @Override
    public boolean equals(Object other) {
        if(this.getClass() != other.getClass())
            return false;
        StudentBlockStats otherBlockStats = (StudentBlockStats) other;
        if(!this.getBaseUserId().equals(otherBlockStats.getBaseUserId())
                || this.getBlockNum() != otherBlockStats.getBlockNum())
            return false;
        return true;
    }
}