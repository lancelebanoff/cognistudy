package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Tutor;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseImageView;
import com.parse.ParseObject;

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
            mCurrPrivateStudentData = PublicUserData.getPublicUserData().getStudent().getPrivateStudentData();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.txtName.setText(mPublicUserData.getDisplayName());
        holder.imgProfile.setParseFile(mPublicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtBiography.setText(mTutor.getBiography());
        showCorrectDisplay();
        mCurrPrivateStudentData.fetchInBackground().continueWith(new Continuation<ParseObject, Object>() {
            @Override
            public Object then(Task<ParseObject> task) throws Exception {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showCorrectDisplay();
                    }
                });
                return null;
            }
        });

        showTutorialDialogIfNeeded(Constants.Tutorial.LINK_TUTOR, null);
    }

    private void showCorrectDisplay() {
        if (mCurrPrivateStudentData.hasTutor(mPublicUserData)) {
            holder.imgLinkedTutor.setVisibility(View.VISIBLE);
        } else {
            holder.imgLinkedTutor.setVisibility(View.INVISIBLE);
        }
        mViewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        ((CogniButton) mViewFlipper.getChildAt(1)).setColor(this, R.color.grey);
        if (mCurrPrivateStudentData.hasRequestedTutor(mPublicUserData)) {
            mViewFlipper.setDisplayedChild(1);
        }
        else if(mCurrPrivateStudentData.hasTutor(mPublicUserData)) {
            mViewFlipper.setDisplayedChild(2);
        }
        else if (mCurrPrivateStudentData.tutorHasSentRequest(mPublicUserData)) {
            mViewFlipper.setDisplayedChild(3);
        }
        else {
            mViewFlipper.setDisplayedChild(0);
        }
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
    }

    public void onClick_btnAcceptRequest(View view) {
        mCurrPrivateStudentData.linkTutor(mPublicUserData);

        holder.imgLinkedTutor.setVisibility(View.VISIBLE);
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        viewFlipper.setDisplayedChild(2);
    }

    public void onClick_btnRemoveTutor(View view) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.title_dialog_unlink_from_tutor)
                .setMessage(R.string.message_dialog_unlink_from_tutor)
                .setPositiveButton(R.string.yes_dialog_unlink_from_tutor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unlinkTutor();
                    }
                })
                .setNegativeButton(R.string.no_dialog_unlink_from_tutor, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing
                    }
                })
                .create().show();
    }

    private void unlinkTutor() {
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

        holder.imgLinkedTutor.setVisibility(View.INVISIBLE);
        ViewFlipper viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        viewFlipper.setDisplayedChild(0);
    }

    public void onClick_btnMessage(View view) {

    }

    private ViewHolder createViewHolder() {
        ViewHolder holder = new ViewHolder();
        holder.txtName = (TextView) findViewById(R.id.txtName);
        holder.imgProfile = (ParseImageView) findViewById(R.id.imgProfile);
        holder.txtBiography = (TextView) findViewById(R.id.txtBiography);
        holder.imgLinkedTutor = (ImageView) findViewById(R.id.imgLinkedTutor);
        return holder;
    }

    private static class ViewHolder {
        public TextView txtName;
        public ParseImageView imgProfile;
        public TextView txtBiography;
        public ImageView imgLinkedTutor;
    }

    @Override
    protected void onDestroy() {
        mPublicUserData.unpinInBackground();
        super.onDestroy();
    }
}
