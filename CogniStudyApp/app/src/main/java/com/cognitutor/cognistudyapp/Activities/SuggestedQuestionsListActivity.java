package com.cognitutor.cognistudyapp.Activities;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;

public class SuggestedQuestionsListActivity extends QuestionListActivity {

    @Override
    protected Class<? extends QuestionMetaObject> getTargetMetaClass() {
        return SuggestedQuestion.class;
    }

    @Override
    protected Class<? extends QuestionActivity> getTargetQuestionActivityClass() {
        return SuggestedQuestionActivity.class;
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY;
    }
}

