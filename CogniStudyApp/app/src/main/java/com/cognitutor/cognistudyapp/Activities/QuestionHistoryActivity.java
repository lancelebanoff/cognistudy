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

    private String mChallengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        mChallengeId =
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
        mAdapter = new QuestionListAdapter(this, Response.getChallengeResponsesFromLocal(mChallengeId));
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.QUESTION_HISTORY_ACTIVITY;
    }

    private ParseQuery<QuestionMetaObject> getChallengeQuestionsQuery(String subject, String category) {
        ParseQuery<QuestionMetaObject> query = Response.getChallengeResponsesFromLocal(mChallengeId)
                .include(Response.Columns.question);
        getSubjectAndCategoryQuery(query, subject, category);
        return query;
    }
}
