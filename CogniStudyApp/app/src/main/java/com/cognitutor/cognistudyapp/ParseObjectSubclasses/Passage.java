package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("Passage")
public class Passage extends ParseObject {

    public class Columns {
        public static final String passageText = "passageText";
        public static final String questions = "questions";
    }

    public Passage() {}
    public Passage(String passageText, List<Question> questions) {
        put(Columns.passageText, passageText);
        put(Columns.questions, questions);
    }
}
