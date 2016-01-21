package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * Created by Lance on 1/19/2016.
 */
public class ChallengeUtils {

    public static Task<BattleshipBoardManager> initializeBattleshipBoardManager(
            final Activity activity, String challengeId, final int user1or2, final boolean canBeAttacked) {

        final Capture<Challenge> challengeCapture = new Capture<Challenge>(null);

        Task<BattleshipBoardManager> task = Challenge.getChallenge(challengeId)
                .onSuccessTask(new Continuation<Challenge, Task<ChallengeUserData>>() {
                    @Override
                    public Task<ChallengeUserData> then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        challengeCapture.set(challenge);
                        return Challenge.getChallengeUserData(challenge, user1or2);
                    }
                }).onSuccess(new Continuation<ChallengeUserData, BattleshipBoardManager>() {
                    @Override
                    public BattleshipBoardManager then(Task<ChallengeUserData> task) throws Exception {
                        ChallengeUserData challengeUserData = task.getResult();
                        Challenge challenge = challengeCapture.get();
                        return new BattleshipBoardManager(activity, challenge, challengeUserData, canBeAttacked);
                    }
                });

        return task;
    }
}
