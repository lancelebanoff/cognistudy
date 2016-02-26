package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ACLUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/7/2016.
 */
@ParseClassName("Student")
public class Student extends ParseObject{

    public class Columns {
        public static final String achievements = "achievements";
        public static final String shopItemsBought = "shopItemsBought";
        public static final String skinSelections = "skinSelections";
        public static final String randomEnabled = "randomEnabled";
        public static final String publicAnalytics = "publicAnalytics";
        public static final String baseUserId = "baseUserId";
        public static final String studentCategoryRollingStats = "studentCategoryRollingStats";
        public static final String studentSubjectRollingStats = "studentSubjectRollingStats";
        public static final String studentTotalRollingStats = "studentTotalRollingStats";
        public static final String privateStudentData = "privateStudentData";
        public static final String studentCategoryDayStats = "studentCategoryDayStats";
        public static final String studentCategoryTridayStats = "studentCategoryTridayStats";
        public static final String studentCategoryMonthStats = "studentCategoryMonthStats";
        public static final String studentSubjectDayStats = "studentSubjectDayStats";
        public static final String studentSubjectTridayStats = "studentSubjectTridayStats";
        public static final String studentSubjectMonthStats = "studentSubjectMonthStats";
    }

    public Student() {}
    public Student(ParseUser user, PrivateStudentData privateStudentData) {
        setACL(ACLUtils.getPublicReadPrivateWriteACL());

        String baseUserId = user.getObjectId();

        put(Columns.privateStudentData, privateStudentData);
        put(Columns.achievements, new ArrayList<ParseObject>());
        put(Columns.shopItemsBought, new ArrayList<ParseObject>());
        put(Columns.skinSelections, new ArrayList<ParseObject>());
        setRandomEnabled(true);
        setPublicAnalytics(true);
        put(Columns.baseUserId, baseUserId);
        createStudentSubjectRollingStats(baseUserId);
        createStudentCategoryRollingStats(baseUserId);
        createStudentTotalRollingStats(baseUserId);
    }

    private void createStudentSubjectRollingStats(String baseUserId) {
        ArrayList<StudentSubjectRollingStats> array = new ArrayList<>();
        for(String subject : Constants.getAllConstants(Constants.Subject.class)) {
            array.add(new StudentSubjectRollingStats(baseUserId, subject));
        }
        put(Columns.studentSubjectRollingStats, array);
    }

    private void createStudentCategoryRollingStats(String baseUserId) {
        ArrayList<StudentCategoryRollingStats> array = new ArrayList<>();
        for(String category : Constants.getAllConstants(Constants.Category.class)) {
            array.add(new StudentCategoryRollingStats(baseUserId, category));
        }
        put(Columns.studentCategoryRollingStats, array);
    }

    private void createStudentTotalRollingStats(String baseUserId) {
        StudentTotalRollingStats stats = new StudentTotalRollingStats(baseUserId);
        put(Columns.studentTotalRollingStats, stats);
    }

    public List<Achievement> getAchievements() { return getList(Columns.achievements); }
    public List<String> getShopItemsBought() { return getList(Columns.shopItemsBought); }
    public List<SkinSelection> getSkinSelections() { return getList(Columns.skinSelections); }
    public void setSkinSelection() {
        //TODO: Write this method
    }
    public void setRandomEnabled(boolean val) { put(Columns.randomEnabled, val); }
    public boolean getRandomEnabled() { return getBoolean(Columns.randomEnabled); }
    public void setPublicAnalytics(boolean val) { put(Columns.publicAnalytics, val); }
    public boolean getPublicAnalytics() { return getBoolean(Columns.publicAnalytics); }
    public String getBaseUserId() { return getString(Columns.baseUserId); }
    public List<StudentCategoryRollingStats> getStudentCategoryRollingStats() { return getList(Columns.studentCategoryRollingStats); }
    public List<StudentSubjectRollingStats> getStudentSubjectRollingStats() { return getList(Columns.studentSubjectRollingStats); }
    public StudentTotalRollingStats getStudentTotalRollingStats() { return (StudentTotalRollingStats) getParseObject(Columns.studentTotalRollingStats); }

    public static ParseQuery<Student> getQuery() {
        return ParseQuery.getQuery(Student.class);
    }

    public static Task<Student> getStudentInBackground() {
        return QueryUtils.getFirstPinElseNetworkInBackground(Constants.PinNames.CurrentUser, new QueryUtils.ParseQueryBuilder<Student>() {
            @Override
            public ParseQuery<Student> buildQuery() {
                return Student.getQuery().whereEqualTo(Columns.baseUserId, UserUtils.getCurrentUserId());
            }
        });
    }

    public static Task<Student> getStudentInBackground(final String baseUserId) {
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<Student>() {
            @Override
            public ParseQuery<Student> buildQuery() {
                return Student.getQuery().whereEqualTo(Columns.baseUserId, baseUserId);
            }
        });
    }

    public static Task<ParseQuery> getBlockStatsQueryInBackground(final Class<? extends StudentBlockStats> clazz) {
        return getStudentInBackground()
                .continueWith(new Continuation<Student, ParseQuery>() {
                    @Override
                    public ParseQuery then(Task<Student> task) throws Exception {
                        Student student = task.getResult();
                        return student.getBlockStatsQuery(student, clazz);
                    }
                });
    }

    public <T extends StudentBlockStats> ParseQuery<T> getBlockStatsQuery(Student student, final Class<T> clazz) {
        return student.getStudentBlockStatsRelation(clazz).getQuery();
    }

    public <T extends StudentBlockStats> ParseRelation<T> getStudentBlockStatsRelation(Class<T> clazz) {
        String className = clazz.getSimpleName();
        String columnName = className.substring(0, 1).toLowerCase() + className.substring(1);
        return getRelation(columnName);
    }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() + " | baseUserId: " + getBaseUserId();
    }
}