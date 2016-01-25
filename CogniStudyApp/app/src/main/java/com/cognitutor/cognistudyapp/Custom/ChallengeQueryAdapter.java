package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChallengeActivity;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

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
                                Intent intent = new Intent(mActivity, ChallengeActivity.class);
                                intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, challenge.getObjectId());
                                intent.putExtra(Constants.IntentExtra.USER1OR2, user1or2);
                                mActivity.startActivity(intent);
                            }
                        });
                    }
                });
            }
        });

        return finalView;
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
        holder.setTxtSubjects(currentUserData.getSubjects());
        holder.txtScore.setText("" + currentUserData.getScore() + " - " + opponentUserData.getScore());
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
