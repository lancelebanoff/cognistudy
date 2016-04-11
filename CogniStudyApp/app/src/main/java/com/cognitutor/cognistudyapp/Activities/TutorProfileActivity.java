package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Tutor;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseImageView;

import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;

public class TutorProfileActivity extends CogniActivity {

    /**
     * Extras:
     *      PUBLICUSERDATA_ID: String
     */
    private ViewHolder holder;
    private ViewFlipper mViewFlipper;
    private Intent mIntent;
    private PrivateStudentData mCurrPrivateStudentData;
    private PublicUserData mPublicUserData;
    private Tutor mTutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_profile);

        holder = createViewHolder();
        mIntent = getIntent();

        mPublicUserData = PublicUserData.getPublicUserData(mIntent.getStringExtra(Constants.IntentExtra.PUBLICUSERDATA_ID)); //TODO: Change this
        if(mPublicUserData == null) { return; } //TODO: Handle this?
        try {
            mTutor = mPublicUserData.getTutor();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.txtName.setText(mPublicUserData.getDisplayName());
        holder.imgProfile.setParseFile(mPublicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtBiography.setText(mTutor.getBiography());

        try {
            mCurrPrivateStudentData = PublicUserData.getPublicUserData().getStudent().getPrivateStudentData();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        if (mCurrPrivateStudentData.hasRequestedTutor(mPublicUserData)) {
            mViewFlipper.setDisplayedChild(1);
        }
        else if(mCurrPrivateStudentData.hasTutor(mPublicUserData)) {
            mViewFlipper.setDisplayedChild(2);
        }

        showTutorialDialogIfNeeded(Constants.Tutorial.LINK_TUTOR, null);
    }

    public void onClick_btnAddTutor(View view) {
        mCurrPrivateStudentData.addRequestToTutor(mPublicUserData);
        mCurrPrivateStudentData.saveInBackground();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("publicStudentDataId", PublicUserData.getPublicUserData().getObjectId());
        params.put("publicTutorDataId", mPublicUserData.getObjectId());
        ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.STUDENT_REQUEST_TO_TUTOR, params).continueWith(new Continuation<Object, Void>() {
            @Override
            public Void then(Task<Object> task) throws Exception {
                if (task.getError() != null) {
                    task.getError().printStackTrace();
                }
                return null;
            }
        });

        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        viewFlipper.showNext();

        Toast.makeText(this, "Request sent to tutor.", Toast.LENGTH_LONG).show();
    }

    public void onClick_btnRemoveTutor(View view) {
        mCurrPrivateStudentData.removeTutor(mPublicUserData);
        mCurrPrivateStudentData.saveInBackground();

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("studentPublicDataId", PublicUserData.getPublicUserData().getObjectId());
        params.put("tutorPublicDataId", mPublicUserData.getObjectId());
        ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.REMOVE_STUDENT, params).continueWith(new Continuation<Object, Void>() {
            @Override
            public Void then(Task<Object> task) throws Exception {
                if (task.getError() != null) {
                    task.getError().printStackTrace();
                }
                return null;
            }
        });

        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        viewFlipper.showNext();

        Toast.makeText(this, "Unlinked from tutor.", Toast.LENGTH_LONG).show();
    }

    public void onClick_btnMessage(View view) {

    }

    private ViewHolder createViewHolder() {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) findViewById(R.id.txtName);
        holder.imgProfile = (ParseImageView) findViewById(R.id.imgProfile);
        holder.txtBiography = (TextView) findViewById(R.id.txtBiography);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public ParseImageView imgProfile;
        public TextView txtBiography;
    }

    @Override
    protected void onDestroy() {
        mPublicUserData.unpinInBackground();
        super.onDestroy();
    }
}
