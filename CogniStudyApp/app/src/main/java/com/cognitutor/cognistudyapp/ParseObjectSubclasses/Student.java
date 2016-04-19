package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ACLUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
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
        public static final String studentTotalDayStats = "studentTotalDayStats";
        public static final String studentTotalTridayStats = "studentTotalTridayStats";
        public static final String studentTotalMonthStats = "studentTotalMonthStats";
        public static final String tutorialProgress = "tutorialProgress";
        public static final String skipBundles = "skipBundles";
        public static final String notificationsEnabled = "notificationsEnabled";
    }

    public Student() {}
    public Student(ParseUser user, PrivateStudentData privateStudentData) {
        setACL(ACLUtils.getPublicReadPrivateWriteACL());

        String baseUserId = user.getObjectId();

        put(Columns.privateStudentData, privateStudentData);
        put(Columns.achievements, new ArrayList<ParseObject>());
        put(Columns.shopItemsBought, new ArrayList<ParseObject>());
        put(Columns.skinSelections, new ArrayList<ParseObject>());
        put(Columns.tutorialProgress, new ArrayList<String>());
        setRandomEnabled(true);
        setPublicAnalytics(true);
        put(Columns.baseUserId, baseUserId);
        createStudentSubjectRollingStats(baseUserId);
        createStudentCategoryRollingStats(baseUserId);
        createStudentTotalRollingStats(baseUserId);
        setSkipBundles(false);
        setNotificationsEnabled(true);
    }

    private void createStudentSubjectRollingStats(String baseUserId) {
        ArrayList<StudentSubjectRollingStats> array = new ArrayList<>();
        for(String subject : Constants.Subject.getSubjects()) {
            array.add(new StudentSubjectRollingStats(baseUserId, subject));
        }
        put(Columns.studentSubjectRollingStats, array);
    }

    private void createStudentCategoryRollingStats(String baseUserId) {
        ArrayList<StudentCategoryRollingStats> array = new ArrayList<>();
        for(String category : Constants.Category.getCategories()) {
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
    public PrivateStudentData getPrivateStudentData() { return (PrivateStudentData) getParseObject(Columns.privateStudentData); }
    public void resetTutorialProgress() { put(Columns.tutorialProgress, new ArrayList<String>()); }
    public boolean tutorialProgressContains(String dialogLabel) { return getTutorialProgress().contains(dialogLabel); }
    public List<String> getTutorialProgress() { return getList(Columns.tutorialProgress); }
    public void addToTutorialProgress(String label) { add(Columns.tutorialProgress, label); }
    public void setSkipBundles(boolean skipBundles) { put(Columns.skipBundles, skipBundles); }
    public boolean getSkipBundles() { return getBoolean(Columns.skipBundles); }
    public void setNotificationsEnabled(boolean notificationsEnabled) { put(Columns.notificationsEnabled, notificationsEnabled); }
    public boolean getNotificationsEnabled() { return getBoolean(Columns.notificationsEnabled); }

    public static ParseQuery<Student> getQuery() {
        return ParseQuery.getQuery(Student.class);
    }

    public static Task<Student> getStudentInBackground() {
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<Student>() {
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

    public ParseQuery getBlockStatsQuery(Student student, final Class<? extends StudentBlockStats> clazz) {
        return student.getStudentBlockStatsRelation(clazz).getQuery();
    }

    public ParseRelation getStudentBlockStatsRelation(Class<? extends StudentBlockStats> clazz) {
        String className = clazz.getSimpleName();
        String columnName = className.substring(0, 1).toLowerCase() + className.substring(1);
        return getRelation(columnName);
    }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() + " | baseUserId: " + getBaseUserId();
    }
}