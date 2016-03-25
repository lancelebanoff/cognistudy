package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;
import java.util.List;

import bolts.Capture;
import bolts.Continuation;
import bolts.Task;

/**
 * Created by Lance on 1/8/2016.
 */
@ParseClassName("Challenge")
public class Challenge extends ParseObject {

    public class Columns {
        public static final String objectId = "objectId";
        public static final String challengeType = "challengeType";
        public static final String user1Data = "user1Data";
        public static final String user2Data = "user2Data";
        public static final String curTurnUserId = "curTurnUserId";
        public static final String otherTurnUserId = "otherTurnUserId";
        public static final String quesAnsThisTurn = "quesAnsThisTurn";
        public static final String numShotsRemaining = "numShotsRemaining";
        public static final String startDate = "startDate";
        public static final String endDate = "endDate";
        public static final String timeLastPlayed = "timeLastPlayed";
        public static final String numTurns = "numTurns";
        public static final String winner = "winner";
        public static final String accepted = "accepted";
        public static final String activated = "activated";
        public static final String hasEnded = "hasEnded";
        public static final String thisTurnQuestionIds = "thisTurnQuestionIds";
        public static final String correctAnsThisTurn = "correctAnsThisTurn";
    }

    public Challenge(ChallengeUserData user1Data, String challengeType) {
        setUser1Data(user1Data);
        setChallengeType(challengeType);
        setStartDate(new Date());
        setNumTurns(0);
        setAccepted(false);
        setActivated(false);
        setHasEnded(false);
        setQuesAnsThisTurn(0);
        setCorrectAnsThisTurn(0);
        setNumShotsRemaining(0);
    }

    public Challenge() {}

    public static ParseQuery<Challenge> getQuery() {
        return ParseQuery.getQuery(Challenge.class);
    }

    public static Task<Challenge> getChallengeInBackground(String objectId) {
        return getQuery().whereEqualTo(Columns.objectId, objectId).getFirstInBackground();
    }

    public static Challenge getChallenge(String objectId) {
        try {
            return getQuery().whereEqualTo(Columns.objectId, objectId).getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ChallengeUserData getChallengeUserData(int user1or2) {
        ChallengeUserData challengeUserData;
        switch (user1or2) {
            case 1:
                challengeUserData = getUser1Data();
                break;
            case 2:
                challengeUserData = getUser2Data();
                break;
            default:
                challengeUserData = null;
                break;
        }
        return challengeUserData;
    }

    public String getChallengeType() {
        return getString(Columns.challengeType);
    }

    public void setChallengeType(String challengeType) {
        put(Columns.challengeType, challengeType);
    }

    public ChallengeUserData getUser1Data() {
        return (ChallengeUserData) getParseObject(Columns.user1Data);
    }

    public void setUser1Data(ChallengeUserData user1Data) {
        put(Columns.user1Data, user1Data);
    }

    public ChallengeUserData getUser2Data() {
        return (ChallengeUserData) getParseObject(Columns.user2Data);
    }

    public void setUser2Data(ChallengeUserData user2Data) {
        put(Columns.user2Data, user2Data);
    }

    public String getCurTurnUserId() {
        return getString(Columns.curTurnUserId);
    }

    public void setCurTurnUserId(String curTurnUserId) {
        put(Columns.curTurnUserId, curTurnUserId);
    }

    public String getOtherTurnUserId() {
        return getString(Columns.otherTurnUserId);
    }

    public void setOtherTurnUserId(String otherTurnUserId) {
        put(Columns.otherTurnUserId, otherTurnUserId);
    }

    public int getQuesAnsThisTurn() {
        return getInt(Columns.quesAnsThisTurn);
    }

    public void setQuesAnsThisTurn(int quesAnsThisTurn) {
        put(Columns.quesAnsThisTurn, quesAnsThisTurn);
    }

    public int incrementAndGetQuesAnsThisTurn() {
        int quesAnsThisTurn = getQuesAnsThisTurn() + 1;
        setQuesAnsThisTurn(quesAnsThisTurn);
        return quesAnsThisTurn;
    }

    // If the number of shots has not been set yet, then set it. Then return the number of shots
    public int initializeAndGetNumShotsRemaining() {
        int numShotsRemaining = getInt(Columns.numShotsRemaining);
        if(numShotsRemaining == 0) {
            numShotsRemaining = getCorrectAnsThisTurn() + 1;
            setNumShotsRemaining(numShotsRemaining);
        }
        return numShotsRemaining;
    }

    public void setNumShotsRemaining(int numShotsRemaining) {
        put(Columns.numShotsRemaining, numShotsRemaining);
    }

    public Date getStartDate() {
        return getDate(Columns.startDate);
    }

    public void setStartDate(Date startDate) {
        put(Columns.startDate, startDate);
    }

    public Date getEndDate() {
        return getDate(Columns.endDate);
    }

    public void setEndDate(Date endDate) {
        put(Columns.endDate, endDate);
    }

    public Date getTimeLastPlayed() {
        return getDate(Columns.timeLastPlayed);
    }

    public void setTimeLastPlayed(Date timeLastPlayed) {
        put(Columns.timeLastPlayed, timeLastPlayed);
    }

    public int getNumTurns() {
        return getInt(Columns.numTurns);
    }

    public void setNumTurns(int numTurns) {
        put(Columns.numTurns, numTurns);
    }

    public void incrementNumTurns() {
        put(Columns.numTurns, getNumTurns() + 1);
    }

    public String getWinner() {
        return getString(Columns.winner);
    }

    public void setWinner(String winner) {
        put(Columns.winner, winner);
    }

    public boolean getAccepted() {
        return getBoolean(Columns.accepted);
    }

    public void setAccepted(boolean accepted) {
        put(Columns.accepted, accepted);
    }

    public boolean getActivated() {
        return getBoolean(Columns.activated);
    }

    public void setActivated(boolean activated) {
        put(Columns.activated, activated);
    }

    public boolean getHasEnded() {
        return getBoolean(Columns.hasEnded);
    }

    public void setHasEnded(boolean hasEnded) {
        put(Columns.hasEnded, hasEnded);
    }

    public List<String> getThisTurnQuestionIds() {
        return (List<String>) get(Columns.thisTurnQuestionIds);
    }

    public void setThisTurnQuestionIds(List<String> thisTurnQuestionIds) {
        put(Columns.thisTurnQuestionIds, thisTurnQuestionIds);
    }

    public int getCorrectAnsThisTurn() {
        return getInt(Columns.correctAnsThisTurn);
    }

    public void setCorrectAnsThisTurn(int correctAnsThisTurn) {
        put(Columns.correctAnsThisTurn, correctAnsThisTurn);
    }

    public void incrementCorrectAnsThisTurn() {
        put(Columns.correctAnsThisTurn, getCorrectAnsThisTurn() + 1);
    }

    public int getUser1Or2() {
        final Capture<String> user1BaseUserId = new Capture<>();
        final Capture<String> user2BaseUserId = new Capture<>();
        try {
            getChallengeUserDataBaseUserId(getUser1Data(), user1BaseUserId).waitForCompletion();
            ChallengeUserData user2Data = getUser2Data();
            if (user2Data != null) {
                getChallengeUserDataBaseUserId(getUser2Data(), user2BaseUserId).waitForCompletion();
            }
        } catch (InterruptedException e) { e.printStackTrace(); }
        String currentUserBaseUserId = PublicUserData.getPublicUserData().getBaseUserId();
        if (user1BaseUserId.get().equals(currentUserBaseUserId)) {
            return 1;
        } else if(user2BaseUserId.get().equals(currentUserBaseUserId)) {
            return 2;
        }
        else {
            return -1;
        }
    }

    private Task<Object> getChallengeUserDataBaseUserId(ChallengeUserData challengeUserData, final Capture<String> baseUserId) {
        return challengeUserData.fetchIfNeededInBackground().continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                ChallengeUserData fetched = (ChallengeUserData) task.getResult();
                return fetched.getPublicUserData().fetchIfNeededInBackground().continueWith(new Continuation<ParseObject, Object>() {
                    @Override
                    public Object then(Task<ParseObject> task) throws Exception {
                        PublicUserData pud = (PublicUserData) task.getResult();
                        baseUserId.set(pud.getBaseUserId());
                        return null;
                    }
                });
            }
        });
    }

    public ChallengeUserData getCurUserChallengeUserData() {
        switch (getUser1Or2()) {
            case 1:
                return getUser1Data();
            case 2:
                return getUser2Data();
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return "objectId: " + getObjectId() +
                " | curTurnUserId: " + getCurTurnUserId() +
                " | otherTurnUserId: " + getOtherTurnUserId();
    }
}
