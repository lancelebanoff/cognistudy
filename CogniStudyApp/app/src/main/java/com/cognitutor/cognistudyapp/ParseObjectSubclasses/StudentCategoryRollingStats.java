package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import bolts.Task;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentCategoryRollingStats")
public class StudentCategoryRollingStats extends StudentTRollingStats {

    public class Columns {
        public static final String category = "category";
        public static final String answeredQuestionIds = "answeredQuestionIds";
    }

    public StudentCategoryRollingStats() {}

    /**
     * Calls saveInBackground() after creation
     * @param baseUserId
     * @param category
     */
    public StudentCategoryRollingStats(String baseUserId, String category) {
        super(baseUserId);
        put(Columns.category, category);
        saveInBackground();
    }

//    public static StudentCategoryRollingStats getCurrentUserStats() {
//        return null;
//    }
//
//    private static ParseQuery<StudentCategoryRollingStats> getQuery() { return ParseQuery.getQuery(StudentCategoryRollingStats.class); }
//
//    private static ParseQuery<StudentCategoryRollingStats> getUserQuery(String baseUserId) {
//        return getQuery()
//                .whereEqualTo(SuperColumns.baseUserId, baseUserId);
//    }
//
//    private static ParseQuery<StudentCategoryRollingStats> getStatsQuery(String baseUserId, String category) {
//        return getUserQuery(baseUserId)
//                .whereEqualTo(Columns.category, category);
//    }
//
//    private static ParseQuery<StudentCategoryRollingStats> getCurrentUserStatsQuery(String category) {
//        return getStatsQuery(UserUtils.getCurrentUserId(), category);
//    }
//
//    public static Task<StudentCategoryRollingStats> getCurrentUserStatsInBackground(final String category) {
//        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<StudentCategoryRollingStats>() {
//            @Override
//            public ParseQuery<StudentCategoryRollingStats> buildQuery() {
//                return getCurrentUserStatsQuery(category);
//            }
//        }, true);
//    }
//
//    public static Task<StudentCategoryRollingStats> getOtherUserStatsInBackground(final String baseUserID, final String category) {
//        return getStatsQuery(baseUserID, category)
//                .getFirstInBackground();
//    }
}
