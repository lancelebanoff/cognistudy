package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/23/2016.
 */
public class SuggestedQuestionActivity extends AnswerableQuestionActivity{

    private SuggestedQuestion mSuggestedQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSuggestedQuestion();
    }

    @Override
    protected Task<Void> createResponse(boolean isSelectedAnswerCorrect) {
        return doCreateResponse(isSelectedAnswerCorrect, getSuggestedQuestionId()).continueWith(new Continuation<Response, Void>() {
            @Override
            public Void then(Task<Response> task) throws Exception {
                mSuggestedQuestion.addResponseAndPin(task.getResult());
                return null;
            }
        });
    }

    private void loadSuggestedQuestion() {
        final String suggestedQuestionId = getSuggestedQuestionId();
        QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<SuggestedQuestion>() {
            @Override
            public ParseQuery<SuggestedQuestion> buildQuery() {
                return SuggestedQuestion.getQuery().whereEqualTo(Constants.ParseObjectColumns.objectId, getSuggestedQuestionId());
            }
        }).continueWith(new Continuation<SuggestedQuestion, Object>() {
            @Override
            public Object then(Task<SuggestedQuestion> task) throws Exception {
                if(task.isFaulted()) {
                    task.getError().printStackTrace();
                }
                mSuggestedQuestion = task.getResult();
                return null;
            }
        });
    }

    private String getSuggestedQuestionId() {
        return mIntent.getStringExtra(Constants.IntentExtra.QUESTION_META_ID);
    }
}
