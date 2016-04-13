package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.PracticeChallengeActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
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
public class ChallengeQueryAdapter extends ParseQueryAdapter<ParseObject> {

    private Activity mActivity;
    private MainFragment mFragment;

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
    public ChallengeQueryAdapter(Context context, MainFragment fragment, final List<Pair> keyValuePairs, final String orderByDateColumn) {
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = Challenge.getQuery();
//                        .fromLocalDatastore()
                for (Pair pair : keyValuePairs) {
                    query = query.whereEqualTo((String) pair.first, pair.second);
                }
                return query.orderByDescending(orderByDateColumn);
            }
        });
        mActivity = (Activity) context;
        mFragment = fragment;
    }

    // Use this constructor for past challenges, which uses an "or" query
    public ChallengeQueryAdapter(Context context, MainFragment fragment, final List<List<Pair>> keyValuePairsList, final String orderByDateColumn,
                                 boolean pastChallenges) {
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                List<ParseQuery<Challenge>> queries = new ArrayList<>();
                for (List<Pair> keyValuePairs : keyValuePairsList) {
                    ParseQuery query = Challenge.getQuery();
//                        .fromLocalDatastore()
                    for (Pair pair : keyValuePairs) {
                        query = query.whereEqualTo((String) pair.first, pair.second);
                    }
                    queries.add(query);
                }
                return ParseQuery.or(queries).orderByDescending(orderByDateColumn);
            }
        });
        mActivity = (Activity) context;
        mFragment = fragment;
    }

    @Override
    public View getItemView(ParseObject object, View view, ViewGroup parent) {
        final ViewHolder holder;
        final View finalView;
        if(view == null) {
            finalView = View.inflate(getContext(), R.layout.list_item_challenge, null);
            holder = createViewHolder(finalView);
            finalView.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
            return view;
        }

        super.getItemView(object, view, parent);

        final Challenge challenge = (Challenge) object;
        if(challenge.getChallengeType().equals(Constants.ChallengeType.PRACTICE)) {
            challenge.getUser1Data().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    ChallengeUserData challengeUserData = (ChallengeUserData) object;
                    setItemViewContentsForPractice(holder, challengeUserData);

                    finalView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            navigateToPracticeChallengeActivity(challenge.getObjectId(), 1);
                        }
                    });
                }
            });
        }
        else {
            challenge.getUser1Data().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    final ChallengeUserData user1Data = (ChallengeUserData) object;
                    challenge.getUser2Data().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                        @Override
                        public void done(ParseObject object, ParseException e) {
                            final ChallengeUserData user2Data = (ChallengeUserData) object;

                            String user1BaseUserId = user1Data.getPublicUserData().getBaseUserId();
                            String currentUserBaseUserId = PublicUserData.getPublicUserData().getBaseUserId();
                            ChallengeUserData currentChallengeUserData, opponentChallengeUserData;
                            final int user1or2;
                            if (user1BaseUserId.equals(currentUserBaseUserId)) {
                                currentChallengeUserData = user1Data;
                                opponentChallengeUserData = user2Data;
                                user1or2 = 1;
                            } else {
                                currentChallengeUserData = user2Data;
                                opponentChallengeUserData = user1Data;
                                user1or2 = 2;
                            }
                            setItemViewContents(holder, challenge, currentChallengeUserData, opponentChallengeUserData);

                            finalView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String userId = PublicUserData.getPublicUserData().getBaseUserId();
                                    if (!challenge.getAccepted() && challenge.getCurTurnUserId().equals(userId) && !challenge.getHasEnded()) {
                                        promptAcceptChallenge(challenge, user1or2);
                                    } else {
                                        navigateToChallengeActivity(challenge.getObjectId(), user1or2);
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }

        return finalView;
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

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) v.findViewById(R.id.txtName);
        holder.imgProfile = (RoundedImageView) v.findViewById(R.id.imgProfileRounded);
        holder.txtScore = (TextView) v.findViewById(R.id.txtScore);
        holder.txtDaysLeft = (TextView) v.findViewById(R.id.txtDaysLeftToPlay);
        return holder;
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

    private static class ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
        public TextView txtScore;
        public TextView txtDaysLeft;

        public void setTxtDaysLeft(Challenge challenge) {
            if (challenge.getChallengeType().equals(Constants.ChallengeType.ONE_PLAYER)) {
                txtDaysLeft.setVisibility(View.GONE);
                return;
            }

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

            if (calendarCurrentDate.compareTo(calendarEndDate) < 0) { // If the end date hasn't come yet
                String timeBetween = DateUtils.getTimeBetween(currentDate, endDate);
                txtDaysLeft.setText(timeBetween + " left to play");
            } else {
                String timeBetween = DateUtils.getTimeBetween(endDate, currentDate);
                txtDaysLeft.setText(timeBetween + " ago");
            }
        }
    }
}
