package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.CogniImageButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import bolts.Continuation;
import bolts.Task;

public class ChallengeActivity extends CogniPushListenerActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;

    private Challenge mChallenge;
    private String mChallengeId;
    private int mCurrentUser1or2;
    private int mViewingUser1or2;
    private boolean mIsComputerOpponent;
    private boolean mIsCurrentUsersTurn;
    private boolean mScoresHaveBeenLoaded;
    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private GridLayout mAnimationsGridLayout;
    private BroadcastReceiver mBroadcastReceiver;

    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);
        hideActionBar();
        initializeBroadcastReceiver();
        initChallenge();
    }

    private void initChallenge() {
        mIntent = getIntent();

        mChallengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        getCurrentUser1or2();
        mViewingUser1or2 = mCurrentUser1or2;
        mScoresHaveBeenLoaded = false;

//        mChallenge = Challenge.getChallenge(mChallengeId);
        try {
            Challenge.getChallengeInBackground(mChallengeId).continueWith(new Continuation<Challenge, Object>() {
                @Override
                public Object then(Task<Challenge> task) throws Exception {
                    mChallenge = task.getResult();
                    mIsComputerOpponent = mChallenge.getChallengeType().equals(Constants.ChallengeType.ONE_PLAYER);
                    mIsCurrentUsersTurn = mChallenge.getCurTurnUserId().equals(UserUtils.getCurrentUserId());

                    initializeBoard(mViewingUser1or2);
                    return null;
                }
            }).waitForCompletion();
        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    private void getCurrentUser1or2() {
        mCurrentUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        //If intent came from notification, the intExtra will actually be a string extra
        if(mCurrentUser1or2 == -1) {
            String stringUser1Or2 = mIntent.getStringExtra(Constants.IntentExtra.USER1OR2);
            if(stringUser1Or2 != null)
                mCurrentUser1or2 = Integer.valueOf(stringUser1Or2);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        showOrHideButtons();
    }

    private void initializeBoard(final int viewingUser1or2) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showLoading();
            }
        });

        ChallengeUtils.initializeBattleshipBoardManager(this, mChallengeId, mCurrentUser1or2, viewingUser1or2, false)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
                                if (mShipsGridLayout == null) {
                                    return;
                                }
                                initializeGridLayouts(viewingUser1or2);
                                showLoadingDone();
                                if (!mScoresHaveBeenLoaded) {
                                    showScores();
                                    showProfilePictures();
                                    mScoresHaveBeenLoaded = true;
                                }
                                handleTutorial();
                                handleOnLoseGame();
                            }
                        });

                        return null;
                    }
                });
    }

    private void handleOnLoseGame() {
        if(mChallenge.getHasEnded()
                && mChallenge.getWinner() != null && !mChallenge.getWinner().equals(UserUtils.getCurrentUserId())
                && !mChallenge.getLoserHasSeenLost()) {
            mBattleshipBoardManager.alertLostChallenge();
            mChallenge.setLoserHasSeenLost();
            mChallenge.saveEventually();
        }
    }

    private void handleTutorial() {
        if (mIsCurrentUsersTurn) {
            showTutorialDialogIfNeeded(Constants.Tutorial.YOUR_TURN, null);
        } else if (mIsComputerOpponent) {
            new AlertDialog.Builder(this)
                    .setTitle("CogniBot's Turn")
                    .setMessage("CogniBot is taking its turn.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            mBattleshipBoardManager.takeComputerTurn();
                            mIsCurrentUsersTurn = true;
                            showOrHideButtons();
                            mBattleshipBoardManager.showPreviousTurn();
                        }
                    })
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mBattleshipBoardManager.takeComputerTurn();
                            mIsCurrentUsersTurn = true;
                            showOrHideButtons();
                            mBattleshipBoardManager.showPreviousTurn();
                        }
                    }).create().show();
        }
    }

    private void showLoadingDone() {
        RoundedImageView imgProfile1 = (RoundedImageView) findViewById(R.id.imgProfile1);
        imgProfile1.setClickable(true);
        RoundedImageView imgProfile2 = (RoundedImageView) findViewById(R.id.imgProfile2);
        imgProfile2.setClickable(true);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarSmall);
        progressBar.setVisibility(View.GONE);
        RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlGridLayoutHolder);
        rlContent.setVisibility(View.VISIBLE);
    }

    private void showLoading() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarSmall);
        progressBar.setVisibility(View.VISIBLE);
        RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlGridLayoutHolder);
        rlContent.setVisibility(View.INVISIBLE);
    }

    private void showOrHideButtons() {
        ProgressBar progressBarYourTurn = (ProgressBar) findViewById(R.id.progressBarYourTurn);
        progressBarYourTurn.setVisibility(View.GONE);

        Button btnYourTurn = (Button) findViewById(R.id.btnYourTurn);
        CogniButton btnWaitingForOpponent = (CogniButton) findViewById(R.id.btnWaitingForOpponent);
        if (mIsCurrentUsersTurn && !mChallenge.getHasEnded()) {
            btnYourTurn.setVisibility(View.VISIBLE);
            btnWaitingForOpponent.setVisibility(View.INVISIBLE);
        } else {
            btnWaitingForOpponent.setColor(this, R.color.grey);
            btnWaitingForOpponent.setVisibility(View.VISIBLE);
            btnYourTurn.setVisibility(View.INVISIBLE);
        }

        if (mChallenge.getHasEnded()) {
            btnWaitingForOpponent.setVisibility(View.INVISIBLE);
            CogniImageButton btnResign = (CogniImageButton) findViewById(R.id.btnResign);
            btnResign.setVisibility(View.INVISIBLE);
        }

        int opponentUser1or2 = mCurrentUser1or2 == 1 ? 2 : 1;
        final ChallengeUserData opponentUserData = mChallenge.getChallengeUserData(opponentUser1or2);

        final CogniImageButton btnQuestionHistory = (CogniImageButton) findViewById(R.id.btnQuestionHistory);
        btnQuestionHistory.setVisibility(View.INVISIBLE);
        final CogniImageButton btnChallengeAnalytics = (CogniImageButton) findViewById(R.id.btnChallengeAnalytics);
        btnChallengeAnalytics.setVisibility(View.INVISIBLE);

        opponentUserData.fetchIfNeededInBackground().continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (opponentUserData != null && opponentUserData.getGameBoard() != null) {
                            btnQuestionHistory.setVisibility(View.VISIBLE);
                            btnChallengeAnalytics.setVisibility(View.VISIBLE);
                        }
                    }
                });
                return null;
            }
        });
    }

    private void initializeGridLayouts(final int viewingUser1or2) {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(viewingUser1or2 == mCurrentUser1or2 || mChallenge.getHasEnded()) {
                    mBattleshipBoardManager.drawShips();
                }
                else {
                    mBattleshipBoardManager.drawDeadShips();
                }
                removeOnGlobalLayoutListener(mShipsGridLayout, this);
            }
        });

        mTargetsGridLayout = (GridLayout) findViewById(R.id.targetsGridLayout);
        mBattleshipBoardManager.setTargetsGridLayout(mTargetsGridLayout);
        observer = mTargetsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.initializeTargets();
                removeOnGlobalLayoutListener(mTargetsGridLayout, this);
            }
        });

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        rlContent.setVisibility(View.VISIBLE);

        mAnimationsGridLayout = (GridLayout) findViewById(R.id.animationsGridLayout);
        mBattleshipBoardManager.setAnimationsGridLayout(mAnimationsGridLayout);
        observer = mAnimationsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.initializeAnimationsGridLayout();
                mBattleshipBoardManager.showPreviousTurn();
                removeOnGlobalLayoutListener(mAnimationsGridLayout, this);
            }
        });
    }

    private void showScores() {
        TextView txtScore = (TextView) findViewById(R.id.txtScore);
        int[] scores = mBattleshipBoardManager.getScores();
        txtScore.setText("  " + scores[0] + " - " + scores[1] + "  ");
        txtScore.setGravity(Gravity.CENTER_VERTICAL);
    }

    private void showProfilePictures() {
        ParseFile[] parseFiles = mBattleshipBoardManager.getProfilePictures();
        RoundedImageView img1 = (RoundedImageView) findViewById(R.id.imgProfile1);
        RoundedImageView img2 = (RoundedImageView) findViewById(R.id.imgProfile2);
        img1.setParseFile(parseFiles[0]);
        img1.loadInBackground().continueWith(new Continuation<byte[], Void>() {
            @Override
            public Void then(Task<byte[]> task) throws Exception {
                ImageView imgHalo1 = (ImageView) findViewById(R.id.imgProfilePicHalo1);
                imgHalo1.setVisibility(View.VISIBLE);
                return null;
            }
        });
        img2.setParseFile(parseFiles[1]);
        img2.loadInBackground();
    }

    public void onClick_imgProfile1(View view) {
        if (mViewingUser1or2 != mCurrentUser1or2) {
            switchView();
        }
    }

    public void onClick_imgProfile2(View view) {
        if (mViewingUser1or2 == mCurrentUser1or2) {
            switchView();
        }
    }

    private void switchView() {
        RoundedImageView imgProfile1 = (RoundedImageView) findViewById(R.id.imgProfile1);
        imgProfile1.setClickable(false);
        RoundedImageView imgProfile2 = (RoundedImageView) findViewById(R.id.imgProfile2);
        imgProfile2.setClickable(false);

        mBattleshipBoardManager.clearImages();
        mViewingUser1or2 = mViewingUser1or2 == 1 ? 2 : 1;
        initializeBoard(mViewingUser1or2);

        ImageView imgHalo1 = (ImageView) findViewById(R.id.imgProfilePicHalo1);
        ImageView imgHalo2 = (ImageView) findViewById(R.id.imgProfilePicHalo2);
        if (mViewingUser1or2 == mCurrentUser1or2) {
            imgHalo1.setVisibility(View.VISIBLE);
            imgHalo2.setVisibility(View.INVISIBLE);
        } else {
            imgHalo1.setVisibility(View.INVISIBLE);
            imgHalo2.setVisibility(View.VISIBLE);
        }
    }

    public void onClick_imgBack(View view) {
        finish();
    }

    public void onClick_btnResign(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_quit_challenge)
                .setMessage(R.string.message_dialog_quit_challenge)
                .setNegativeButton(R.string.no_dialog_cancel_challenge, null)
                .setPositiveButton(R.string.yes_dialog_cancel_challenge, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        mBattleshipBoardManager.quitChallenge();
                        Button btnYourTurn = (Button) findViewById(R.id.btnYourTurn);
                        btnYourTurn.setVisibility(View.INVISIBLE);
                    }
                }).create().show();
    }

    public void onClick_btnYourTurn(View view) {
        CogniButton btnYourTurn = (CogniButton) findViewById(R.id.btnYourTurn);
        btnYourTurn.setVisibility(View.INVISIBLE);
        ProgressBar progressBarYourTurn = (ProgressBar) findViewById(R.id.progressBarYourTurn);
        progressBarYourTurn.setVisibility(View.VISIBLE);

        int quesAnsThisTurn = mChallenge.getQuesAnsThisTurn();
        if (quesAnsThisTurn == Constants.Questions.NUM_QUESTIONS_PER_TURN) { // All questions have been answered
            navigateToBattleshipAttackActivity();
        } else {
            List<String> questionIds = mChallenge.getThisTurnQuestionIds();
            if (questionIds != null && questionIds.size() > 0) {
                navigateToChallengeQuestionActivity(questionIds.get(quesAnsThisTurn));
            } else {
                chooseThreeQuestionIdsThenNavigate(); // TODO:2 do this during onCreate?
            }
        }
    }

    private void chooseThreeQuestionIdsThenNavigate() {
        Question.chooseThreeQuestionIds(mChallenge, mCurrentUser1or2).continueWith(new Continuation<List<String>, Void>() {
            @Override
            public Void then(Task<List<String>> task) throws Exception {
                if (task.isFaulted()) {
                    task.getError().printStackTrace();
                    Log.e("chooseThreeQuestionIds", task.getError().getMessage());
                    return null;
                }
                List<String> questionIds = task.getResult();
                navigateToChallengeQuestionActivity(questionIds.get(0));
                return null;
            }
        });
    }

    private void navigateToChallengeQuestionActivity(String questionId) {
        Intent intent = new Intent(this, ChallengeQuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, mCurrentUser1or2);
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, questionId);
        startActivity(intent);
    }

    private void navigateToBattleshipAttackActivity() {
        Intent intent = new Intent(this, BattleshipAttackActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, mCurrentUser1or2);
        startActivity(intent);
    }

    public void navigateToQuestionHistoryActivity(View view) {
        Intent intent = new Intent(this, QuestionHistoryActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
        startActivity(intent);
    }

    public void navigateToChallengeAnalyticsActivity(View view) {
        Intent intent = new Intent(this, ChallengeAnalyticsActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, mCurrentUser1or2);
        startActivity(intent);
    }

    private void hideActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
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
        unbindDrawables(findViewById(R.id.rootView));
        System.gc();
    }

    @Override
    public void onReceiveHandler() {
        initChallenge();
        showOrHideButtons();
    }

    @Override
    public JSONObject getConditions() {
        JSONObject conditions = new JSONObject();
        try {
            conditions.put(Constants.NotificationData.ACTIVITY, Constants.NotificationData.Activity.CHALLENGE_ACTIVITY);
            conditions.put(Constants.NotificationData.challengeId, mChallengeId);
        } catch (JSONException e) { e.printStackTrace(); }
        return conditions;
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }
}
