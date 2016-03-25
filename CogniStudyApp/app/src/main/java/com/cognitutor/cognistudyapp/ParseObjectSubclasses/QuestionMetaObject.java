package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 3/18/2016.
 */
public abstract class QuestionMetaObject extends ParseObject{

    public abstract String getSubject();
    public abstract String getCategory();
    public abstract String getResponseStatus();
    public abstract String getQuestionId();
    public abstract String getResponseId();

    public String getDate() {
        return getCreatedAt().toString(); //TODO: Format?
    }

    public static ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(Class<? extends QuestionMetaObject> clazz,
                                                                            String challengeId, String subject, String category) {
        ParseQuery<QuestionMetaObject> query = getMetaQuery(clazz);

        if(challengeId != null) {
            query.fromPin(challengeId);
        }
        else {
            query.fromLocalDatastore();
        }

        ParseQuery<Question> questionQuery = Question.getQuery();
        boolean includeQuestionQuery = false;
        if(!subject.equals(Constants.Subject.ALL_SUBJECTS)) {
            questionQuery.whereEqualTo(Question.Columns.subject, subject);
            includeQuestionQuery = true;
        }
        if(!category.equals(Constants.Category.ALL_CATEGORIES)) {
            questionQuery.whereEqualTo(Question.Columns.category, category);
            includeQuestionQuery = true;
        }

        ParseQuery<Response> responseQuery = Response.getQuery();
        boolean includeResponseQuery = false;
        if(clazz == Response.class) {
            query.include(Response.Columns.question);
        }
        else if(clazz == Bookmark.class) {
            query.include(Bookmark.Columns.response + "." + Response.Columns.question);
            includeResponseQuery = true;
            responseQuery.whereMatchesQuery(Response.Columns.question, questionQuery);
        }
        else {
            query.include(SuggestedQuestion.Columns.question);
        }

        if(includeQuestionQuery) {
            if(includeResponseQuery) {
                query.whereMatchesQuery("response", responseQuery);
            }
            else {
                query.whereMatchesQuery("question", questionQuery);
            }
        }
        return query;
    }

    private static ParseQuery<QuestionMetaObject> getMetaQuery(Class<? extends QuestionMetaObject> clazz) {
        return ParseQuery.getQuery(clazz.getSimpleName());
    }
}
