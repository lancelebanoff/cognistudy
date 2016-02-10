package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * Created by Lance on 1/19/2016.
 */
public class ChallengeUtils {

    public static Task<BattleshipBoardManager> initializeBattleshipBoardManager(
            final Activity activity, String challengeId, final int user1or2, final boolean canBeAttacked) {

        final Capture<Challenge> challengeCapture = new Capture<>(null);
        final Capture<ChallengeUserData> challengeUserDataCapture = new Capture<>(null);

        Task<BattleshipBoardManager> task = Challenge.getChallenge(challengeId)
                .onSuccessTask(new Continuation<Challenge, Task<ChallengeUserData>>() {
                    @Override
                    public Task<ChallengeUserData> then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        challengeCapture.set(challenge);
                        return Challenge.getChallengeUserData(challenge, user1or2);
                    }
                }).onSuccessTask(new Continuation<ChallengeUserData, Task<GameBoard>>() {
                    @Override
                    public Task<GameBoard> then(Task<ChallengeUserData> task) {
                        ChallengeUserData challengeUserData = task.getResult();
                        challengeUserDataCapture.set(challengeUserData);
                        return challengeUserData.getGameBoard();
                    }
                }).onSuccess(new Continuation<GameBoard, BattleshipBoardManager>() {
                    @Override
                    public BattleshipBoardManager then(Task<GameBoard> task) {
                        GameBoard gameBoard;
                        if(task != null) {
                            gameBoard = task.getResult();
                        }
                        else {
                            gameBoard = null;
                        }
                        Challenge challenge = challengeCapture.get();
                        ChallengeUserData challengeUserData = challengeUserDataCapture.get();
                        return new BattleshipBoardManager(activity, challenge, challengeUserData,
                                gameBoard, canBeAttacked);
                    }
                });

        return task;
    }
}
