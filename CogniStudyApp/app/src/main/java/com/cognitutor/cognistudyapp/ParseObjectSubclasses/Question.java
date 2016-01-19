package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("Question")
public class Question extends ParseObject {

    public Question() {}

    public static class Columns {
        public static final String subject = "subject";
        public static final String category = "category";
        public static final String hasPassage = "hasPassage";
        public static final String questionData = "questionData";
        public static final String questionContents = "QuestionContents";
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
    public boolean hasPassage() { return getBoolean(Columns.hasPassage); }
    public QuestionContents getQuestionContents() { return (QuestionContents) getParseObject(Columns.questionContents); }

    public ParseQuery<Question> getQuery() { return ParseQuery.getQuery(Question.class); }
}
