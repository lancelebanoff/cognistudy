package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

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
import com.parse.ParseUser;

import java.util.HashMap;

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
        showTutorialDialogIfNeeded(Constants.Tutorial.CHOOSE_OPPONENT, null);
    }

    private void createPeopleFragment() {
        PeopleFragment fragment = PeopleFragment.newInstance(new PeopleListOnClickHandler() {
            @Override
            public void onListItemClick(PublicUserData publicUserData) {
                chooseOpponent(publicUserData);
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }

    public void chooseOpponent(PublicUserData publicUserData) {
        saveOpponent(publicUserData);

        Intent intent = new Intent(this, ChooseBoardConfigurationActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID,
                mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2,
                mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        startActivity(intent);
        finish();
    }

    private void saveOpponent(final PublicUserData publicUserData) {
        String challengeId = mIntent.getStringExtra(
                Constants.IntentExtra.CHALLENGE_ID);
        ParseQuery<Challenge> query = Challenge.getQuery();
        query.getInBackground(challengeId, new GetCallback<Challenge>() {
            @Override
            public void done(Challenge challenge, ParseException e) {
                if (e == null) {
                    ChallengeUserData user2Data = new ChallengeUserData(publicUserData);
                    user2Data.saveInBackground();

                    challenge.setUser2Data(user2Data);
                    challenge.setCurTurnUserId(user2Data.getPublicUserData().getBaseUserId());
                    challenge.setOtherTurnUserId(ParseUser.getCurrentUser().getObjectId());
                    challenge.saveInBackground();
                } else {
                    e.printStackTrace();
                }
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
                                Constants.IntentExtra.CHALLENGE_ID);
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
