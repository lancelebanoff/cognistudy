package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
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
        return getQuestion().getSubject();
    }

    @Override
    public String getCategory() {
        return getQuestion().getCategory();
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
    }

    public Response() {}

    public Response(Question question, boolean correct, int selectedAnswer, String rating) {
        put(Columns.baseUserID, ParseUser.getCurrentUser().getObjectId());
        put(Columns.question, question);
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

    public static ParseQuery<QuestionMetaObject> getMetaQuery() { return ParseQuery.getQuery(Response.class.getSimpleName()); }

    public static ParseQuery<QuestionMetaObject> getChallengeResponsesFromLocal(String challengeId) {
        return getMetaQuery()
                .fromPin(challengeId);
    }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() +
                " | questionId: " + getQuestion().getObjectId() +
                " | correct: " + getCorrect() +
                " | selected: " + getSelectedAnswer();
    }
}
