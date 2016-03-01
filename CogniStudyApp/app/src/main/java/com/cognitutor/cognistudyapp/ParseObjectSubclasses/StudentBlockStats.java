package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 2/13/2016.
 */
public abstract class StudentBlockStats extends ParseObject{

    public abstract void setSubjectOrCategory(String category);
    public abstract void setBlockNum();
    public abstract ParseQuery<ParseObject> getCurrentBlockStats(String category);

    public static class SuperColumns {
        public static final String baseUserId = "baseUserId";
        public static final String blockNum = "blockNum";
        public static final String total = "total";
        public static final String correct = "correct";
    }

    public int getBlockNum() { return getInt(SuperColumns.blockNum); }
    public int getTotal() { return getInt(SuperColumns.total); }
    public int getCorrect() { return getInt(SuperColumns.correct); }

    public StudentBlockStats() {
    }

    public ParseQuery getRelationQuery(Student student) {
        return student.getStudentBlockStatsRelation(getClass()).getQuery();
    }

    protected ParseQuery<ParseObject> getClassQuery() {
        return new ParseQuery(getClass().getSimpleName());
    }

    private static List<StudentBlockStats> subclassesList;
    private static List<StudentBlockStats> getSubclassInstances() {
        if(subclassesList != null)
            return subclassesList;
        subclassesList = new ArrayList<>();
        subclassesList.add(new StudentCategoryDayStats());
        subclassesList.add(new StudentCategoryTridayStats());
        subclassesList.add(new StudentCategoryMonthStats());
        subclassesList.add(new StudentSubjectDayStats());
        subclassesList.add(new StudentSubjectTridayStats());
        subclassesList.add(new StudentSubjectMonthStats());
        subclassesList.add(new StudentTotalDayStats());
        subclassesList.add(new StudentTotalTridayStats());
        subclassesList.add(new StudentTotalMonthStats());
        return subclassesList;
    }

    private void initFields(String category) {
        put(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
        put(SuperColumns.total, 0);
        put(SuperColumns.correct, 0);
        setSubjectOrCategory(category);
        setACL();
        setBlockNum();
        ParseObjectUtils.addToSaveQueue(this);
    }

    enum ParallelOption {
        ALL_AT_ONCE, SEVERAL, NONE
    }

    public static Task<Void> incrementAll(final String category, final boolean correct) {

        final List<? extends StudentBlockStats> subclassInstances = getSubclassInstances();

        final long start;
        ParallelOption option = ParallelOption.NONE;
        Iterator<? extends StudentBlockStats> iterator = subclassInstances.iterator();

//        if(option == ParallelOption.NONE) {
//            start = System.currentTimeMillis();
//            while (iterator.hasNext()) {
//                final Class clazz = iterator.next();
//                final StudentBlockStatsSubclassInterface inter = subclasses.get(clazz);
//                final String className = inter.getClassName();
//                try {
//                    getOrCreateAndIncrement(inter, category, className, clazz, correct)
//                            .waitForCompletion();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            Log.d("time taken", String.valueOf(System.currentTimeMillis() - start));
//            return ParseObjectUtils.saveAllInBackground();
//        }
//        else if(option == ParallelOption.SEVERAL) {
//            //Running all 6 blockstats tasks at the same time is problematic when using certain types of queries in QueryUtils
//            //This became a problem after taking out inBackground queries in QueryUtils
//            start = System.currentTimeMillis();
//            int length = subclasses.keySet().size();
//            final int MAX_NUM_THREADS = 3;
//            int N = length / MAX_NUM_THREADS;
//            int M = MAX_NUM_THREADS;
//            for (int n = 0; n < N; n++) {
//                List<Task<Void>> tasks = new ArrayList<>();
//                for (int m = 0; m < M; m++) {
//                    if (!iterator.hasNext())
//                        break;
//                    final Class clazz = iterator.next();
//                    final StudentBlockStatsSubclassInterface inter = subclasses.get(clazz);
//                    final String className = inter.getClassName();
//                    tasks.add(getOrCreateAndIncrement(inter, category, className, clazz, correct));
//                }
//                try {
//                    Task.whenAll(tasks).waitForCompletion();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            Log.d("time taken", String.valueOf(System.currentTimeMillis() - start));
//            return ParseObjectUtils.saveAllInBackground();
//        }
//        else {
            start = System.currentTimeMillis();
            final List<Task<Void>> tasks = new ArrayList<>();
            return Student.getStudentInBackground()
            .continueWithTask(new Continuation<Student, Task<Void>>() {
                @Override
                public Task<Void> then(Task<Student> task) throws Exception {
                    final Student student = task.getResult();
                    for (final StudentBlockStats instance : getSubclassInstances()) {
                        tasks.add(getOrCreateAndIncrement(instance, category, student, correct));
                        //getOrCreateAndIncrement(instance, category, student, correct).waitForCompletion();
                    }
                    return Task.whenAll(tasks)
                            .continueWithTask(new Continuation<Void, Task<Void>>() {
                                @Override
                                public Task<Void> then(Task<Void> task) throws Exception {
                                    Log.d("time taken", String.valueOf(System.currentTimeMillis() - start));
                                    return student.saveEventually();
//                                    return ParseObjectUtils.saveAllInBackground();
                                }
                            });
                }
            });
//        }
    }

    public static Task<Object> pinAllBlockStatsInBackground(final Student student) {
        return Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final List<ParseObject> blockStatsToPin = new ArrayList<>();
                for (final StudentBlockStats instance : getSubclassInstances()) {
                    try {
                        List<ParseObject> found = instance.getRelationQuery(student).find();
                        blockStatsToPin.addAll(found);
                    } catch (ParseException e) {
                    }
                }
                return ParseObject.pinAllInBackground(Constants.PinNames.BlockStats, blockStatsToPin);
//                return ParseObjectUtils.pinAllInBackground(Constants.PinNames.BlockStats, blockStatsToPin);
            }
        });
    }

    private static Task<Void> getOrCreateAndIncrement(final StudentBlockStats instance, final String category,
                                                       final Student student, final boolean correct) {

        return instance.getCurrentBlockStats(category)
        .fromLocalDatastore()
        .getFirstInBackground()
        .continueWithTask(new Continuation<ParseObject, Task<Void>>() {
            @Override
            public Task<Void> then(Task<ParseObject> task) throws Exception {
                StudentBlockStats blockStats = (task.getResult() == null) ? null : (StudentBlockStats) task.getResult();
                return createIfNecessaryAndIncrement(blockStats, instance.getClass(), student, category, correct);
            }
        });
    }

    private static Task<Void> createIfNecessaryAndIncrement(StudentBlockStats existed, Class<? extends StudentBlockStats> clazz,
                                                      final Student student, String category, boolean correct) {
        if(existed == null) {
            final StudentBlockStats created = createInstance(clazz);
            created.initFields(category);
            created.increment(correct);
            return created.saveInBackground().continueWith(new Continuation<Void, Void>() {
                @Override
                public Void then(Task<Void> task) throws Exception {
                    created.pinInBackground(Constants.PinNames.BlockStats);
                    addBlockStatsToRelation(student, created);
                    return null;
//                    ParseObjectUtils.pinInBackground(Constants.PinNames.BlockStats, created);
                }
            });
        }
        else {
            //Increment operations are performed atomically in Parse. When followed by saveInBackground(), the effect
            //is incrementAndGet()
            existed.increment(correct);
            return existed.saveInBackground();
        }
    }

    protected void increment(boolean correct) {
        increment(SuperColumns.total);
        if(correct)
            increment(SuperColumns.correct);
        Log.i("total", Integer.toString(getInt(SuperColumns.total)));
    }

    private static void addBlockStatsToRelation(final Student student, final StudentBlockStats blockStats) {
        student.getStudentBlockStatsRelation(blockStats.getClass()).add(blockStats);
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

    private static Date testDate;
    public static void setTestDate(Date date) { testDate = date; }

    protected static ParseQuery<ParseObject> getCurrentDayStats(ParseQuery<ParseObject> query) {
        int blockNum = testDate == null ? DateUtils.getCurrentDayBlockNum() : DateUtils.getDayBlockNum(testDate);
        return query.whereEqualTo(SuperColumns.blockNum, blockNum);
    }

    protected static ParseQuery<ParseObject> getCurrentMonthStats(ParseQuery<ParseObject> query) {
        if(testDate == null)
            return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentMonthBlockNum());
        else
            return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getMonthBlockNum(testDate));
    }

    protected static ParseQuery<ParseObject> getCurrentTridayStats(ParseQuery<ParseObject> query) {
        if(testDate == null)
            return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentTridayBlockNum());
        else
            return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getTridayBlockNum(testDate));
    }

    protected void setDayBlockNum() {
        if(testDate == null)
            doSetBlockNum(DateUtils.getCurrentDayBlockNum());
        else
            doSetBlockNum(DateUtils.getDayBlockNum(testDate));
    }

    protected void setTridayBlockNum() {
        if(testDate == null)
            doSetBlockNum(DateUtils.getCurrentTridayBlockNum());
        else
            doSetBlockNum(DateUtils.getTridayBlockNum(testDate));
    }

    protected void setMonthBlockNum() {
        if(testDate == null)
            doSetBlockNum(DateUtils.getCurrentMonthBlockNum());
        else
            doSetBlockNum(DateUtils.getMonthBlockNum(testDate));
    }

    private void doSetBlockNum(int num) {
        put(SuperColumns.blockNum, num);
    }

    @Override
    public String toString() {
        String s = "";
        HashMap<String, String> map = new HashMap<>();
        map.put(SuperColumns.blockNum, String.format("%2d", getBlockNum()));
        map.put("objectId", getObjectId());
        for(String key : map.keySet()) {
            s += key + ": " + map.get(key) + " | ";
        }
        s += String.valueOf(getCorrect()) + "/" + String.valueOf(getTotal());
        return s;
    }
}