package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.R;

import bolts.Continuation;
import bolts.Task;

public class BattleshipAttackActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private GridLayout mAnimationsGridLayout;
    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battleship_attack);
        mIntent = getIntent();

        // Exit ChallengeActivity
        Intent finishActivityIntent = new Intent(Constants.IntentExtra.FINISH_CHALLENGE_ACTIVITY);
        sendBroadcast(finishActivityIntent);

        initializeBoard();
        showTutorialDialogIfNeeded(Constants.Tutorial.ATTACK, null);
    }

    private void initializeBoard() {
        String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        int currentUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        int opponentUser1or2 = currentUser1or2 == 1 ? 2: 1;

        ChallengeUtils.initializeBattleshipBoardManager(this, challengeId, currentUser1or2, opponentUser1or2, true)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        TextView txtNumShotsRemaining = (TextView) findViewById(R.id.txtNumShotsRemaining);
                        mBattleshipBoardManager.startShowingNumShotsRemaining(txtNumShotsRemaining);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeGridLayouts();
                                showLoadingDone();
                            }
                        });

                        return null;
                    }
                });
    }

    private void initializeGridLayouts() {
        mShipsGridLayout = (GridLayout) findViewById(R.id.shipsGridLayout);
        mBattleshipBoardManager.setShipsGridLayout(mShipsGridLayout);
        ViewTreeObserver observer = mShipsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.drawDeadShips();
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

        mAnimationsGridLayout = (GridLayout) findViewById(R.id.animationsGridLayout);
        mBattleshipBoardManager.setAnimationsGridLayout(mAnimationsGridLayout);
        observer = mAnimationsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.initializeAnimationsGridLayout();
                removeOnGlobalLayoutListener(mAnimationsGridLayout, this);
            }
        });
    }

    private void showLoadingDone() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        RelativeLayout rlContent = (RelativeLayout) findViewById(R.id.rlContent);
        rlContent.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        onClick_btnDone(null);
    }

    public void showBtnDone() {
        CogniButton btnDone = (CogniButton) findViewById(R.id.btnDone);
        btnDone.setVisibility(View.VISIBLE);
    }

    public void onClick_btnDone(View view) {
        mBattleshipBoardManager.saveGameBoard();
        Challenge.getChallengeInBackground(mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID)).continueWith(new Continuation<Challenge, Void>() {
            @Override
            public Void then(Task<Challenge> task) throws Exception {
                Challenge challenge = task.getResult();
                boolean isComputerOpponent = challenge.getChallengeType().equals(Constants.ChallengeType.ONE_PLAYER);
                if (isComputerOpponent) {
                    navigateToChallengeActivity();
                    finish();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            boolean dialogShown = showTutorialDialogIfNeeded(Constants.Tutorial.OPPONENTS_TURN, new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            });

                            if (!dialogShown) {
                                finish();
                            }
                        }
                    });
                }
                return null;
            }
        });
    }

    private void navigateToChallengeActivity() {
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        startActivity(intent);
        finish();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.rootView));
        System.gc();
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
