package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.FollowButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseImageView;

import bolts.Continuation;
import bolts.Task;

public class StudentProfileActivity extends CogniActivity {

    /**
     * Extras:
     *      PUBLICUSERDATA_ID: String
     */
    private ViewHolder holder;
    private Intent mIntent;
    private PublicUserData mPublicUserData;
    private PrivateStudentData mCurrPrivateStudentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        holder = createViewHolder();
        mIntent = getIntent();

        try {
            mCurrPrivateStudentData = PublicUserData.getPublicUserData().getStudent().getPrivateStudentData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mPublicUserData = PublicUserData.getPublicUserData(mIntent.getStringExtra(Constants.IntentExtra.PUBLICUSERDATA_ID)); //TODO: Change this
        if(mPublicUserData == null) { return; } //TODO: Handle this?
        holder.txtName.setText(mPublicUserData.getDisplayName());
        holder.imgProfile.setParseFile(mPublicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();

        showOrHideButtonsAndTutorialDialog();
        if (mCurrPrivateStudentData.isFriendsWith(mPublicUserData)) {
            holder.btnFollow.setChecked(true);
        }
        else {
            holder.btnFollow.setChecked(false);
        }
    }

    private void showOrHideButtonsAndTutorialDialog() {
        if (mPublicUserData.getObjectId().equals(PublicUserData.getPublicUserData().getObjectId())) {
            holder.btnFollow.setVisibility(View.GONE);
            CogniButton btnUnfollow = (CogniButton) findViewById(R.id.btnUnfollow);
            btnUnfollow.setVisibility(View.GONE);
            CogniButton btnMessage = (CogniButton) findViewById(R.id.btnMessage);
            btnMessage.setVisibility(View.GONE);
            CogniButton btnChallenge = (CogniButton) findViewById(R.id.btnChallenge);
            btnChallenge.setVisibility(View.GONE);
        } else {
            showTutorialDialogIfNeeded(Constants.Tutorial.FOLLOW_STUDENT, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        doOrUndoFollow();
    }

    private void doOrUndoFollow() {
        boolean followed = mCurrPrivateStudentData.isFriendsWith(mPublicUserData);
        if(holder.btnFollow.isChecked() && !followed) {
            doFollow();
        }
        else if(!holder.btnFollow.isChecked() && followed) {
            doUnfollow();
        }
    }

    private void doFollow() {
        mCurrPrivateStudentData.addFriend(mPublicUserData);
        mCurrPrivateStudentData.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                mPublicUserData.pinInBackground(Constants.PinNames.CurrentUser);
                return null;
            }
        });
    }

    private void doUnfollow() {
        mCurrPrivateStudentData.removeFriend(mPublicUserData);
        mCurrPrivateStudentData.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                mPublicUserData.unpinInBackground();
                mPublicUserData.unpinInBackground(Constants.PinNames.CurrentUser);
                return null;
            }
        });
    }

    public void onClick_btnMessage(View view) {

    }

    public void onClick_btnChallenge(View view) {
        Intent intent = new Intent(this, NewChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.STUDENT_PROFILE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.USER1OR2, 1);
        intent.putExtra(Constants.IntentExtra.OPPONENT_BASEUSERID, mPublicUserData.getBaseUserId());
        startActivity(intent);
    }

    private ViewHolder createViewHolder() {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) findViewById(R.id.txtName);
        holder.imgProfile = (ParseImageView) findViewById(R.id.imgProfile);
        holder.btnFollow = (FollowButton) findViewById(R.id.btnFollow);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public ParseImageView imgProfile;
        public FollowButton btnFollow;
    }

    @Override
    protected void onDestroy() {
        mPublicUserData.unpinInBackground();
        super.onDestroy();
    }
}
