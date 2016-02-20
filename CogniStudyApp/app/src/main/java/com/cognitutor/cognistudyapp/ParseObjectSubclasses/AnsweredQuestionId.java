package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

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
    public AnsweredQuestionId(String questionId, boolean saveNow) {
        put(Columns.questionId, questionId);
        if(saveNow)
            saveInBackground();
//        else
//            SubclassUtils.addToSaveQueue(this);
    }
}
