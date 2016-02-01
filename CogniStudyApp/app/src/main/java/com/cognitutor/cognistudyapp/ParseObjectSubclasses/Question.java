package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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
        public static final String isBundle = "isBundle";
        public static final String questionData = "questionData";
        public static final String questionContents = "questionContents";
        public static final String reviewStatus = "reviewStatus";
    }

    public Question(String subject, String category, String reviewStatus, QuestionContents contents, QuestionData data) {
        put(Columns.subject, subject);
        put(Columns.category, category);
        put(Columns.reviewStatus, reviewStatus);
        put(Columns.questionContents, contents);
        put(Columns.questionData, data);
    }

    public String getSubject() { return getString(Columns.subject); }
    public String getCategory() { return getString(Columns.category); }
    public boolean isBundle() { return getBoolean(Columns.isBundle); }
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
}
