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
import java.util.Map;
import java.util.concurrent.Callable;

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
        ParseQuery<StudentBlockStats> getCurrentUserCurrentStats(Student student, String category);
        ParseQuery<StudentBlockStats> getAllCurrentUserStats(Student student, String category);
    }

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

    public static Task<Boolean> incrementAll(final String category, final boolean correct) {

        final Map<Class<? extends StudentBlockStats>, StudentBlockStatsSubclassInterface> subclasses = getSubclassesMap();

        final long start;
        ParallelOption option = ParallelOption.NONE;
        Iterator<Class<? extends StudentBlockStats>> iterator = subclasses.keySet().iterator();

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
            .continueWithTask(new Continuation<Student, Task<Boolean>>() {
                @Override
                public Task<Boolean> then(Task<Student> task) throws Exception {
                    Student student = task.getResult();
                    for (final Class clazz : subclasses.keySet()) {
                        final StudentBlockStatsSubclassInterface inter = subclasses.get(clazz);
                        tasks.add(getOrCreateAndIncrement(inter, category, student, clazz, correct));
                    }
                    return Task.whenAll(tasks)
                            .continueWithTask(new Continuation<Void, Task<Boolean>>() {
                                @Override
                                public Task<Boolean> then(Task<Void> task) throws Exception {
                                    Log.d("time taken", String.valueOf(System.currentTimeMillis() - start));
                                    return ParseObjectUtils.saveAllInBackground();
                                }
                            });
                }
            });
//        }
    }

    public static Task<Object> pinAllCurrentStatsInBackground(final Student student) {
        return Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final List<ParseObject> blockStatsToPin = new ArrayList<>();
                for (final Class clazz : subclasses.keySet()) {
                    final StudentBlockStatsSubclassInterface inter = subclasses.get(clazz);
                    for (String category : Constants.getAllConstants(Constants.Category.class)) {
                        try {
                            StudentBlockStats blockStats = inter.getCurrentUserCurrentStats(student, category).getFirst();
                            blockStatsToPin.add(blockStats);
                        } catch (ParseException e) {
                        }
                    }
                }
                ParseObjectUtils.pinAll(Constants.PinNames.CurrentUser, blockStatsToPin);
                return null;
            }
        });
    }

    private static Task<Void> getOrCreateAndIncrement(final StudentBlockStatsSubclassInterface inter, final String category,
                                                       final Student student, final Class clazz, final boolean correct) {

        return inter.getCurrentUserCurrentStats(student, category)
        .fromPin(Constants.PinNames.CurrentUser)
        .getFirstInBackground()
        .continueWith(new Continuation<StudentBlockStats, Void>() {
            @Override
            public Void then(Task<StudentBlockStats> task) throws Exception {
                createIfNecessaryAndIncrement(task.getResult(), student, clazz, category,
                        inter.getAllCurrentUserStats(student, category), correct);
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

    public static List<Class<? extends StudentBlockStats>> getStudentBlockStatsSubclasses() {
        return ParseObjectUtils.convertSetToList(getSubclassesMap().keySet());
    }

    protected void increment(boolean correct) {
        increment(SuperColumns.total);
        if(correct)
            increment(SuperColumns.correct);
        Log.i("total", Integer.toString(getInt(SuperColumns.total)));
    }

    private static void createIfNecessaryAndIncrement(StudentBlockStats blockStats, Student student, Class clazz, String category,
                                                      ParseQuery deletePinQuery, boolean correct) {
        if(blockStats == null) {
            blockStats = createInstance(clazz);
            blockStats.initFields(category);
            ParseObjectUtils.unpinAllInBackground(deletePinQuery);
            blockStats.increment(correct);
            addBlockStatsToRelation(student, clazz, blockStats);
            ParseObjectUtils.addToSaveThenPinQueue(Constants.PinNames.CurrentUser, blockStats);
        }
        else {
            //Increment operations are performed atomically in Parse. When followed by saveInBackground(), the effect
            //is incrementAndGet()
            blockStats.increment(correct);
            blockStats.saveInBackground();
        }
    }

    private static <T extends StudentBlockStats> void addBlockStatsToRelation(final Student student,
                                                                                      final Class<T> clazz, final T blockStats) {
        student.getStudentBlockStatsRelation(clazz).add(blockStats);
        student.saveEventually();
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

//    protected static ParseQuery getCurrentUserQuery(String className) {
//        return ParseQuery.getQuery(className)
//                .whereEqualTo(SuperColumns.baseUserId, UserUtils.getCurrentUserId());
//    }

    private static Date testDate;
    public static void setTestDate(Date date) { testDate = date; }

    protected static ParseQuery<StudentBlockStats> getCurrentDayStats(ParseQuery<StudentBlockStats> query) {
        int blockNum = testDate == null ? DateUtils.getCurrentDayBlockNum() : DateUtils.getDayBlockNum(testDate);
        return query.whereEqualTo(SuperColumns.blockNum, blockNum);
    }

    protected static ParseQuery<StudentBlockStats> getCurrentMonthStats(ParseQuery<StudentBlockStats> query) {
        if(testDate == null)
            return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getCurrentMonthBlockNum());
        else
            return query.whereEqualTo(SuperColumns.blockNum, DateUtils.getMonthBlockNum(testDate));
    }

    protected static ParseQuery<StudentBlockStats> getCurrentTridayStats(ParseQuery<StudentBlockStats> query) {
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