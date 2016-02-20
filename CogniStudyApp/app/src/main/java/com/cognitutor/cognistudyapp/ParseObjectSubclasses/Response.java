package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("Response")
public class Response extends ParseObject {

    public static class Columns {
        public static final String baseUserID = "baseUserId";
        public static final String question = "question";
        public static final String correct = "correct";
        public static final String selectedAnswer = "selectedAnswer";
        public static final String rating = "rating";
    }

    public Response() {}

    public Response(Question question, boolean correct, int selectedAnswer, String rating) {
        put(Columns.baseUserID, ParseUser.getCurrentUser().getObjectId());
        put(Columns.question, question);
        put(Columns.correct, correct);
        put(Columns.selectedAnswer, selectedAnswer);
        put(Columns.rating, rating);
//        SubclassUtils.addToSaveQueue(this);
    }
}
