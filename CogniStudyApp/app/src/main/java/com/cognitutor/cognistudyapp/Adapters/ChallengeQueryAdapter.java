package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Kevin on 1/14/2016.
 */
public class ChallengeQueryAdapter extends ParseQueryAdapter<ParseObject> {

    private Activity mActivity;

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
    public ChallengeQueryAdapter(Context context, final List<Pair> keyValuePairs) {
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                ParseQuery query = Challenge.getQuery();
//                        .fromLocalDatastore()
                for (Pair pair : keyValuePairs) {
                    query = query.whereEqualTo((String) pair.first, pair.second);
                }
                return query;
            }
        });
        mActivity = (Activity) context;
    }

    // Use this constructor for past challenges, which uses an "or" query
    public ChallengeQueryAdapter(Context context, final List<List<Pair>> keyValuePairsList, boolean pastChallenges) {
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
                return ParseQuery.or(queries);
            }
        });
        mActivity = (Activity) context;
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
                        setItemViewContents(holder, currentChallengeUserData, opponentChallengeUserData);

                        finalView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String userId = PublicUserData.getPublicUserData().getBaseUserId();
                                if(!challenge.getAccepted() && challenge.getCurTurnUserId().equals(userId)) {
                                    promptAcceptChallenge(challenge, user1or2);
                                }
                                else {
                                    navigateToChallengeActivity(challenge.getObjectId(), user1or2);
                                }
                            }
                        });
                    }
                });
            }
        });

        return finalView;
    }

    private void navigateToChallengeActivity(String challengeId, int user1or2) {
        Intent intent = new Intent(mActivity, ChallengeActivity.class);
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
                .setTitle(R.string.title_dialog_cancel_challenge)
                .setMessage(R.string.message_dialog_accept_challenge)
                .setNeutralButton(R.string.cancel_dialog_accept_challenge, null)
                .setNegativeButton(R.string.no_dialog_accept_challenge, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        challenge.setAccepted(true);
                        challenge.setHasEnded(true);
                        challenge.setEndDate(new Date());
                        challenge.setWinner(Constants.ChallengeAttribute.Winner.NO_WINNER);
                        challenge.saveInBackground();
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
        holder.txtSubjects = (TextView) v.findViewById(R.id.txtSubjects);
        holder.txtScore = (TextView) v.findViewById(R.id.txtScore);
        return holder;
    }

    private void setItemViewContents(ViewHolder holder, ChallengeUserData currentUserData, ChallengeUserData opponentUserData) {
        holder.imgProfile.setParseFile(opponentUserData.getPublicUserData().getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(opponentUserData.getPublicUserData().getDisplayName());
        if(currentUserData.getSubjects() != null) {
            holder.setTxtSubjects(currentUserData.getSubjects());
            holder.txtScore.setText("" + currentUserData.getScore() + " - " + opponentUserData.getScore());
        }
        else {
            holder.txtSubjects.setText("");
            holder.txtScore.setText("");
        }
    }

    private static class ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
        public TextView txtSubjects;
        public TextView txtScore;

        public void setTxtSubjects(List<String> selectedSubjects) {
            String[] subjects = Constants.getAllConstants(Constants.Subject.class);

            String output = "";
            for(String subject : subjects) {
                if(selectedSubjects.contains(subject)) {
                    output += subject.substring(0, 1) + " ";
                }
                else {
                    output += "  ";
                }
            }
            txtSubjects.setText(output);
        }
    }
}
