package com.cognitutor.cognistudyapp.Activities;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;

import bolts.Task;

/**
 * Created by Kevin on 3/23/2016.
 */
public class SuggestedQuestionActivity extends AnswerableQuestionActivity{

    private SuggestedQuestion mSuggestedQuestion;

    @Override
    protected Task<Void> createResponse(boolean isSelectedAnswerCorrect) {
        doCreateResponse(isSelectedAnswerCorrect, null);
        return CommonUtils.getCompletionTask(null);
    }
}
