package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private PublicUserData publicUserData;
    private ViewSwitcher mViewSwitcher;
    private PrivateStudentData mCurrPrivateStudentData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        holder = createViewHolder();
        mIntent = getIntent();

        publicUserData = PublicUserData.getPublicUserData(mIntent.getStringExtra(Constants.IntentExtra.PUBLICUSERDATA_ID)); //TODO: Change this
        if(publicUserData == null) { return; } //TODO: Handle this?
        holder.txtName.setText(publicUserData.getDisplayName());
        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();

        try {
            mCurrPrivateStudentData = PublicUserData.getPublicUserData().getStudent().getPrivateStudentData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        if (mCurrPrivateStudentData.isFriendsWith(publicUserData)) {
            mViewSwitcher.showNext();
        }
    }

    public void onClick_btnFollow(View view) {
        mViewSwitcher.showNext();
        final CogniButton btnUnfollow = (CogniButton) findViewById(R.id.btnUnfollow);
        btnUnfollow.setClickable(false);

        mCurrPrivateStudentData.addFriend(publicUserData);
        mCurrPrivateStudentData.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                publicUserData.pinInBackground();
                btnUnfollow.setClickable(true);
                return null;
            }
        });
    }

    public void onClick_btnUnfollow(View view) {
        mViewSwitcher.showNext();
        final CogniButton btnFollow = (CogniButton) findViewById(R.id.btnUnfollow);
        btnFollow.setClickable(false);

        mCurrPrivateStudentData.removeFriend(publicUserData);
        mCurrPrivateStudentData.saveInBackground().continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                publicUserData.unpinInBackground();
                btnFollow.setClickable(true);
                return null;
            }
        });
    }

    public void navigateToNewChallengeActivity(View view) {
        HashMap<String, Object> pushParams = new HashMap<String, Object>();
        pushParams.put("baseUserId", publicUserData.getBaseUserId());
        ParseCloud.callFunctionInBackground("sendPush", pushParams);

        Intent intent = new Intent(this, NewChallengeActivity.class);
        // TODO:2 put opponent's user id
        startActivity(intent);
    }

    private ViewHolder createViewHolder() {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) findViewById(R.id.txtName);
        holder.imgProfile = (ParseImageView) findViewById(R.id.imgProfile);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public ParseImageView imgProfile;
    }
}
