package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.ACLUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/18/2016.
 */
public abstract class StudentTRollingStats extends ParseObject{

    public class SuperColumns {
        public static final String baseUserId = "baseUserId";
        public static final String totalAllTime = "totalAllTime";
        public static final String correctAllTime = "correctAllTime";
        public static final String totalPastMonth = "totalPastMonth";
        public static final String correctPastMonth = "correctPastMonth";
        public static final String totalPastWeek = "totalPastWeek";
        public static final String correctPastWeek = "correctPastWeek";
    }

    public StudentTRollingStats() {}
    public StudentTRollingStats(String baseUserId) {
        put(SuperColumns.baseUserId, baseUserId);
        put(SuperColumns.totalAllTime, 0);
        put(SuperColumns.correctAllTime, 0);
        put(SuperColumns.totalPastMonth, 0);
        put(SuperColumns.correctPastMonth, 0);
        put(SuperColumns.totalPastWeek, 0);
        put(SuperColumns.correctPastWeek, 0);
        setACL(ACLUtils.getPublicReadPrivateWriteACL());
    }

    private static final List<Class<? extends StudentTRollingStats>> subclasses;
    static {
        List<Class<? extends StudentTRollingStats>> list = new ArrayList<>();
        list.add(StudentCategoryRollingStats.class);
        list.add(StudentSubjectRollingStats.class);
        list.add(StudentTotalRollingStats.class);
        subclasses = Collections.unmodifiableList(list);
    }

    public String getBaseUserId() { return getString(SuperColumns.baseUserId); }
    public int getTotalAllTime() { return getInt(SuperColumns.totalAllTime); }
    public int getCorrectAllTime() { return getInt(SuperColumns.correctAllTime); }
    public int getTotalPastMonth() { return getInt(SuperColumns.totalPastMonth); }
    public int getCorrectPastMonth() { return getInt(SuperColumns.correctPastMonth); }
    public int getTotalPastWeek() { return getInt(SuperColumns.totalPastWeek); }
    public int getCorrectPastWeek() { return getInt(SuperColumns.correctPastWeek); }

    @SuppressWarnings("unchecked")
    public static void setPublicAnalyticsInBackground(final boolean isPublic) {
        for(Class clazz : subclasses) {
            for(String category : Constants.getAllConstants(Constants.Category.class)) {
                getCacheElseNetworkInBackground(clazz, category)
                .continueWith(new Continuation<StudentTRollingStats, Object>() {
                    @Override
                    public Object then(Task<StudentTRollingStats> task) throws Exception {
                        //TODO: Handle null result?
                        StudentTRollingStats stats = task.getResult();
                        stats.getACL().setPublicReadAccess(isPublic);
                        if(!isPublic) {
                            //TODO: Allow tutors read access
                        }
                        return null;
                    }
                });
            }
        }
    }

    /**
     *
     * @param questionId
     * @param category
     * @param correct
     */
    public static void incrementAllInBackground(String questionId, String category, boolean correct) {
        for(Class clazz : subclasses) {
            incrementSubclassInBackground(questionId, clazz, category, correct);
        }
    }

    private static void incrementSubclassInBackground(final String questionId,
                                  final Class<? extends StudentTRollingStats> clazz, final String category, final boolean correct) {

        getCacheElseNetworkInBackground(clazz, category)
        .continueWith(new Continuation<StudentTRollingStats, Object>() {
            @Override
            public Object then(Task<StudentTRollingStats> task) throws Exception {
                //TODO: Handle null result?
                StudentTRollingStats stats = task.getResult();
                if(stats instanceof StudentCategoryRollingStats) {
                    ((StudentCategoryRollingStats) stats).addAnsweredQuestionIdAndSaveEventually(questionId);
                }
                stats.doIncrementAndSaveEventually(correct);
                return null;
            }
        });
    }

    private void doIncrementAndSaveEventually(boolean correct) {
        String[] totalColumns = { SuperColumns.totalAllTime, SuperColumns.totalPastMonth, SuperColumns.totalPastWeek };
        String[] correctColumns = { SuperColumns.correctAllTime, SuperColumns.correctPastMonth, SuperColumns.correctPastWeek };
        for(String totalColumn : totalColumns) {
            increment(totalColumn);
        }
        if(correct) {
            for(String correctColumn : correctColumns) {
                increment(correctColumn);
            }
        }
        saveEventually();
    }

    private static <T extends StudentTRollingStats> ParseQuery<StudentTRollingStats> getQuery(Class<T> clazz) {
        return ParseQuery.getQuery(clazz.getSimpleName());
    }

    private static <T extends StudentTRollingStats> ParseQuery<StudentTRollingStats> getUserQuery(Class<T> clazz, String baseUserId) {
        return getQuery(clazz)
                .whereEqualTo(SuperColumns.baseUserId, baseUserId);
    }

    private static <T extends StudentTRollingStats> ParseQuery<StudentTRollingStats> getStatsQuery(
            Class<T> clazz, String baseUserId, String category) {

        ParseQuery<StudentTRollingStats> query = getUserQuery(clazz, baseUserId);
        if(clazz == StudentTotalRollingStats.class) {
            return query;
        }
        else if(clazz == StudentCategoryRollingStats.class) {
            return query.whereEqualTo(StudentCategoryRollingStats.Columns.category, category);
        }
        else if(clazz == StudentSubjectRollingStats.class) {
            String subject = CommonUtils.getSubjectFromCategory(category);
            return query.whereEqualTo(StudentSubjectRollingStats.Columns.subject, subject);
        }
        return null;
    }

    private static <T extends StudentTRollingStats> ParseQuery<StudentTRollingStats> getCurrentUserStatsQuery(Class<T> clazz, String category) {
        return getStatsQuery(clazz, UserUtils.getCurrentUserId(), category);
    }

    public static void updateAllCacheElseNetworkInBackground() {
        for(String category : Constants.Category.getCategories()) {
            updateCacheElseNetworkInBackground(StudentCategoryRollingStats.class, category);
        }
        for(String subject : Constants.Subject.getSubjects()) {
            String category = Constants.SubjectToCategory.get(subject)[0];
            updateCacheElseNetworkInBackground(StudentSubjectRollingStats.class, category);
        }
        updateCacheElseNetworkInBackground(StudentTotalRollingStats.class, null);
    }

    private static Task<StudentTRollingStats> updateCacheElseNetworkInBackground(final Class<? extends StudentTRollingStats> clazz, final String category) {
        return QueryUtils.getFirstPinElseNetworkInBackground(Constants.PinNames.CurrentUser,
                new QueryUtils.ParseQueryBuilder<StudentTRollingStats>() {
                    @Override
                    public ParseQuery<StudentTRollingStats> buildQuery() {
                        return getCurrentUserStatsQuery(clazz, category)
                                .whereGreaterThanOrEqualTo(Constants.ParseObjectColumns.updatedAt, DateUtils.getMidnightOfToday());
                    }
                });
    }

    public static Task<StudentTRollingStats> getCacheElseNetworkInBackground(final Class<? extends StudentTRollingStats> clazz, final String category) {
        return QueryUtils.getFirstPinElseNetworkInBackground(Constants.PinNames.CurrentUser,
                new QueryUtils.ParseQueryBuilder<StudentTRollingStats>() {
                    @Override
                    public ParseQuery<StudentTRollingStats> buildQuery() {
                        return getCurrentUserStatsQuery(clazz, category);
                    }
                });
    }

    public static StudentTRollingStats getCacheElseNetwork(final Class<? extends StudentTRollingStats> clazz, final String category) {
        return QueryUtils.getFirstPinElseNetwork(Constants.PinNames.CurrentUser,
                new QueryUtils.ParseQueryBuilder<StudentTRollingStats>() {
            @Override
            public ParseQuery<StudentTRollingStats> buildQuery() {
                return getCurrentUserStatsQuery(clazz, category);
            }
        });
    }

    public static Task<StudentTRollingStats> getOtherUserStatsInBackground(Class<? extends StudentTRollingStats> clazz, final String baseUserID, final String category) {
        return getStatsQuery(clazz, baseUserID, category)
                .getFirstInBackground();
    }

    public static StudentTRollingStats getOtherUserStats(Class<? extends StudentTRollingStats> clazz, final String baseUserID, final String category) {
        try {
            return getStatsQuery(clazz, baseUserID, category)
                    .getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("getOtherUserStats", e.getMessage());
            return null;
        }
    }

    @Override
    public String toString() {
        return "AllTime: " + getCorrectAllTime() + "/" + getTotalAllTime() + " | " +
                "PastMonth: " + getCorrectPastMonth() + "/" + getTotalPastMonth() + " | " +
                "PastWeek: " + getCorrectPastWeek() + "/" + getTotalPastWeek() + " | " +
                "objectId: " + getObjectId();
    }
}
