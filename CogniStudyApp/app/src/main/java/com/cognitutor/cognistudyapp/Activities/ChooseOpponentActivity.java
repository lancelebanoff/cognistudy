package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

public class ChooseOpponentActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     */
    private Intent mIntent;
    private String challengeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_opponent);
        mIntent = getIntent();
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
                            if(e == null) {
                                ChallengeUserData user2Data = new ChallengeUserData(user2PublicUserData);
                                user2Data.saveInBackground();

                                challenge.setUser2Data(user2Data);
                                challenge.saveInBackground();
                            }
                            else {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                return null;
            }
        });
    }
}
