package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 4/16/2016.
 */
@ParseClassName("QuestionReport")
public class QuestionReport extends ParseObject {

    public static class Columns {
        public static final String baseUserId = "baseUserId";
        public static final String questionId = "questionId";
        public static final String message = "message";
    }

    public QuestionReport() {}

    public QuestionReport(String questionId, String message) {
        put(Columns.baseUserId, UserUtils.getCurrentUserId());
        put(Columns.questionId, questionId);
        put(Columns.message, message);
    }
}
