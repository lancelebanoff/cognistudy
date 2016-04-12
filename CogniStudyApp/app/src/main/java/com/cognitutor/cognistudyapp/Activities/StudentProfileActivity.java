package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseImageView;

import java.util.HashMap;

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
    private ViewSwitcher mViewSwitcher;
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
        if (mCurrPrivateStudentData.isFriendsWith(mPublicUserData)) {
            holder.imgFollowedStudent.setVisibility(View.VISIBLE);
        } else {
            holder.imgFollowedStudent.setVisibility(View.INVISIBLE);
        }

        showOrHideButtonsAndTutorialDialog();
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        if (mCurrPrivateStudentData.isFriendsWith(mPublicUserData)) {
            mViewSwitcher.showNext();
        }
    }

    private void showOrHideButtonsAndTutorialDialog() {
        if (mPublicUserData.getObjectId().equals(PublicUserData.getPublicUserData().getObjectId())) {
            CogniButton btnFollow = (CogniButton) findViewById(R.id.btnFollow);
            btnFollow.setVisibility(View.GONE);
            CogniButton btnUnfollow = (CogniButton) findViewById(R.id.btnUnfollow);
            btnUnfollow.setVisibility(View.GONE);
            CogniButton btnMessage = (CogniButton) findViewById(R.id.btnMessage);
            btnMessage.setVisibility(View.GONE);
        } else {
            showTutorialDialogIfNeeded(Constants.Tutorial.FOLLOW_STUDENT, null);
        }
    }

    public void onClick_btnFollow(View view) {
        mViewSwitcher.showNext();
        final CogniButton btnUnfollow = (CogniButton) findViewById(R.id.btnUnfollow);
        btnUnfollow.setClickable(false);
        holder.imgFollowedStudent.setVisibility(View.VISIBLE);

        mCurrPrivateStudentData.addFriend(mPublicUserData);
        mCurrPrivateStudentData.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                mPublicUserData.pinInBackground(Constants.PinNames.CurrentUser);
                btnUnfollow.setClickable(true);
                return null;
            }
        });
    }

    public void onClick_btnUnfollow(View view) {
        mViewSwitcher.showNext();
        final CogniButton btnFollow = (CogniButton) findViewById(R.id.btnUnfollow);
        btnFollow.setClickable(false);
        holder.imgFollowedStudent.setVisibility(View.INVISIBLE);

        mCurrPrivateStudentData.removeFriend(mPublicUserData);
        mCurrPrivateStudentData.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                mPublicUserData.unpinInBackground();
                mPublicUserData.unpinInBackground(Constants.PinNames.CurrentUser);
                btnFollow.setClickable(true);
                return null;
            }
        });
    }

    public void onClick_btnMessage(View view) {

    }

    public void navigateToNewChallengeActivity(View view) {
        HashMap<String, Object> pushParams = new HashMap<String, Object>();
        pushParams.put("baseUserId", mPublicUserData.getBaseUserId());
        ParseCloud.callFunctionInBackground("sendPush", pushParams);

        Intent intent = new Intent(this, NewChallengeActivity.class);
        // TODO:2 put opponent's user id
        startActivity(intent);
    }

    private ViewHolder createViewHolder() {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) findViewById(R.id.txtName);
        holder.imgProfile = (ParseImageView) findViewById(R.id.imgProfile);
        holder.imgFollowedStudent = (ImageView) findViewById(R.id.imgFollowedStudent);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public ParseImageView imgProfile;
        public ImageView imgFollowedStudent;
    }

    @Override
    protected void onDestroy() {
        mPublicUserData.unpinInBackground();
        super.onDestroy();
    }
}
