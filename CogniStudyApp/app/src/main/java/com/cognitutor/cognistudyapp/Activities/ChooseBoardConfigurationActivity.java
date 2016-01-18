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

import com.cognitutor.cognistudyapp.Custom.BattleshipBoardManager;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.R;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.HashMap;

public class ChooseBoardConfigurationActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     */
    private Intent mIntent;

    private GridLayout mShipsGridLayout;
    private GridLayout mTargetsGridLayout;
    private BattleshipBoardManager mBattleshipBoardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_board_configuration);
        mIntent = getIntent();
        // TODO:2 Delete challenge from database when hitting back button

        mBattleshipBoardManager = new BattleshipBoardManager(this, false);
        initializeGridLayouts();
    }

    public void onClick_Randomize(View view) {
        mBattleshipBoardManager.placeShips();
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

    public void onClick_btnStartChallenge(View view) {
        saveBoardConfiguration();

        Intent intent = new Intent(this, ChallengeActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveBoardConfiguration() {
        // TODO:1 save board configuration

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_cancel_challenge)
                .setMessage(R.string.message_dialog_cancel_challenge)
                .setNegativeButton(R.string.no_dialog_cancel_challenge, null)
                .setPositiveButton(R.string.yes_dialog_cancel_challenge, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {

                        String challengeId = mIntent.getStringExtra(
                                Constants.IntentExtra.ChallengeId.CHALLENGE_ID);
                        ParseQuery<Challenge> query = Challenge.getQuery();
                        query.getInBackground(challengeId, new GetCallback<Challenge>() {
                            @Override
                            public void done(Challenge challenge, ParseException e) {
                                if (e == null) {
                                    final HashMap<String, Object> params = new HashMap<>();
                                    params.put("challengeId", challenge.getObjectId());
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

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void removeOnGlobalLayoutListener(View v, ViewTreeObserver.OnGlobalLayoutListener listener){
        if (Build.VERSION.SDK_INT < 16) {
            v.getViewTreeObserver().removeGlobalOnLayoutListener(listener);
        } else {
            v.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
        }
    }
}
