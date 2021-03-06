package com.cognitutor.cognistudyapp.Activities;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.ChallengeUtils;
import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;

public class ChooseBoardConfigurationActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;
    private String mChallengeId;
    private int mUser1or2;
    private Challenge mChallenge;
    private boolean mIsComputerOpponent;

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        mIntent = getIntent();
        mChallengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        mUser1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        mChallenge = Challenge.getChallenge(mChallengeId);
        mIsComputerOpponent = mChallenge.getChallengeType().equals(Constants.ChallengeType.ONE_PLAYER);

        initializeBoard();
        showTutorialDialogIfNeeded(Constants.Tutorial.CHOOSE_BOARD_CONFIGURATION, null);
    }

    private void initializeBoard() {
        ChallengeUtils.initializeNewBattleshipBoardManager(this, mChallengeId, mUser1or2, false)
                .continueWith(new Continuation<BattleshipBoardManager, Void>() {
                    @Override
                    public Void then(Task<BattleshipBoardManager> task) throws Exception {
                        mBattleshipBoardManager = task.getResult();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initializeGridLayouts();
                                showButtons();
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
                mBattleshipBoardManager.placeShips();
                removeOnGlobalLayoutListener(mShipsGridLayout, this);
            }
        });

        mTargetsGridLayout = (GridLayout) findViewById(R.id.targetsGridLayout);
        mBattleshipBoardManager.setTargetsGridLayout(mTargetsGridLayout);
        observer = mTargetsGridLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mBattleshipBoardManager.drawAllEmptyTargets();
                removeOnGlobalLayoutListener(mTargetsGridLayout, this);
            }
        });
    }

    private void showButtons() {
        CogniButton btnRandomize = (CogniButton) findViewById(R.id.btnRandomize);
        btnRandomize.setVisibility(View.VISIBLE);
        CogniButton btnStartChallenge = (CogniButton) findViewById(R.id.btnStartChallenge);
        btnStartChallenge.setVisibility(View.VISIBLE);
    }

    public void onClick_Randomize(View view) {
        mBattleshipBoardManager.placeShips();
    }

    public void onClick_btnStartChallenge(View view) {
        mBattleshipBoardManager.saveNewGameBoard();
        if (mIsComputerOpponent) {
            createComputerBoard();
            mChallenge.setActivated(true);
            mChallenge.setOtherTurnUserId(PublicUserData.getComputerPublicUserData().getBaseUserId());
            setChallengeAccepted(mChallenge);
            navigateToChallengeActivity();
            finish();
        } else if (mUser1or2 == 1) {
            setChallengeActivated(mChallenge);
            sendChallengeRequestNotification();
            boolean dialogShown = showTutorialDialogIfNeeded(Constants.Tutorial.CHALLENGE_REQUEST_SENT, new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });

            if (!dialogShown) {
                Toast.makeText(this, "Challenge request sent", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            setChallengeAccepted(mChallenge);
            navigateToChallengeActivity();
            finish();
        }
    }

    private void sendChallengeRequestNotification() {
        String challengeId = mChallenge.getObjectId();
        String senderBaseUserId = UserUtils.getCurrentUserId();
        String receiverBaseUserId = mChallenge.getOpponentBaseUserId();
        int user1Or2 = mChallenge.getOpponentUser1Or2();
        CommonUtils.sendChallengeNotification(Constants.CloudCodeFunction.SendChallengeNotification.NotificationType.CHALLENGE_REQUEST,
                challengeId, senderBaseUserId, receiverBaseUserId, user1Or2);
    }

    private void createComputerBoard() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                GridLayout computerShipsGridLayout = (GridLayout) findViewById(R.id.computerShipsGridLayout);
                mBattleshipBoardManager.setShipsGridLayout(computerShipsGridLayout);
                mBattleshipBoardManager.createComputerBoard();
            }
        });
    }

    private void setChallengeActivated(Challenge challenge) {
        challenge.setTimeLastPlayed(new Date());
        challenge.setActivated(true);
        MainFragment.loadFromLocalDatastore = true;
        challenge.saveInBackground();
//        challenge.saveInBackground().continueWith(new Continuation<Void, Void>() {
//            @Override
//            public Void then(Task<Void> task) throws Exception {
//                // Refresh Challenge list
//                Intent refreshIntent = new Intent(Constants.IntentExtra.REFRESH_CHALLENGE_LIST);
//                refreshIntent.putExtra(Constants.IntentExtra.REFRESH_CHALLENGE_LIST, true);
//                sendBroadcast(refreshIntent);
//                return null;
//            }
//        });
    }

    private void setChallengeAccepted(Challenge challenge) {
        challenge.setAccepted(true);
        challenge.setTimeLastPlayed(new Date());
        challenge.setCurTurnUserId(UserUtils.getCurrentUserId());
        challenge.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                // Refresh Challenge list
                Intent refreshIntent = new Intent(Constants.IntentExtra.REFRESH_CHALLENGE_LIST);
                refreshIntent.putExtra(Constants.IntentExtra.REFRESH_CHALLENGE_LIST, true);
                sendBroadcast(refreshIntent);
                return null;
            }
        });
    }

    private void navigateToChallengeActivity() {
        Intent intent = new Intent(this, ChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mChallengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, mUser1or2);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);

        if(user1or2 == 1) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.title_dialog_cancel_challenge)
                    .setMessage(R.string.message_dialog_cancel_challenge)
                    .setNegativeButton(R.string.no_dialog_cancel_challenge, null)
                    .setPositiveButton(R.string.yes_dialog_cancel_challenge, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {

                            ParseQuery<Challenge> query = Challenge.getQuery();
                            query.getInBackground(mChallengeId, new GetCallback<Challenge>() {
                                @Override
                                public void done(Challenge challenge, ParseException e) {
                                    if (e == null) {
                                        ParseObject.unpinAllInBackground(challenge.getObjectId());
                                        final HashMap<String, Object> params = new HashMap<>();
                                        params.put(Challenge.Columns.objectId, challenge.getObjectId());
                                        ParseCloud.callFunctionInBackground(
                                                Constants.CloudCodeFunction.DELETE_CHALLENGE,
                                                params, new FunctionCallback<Object>() {
                                                    @Override
                                                    public void done(Object object, ParseException e) {
                                                        if (e != null) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                    } else {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            ChooseBoardConfigurationActivity.super.onBackPressed();
                        }
                    }).create().show();
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
}
