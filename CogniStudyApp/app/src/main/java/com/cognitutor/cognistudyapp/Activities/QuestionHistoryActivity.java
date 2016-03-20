package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.parse.ParseQuery;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class QuestionHistoryActivity extends QuestionListActivity {

//    private String mChallengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new QuestionListAdapter(this, QuestionMetaObject.getSubjectAndCategoryQuery(Response.class, getChallengeId(),
                Constants.Subject.ALL_SUBJECTS, Constants.Category.ALL_CATEGORIES));
        mQuestionList.setAdapter(mAdapter);
        getAndDisplay(Constants.Subject.ALL_SUBJECTS, Constants.Category.ALL_CATEGORIES);
//        mChallengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }

    @Override
    protected void getAndDisplay(String subject, String category) {

        ParseQuery<QuestionMetaObject> query = getChallengeQuestionsQuery(subject, category);
        query.findInBackground()
                .continueWith(new Continuation<List<QuestionMetaObject>, Object>() {
                    @Override
                    public Object then(Task<List<QuestionMetaObject>> task) throws Exception {
                        mAdapter.onDataLoaded(task.getResult());
                        return null;
                    }
                });
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.QUESTION_HISTORY_ACTIVITY;
    }

    private ParseQuery<QuestionMetaObject> getChallengeQuestionsQuery(String subject, String category) {
        return QuestionMetaObject.getSubjectAndCategoryQuery(Response.class, getChallengeId(), subject, category);
    }
}
