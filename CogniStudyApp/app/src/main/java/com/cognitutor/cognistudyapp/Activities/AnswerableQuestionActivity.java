package com.cognitutor.cognistudyapp.Activities;

import android.view.View;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.parse.ParseObject;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/23/2016.
 */
public abstract class AnswerableQuestionActivity extends QuestionActivity {

    protected abstract Task<Void> createResponse(boolean isSelectedAnswerCorrect);

    protected Task<Response> doCreateResponse(boolean isSelectedAnswerCorrect, final String challengeId) {
        //TODO: Pin related objects
        //TODO: Implement rating
        final Response response = new Response(mQuestionWithoutContents, isSelectedAnswerCorrect, getSelectedAnswer(), Constants.QuestionRating.NOT_RATED);
        return response.getQuestion().fetchIfNeededInBackground().continueWithTask(new Continuation<ParseObject, Task<Response>>() {
            @Override
            public Task<Response> then(Task<ParseObject> task) throws Exception {
                return ParseObjectUtils.pinThenSaveInBackground(challengeId, response)
                        .continueWith(new Continuation<Void, Response>() {
                            @Override
                            public Response then(Task<Void> task) throws Exception {
                                PrivateStudentData.addResponseAndSaveEventually(response);
                                return response;
                            }
                        });
            }
        });
    }

    public void showAnswerAndIncrementAnalytics(View view) {

        final boolean isSelectedAnswerCorrect = isSelectedAnswerCorrect();
        showAnswer(isSelectedAnswerCorrect);
        //Response and analytics
        incrementAnalytics(mQuestion.getCategory(), isSelectedAnswerCorrect);
        createResponse(isSelectedAnswerCorrect);
    }

    protected void incrementAnalytics(String category, boolean isSelectedAnswerCorrect) {
        //TODO: wait for incrementAll to finish when necessary
        StudentBlockStats.incrementAll(category, isSelectedAnswerCorrect);
        StudentTRollingStats.incrementAllInBackground(mQuestion, category, isSelectedAnswerCorrect);
    }
}
