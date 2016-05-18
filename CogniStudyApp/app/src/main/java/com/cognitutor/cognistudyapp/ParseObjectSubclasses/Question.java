package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("Question")
public class Question extends ParseObject {

    public Question() {}

    public static class Columns {
        public static final String subject = "subject";
        public static final String category = "category";
        public static final String inBundle = "inBundle";
        public static final String questionData = "questionData";
        public static final String questionContents = "questionContents";
        public static final String reviewStatus = "reviewStatus";
        public static final String bundle = "bundle";
        public static final String numberInBundle = "numberInBundle";
    }

    public Question(String subject, String category, String reviewStatus, QuestionContents contents, QuestionData data) {
        put(Columns.subject, subject);
        put(Columns.category, category);
        put(Columns.reviewStatus, reviewStatus);
        put(Columns.questionContents, contents);
        if(data != null)
            put(Columns.questionData, data);
    }

    public QuestionBundle getQuestionBundle() { return (QuestionBundle) getParseObject(Columns.bundle); }
    public String getSubject() { return getString(Columns.subject); }
    public String getCategory() { return getString(Columns.category); }
    public boolean inBundle() { return getBoolean(Columns.inBundle); }
    public QuestionContents getQuestionContents() { return (QuestionContents) getParseObject(Columns.questionContents); }
    public Task<QuestionContents> getQuestionContentsInBackground() {
        return ((QuestionContents) getParseObject(Columns.questionContents)).fetchIfNeededInBackground();
    }
    public int getNumberInBundle() { return getInt(Columns.numberInBundle); }

    public static ParseQuery<Question> getQuery() { return ParseQuery.getQuery(Question.class); }

    public static Task<Question> getQuestionWithContentsInBackground(final String pinName, final String questionId) {
        return QueryUtils.getFirstCacheElseNetworkInBackground(pinName, new QueryUtils.ParseQueryBuilder<Question>() {
            @Override
            public ParseQuery<Question> buildQuery() {
                return Question.getQuery()
                    .include(Columns.questionContents)
                    .include(Columns.bundle)
                    .whereEqualTo(Constants.ParseObjectColumns.objectId, questionId);
            }
        });
    }

    public static Task<List<String>> chooseThreeQuestionIds(final Challenge challenge, int user1or2, final Context context) {
        Task<List<String>> task = challenge.getChallengeUserData(user1or2).fetchIfNeededInBackground().continueWithTask(new Continuation<ParseObject, Task<List<Question>>>() {
            @Override
            public Task<List<Question>> then(Task<ParseObject> task) throws Exception {
                ChallengeUserData challengeUserData = (ChallengeUserData) task.getResult();

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("challengeUserDataId", challengeUserData.getObjectId());
                params.put("challengeId", challenge.getObjectId());
                params.put("skipBundles", PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.Settings.SKIP_BUNDLES, false));

                List<String> categories = challengeUserData.getCategories();
                int randomIndex = new Random().nextInt(categories.size());
                params.put("category", categories.get(randomIndex));

                return ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.CHOOSE_RANDOM_QUESTIONS, params);
            }
        }).continueWith(new Continuation<List<Question>, List<String>>() {
            @Override
            public List<String> then(Task<List<Question>> task) throws Exception {
                if(task.getError() != null) { task.getError().printStackTrace(); }

                List<Question> questions = task.getResult();
                try {
                    ParseObject.pinAllInBackground(challenge.getObjectId(), questions).waitForCompletion();
                } catch (InterruptedException e) { e.printStackTrace(); }
                List<String> questionIds = new ArrayList<String>();
                for (Question question : questions) {
                    questionIds.add(question.getObjectId());
                }
                challenge.setThisTurnQuestionIds(questionIds);
                challenge.setThisTurnQsAreBundle(questions.get(0).inBundle());
                try {
                    challenge.save();
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
                return questionIds;
            }
        });
        return task;
    }

    //TODO: Delete later
    public static void generateRandomQuestions() {

        int cat = 0;
        int lastCat = Constants.Category.getCategories().length;
        for(String category : Constants.Category.getCategories()) {
            cat++;
            Log.d("generateRandomQues", "====== Category " + cat + "/" + lastCat + ": " + category);
            for(int bun = 0; bun < 2; bun++) {
                Log.d("generateRandomQues", "==     Bundle " + (bun+1));
                final QuestionBundle bundle = new QuestionBundle(category + " bundle " + bun);
                final List<Question> list = new ArrayList<>();
                List<Task<Object>> tasks = new ArrayList<>();
                for(int ques = 0; ques < 3; ques++) {
                    Log.d("generateRandomQues", "       Bundle ques " + (ques + 1));
                    String text = category + " bundle " + bun + " question " + ques;
                    QuestionContents contents = createQuestionContents(category, text);
                    contents.saveEventually();
                    final Question question = createQuestion(category, contents);
                    question.put(Columns.inBundle, true);
                    question.put(Columns.bundle, bundle);
                    tasks.add(question.saveEventually().continueWithTask(new Continuation<Void, Task<Object>>() {
                        @Override
                        public Task<Object> then(Task<Void> task) throws Exception {
                            if (task.isFaulted()) {
                                Log.e("ques saveEventually", task.getError().getMessage());
                            }
                            list.add(question);
                            return CommonUtils.getCompletionTask(null);
                        }
                    }));
                }
                Task.whenAll(tasks).continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        bundle.put("test", true);
                        bundle.put(QuestionBundle.Columns.questions, list);
                        bundle.saveEventually().continueWith(new Continuation<Void, Object>() {
                            @Override
                            public Object then(Task<Void> task) throws Exception {
                                if(task.isFaulted()) {
                                    Log.e("bundle saveEventually", task.getError().getMessage());
                                }
                                return null;
                            }
                        });
                        return null;
                    }
                });
            }
            int numPerCat = 4;
            for(int i=0; i<numPerCat; i++) {
                Log.d("generateRandomQues", "       Reg ques " + (i+1));
                QuestionContents contents = createQuestionContents(category, category + " question " + i);
                Question question = createQuestion(category, contents);
                question.put(Columns.inBundle, false);
                contents.saveEventually();
                question.saveEventually();
            }
        }
    }

    private static Question createQuestion(String category, QuestionContents contents) {
        Question question = new Question(CommonUtils.getSubjectFromCategory(category), category,
                Constants.ReviewStatusType.APPROVED, contents, null);
        question.put("test", true);
        question.put("isActive", true);
        return question;
    }

    private static QuestionContents createQuestionContents(String category, String text) {
        List<String> answers = new ArrayList<>();
        for(int j=1; j<5; j++)
            answers.add("Answer" + j);
        QuestionContents contents = new QuestionContents(text, null, null, answers, 0, "Because it is");
        contents.put("test", true);
        return contents;
    }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() +
                " | questionContentsId: " + getQuestionContents().getObjectId() +
                " | subject: " + getSubject() +
                " | category: " + getCategory();
    }
}
