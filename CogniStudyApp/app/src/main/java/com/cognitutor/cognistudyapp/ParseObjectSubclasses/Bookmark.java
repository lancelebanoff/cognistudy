package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Kevin on 3/23/2016.
 */
@ParseClassName("Bookmark")
public class Bookmark extends QuestionMetaObject {

    public class Columns {
        public static final String dateMarked = "dateMarked";
        public static final String baseUserId = "baseUserId";
        public static final String response = "response";
    }

    public Bookmark() {}

    public Response getResponse() { return (Response) getParseObject(Columns.response); }
    public Question getQuestion() { return ((Response) getParseObject(Columns.response)).getQuestion(); }

    @Override
    public String getSubject() {
        return getQuestion().getSubject();
    }

    @Override
    public String getCategory() {
        return getQuestion().getCategory();
    }

    @Override
    public String getResponseStatus() {
        return getResponse().getResponseStatus();
    }

    @Override
    public String getQuestionId() {
        return getQuestion().getObjectId();
    }

    @Override
    public String getResponseId() {
        return getResponse().getObjectId();
    }
}
