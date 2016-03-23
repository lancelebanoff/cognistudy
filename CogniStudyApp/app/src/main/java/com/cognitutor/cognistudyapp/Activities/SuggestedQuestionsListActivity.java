package com.cognitutor.cognistudyapp.Activities;

import com.cognitutor.cognistudyapp.Custom.Constants;

public class SuggestedQuestionsListActivity extends QuestionListActivity {

    @Override
    protected void getAndDisplay(String subject, String category) {
        //TODO: Implement
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY;
    }
}

