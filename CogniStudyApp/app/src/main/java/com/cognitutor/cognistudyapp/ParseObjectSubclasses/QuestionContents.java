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
        public static final String questionText = "questionText";
        public static final String image = "image";
        public static final String author = "author";
        public static final String answers = "answers";
        public static final String correctAnswer = "correctAnswer";
        public static final String explanation = "explanation";
    }

    public QuestionContents() {}
    public QuestionContents(String questionText, ParseFile image, PublicUserData author,
                            List<String> answers, int correctAnswer, String explanation) {

        put(Columns.questionText, questionText);
        put(Columns.answers, answers);
        put(Columns.correctAnswer, correctAnswer);
        put(Columns.explanation, explanation);
        if(author != null)
            put(Columns.author, author);
        if(image != null)
            put(Columns.image, image);
    }

    public String getQuestionText() { return getString(Columns.questionText); }
    public ParseFile getImage() { return getParseFile(Columns.image); }
    //TODO: author
    public List<String> getAnswers() { return getList(Columns.answers); }
    public int getCorrectIdx() { return getInt(Columns.correctAnswer); }
    public String getExplanation() { return getString(Columns.explanation); }

    public boolean isCorrect(int idx) { return idx == getCorrectIdx(); }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() +
                " | questionText: " + getQuestionText().substring(0, 50);
    }
}
