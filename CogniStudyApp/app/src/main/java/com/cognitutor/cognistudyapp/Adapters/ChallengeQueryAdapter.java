package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.PracticeChallengeActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtilsCacheThenNetworkHelper;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Fragments.MainFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Kevin on 1/14/2016.
 */
public class ChallengeQueryAdapter extends CogniRecyclerAdapter<Challenge, ChallengeQueryAdapter.ViewHolder> {

    private MainFragment mFragment;
    private QueryUtils.ParseQueryBuilder<Challenge> mQueryBuilder;
    private QueryUtilsCacheThenNetworkHelper mCacheThenNetworkHelper;

    /*
    public PeopleQueryAdapter(Context context) {
        super(context, new ParseQueryAdapter.QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = PublicUserData.getQuery()
                        .fromLocalDatastore()
                        .whereEqualTo(PublicUserData.Columns.fbLinked, true)
                        .whereNotEqualTo(PublicUserData.Columns.baseUserId, ParseUser.getCurrentUser().getObjectId());
                return query;
            }
        });
    }
    */
    public ChallengeQueryAdapter(Activity activity, MainFragment fragment, final List<Pair> keyValuePairs) {
        super(activity, new ParseQueryAdapter.QueryFactory<Challenge>() {
            public ParseQuery create() {
                return getChallengeNonOrQuery(keyValuePairs).fromLocalDatastore();
            }
        }, true);
        mQueryBuilder = new QueryUtils.ParseQueryBuilder<Challenge>() {
            @Override
            public ParseQuery<Challenge> buildQuery() {
                return getChallengeNonOrQuery(keyValuePairs);
            }
        };
        mCacheThenNetworkHelper = new QueryUtilsCacheThenNetworkHelper();
        mFragment = fragment;
    }

    // Use this constructor for past challenges, which uses an "or" query
    public ChallengeQueryAdapter(Activity activity, MainFragment fragment, final List<List<Pair>> keyValuePairsList, boolean pastChallenges) {
        super(activity, new ParseQueryAdapter.QueryFactory<Challenge>() {
            public ParseQuery create() {
                return getChallengeOrQuery(keyValuePairsList).fromLocalDatastore();
            }
        }, true);
        mQueryBuilder = new QueryUtils.ParseQueryBuilder<Challenge>() {
            @Override
            public ParseQuery<Challenge> buildQuery() {
                return getChallengeOrQuery(keyValuePairsList);
            }
        };
        mCacheThenNetworkHelper = new QueryUtilsCacheThenNetworkHelper();
        mFragment = fragment;
    }

    private static ParseQuery<Challenge> getChallengeNonOrQuery(List<Pair> keyValuePairs) {
        ParseQuery query = Challenge.getQuery();
        for (Pair pair : keyValuePairs) {
            query = query.whereEqualTo((String) pair.first, pair.second);
        }
        return includeColumns(query);
    }

    private static ParseQuery<Challenge> getChallengeOrQuery(List<List<Pair>> keyValuePairsList) {
        List<ParseQuery<Challenge>> queries = new ArrayList<>();
        for (List<Pair> keyValuePairs : keyValuePairsList) {
            ParseQuery query = Challenge.getQuery();
            for (Pair pair : keyValuePairs) {
                query = query.whereEqualTo((String) pair.first, pair.second);
            }
            queries.add(query);
        }
        ParseQuery<Challenge> orQuery = ParseQuery.or(queries);
        return includeColumns(orQuery);
    }

    private static ParseQuery<Challenge> includeColumns(ParseQuery<Challenge> query) {
        query.include(Challenge.Columns.user1Data + "." + ChallengeUserData.Columns.publicUserData);
        query.include(Challenge.Columns.user2Data + "." + ChallengeUserData.Columns.publicUserData);
        return query;
    }

    public void loadFromNetwork() {
        mCacheThenNetworkHelper.findCacheThenNetworkInBackgroundCancelleablePinWithObjectId(true, this, mQueryBuilder);
    }

    private void navigateToChallengeActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(mActivity, ChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
        mActivity.startActivity(intent);
    }

    private void navigateToPracticeChallengeActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(mActivity, PracticeChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
        mActivity.startActivity(intent);
    }

    private void navigateToNewChallengeActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(mActivity, NewChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challengeId);
        intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
        mActivity.startActivity(intent);
    }

    private void promptAcceptChallenge(final Challenge challenge, final int user1or2) {

        new AlertDialog.Builder(mActivity)
                .setTitle(R.string.title_dialog_accept_challenge)
                .setMessage(R.string.message_dialog_accept_challenge)
                .setNeutralButton(R.string.cancel_dialog_accept_challenge, null)
                .setNegativeButton(R.string.no_dialog_accept_challenge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        challenge.setAccepted(true);
                        challenge.setHasEnded(true);
                        challenge.setTimeLastPlayed(new Date());
                        challenge.setEndDate(new Date());
                        challenge.setWinner(Constants.ChallengeAttribute.Winner.NO_WINNER);
                        challenge.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                mFragment.refresh();
                            }
                        });
                    }
                })
                .setPositiveButton(R.string.yes_dialog_accept_challenge, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        navigateToNewChallengeActivity(challenge.getObjectId(), user1or2);
                    }
                }).create().show();
    }

    private void setItemViewContents(ViewHolder holder, Challenge challenge, ChallengeUserData currentUserData, ChallengeUserData opponentUserData) {
        holder.imgProfile.setParseFile(opponentUserData.getPublicUserData().getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(opponentUserData.getPublicUserData().getDisplayName());
        holder.setTxtDaysLeft(challenge);

        if(currentUserData.getSubjects() != null) {
            holder.txtScore.setText("" + currentUserData.getScore() + " - " + opponentUserData.getScore());
        } else {
            holder.txtScore.setText("");
        }
    }

    private void setItemViewContentsForPractice(ViewHolder holder, ChallengeUserData challengeUserData) {
        holder.imgProfile.setParseFile(challengeUserData.getPublicUserData().getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtDaysLeft.setVisibility(View.GONE);
        holder.txtName.setGravity(Gravity.CENTER_VERTICAL);
        holder.txtName.setText("Practice");
        holder.txtScore.setVisibility(View.GONE);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_challenge, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Challenge challenge = getItem(position);
        if(challenge.getChallengeType().equals(Constants.ChallengeType.PRACTICE)) {

        }
        else {

        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
        public TextView txtScore;
        public TextView txtDaysLeft;

        public ViewHolder(View v) {
            super(v);
            txtName = (TextView) v.findViewById(R.id.txtName);
            imgProfile = (RoundedImageView) v.findViewById(R.id.imgProfileRounded);
            txtScore = (TextView) v.findViewById(R.id.txtScore);
            txtDaysLeft = (TextView) v.findViewById(R.id.txtDaysLeftToPlay);
        }

        public void setTxtDaysLeft(Challenge challenge) {
            Calendar calendarEndDate = Calendar.getInstance();
            if (challenge.getHasEnded()) {
                calendarEndDate.setTime(challenge.getEndDate());
            } else {
                calendarEndDate.setTime(challenge.getTimeLastPlayed());
                calendarEndDate.add(Calendar.DATE, Constants.ChallengeAttribute.NUM_DAYS_PER_TURN);
            }
            Date endDate = calendarEndDate.getTime();

            Date currentDate = new Date();
            Calendar calendarCurrentDate = Calendar.getInstance();
            calendarCurrentDate.setTime(currentDate);

            if(calendarCurrentDate.compareTo(calendarEndDate) < 0) { // If the end date hasn't come yet
                String timeBetween = DateUtils.getTimeBetween(currentDate, endDate);
                txtDaysLeft.setText(timeBetween + " left to play");
            } else {
                String timeBetween = DateUtils.getTimeBetween(endDate, currentDate);
                txtDaysLeft.setText(timeBetween + " ago");
            }
        }
    }
}
