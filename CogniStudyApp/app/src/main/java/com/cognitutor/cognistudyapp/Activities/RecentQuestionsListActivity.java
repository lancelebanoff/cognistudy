package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.BookmarkAndQuestionHistoryListFragment;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.parse.ParseQuery;

/**
 * Created by Kevin on 4/17/2016.
 */
public class RecentQuestionsListActivity extends QuestionListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showTutorialDialogIfNeeded(Constants.Tutorial.RECENT_QUESTIONS, null);
    }

    @Override
    protected Class<? extends QuestionActivity> getTargetQuestionActivityClass() {
        return PastQuestionActivity.class;
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.RECENT_QUESTIONS_LIST_ACTIVITY;
    }

    @Override
    protected Class<? extends CogniFragment> getFragmentClass() {
        return BookmarkAndQuestionHistoryListFragment.class;
    }

    @Override
    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(String subject, String category) {
        return QuestionMetaObject.getSubjectAndCategoryQuery(Response.class, subject, category)
                .fromLocalDatastore();
    }
}
