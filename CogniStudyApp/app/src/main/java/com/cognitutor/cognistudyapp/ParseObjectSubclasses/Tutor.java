package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Kevin on 1/10/2016.
 */
@ParseClassName("Tutor")
public class Tutor extends ParseObject {

    public class Columns {
        public static final String numQuestionsCreated = "numQuestionsCreated";
        public static final String numQuestionsReviewed = "numQuestionsReviewed";
        public static final String biography = "biography";
        public static final String baseUserId = "baseUserId";
        //Purposefully omitting privateTutorData
    }
    public Tutor() {}

    public int getNumQuestionsCreated() { return getInt(Columns.numQuestionsCreated); }
    public int getNumQuestionsReviewed() { return getInt(Columns.numQuestionsReviewed); }
    public String getBiography() { return getString(Columns.biography); }
    public String getBaseUserId() { return getString(Columns.baseUserId); }
}
