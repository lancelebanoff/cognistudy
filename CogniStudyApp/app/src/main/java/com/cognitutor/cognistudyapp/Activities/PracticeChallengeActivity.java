package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.CogniImageButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.R;

import java.util.Date;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class PracticeChallengeActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;

    private Challenge mChallenge;
    private String mChallengeId;
    private int mCurrentUser1or2;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_challenge);
        mIntent = getIntent();
        initializeBroadcastReceiver();

        mChallengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        mCurrentUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);

        mChallenge = Challenge.getChallenge(mChallengeId);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setButtonSizes();
    }

    private void setButtonSizes() {
        CogniImageButton btnResign = (CogniImageButton) findViewById(R.id.btnResign);
        CogniImageButton btnQuestionHistory = (CogniImageButton) findViewById(R.id.btnQuestionHistory);
        CogniImageButton btnChallengeAnalytics = (CogniImageButton) findViewById(R.id.btnChallengeAnalytics);

        CogniButton btnYourTurn = (CogniButton) findViewById(R.id.btnYourTurn);
        int buttonHeight = btnYourTurn.getHeight();

        btnResign.setLayoutParams(new LinearLayout.LayoutParams(buttonHeight, buttonHeight));
        btnQuestionHistory.setLayoutParams(new LinearLayout.LayoutParams(buttonHeight, buttonHeight));
        btnChallengeAnalytics.setLayoutParams(new LinearLayout.LayoutParams(buttonHeight, buttonHeight));
    }

    public void onClick_btnResign(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_quit_challenge)
                .setMessage(R.string.message_dialog_quit_challenge)
                .setNegativeButton(R.string.no_dialog_cancel_challenge, null)
                .setPositiveButton(R.string.yes_dialog_cancel_challenge, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        quitChallenge();
                        Button btnYourTurn = (Button) findViewById(R.id.btnYourTurn);
                        btnYourTurn.setVisibility(View.INVISIBLE);
                    }
                }).create().show();
    }

    private void quitChallenge() {
        mChallenge.setHasEnded(true);
        mChallenge.setEndDate(new Date());
        mChallenge.saveInBackground();
    }

    public void onClick_btnYourTurn(View view) {
        int quesAnsThisTurn = mChallenge.getQuesAnsThisTurn();
        List<String> questionIds = mChallenge.getThisTurnQuestionIds();
        if (questionIds != null) {
            navigateToQuestionActivity(questionIds.get(quesAnsThisTurn));
        } else {
            chooseThreeQuestionIdsThenNavigate(); // TODO:2 do this during onCreate?
        }
    }

    private void chooseThreeQuestionIdsThenNavigate() {
        Question.chooseThreeQuestionIds(mChallenge, mCurrentUser1or2).onSuccess(new Continuation<List<String>, Void>() {
            @Override
            public Void then(Task<List<String>> task) throws Exception {
                List<String> questionIds = task.getResult();
                navigateToQuestionActivity(questionIds.get(0));
                return null;
            }
        });
    }

    private void navigateToQuestionActivity(String questionId) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, questionId);
//        fF4lsHt2iW
//        eO4TCrdBdn
        startActivity(intent);
    }

    public void navigateToQuestionHistoryActivity(View view) {
        Intent intent = new Intent(this, QuestionHistoryActivity.class);
        startActivity(intent);
    }

    public void navigateToChallengeAnalyticsActivity(View view) {
        Intent intent = new Intent(this, ChallengeAnalyticsActivity.class);
        startActivity(intent);
    }

    // Exits this activity when finishing BattleshipAttackActivity
    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY)) {
                    finish();
                }
            }
        };
        registerReceiver(mBroadcastReceiver, new IntentFilter(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
