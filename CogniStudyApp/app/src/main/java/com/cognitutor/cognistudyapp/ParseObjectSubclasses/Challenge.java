package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by Lance on 1/8/2016.
 */
@ParseClassName("Challenge")
public class Challenge extends ParseObject {

    public class Columns {
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

    public String getNumShotsRemaining() {
        return getString(Columns.numShotsRemaining);
    }

    public void setNumShotsRemaining(String numShotsRemaining) {
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

    public boolean getAccepted() {
        return getBoolean(Columns.accepted);
    }

    public void setAccepted(boolean accepted) {
        put(Columns.accepted, accepted);
    }
}
