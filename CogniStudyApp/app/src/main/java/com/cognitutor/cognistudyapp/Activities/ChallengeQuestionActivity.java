package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/23/2016.
 */
public class ChallengeQuestionActivity extends AnswerableQuestionActivity {

    private int mUser1or2;
    private Challenge mChallenge = null;
    private int mQuesAnsThisTurn = -1;

    @Override
    protected String getQuestionAndResponsePinName() {
        return getChallengeId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        loadChallenge();
    }

    @Override
    protected void onPostCreateResponse(Response response) {
        mChallenge.getChallengeUserData(mUser1or2).addResponseAndSaveEventually(response);
    }

    @Override
    public void showAnswerAndIncrementAnalytics(View view) {
        super.showAnswerAndIncrementAnalytics(view);
        incrementQuesAnsThisTurn(isSelectedAnswerCorrect());
    }

    public void navigateToNextActivity(View view) {
        String parentActivity = mIntent.getStringExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY);
        switch(parentActivity) {
            case Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY:
                if(mQuesAnsThisTurn == Constants.Questions.NUM_QUESTIONS_PER_TURN) {
                    navigateToBattleshipAttackActivity();
                } else {
                    String questionId = mChallenge.getThisTurnQuestionIds().get(mQuesAnsThisTurn);
                    navigateToNextQuestionActivity(questionId);
                }
                break;
            default:
                navigateToParentActivity();
                break;
        }
    }


    private void loadChallenge() {
        String challengeId = getChallengeId();
        //TODO: Load from localDataStore
        Challenge.getChallengeInBackground(challengeId)
                .continueWith(new Continuation<Challenge, Void>() {
                    @Override
                    public Void then(Task<Challenge> task) throws Exception {
                        mChallenge = task.getResult();

                        if (!mChallenge.getChallengeType().equals(Constants.ChallengeType.PRACTICE)) {
                            showTutorialDialogIfNeeded(Constants.Tutorial.QUESTION, null);
                        }

                        return null;
                    }
                });
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }

    private void incrementQuesAnsThisTurn(final boolean isSelectedAnswerCorrect) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mChallenge == null) {} // Wait until challenge is loaded

                mQuesAnsThisTurn = mChallenge.incrementAndGetQuesAnsThisTurn();
                if(isSelectedAnswerCorrect) {
                    mChallenge.incrementCorrectAnsThisTurn();
                }
                if(mChallenge.getChallengeType().equals(Constants.ChallengeType.PRACTICE) &&
                        mChallenge.getQuesAnsThisTurn() == Constants.Questions.NUM_QUESTIONS_PER_TURN) {
                    mQuesAnsThisTurn = 0;
                    mChallenge.setQuesAnsThisTurn(0);
                    Question.chooseThreeQuestionIds(mChallenge, mUser1or2).onSuccess(new Continuation<List<String>, Void>() {
                        @Override
                        public Void then(Task<List<String>> task) throws Exception {
                            saveChallengeAndShowButton();
                            return null;
                        }
                    });
                }
                else {
                    saveChallengeAndShowButton();
                }
            }
        }).start();
    }

    private void saveChallengeAndShowButton() {
        try {
            mChallenge.save();
        } catch (ParseException e) {
            e.printStackTrace();
        }
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                // Switch Submit button to Continue button
//                ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
//                viewSwitcher.setVisibility(View.VISIBLE);
//                viewSwitcher.showNext();
//            }
//        });
    }

    private void navigateToBattleshipAttackActivity() {
        Intent intent = new Intent(this, BattleshipAttackActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        startActivity(intent);
        finish();
    }

    private void navigateToNextQuestionActivity(String questionId) {
        Intent intent = new Intent(this, ChallengeQuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, questionId);
        startActivity(intent);
        finish();
    }
}
