package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.Fragments.PastQuestionsFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.parse.ParseQuery;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class QuestionHistoryActivity extends QuestionListActivity {

    @Override
    protected Class<? extends QuestionActivity> getTargetQuestionActivityClass() {
        return PastQuestionActivity.class;
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.QUESTION_HISTORY_ACTIVITY;
    }

    @Override
    protected Class<? extends CogniFragment> getFragmentClass() {
        return PastQuestionsFragment.class;
    }

    @Override
    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(String subject, String category) {
        return QuestionMetaObject.getSubjectAndCategoryQuery(Response.class, subject, category)
                .fromPin(getChallengeId());
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }
}
