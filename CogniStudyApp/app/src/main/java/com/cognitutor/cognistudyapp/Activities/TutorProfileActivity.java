package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Tutor;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseImageView;

import java.util.HashMap;

public class TutorProfileActivity extends CogniActivity {

    /**
     * Extras:
     *      PUBLICUSERDATA_ID: String
     */
    private ViewHolder holder;
    private Intent mIntent;
    private PublicUserData publicUserData;
    private Tutor mTutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutor_profile);

        holder = createViewHolder();
        mIntent = getIntent();

        publicUserData = PublicUserData.getPublicUserData(mIntent.getStringExtra(Constants.IntentExtra.PUBLICUSERDATA_ID)); //TODO: Change this
        if(publicUserData == null) { return; } //TODO: Handle this?
        try {
            mTutor = publicUserData.getTutor();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.txtName.setText(publicUserData.getDisplayName());
        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
        holder.txtBiography.setText(mTutor.getBiography());
    }

    public void onClick_btnAddTutor(View view) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("studentId", UserUtils.getStudent().getObjectId());
            params.put("privateTutorDataId", publicUserData.getTutor().getObjectId());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        ParseCloud.callFunctionInBackground(Constants.CloudCodeFunction.STUDENT_REQUEST_TO_TUTOR, params);

        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.showNext();

        Toast.makeText(this, "Request sent to tutor.", Toast.LENGTH_LONG).show();
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
}
