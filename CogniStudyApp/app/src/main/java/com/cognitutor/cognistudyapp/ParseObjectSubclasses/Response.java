package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ACLUtils;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("Response")
public class Response extends QuestionMetaObject {

    @Override
    public String getResponseStatus() {
        return getCorrect() ? Constants.ResponseStatusType.CORRECT : Constants.ResponseStatusType.INCORRECT;
    }

    @Override
    public String getSubject() {
//        return getString(Columns.subject); //This column was added to Response after some Responses were already made
        return getQuestion().getString(Question.Columns.subject);
    }

    @Override
    public String getCategory() {
//        return getString(Columns.category); //This column was added to Response after some Responses were already made
        return getQuestion().getString(Question.Columns.category);
    }

    @Override
    public String getQuestionId() {
        return getQuestion().getObjectId();
    }

    @Override
    public String getResponseId() {
        return getObjectId();
    }

    public static class Columns {
        public static final String baseUserID = "baseUserId";
        public static final String question = "question";
        public static final String correct = "correct";
        public static final String selectedAnswer = "selectedAnswer";
        public static final String rating = "rating";
        public static final String category = "category";
        public static final String subject = "subject";
    }

    public Response() {}

    public Response(Question question, boolean correct, int selectedAnswer, String rating) {
        put(Columns.baseUserID, ParseUser.getCurrentUser().getObjectId());
        put(Columns.question, question);
        put(Columns.category, question.getCategory());
        put(Columns.subject, question.getSubject());
        put(Columns.correct, correct);
        put(Columns.selectedAnswer, selectedAnswer);
        put(Columns.rating, rating);
    }

    public String getBaseUserId() { return getString(Columns.baseUserID); }
    public boolean getCorrect() { return getBoolean(Columns.correct); }
    public int getSelectedAnswer() { return getInt(Columns.selectedAnswer); }
    public String getRating() { return getString(Columns.rating); }
    public Question getQuestion() { return (Question) getParseObject(Columns.question); }

    public static ParseQuery<Response> getQuery() { return ParseQuery.getQuery(Response.class); }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() +
                " | questionId: " + getQuestion().getObjectId() +
                " | correct: " + getCorrect() +
                " | selected: " + getSelectedAnswer();
    }
}
