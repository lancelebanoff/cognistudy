package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.ACLUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.ParseACL;
import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin on 2/13/2016.
 */
@ParseClassName("AnsweredQuestionIds")
public class AnsweredQuestionIds extends ParseObject{

    public static class Columns {
        public static final String baseUserId = "baseUserId";
        public static final String category = "category";
        public static final String questionIds = "questionIds";
    }

    public AnsweredQuestionIds() {}
    public AnsweredQuestionIds(String category) {
        setACL(ACLUtils.getPrivateReadACL());
        put(Columns.baseUserId, UserUtils.getCurrentUserId());
        put(Columns.category, category);
        put(Columns.questionIds, new ArrayList<String>());
       saveInBackground();
    }

    public void addAnsweredQuestionIdAndSaveEventually(String questionId) {
        addUnique(Columns.questionIds, questionId);
        saveEventually();
    }

    @Override
    public String toString() {
        String objectId = getObjectId();
        String s = "objectId: ";
        s += objectId == null ? "null" : objectId;
        String category = getString(Columns.category);
        s += " | category: " + (category == null ? "null" : category);
        s += " | questionIds:";
        List<String> questionIds = getList(Columns.questionIds);
        for(String id : questionIds) {
            s += " " + id;
        }
        return s;
    }
}
