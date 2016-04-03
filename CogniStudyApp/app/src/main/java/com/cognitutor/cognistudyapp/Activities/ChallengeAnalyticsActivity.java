package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.R;
import com.parse.CountCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

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
        displayStats();
    }

    private void displayStats() {
        LinearLayout llChallengeStats = (LinearLayout) findViewById(R.id.llChallengeStats);
        String[] subjects = Constants.Subject.getSubjects();
        for (String subject : subjects) {
            View listItem = View.inflate(this, R.layout.list_item_challenge_stat, null);
            fillListItemForSubject(listItem, subject);
            llChallengeStats.addView(listItem);
        }
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
        ImageView ivSubject = (ImageView) listItem.findViewById(R.id.ivSubject);
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
}
