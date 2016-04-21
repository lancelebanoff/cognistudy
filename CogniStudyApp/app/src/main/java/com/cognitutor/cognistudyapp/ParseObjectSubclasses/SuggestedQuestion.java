package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import android.util.Log;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.Date;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/17/2016.
 */
@ParseClassName("SuggestedQuestion")
public class SuggestedQuestion extends QuestionMetaObject {


    public static final int NOT_ANSWERED = 0;
    public static final int ANSWERED = 1;

    @Override
    public String getResponseStatus() {
        Response response = getResponse();
        if(response == null) {
            return Constants.ResponseStatusType.UNANSWERED;
        }
        return response.getResponseStatus();
    }

    @Override
    public Date getDate() {
        return getUpdatedAt();
    }

    @Override
    public String getSubject() { return getString(Columns.subject); }

    @Override
    public String getCategory() {
        return getString(Columns.category);
    }

    @Override
    public String getResponseId() {
        Response response = getResponse();
        if (response == null) {
            return null;
        }
        return response.getObjectId();
    }

    @Override
    public String getQuestionId() {
        return getQuestion().getObjectId();
    }

    public class Columns {
        public static final String studentBaseUserId = "studentBaseUserId";
        public static final String question = "question";
        public static final String response = "response";
        public static final String answeredInt = "answeredInt";
        public static final String subject = "subject";
        public static final String category = "category";
        public static final String tutor = "tutor";
    }

    public SuggestedQuestion() {}

    //For testing only
    public SuggestedQuestion(final Question question) {
        put(Columns.question, question);
        put(Columns.answeredInt, NOT_ANSWERED);
    }
    public static Task<Object> createSuggestedQuestion(final Question question) {
        final SuggestedQuestion suggestedQuestion = new SuggestedQuestion(question);
        return suggestedQuestion.saveInBackground().continueWith(new Continuation<Void, Object>() {
            @Override
            public Object then(Task<Void> task) throws Exception {
                if(task.isFaulted()) {
                    task.getError().printStackTrace();
                    Log.e("Error in createSuggestedQuestion", task.getError().getMessage());
                }
                PrivateStudentData.getPrivateStudentData().getRelation("blah").add(suggestedQuestion);
                return PrivateStudentData.getPrivateStudentData().saveInBackground().continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        if(task.isFaulted()) {
                            task.getError().printStackTrace();
                            Log.e("Error in createSuggestedQuestion", task.getError().getMessage());
                        }
                        return null;
                    }
                });
            }
        });
    }

    public Question getQuestion() { return (Question) getParseObject(Columns.question); }
    public Response getResponse() { return (Response) getParseObject(Columns.response); }
    public boolean isAnswered() { return getInt(Columns.answeredInt) == 1; }
    public PublicUserData getTutor() { return (PublicUserData) getParseObject(Columns.tutor); }

    public String getTutorDisplayName() {
        try {
            getTutor().fetchFromLocalDatastore();
            return getTutor().getDisplayName();
        } catch (ParseException e) {
            try {
                return ((PublicUserData) getTutor().fetchIfNeeded()).getDisplayName();
            } catch (ParseException e2) {
                e2.printStackTrace(); return "";
            }
        }
    }

    public static ParseQuery<SuggestedQuestion> getQuery() { return ParseQuery.getQuery(SuggestedQuestion.class); }

    public void addResponseAndPin(Response response) {
        put(Columns.response, response);
        put(Columns.answeredInt, SuggestedQuestion.ANSWERED);
        response.pinInBackground(getObjectId());
        saveInBackground();
    }
}
