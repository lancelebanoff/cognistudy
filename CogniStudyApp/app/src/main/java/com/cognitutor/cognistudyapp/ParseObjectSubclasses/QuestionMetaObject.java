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
        ParseQuery<QuestionMetaObject> query = getMetaQuery(clazz)
                .fromPin(challengeId);

        //TODO: Implement Bookmarks
        if(clazz == Response.class || clazz == SuggestedQuestion.class) {
            if(clazz == Response.class) {
                query.include(Response.Columns.question);
            }
            else {
                query.include(SuggestedQuestion.Columns.question);
            }
            ParseQuery<Question> innerQuery = Question.getQuery();

            boolean includeInnerQuery = false;
            if(!subject.equals(Constants.Subject.ALL_SUBJECTS)) {
                innerQuery.whereEqualTo(Question.Columns.subject, subject);
                includeInnerQuery = true;
            }
            if(!category.equals(Constants.Category.ALL_CATEGORIES)) {
                innerQuery.whereEqualTo(Question.Columns.category, category);
                includeInnerQuery = true;
            }
            if(includeInnerQuery) {
                query.whereMatchesQuery("question", innerQuery);
            }
        }
        return query;
    }

    private static ParseQuery<QuestionMetaObject> getMetaQuery(Class<? extends QuestionMetaObject> clazz) {
        return ParseQuery.getQuery(clazz.getSimpleName());
    }

}
