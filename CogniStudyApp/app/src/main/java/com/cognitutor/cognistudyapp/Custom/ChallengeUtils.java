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
            final Activity activity, String challengeId, final int user1or2, final boolean canBeAttacked) {

        final Capture<Challenge> challengeCapture = new Capture<>(null);
        final Capture<ChallengeUserData> challengeUserDataCapture = new Capture<>(null);
        final Capture<ChallengeUserData> otherUserDataCapture = new Capture<>(null);
        final Capture<GameBoard> gameBoardCapture = new Capture<>(null);

        Task<BattleshipBoardManager> task = Challenge.getChallenge(challengeId)
                .onSuccessTask(new Continuation<Challenge, Task<ChallengeUserData>>() {
                    @Override
                    public Task<ChallengeUserData> then(Task<Challenge> task) throws Exception {
                        Challenge challenge = task.getResult();
                        challengeCapture.set(challenge);
                        int otherUser1or2 = user1or2 == 1 ? 2 : 1;
                        return Challenge.getChallengeUserData(challenge, otherUser1or2);
                    }
                }).onSuccessTask(new Continuation<ChallengeUserData, Task<ChallengeUserData>>() {
                    @Override
                    public Task<ChallengeUserData> then(Task<ChallengeUserData> task) {
                        ChallengeUserData otherUserData = task.getResult();
                        otherUserDataCapture.set(otherUserData);
                        Challenge challenge = challengeCapture.get();
                        return Challenge.getChallengeUserData(challenge, user1or2);
                    }
                }).onSuccessTask(new Continuation<ChallengeUserData, Task<GameBoard>>() {
                    @Override
                    public Task<GameBoard> then(Task<ChallengeUserData> task) {
                        ChallengeUserData challengeUserData = task.getResult();
                        challengeUserDataCapture.set(challengeUserData);
                        return challengeUserData.getGameBoard();
                    }
                }).onSuccessTask(new Continuation<GameBoard, Task<List<Ship>>>() {
                    @Override
                    public Task<List<Ship>> then(Task<GameBoard> task) {
                        GameBoard gameBoard = task.getResult();
                        gameBoardCapture.set(gameBoard);
                        if(gameBoard == null) { // If the other player hasn't set up their board yet
                            return null;
                        }
                        List<Ship> ships = gameBoard.getShips();
                        return ParseObject.fetchAllIfNeededInBackground(ships);
                    }
                }).onSuccess(new Continuation<List<Ship>, BattleshipBoardManager>() {
                    @Override
                    public BattleshipBoardManager then(Task<List<Ship>> task) throws Exception {
                        List<Ship> ships = null;
                        if(task != null) {
                            ships = task.getResult();
                        }
                        Challenge challenge = challengeCapture.get();
                        ChallengeUserData challengeUserData = challengeUserDataCapture.get();
                        ChallengeUserData otherUserData = otherUserDataCapture.get();
                        GameBoard gameBoard = gameBoardCapture.get();
                        return new BattleshipBoardManager(activity, challenge, challengeUserData,
                                otherUserData, gameBoard, ships, canBeAttacked);
                    }
                });

        return task;
    }
}
