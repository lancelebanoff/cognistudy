package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseClassName;
import com.parse.ParseException;

/**
 * Created by Kevin on 3/17/2016.
 */
@ParseClassName("SuggestedQuestion")
public class SuggestedQuestion extends QuestionMetaObject {

    @Override
    public String getResponseStatus() {
        Response response = getResponse();
        if(response == null) {
            return Constants.ResponseStatusType.UNANSWERED;
        }
        return response.getResponseStatus();
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
    public String getResponseId() {
        Response response = getResponse();
        if (response == null) {
            return Constants.ResponseStatusType.UNANSWERED;
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
        public static final String answered = "answered";
        public static final String tutor = "tutor";
    }

    public SuggestedQuestion() {}

    public Question getQuestion() { return (Question) getParseObject(Columns.question); }
    public Response getResponse() { return (Response) getParseObject(Columns.response); }
    public boolean isAnswered() { return getBoolean(Columns.answered); }
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
}
