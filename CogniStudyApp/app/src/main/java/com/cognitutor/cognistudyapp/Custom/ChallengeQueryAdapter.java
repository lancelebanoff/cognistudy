package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChallengeActivity;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.FindCallback;
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
    public ChallengeQueryAdapter(Context context) {
        super(context, new QueryFactory<ParseObject>() {
            public ParseQuery create() {
                String currentUserId = PublicUserData.getPublicUserData().getBaseUserId();
                ParseQuery query = Challenge.getQuery()
//                        .fromLocalDatastore()
                        .whereEqualTo(Challenge.Columns.otherTurnUserId, currentUserId);
                query.findInBackground(new FindCallback() {
                    @Override
                    public void done(List objects, ParseException e) {
                        if (e == null) {
                            Log.i("", "");
                        } else {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void done(Object o, Throwable throwable) {
                        if (throwable == null) {
                            Log.i("", "");
                        } else {
                            throwable.printStackTrace();
                        }
                    }
                });
                query = Challenge.getQuery()
//                        .fromLocalDatastore()
                        .whereEqualTo(Challenge.Columns.otherTurnUserId, currentUserId);
                return query;
            }
        });
        mActivity = (Activity) context;
    }

    @Override
    public View getItemView(ParseObject object, View view, ViewGroup parent) {
        final ViewHolder holder;
        if(view == null) {
            view = View.inflate(getContext(), R.layout.list_item_challenge, null);
            holder = createViewHolder(view);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
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
                        if (user1BaseUserId.equals(currentUserBaseUserId)) {
                            currentChallengeUserData = user1Data;
                            opponentChallengeUserData = user2Data;
                        }
                        else {
                            currentChallengeUserData = user2Data;
                            opponentChallengeUserData = user1Data;
                        }
                        setItemViewContents(holder, currentChallengeUserData, opponentChallengeUserData);
                    }
                });
            }
        });

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mActivity, ChallengeActivity.class);
                intent.putExtra(Constants.IntentExtra.ChallengeId.CHALLENGE_ID, challenge.getObjectId());
                mActivity.startActivity(intent);
            }
        });

        return view;
    }

    private ViewHolder createViewHolder(View v) {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) v.findViewById(R.id.txtName);
        holder.imgProfile = (RoundedImageView) v.findViewById(R.id.imgProfileRounded);
        holder.txtScore = (TextView) v.findViewById(R.id.txtScore);
        return holder;
    }

    private void setItemViewContents(ViewHolder holder, ChallengeUserData currentUserData, ChallengeUserData opponentUserData) {
        holder.imgProfile.setParseFile(opponentUserData.getPublicUserData().getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtName.setText(opponentUserData.getPublicUserData().getDisplayName());
        holder.txtScore.setText("" + currentUserData.getScore() + " - " + opponentUserData.getScore());
    }

    private static class ViewHolder {
        public TextView txtName;
        public RoundedImageView imgProfile;
        public TextView txtScore;
    }
}
