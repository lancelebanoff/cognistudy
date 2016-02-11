package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("QuestionData")
public class QuestionData extends ParseObject{

    public class Columns {
        public static final String responses = "responses";
        public static final String totalResponses = "totalAllTime";
        public static final String correctResponses = "correctAllTime";
        public static final String reviews = "reviews";
        public static final String newlyApproved = "newlyApproved";
    }
}
