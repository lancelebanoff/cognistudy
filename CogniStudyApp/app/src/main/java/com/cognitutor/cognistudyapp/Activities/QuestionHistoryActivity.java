package com.cognitutor.cognistudyapp.Activities;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.Fragments.BookmarkAndQuestionHistoryListFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.parse.ParseQuery;

import java.util.HashMap;

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
        return BookmarkAndQuestionHistoryListFragment.class;
    }

    @Override
    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(String subject, String category) {
        return QuestionMetaObject.getSubjectAndCategoryQuery(Response.class, subject, category)
                .fromPin(getChallengeId());
    }

    @Override
    protected QuestionListAdapter createQuestionListAdapter() {
        HashMap<String, String> intentExtras = new HashMap<>();
        intentExtras.put(Constants.IntentExtra.CHALLENGE_ID, getChallengeId());
        return new QuestionListAdapter(this, getTargetQuestionActivityClass(), intentExtras,
                getSubjectAndCategoryQuery(Constants.Subject.ALL_SUBJECTS, Constants.Category.ALL_CATEGORIES));
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }
}
