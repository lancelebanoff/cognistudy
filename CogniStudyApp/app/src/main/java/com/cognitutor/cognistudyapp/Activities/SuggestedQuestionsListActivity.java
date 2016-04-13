package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.Fragments.SuggestedQuestionsListFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;
import com.parse.ParseQuery;

public class SuggestedQuestionsListActivity extends QuestionListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showTutorialDialogIfNeeded(Constants.Tutorial.ASSIGNED_QUESTIONS, null);
    }

    @Override
    protected Class<? extends QuestionActivity> getTargetQuestionActivityClass() {
        return SuggestedQuestionActivity.class;
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY;
    }

    @Override
    protected Class<? extends CogniFragment> getFragmentClass() {
        return SuggestedQuestionsListFragment.class;
    }

    @Override
    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(String subject, String category) {
        return QuestionMetaObject.getSubjectAndCategoryQuery(SuggestedQuestion.class, subject, category)
                .whereEqualTo(SuggestedQuestion.Columns.studentBaseUserId, UserUtils.getCurrentUserId());
    }

    @Override
    protected void getAndDisplay(final String subject, final String category) {

        QueryUtils.findCacheThenNetworkInBackgroundPinWithObjId(mAdapter, new QueryUtils.ParseQueryBuilder<QuestionMetaObject>() {
            @Override
            public ParseQuery<QuestionMetaObject> buildQuery() {
                return getSubjectAndCategoryQuery(subject, category);
            }
        });
    }
}

