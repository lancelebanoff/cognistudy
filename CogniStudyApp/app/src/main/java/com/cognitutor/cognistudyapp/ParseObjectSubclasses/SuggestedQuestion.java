package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;

/**
 * Created by Kevin on 3/17/2016.
 */
@ParseClassName("SuggestedQuestion")
public class SuggestedQuestion extends ParseObject {

    public class Columns {
        public static final String studentBaseUserId = "studentBaseUserId";
        public static final String question = "question";
        public static final String response = "response";
        public static final String answered = "answered";
        public static final String tutor = "tutor";
    }

    public SuggestedQuestion() {}

    public Question getQuestion() { return (Question) getParseObject(Columns.question); }
    public Response getResponse() { return (Response) getParseObject(Columns.response); }
    public boolean isAnswered() { return getBoolean(Columns.answered); }
    public PublicUserData getTutor() { return (PublicUserData) getParseObject(Columns.tutor); }

    public String getTutorDisplayName() {
        try {
            getTutor().fetchFromLocalDatastore();
            return getTutor().getDisplayName();
        } catch (ParseException e) {
            try {
                return ((PublicUserData) getTutor().fetchIfNeeded()).getDisplayName();
            } catch (ParseException e2) {
                e2.printStackTrace(); return "";
            }
        }
    }
}
