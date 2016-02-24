package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
            return query.whereEqualTo(StudentSubjectRollingStats.Columns.subject, category);
        }
        else if(clazz == StudentSubjectRollingStats.class) {
            String subject = StudentSubjectBlockStats.getSubjectFromCategory(category);
            return query.whereEqualTo(StudentCategoryRollingStats.Columns.category, subject);
        }
        return null;
    }

    private static <T extends StudentTRollingStats> ParseQuery<StudentTRollingStats> getCurrentUserStatsQuery(Class<T> clazz, String category) {
        return getStatsQuery(clazz, UserUtils.getCurrentUserId(), category);
    }

    @SuppressWarnings("unchecked")
    public static <T extends StudentTRollingStats> Task<T> getCurrentUserStatsInBackground(final Class<T> clazz, final String category) {
        return (Task<T>) QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<StudentTRollingStats>() {
            @Override
            public ParseQuery<StudentTRollingStats> buildQuery() {
                return getCurrentUserStatsQuery(clazz, category);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends StudentTRollingStats> T getCurrentUserStats(final Class<T> clazz, final String category) {
        return (T) QueryUtils.getFirstCacheElseNetwork(new QueryUtils.ParseQueryBuilder<StudentTRollingStats>() {
            @Override
            public ParseQuery<StudentTRollingStats> buildQuery() {
                return getCurrentUserStatsQuery(clazz, category);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static <T extends StudentTRollingStats> Task<T> getOtherUserStatsInBackground(Class<T> clazz, final String baseUserID, final String category) {
        return (Task<T>) getStatsQuery(clazz, baseUserID, category)
                .getFirstInBackground();
    }

    @SuppressWarnings("unchecked")
    public static <T extends StudentTRollingStats> T getOtherUserStats(Class<T> clazz, final String baseUserID, final String category) {
        try {
            return (T) getStatsQuery(clazz, baseUserID, category)
                    .getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("getOtherUserStats", e.getMessage());
            return null;
        }
    }
}
