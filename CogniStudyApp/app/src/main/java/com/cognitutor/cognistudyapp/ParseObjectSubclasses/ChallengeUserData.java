package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.List;

import bolts.Task;

/**
 * Created by Lance on 1/9/2016.
 */
@ParseClassName("ChallengeUserData")
public class ChallengeUserData extends ParseObject {

    public class Columns {
        public static final String publicUserData = "publicUserData";
        public static final String score = "score";
        public static final String gameBoard = "gameBoard";
        public static final String responses = "responses";
        public static final String subjects = "subjects";
        public static final String categories = "categories";
    }

    public ChallengeUserData(PublicUserData publicUserData, List<String> subjects, List<String> categories) {
        put(Columns.publicUserData, publicUserData);
        put(Columns.score, 0);
        // TODO:1 put gameboard and responses
        put(Columns.subjects, subjects);
        put(Columns.categories, categories);
    }

    public ChallengeUserData(PublicUserData publicUserData) {
        put(Columns.publicUserData, publicUserData);
        put(Columns.score, 0);
        // TODO:1 put gameboard and responses
    }

    public ChallengeUserData() {}

    public PublicUserData getPublicUserData() {
        return (PublicUserData) getParseObject(Columns.publicUserData);
    }

    // TODO:1 get rid of useless methods

    public void setPublicUserData(PublicUserData publicUserData) {
        put(Columns.publicUserData, publicUserData);
    }

    public int getScore() {
        return getInt(Columns.score);
    }

    public void setScore(int score) {
        put(Columns.score, score);
    }

    public void incrementScore() {
        increment(Columns.score);
    }

    public Task<GameBoard> getGameBoard() {
        GameBoard gameBoard = (GameBoard) getParseObject(Columns.gameBoard);
        if(gameBoard != null) {
            return gameBoard.fetchInBackground();
        }
        else {
            return null;
        }
    }

    public void setGameBoard(GameBoard gameBoard) {
        put(Columns.gameBoard, gameBoard);
    }

    public List<ParseObject> getResponses() {
        return getList(Columns.responses);
    }

    public void setResponses(List<ParseObject> responses) {
        put(Columns.responses, responses);
    }

    public List<String> getSubjects() {
        return getList(Columns.subjects);
    }

    public void setSubjects(List<String> subjects) {
        put(Columns.subjects, subjects);
    }

    public List<String> getCategories() {
        return getList(Columns.categories);
    }

    public void setCategories(List<String> categories) {
        put(Columns.categories, categories);
    }
}
