package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;

public class ChooseOpponentActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     */
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_opponent);
        mIntent = getIntent();

        createPeopleFragment();
    }

    private void createPeopleFragment() {
        PeopleFragment fragment = new PeopleFragment(new PeopleListOnClickHandler() {
            @Override
            public void onListItemClick(PublicUserData publicUserData) {
                onClick_tempButton(null);
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    public void onClick_tempButton(View view) {
        saveOpponent();

        // TODO:1 intent extras for which challenge this is
        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        intent.putExtra(Constants.IntentExtra.ChallengeId.CHALLENGE_ID,
                mIntent.getStringExtra(Constants.IntentExtra.ChallengeId.CHALLENGE_ID));
        startActivity(intent);
        finish();
    }

    private void saveOpponent() {
        String kevinBaseUserId = "4lXOYetlmq";
        Task<PublicUserData> task = PublicUserData.getPublicUserDataInBackground(kevinBaseUserId);
        task.continueWith(new Continuation<PublicUserData, Void>() {
            @Override
            public Void then(Task<PublicUserData> task) throws Exception {
                if (task.isCancelled()) {
                    // the save was cancelled.
                } else if (task.isFaulted()) {
                    // the save failed.
                    Exception error = task.getError();
                    error.printStackTrace();
                } else {
                    // the object was saved successfully.
                    final PublicUserData user2PublicUserData = task.getResult();

                    String challengeId = mIntent.getStringExtra(
                            Constants.IntentExtra.ChallengeId.CHALLENGE_ID);
                    ParseQuery<Challenge> query = Challenge.getQuery();
                    query.getInBackground(challengeId, new GetCallback<Challenge>() {
                        @Override
                        public void done(Challenge challenge, ParseException e) {
                            if (e == null) {
                                ChallengeUserData user2Data = new ChallengeUserData(user2PublicUserData);
                                user2Data.saveInBackground();

                                challenge.setUser2Data(user2Data);
                                challenge.saveInBackground();
                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                return null;
            }
        });
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

                        ChooseOpponentActivity.super.onBackPressed();
                    }
                }).create().show();
    }
}
