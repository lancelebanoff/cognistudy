package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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

    public Bookmark(Response response) {
        put(Columns.response, response);
        put(Columns.baseUserId, UserUtils.getCurrentUserId());
    }

    public Response getResponse() { return (Response) getParseObject(Columns.response); }
    public Question getQuestion() { return ((Response) getParseObject(Columns.response)).getQuestion(); }
    public static ParseQuery<Bookmark> getQuery() { return ParseQuery.getQuery(Bookmark.class); }

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

    public static ParseQuery<Bookmark> getQueryWithResponseId(String responseId) {
        ParseQuery<Response> innerQuery = Response.getQuery().whereEqualTo("objectId", responseId);
        return Bookmark.getQuery()
                .fromLocalDatastore()
                .include(Bookmark.Columns.response)
                .whereMatchesQuery(Bookmark.Columns.response, innerQuery);
    }
}
