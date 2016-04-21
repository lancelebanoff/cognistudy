package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

/**
 * Created by Kevin on 3/18/2016.
 */
public abstract class QuestionMetaObject extends ParseObject{

    public abstract String getSubject();
    public abstract String getCategory();
    public abstract String getResponseStatus();
    public abstract String getQuestionId();
    public abstract String getResponseId();

    public Date getDate() {
        return getCreatedAt();
    }

    //Not all QuestionMetaObject subclasses have all of these columns
    public class Columns {
        public static final String createdAt = "createdAt";
        public static final String response = "response";
        public static final String question = "question";
        public static final String baseUserId = "baseUserId";
    }

    public static ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(Class<? extends QuestionMetaObject> clazz,
                                                                            String subject, String category) {
        ParseQuery<QuestionMetaObject> query = getMetaQuery(clazz);

        if(clazz == SuggestedQuestion.class) {
            query.orderByAscending(SuggestedQuestion.Columns.answeredInt);
        }
        query.addDescendingOrder(Columns.createdAt);

        ParseQuery<Response> responseQuery = Response.getQuery();
        boolean isBookmark = clazz == Bookmark.class;
        if(isBookmark) {
            query.whereMatchesQuery(Columns.response, responseQuery);
        }

        if(!subject.equals(Constants.Subject.ALL_SUBJECTS)) {
            if(isBookmark)
                responseQuery.whereEqualTo(Response.Columns.subject, subject);
            else
                query.whereEqualTo(Response.Columns.subject, subject);
        }
        if(!category.equals(Constants.Category.ALL_CATEGORIES)) {
            if(isBookmark)
                responseQuery.whereEqualTo(Response.Columns.category, category);
            else
                query.whereEqualTo(Response.Columns.category, category);
        }

        if(clazz == Response.class) {
            query.include(Response.Columns.question);
        }
        else if(isBookmark) {
            query.include(Bookmark.Columns.response + "." + Response.Columns.question);
        }
        else {
            query.include(SuggestedQuestion.Columns.question);
            query.include(SuggestedQuestion.Columns.response);
        }

        return query;
    }

    private static ParseQuery<QuestionMetaObject> getMetaQuery(Class<? extends QuestionMetaObject> clazz) {
        return ParseQuery.getQuery(clazz.getSimpleName());
    }

    @Override
    public String toString() {
        String s = "objectId: " + getObjectId() + " | " +
                "questionId: " + getQuestionId() + " | " +
                "responseId: " + getResponseId() + " | " +
                "subject: " + getSubject() + " | " +
                "category: " + getCategory() + " | " +
                "status: " + getResponseStatus();
        return s;
    }
}
