package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("QuestionBundle")
public class QuestionBundle extends ParseObject {

    public class Columns {
        public static final String passageText = "passageText";
        public static final String questions = "questions";
        public static final String image = "image";
    }

    public QuestionBundle() {}
    public QuestionBundle(String passageText, List<Question> questions) {
        put(Columns.passageText, passageText);
        put(Columns.questions, questions);
    }
    public QuestionBundle(ParseFile image, List<Question> questions) {
        put(Columns.image, image);
        put(Columns.questions, questions);
    }
    public QuestionBundle(String passageText, ParseFile image, List<Question> questions) {
        put(Columns.passageText, passageText);
        put(Columns.image, image);
        put(Columns.questions, questions);
    }

    public String getPassageText() { return getString(Columns.passageText); }
    public ParseFile getImage() { return getParseFile(Columns.image); }
}