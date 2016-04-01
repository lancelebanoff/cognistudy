package com.cognitutor.cognistudyapp.Custom;

import android.util.Log;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionIds;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Student;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentCategoryRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
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

    public static Task<Void> pinCurrentUser() throws ParseException {

        String colPrivateStudentData = PublicUserData.Columns.student + "." + Student.Columns.privateStudentData;
        final PublicUserData publicUserData = PublicUserData.getQuery()
                .whereEqualTo(PublicUserData.Columns.baseUserId, UserUtils.getCurrentUserId())
                .include(colPrivateStudentData + "." + PrivateStudentData.Columns.friends)
//                .include(colPrivateStudentData + "." + PrivateStudentData.Columns.assignedQuestions)
                .getFirst();
        final Student student = publicUserData.getStudent();
        return publicUserData.pinInBackground().continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) {
                if (task.getError() != null) {
                    task.getError().printStackTrace();
                }
                pinBookmarksInBackground();
                pinSuggestedQuestionsInBackground();
                pinChallengeResponsesInBackground();
                return pinRollingStatsInBackground(student);
            }
        }).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                if (task.getError() != null) {
                    task.getError().printStackTrace();
                }
                return StudentBlockStats.pinAllBlockStatsInBackground(student);
            }
        });
    }

    private static Task<Object> pinChallengeResponsesInBackground() {
        return Challenge.getAllChallengesForUserInBackground().continueWith(new Continuation<List<Challenge>, Object>() {
            @Override
            public Object then(Task<List<Challenge>> task) throws Exception {
                for(Challenge challenge : task.getResult()) {
                    final String challengeId = challenge.getObjectId();
                    final String questionCol = Response.Columns.question;
                    ParseRelation<Response> responseRelation = challenge.getCurUserChallengeUserData().getResponses();
                    responseRelation.getQuery()
                        .include(questionCol)
                        .include(questionCol + "." + Question.Columns.bundle)
                        .findInBackground().continueWith(new Continuation<List<Response>, Object>() {
                        @Override
                        public Object then(Task<List<Response>> task) throws Exception {
                            Log.d("pinning challenge " + challengeId, task.getResult().size() + " questions in relation");
                            ParseObject.pinAllInBackground(challengeId, task.getResult());
                            return null;
                        }
                    });
                    challenge.pinInBackground(challengeId);
                }
                return null;
            }
        });
    }

    private static Task<Object> pinBookmarksInBackground() {
        final String questionCol = Bookmark.Columns.response + "." + Response.Columns.question;
        ParseQuery query = PrivateStudentData.getPrivateStudentData().getBookmarks().getQuery()
                .include(questionCol + "." + Question.Columns.bundle)
                .include(questionCol + "." + Question.Columns.questionContents);
        return pinWithObjectIdInBackground(query);
    }

    private static Task<Object> pinSuggestedQuestionsInBackground() {
        final String questionCol = SuggestedQuestion.Columns.question;
        ParseQuery query = PrivateStudentData.getPrivateStudentData().getAssignedQuestions().getQuery()
//        ParseQuery query = PrivateStudentData.getPrivateStudentData().getRelation("blah").getQuery()
                .include(SuggestedQuestion.Columns.response)
                .include(questionCol)
                .include(questionCol + "." + Question.Columns.bundle);
//                .include(questionCol + "." + Question.Columns.questionContents);
        return pinWithObjectIdInBackground(query);
    }

    private static Task<Object> pinWithObjectIdInBackground(ParseQuery<ParseObject> query) {
        return query.findInBackground()
        .continueWith(new Continuation<List<ParseObject>, Object>() {
            @Override
            public Object then(Task<List<ParseObject>> task) throws Exception {
                if (task.isFaulted()) {
                    Log.e("pinWithObjId", task.getError().getMessage());
                }
                List<ParseObject> list = task.getResult();
                for (ParseObject obj : list) {
                    obj.pinInBackground(obj.getObjectId());
                }
                return null;
            }
        });
    }

    private static Task<Void> pinRollingStatsInBackground(final Student student) {

        return Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    student.fetchIfNeeded();
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("pinRollingStatsInBg", e.getMessage());
                    return null;
                }
                List<StudentTRollingStats> rollingStatsList = new ArrayList<>();
                List<AnsweredQuestionIds> answeredQuestionIdsList = new ArrayList<>();
                rollingStatsList.addAll(student.getStudentCategoryRollingStats());
                rollingStatsList.addAll(student.getStudentSubjectRollingStats());
                rollingStatsList.add(student.getStudentTotalRollingStats());
                for (StudentTRollingStats rollingStats : rollingStatsList) {
                    rollingStats.fetchIfNeededInBackground().waitForCompletion();
                    if (!(rollingStats instanceof StudentCategoryRollingStats))
                        continue; //Unnecessary if order of the code does not change
                    AnsweredQuestionIds answeredQuestionIds = ((StudentCategoryRollingStats) rollingStats).getAnsweredQuestionIds();
                    answeredQuestionIdsList.add(answeredQuestionIds);
                }
                //TODO: Change ParseObjectUtils.pin to ParseObject.pin (later)
                List<Task<Void>> tasks = new ArrayList<Task<Void>>();
                tasks.add(ParseObject.pinAllInBackground(rollingStatsList));
                tasks.add(ParseObject.pinAllInBackground(answeredQuestionIdsList));
                Task.whenAll(tasks).waitForCompletion();
                return null;
            }
        });
    }

    public static String getCurrentUserId() {
        return ParseUser.getCurrentUser().getObjectId();
    }
}
