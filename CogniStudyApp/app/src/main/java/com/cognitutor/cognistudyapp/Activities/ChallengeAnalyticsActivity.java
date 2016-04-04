package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.GameBoard;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.R;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import java.util.List;

public class ChallengeAnalyticsActivity extends CogniActivity {

    /**
     * Extras:
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;
    private Challenge mChallenge;
    private ChallengeUserData mCurrUserData;
    private ChallengeUserData mOpponentUserData;
    private GameBoard mCurrGameBoard;
    private GameBoard mOpponentGameBoard;
    private LinearLayout mLlSubjectStats;
    private LinearLayout mLlBattleshipStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_analytics);
        mIntent = getIntent();

        getAndDisplayStats();
    }

    private void getAndDisplayStats() {
        final String challengeId = mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
        final int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        final int opponentUser1or2 = user1or2 == 1 ? 2 : 1;
        mChallenge = Challenge.getChallenge(challengeId);
        mCurrUserData = mChallenge.getChallengeUserData(user1or2);
        mOpponentUserData = mChallenge.getChallengeUserData(opponentUser1or2);
        try {
            mCurrGameBoard = mCurrUserData.getGameBoard().fetchIfNeeded();
            mOpponentGameBoard = mOpponentUserData.getGameBoard().fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        displayStats();
    }

    private void displayStats() {
        mLlSubjectStats = (LinearLayout) findViewById(R.id.llSubjectStats);
        addHeaderListItem();
        String[] subjects = Constants.Subject.getSubjects();
        for (int i = 0; i < subjects.length; i++) {
            String subject = subjects[i];
            View listItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
            fillListItemForSubject(listItem, subject);
            if (i == subjects.length - 1) {
                removeDividerLine(listItem);
            }
            mLlSubjectStats.addView(listItem);
        }
        fillBattleshipListItems();
    }

    private void addHeaderListItem() {
        View listItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
        ImageView ivIcon = (ImageView) listItem.findViewById(R.id.ivIcon);
        ivIcon.setImageResource(R.drawable.icon_empty);
        TextView txtLabel = (TextView) listItem.findViewById(R.id.txtLabel);
        txtLabel.setText("Subject");
        TextView txtCurrentValue = (TextView) listItem.findViewById(R.id.txtCurrentValue);
        txtCurrentValue.setText("You");
        TextView txtOpponentValue = (TextView) listItem.findViewById(R.id.txtOpponentValue);
        txtOpponentValue.setText("Them");
        enlargeDividerLine(listItem);
        mLlSubjectStats.addView(listItem);
    }

    private void fillListItemForSubject(View listItem, String subject) {
        setSubjectIcon(listItem, subject);
        TextView txtLabel = (TextView) listItem.findViewById(R.id.txtLabel);
        txtLabel.setText(subject);
        displayPercentages(listItem, subject);
    }

    private void setSubjectIcon(View listItem, String subject) {
        int icon;
        switch (subject) {
            case Constants.Subject.ENGLISH:
                icon = R.drawable.icon_english;
                break;
            case Constants.Subject.MATH:
                icon = R.drawable.icon_math;
                break;
            case Constants.Subject.SCIENCE:
                icon = R.drawable.icon_science;
                break;
            default:
                icon = R.drawable.icon_reading;
                break;
        }
        ImageView ivSubject = (ImageView) listItem.findViewById(R.id.ivIcon);
        ivSubject.setImageResource(icon);
    }

    private void displayPercentages(View listItem, String subject) {
        ParseRelation<Response> currUserResponses = mCurrUserData.getResponses();
        int currUserViewId = R.id.txtCurrentValue;
        displayPercentage(listItem, subject, currUserResponses, currUserViewId);
        ParseRelation<Response> opponentUserResponses = mOpponentUserData.getResponses();
        int opponentUserViewId = R.id.txtOpponentValue;
        displayPercentage(listItem, subject, opponentUserResponses, opponentUserViewId);
    }

    private void displayPercentage(final View listItem, final String subject, final ParseRelation<Response> responses, final int viewId) {
        final TextView txtPercentage = (TextView) listItem.findViewById(viewId);

        if (responses == null) {
            txtPercentage.setText("-");
            return;
        }

        final ParseQuery<Question> innerQuery = Question.getQuery().whereEqualTo(Question.Columns.subject, subject);
        ParseQuery<Response> query = responses.getQuery()
                .whereMatchesQuery(Response.Columns.question, innerQuery);

        query.countInBackground(new CountCallback() {
            @Override
            public void done(final int totalCount, ParseException e) {
                ParseQuery<Response> query = responses.getQuery()
                        .whereMatchesQuery(Response.Columns.question, innerQuery)
                        .whereEqualTo(Response.Columns.correct, true);

                if (totalCount == 0) {
                    txtPercentage.setText("-");
                    return;
                }

                query.countInBackground(new CountCallback() {
                    @Override
                    public void done(int correctCount, ParseException e) {
                        int percentage = (int) (correctCount * 100.0 / totalCount);
                        txtPercentage.setText(percentage + "%");
                    }
                });
            }
        });
    }

    private void fillBattleshipListItems() {
        mLlBattleshipStats = (LinearLayout) findViewById(R.id.llBattleshipStats);
        addBattleshipHeaderListItem();
        fillHitsAndMissesListItems();
        fillBattleshipsDestroyedListItem();
        fillNumTurnsListItem();
    }

    private void addBattleshipHeaderListItem() {
        View listItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
        ImageView ivIcon = (ImageView) listItem.findViewById(R.id.ivIcon);
        ivIcon.setImageResource(R.drawable.icon_empty);
        TextView txtLabel = (TextView) listItem.findViewById(R.id.txtLabel);
        txtLabel.setText("Game Stats");
        TextView txtCurrentValue = (TextView) listItem.findViewById(R.id.txtCurrentValue);
        txtCurrentValue.setText("You");
        TextView txtOpponentValue = (TextView) listItem.findViewById(R.id.txtOpponentValue);
        txtOpponentValue.setText("Them");
        enlargeDividerLine(listItem);
        mLlBattleshipStats.addView(listItem);
    }

    private void fillHitsAndMissesListItems() {
        View hitsListItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
        View missesListItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
        ImageView ivHitIcon = (ImageView) hitsListItem.findViewById(R.id.ivIcon);
        ivHitIcon.setImageResource(R.drawable.icon_hit);
        ImageView ivMissIcon = (ImageView) missesListItem.findViewById(R.id.ivIcon);
        ivMissIcon.setImageResource(R.drawable.icon_miss);
        TextView txtHitsLabel = (TextView) hitsListItem.findViewById(R.id.txtLabel);
        txtHitsLabel.setText("Hits");
        TextView txtMissesLabel = (TextView) missesListItem.findViewById(R.id.txtLabel);
        txtMissesLabel.setText("Misses");
        int currUserViewId = R.id.txtCurrentValue;
        fillHitsAndMissesForUser(hitsListItem, missesListItem, mOpponentGameBoard, currUserViewId);
        int opponentUserViewId = R.id.txtOpponentValue;
        fillHitsAndMissesForUser(hitsListItem, missesListItem, mCurrGameBoard, opponentUserViewId);
        mLlBattleshipStats.addView(hitsListItem);
        mLlBattleshipStats.addView(missesListItem);
    }

    private void fillHitsAndMissesForUser(View hitsListItem, View missesListItem, GameBoard gameBoard, int viewId) {
        int numHits = 0, numMisses = 0;
        List<List<String>> boardPositionStatus = gameBoard.getStatus();
        for (List<String> row : boardPositionStatus) {
            for (String space : row) {
                if (space.equals(Constants.GameBoardPositionStatus.HIT)) {
                    numHits++;
                } else if (space.equals(Constants.GameBoardPositionStatus.MISS)) {
                    numMisses++;
                }
            }
        }
        TextView txtHits = (TextView) hitsListItem.findViewById(viewId);
        txtHits.setText("" + numHits);
        TextView txtMisses = (TextView) missesListItem.findViewById(viewId);
        txtMisses.setText("" + numMisses);
    }

    private void fillBattleshipsDestroyedListItem() {
        View listItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
        ImageView ivIcon = (ImageView) listItem.findViewById(R.id.ivIcon);
        ivIcon.setImageResource(R.drawable.icon_score);
        TextView txtLabel = (TextView) listItem.findViewById(R.id.txtLabel);
        txtLabel.setText("Score");
        int currUserViewId = R.id.txtCurrentValue;
        fillBattleshipsDestroyedForUser(listItem, mCurrUserData, currUserViewId);
        int opponentUserViewId = R.id.txtOpponentValue;
        fillBattleshipsDestroyedForUser(listItem, mOpponentUserData, opponentUserViewId);
        mLlBattleshipStats.addView(listItem);
    }

    private void fillBattleshipsDestroyedForUser(View listItem, ChallengeUserData challengeUserData, int viewId) {
        TextView txtBattleshipsDestroyed = (TextView) listItem.findViewById(viewId);
        int score = challengeUserData.getScore();
        txtBattleshipsDestroyed.setText("" + score);
    }

    private void fillNumTurnsListItem() {
        View listItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
        ImageView ivIcon = (ImageView) listItem.findViewById(R.id.ivIcon);
        ivIcon.setImageResource(R.drawable.icon_turn);
        TextView txtLabel = (TextView) listItem.findViewById(R.id.txtLabel);
        txtLabel.setText("Turns taken");
        int totalTurns = mChallenge.getNumTurns();
        int user1ViewId, user2ViewId;
        int user1or2 = mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1);
        if (user1or2 == 1) {
            user1ViewId = R.id.txtCurrentValue;
            user2ViewId = R.id.txtOpponentValue;
        } else {
            user1ViewId = R.id.txtOpponentValue;
            user2ViewId = R.id.txtCurrentValue;
        }
        fillNumTurnsForUser(listItem, totalTurns / 2, user2ViewId);
        if (totalTurns % 2 == 0) {
            fillNumTurnsForUser(listItem, totalTurns / 2, user1ViewId);
        } else {
            fillNumTurnsForUser(listItem, totalTurns / 2 + 1, user1ViewId);
        }
        removeDividerLine(listItem);
        mLlBattleshipStats.addView(listItem);
    }

    private void fillNumTurnsForUser(View listItem, int numTurns, int viewId) {
        TextView txtTurns = (TextView) listItem.findViewById(viewId);
        txtTurns.setText("" + numTurns);
    }

    private void enlargeDividerLine(View listItem) {
        View dividerLine = listItem.findViewById(R.id.dividerLine);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) dividerLine.getLayoutParams();
        layoutParams.height = layoutParams.height * 3;
        dividerLine.setLayoutParams(layoutParams);
    }

    private void removeDividerLine(View listItem) {
        View dividerLine = listItem.findViewById(R.id.dividerLine);
        dividerLine.setVisibility(View.GONE);
    }
}
