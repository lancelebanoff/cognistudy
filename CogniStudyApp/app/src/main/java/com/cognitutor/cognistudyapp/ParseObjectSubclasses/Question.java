package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

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

    public static ParseQuery<Question> getQuery() { return ParseQuery.getQuery(Question.class); }

    public static Question getQuestionWithContents(String questionId) throws ParseException{
        return Question.getQuery()
            .include(Question.Columns.questionContents)
            .get(questionId);
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
}
