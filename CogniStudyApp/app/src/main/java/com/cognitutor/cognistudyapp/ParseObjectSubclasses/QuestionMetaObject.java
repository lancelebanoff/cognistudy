package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

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
}
