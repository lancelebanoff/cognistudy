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

    protected abstract void onPostCreateResponse(Response response);

    protected Task<Void> createResponse(boolean isSelectedAnswerCorrect) {
        return doCreateResponse(isSelectedAnswerCorrect, getQuestionAndResponsePinName()).continueWithTask(new Continuation<Response, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Response> task) throws Exception {
                onPostCreateResponse(task.getResult());
                return null;
            }
        });
    }

    protected Task<Response> doCreateResponse(final boolean isSelectedAnswerCorrect, final String pinName) {
        //TODO: Pin related objects
        //TODO: Implement rating
        return mQuestion.fetchIfNeededInBackground().continueWithTask(new Continuation<ParseObject, Task<Response>>() {
            @Override
            public Task<Response> then(Task<ParseObject> task) throws Exception {
                final Response response = new Response(mQuestion, isSelectedAnswerCorrect, getSelectedAnswer(), Constants.QuestionRating.NOT_RATED);
                return ParseObjectUtils.pinThenSaveInBackground(pinName, response)
                        .continueWith(new Continuation<Void, Response>() {
                            @Override
                            public Response then(Task<Void> task) throws Exception {
                                PrivateStudentData.addResponseAndSaveEventually(response);
                                mResponse = response;
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
