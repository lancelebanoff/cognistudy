package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("QuestionContents")
public class QuestionContents extends ParseObject{

    public class Columns {
        public static final String passage = "passage";
        public static final String questionText = "questionText";
        public static final String image = "image";
        public static final String author = "author";
        public static final String answers = "answers";
        public static final String correctAnswer = "correctAnswer";
        public static final String explanation = "explanation";
    }

    public QuestionContents() {}
    public QuestionContents(Passage passage, String questionText, ParseFile image, PublicUserData author,
                            List<String> answers, int correctAnswer, String explanation) {

        put(Columns.passage, passage);
        put(Columns.questionText, questionText);
        put(Columns.image, image);
        put(Columns.author, author);
        put(Columns.answers, answers);
        put(Columns.correctAnswer, correctAnswer);
        put(Columns.explanation, explanation);
    }
}
