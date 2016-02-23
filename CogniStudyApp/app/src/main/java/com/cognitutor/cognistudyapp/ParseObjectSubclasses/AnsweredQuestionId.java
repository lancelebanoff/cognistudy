package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("AnsweredQuestionId")
public class AnsweredQuestionId extends ParseObject{

    public static class Columns {
        public static final String questionId = "questionId";
    }

    public AnsweredQuestionId() {}
    public AnsweredQuestionId(String questionId) {
        put(Columns.questionId, questionId);
    }

    @Override
    public String toString() {
        String objectId = getObjectId();
        String s = "objectId: ";
        s += (objectId == null ? "null" : objectId) + ", id: ";
        String id = getString(Columns.questionId);
        s += (id == null ? "null" : id);
        return s;
    }
}
