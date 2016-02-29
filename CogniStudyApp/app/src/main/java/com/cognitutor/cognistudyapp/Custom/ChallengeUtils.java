package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Ship;
import com.parse.ParseObject;

import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * Created by Lance on 1/19/2016.
 */
public class ChallengeUtils {

    public static Task<BattleshipBoardManager> initializeNewBattleshipBoardManager(
            final Activity activity, String challengeId, final int user1or2, final boolean canBeAttacked) {

        final Capture<Challenge> challengeCapture = new Capture<>(null);

        Task<BattleshipBoardManager> task = Challenge.getChallengeInBackground(challengeId)
                .onSuccessTask(new Continuation<Challenge, Task<ChallengeUserData>>() {
                    @Override
                    public Task<ChallengeUserData> then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        challengeCapture.set(challenge);
                        return challenge.getChallengeUserData(user1or2).fetchInBackground();
                    }
                }).onSuccess(new Continuation<ChallengeUserData, BattleshipBoardManager>() {
                    @Override
                    public BattleshipBoardManager then(Task<ChallengeUserData> task) {
                        ChallengeUserData challengeUserData = task.getResult();
                        Challenge challenge = challengeCapture.get();
                        return new BattleshipBoardManager(activity, challenge, challengeUserData,
                                canBeAttacked);
                    }
                });

        return task;
    }

    public static Task<BattleshipBoardManager> initializeBattleshipBoardManager(
            final Activity activity, String challengeId, final int currentUser1or2,
            final int viewingUser1or2, final boolean canBeAttacked) {

        Task<BattleshipBoardManager> task = Challenge.getChallengeInBackground(challengeId)
                .onSuccess(new Continuation<Challenge, BattleshipBoardManager>() {
                    @Override
                    public BattleshipBoardManager then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        ChallengeUserData viewedUserData = challenge.getChallengeUserData(viewingUser1or2)
                                .fetch();
                        ChallengeUserData currentUserData = challenge.getChallengeUserData(currentUser1or2)
                                .fetch();
                        ChallengeUserData opponentUserData = challenge.getChallengeUserData(currentUser1or2 == 1 ? 2 : 1)
                                .fetch();

                        GameBoard gameBoard = viewedUserData.getGameBoard();
                        List<Ship> ships = null;
                        if(gameBoard != null) {
                            gameBoard = gameBoard.fetch();
                            ships = ParseObject.fetchAll(gameBoard.getShips());
                        }

                        return new BattleshipBoardManager(activity, challenge, viewedUserData,
                                currentUserData, opponentUserData, gameBoard, ships, canBeAttacked);
                    }
                });

        return task;
    }
}
