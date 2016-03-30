package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/23/2016.
 */
public class SuggestedQuestionActivity extends AnswerableQuestionActivity{

    private SuggestedQuestion mSuggestedQuestion;
    private boolean notifyParentAnswered;

    @Override
    protected String getQuestionAndResponsePinName() {
        return getQuestionMetaId(); //SuggestedQuestionId
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSuggestedQuestion();
    }

    @Override
    protected void onPostCreateResponse(Response response) {
        mSuggestedQuestion.addResponseAndPin(response);
    }

    private void loadSuggestedQuestion() {
        QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<SuggestedQuestion>() {
            @Override
            public ParseQuery<SuggestedQuestion> buildQuery() {
                return SuggestedQuestion.getQuery().whereEqualTo(Constants.ParseObjectColumns.objectId, getQuestionMetaId());
            }
        }).continueWith(new Continuation<SuggestedQuestion, Object>() {
            @Override
            public Object then(Task<SuggestedQuestion> task) throws Exception {
                if (task.isFaulted()) {
                    task.getError().printStackTrace();
                }
                mSuggestedQuestion = task.getResult();
                return null;
            }
        });
    }

    @Override
    protected Task<Void> createResponse(boolean isSelectedAnswerCorrect) {
        notifyParentAnswered = true;
        return super.createResponse(isSelectedAnswerCorrect);
    }

    @Override
    protected void navigateToParentActivity() {
        Intent intent = new Intent();
        if(notifyParentAnswered) {
            intent.putExtra(Constants.IntentExtra.UPDATE_OBJECT_ID_IN_LIST, mSuggestedQuestion.getObjectId());
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navigateToParentActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
