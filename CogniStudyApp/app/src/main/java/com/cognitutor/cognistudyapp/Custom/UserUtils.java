package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionIds;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Task;

/**
 * Created by Kevin on 1/7/2016.
 */
public class UserUtils {

    private static boolean userLoggedIn;

    public static void setUserLoggedIn(boolean val) {
        userLoggedIn = val;
    }

    public static boolean isUserLoggedIn() { return userLoggedIn; }

    public static PublicUserData getPublicUserData() throws ParseException {
        return (PublicUserData) ParseUser.getCurrentUser().getParseObject("publicUserData").fetchIfNeeded();
    }

    public static Student getStudent() throws ParseException {
        return getPublicUserData().getStudent();
    }

    public static void pinCurrentUser() throws ParseException {
        PublicUserData publicUserData = PublicUserData.getQuery()
                .whereEqualTo(PublicUserData.Columns.baseUserId, UserUtils.getCurrentUserId())
                .include(PublicUserData.Columns.student + "." + Student.Columns.privateStudentData)
                .getFirst();
        ParseObjectUtils.pin(Constants.PinNames.CurrentUser, publicUserData);
        Student student = publicUserData.getStudent();
        pinRollingStatsInBackground(student);
        StudentBlockStats.pinAllBlockStatsInBackground(student);
    }

    private static Task<Boolean> pinRollingStatsInBackground(final Student student) {

        return Task.callInBackground(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    student.fetchIfNeeded();
                } catch (ParseException e) { e.printStackTrace(); Log.e("pinRollingStatsInBg", e.getMessage()); return false; }
                List<StudentTRollingStats> rollingStatsList = new ArrayList<>();
                List<AnsweredQuestionIds> answeredQuestionIdsList = new ArrayList<>();
                rollingStatsList.addAll(student.getStudentCategoryRollingStats());
                rollingStatsList.addAll(student.getStudentSubjectRollingStats());
                rollingStatsList.add(student.getStudentTotalRollingStats());
                for(StudentTRollingStats rollingStats : rollingStatsList) {
                    rollingStats.fetchIfNeeded();
                    if(!(rollingStats instanceof StudentCategoryRollingStats)) continue; //Unnecessary if order of the code does not change
                    AnsweredQuestionIds answeredQuestionIds = ((StudentCategoryRollingStats) rollingStats).getAnsweredQuestionIds();
                    answeredQuestionIdsList.add(answeredQuestionIds);
                }
                //TODO: Change ParseObjectUtils.pin to ParseObject.pin (later)
                ParseObjectUtils.pinAllInBackground(Constants.PinNames.CurrentUser, rollingStatsList);
                ParseObjectUtils.pinAllInBackground(Constants.PinNames.CurrentUser, answeredQuestionIdsList);
                return true;
            }
        });
    }

    public static String getCurrentUserId() {
        return ParseUser.getCurrentUser().getObjectId();
    }
}
