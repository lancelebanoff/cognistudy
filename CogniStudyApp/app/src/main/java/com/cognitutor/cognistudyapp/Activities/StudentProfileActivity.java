package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseImageView;

import java.util.HashMap;

public class StudentProfileActivity extends CogniActivity {

    private ViewHolder holder;
    private Intent mIntent;
    private PublicUserData publicUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_profile);

        holder = createViewHolder();
        mIntent = getIntent();

        publicUserData = PublicUserData.getPublicUserData(mIntent.getStringExtra("publicUserDataId"));
        holder.txtName.setText(publicUserData.getDisplayName());
        holder.imgProfile.setParseFile(publicUserData.getProfilePic());
        holder.imgProfile.loadInBackground();
    }

    public void navigateToNewChallengeActivity(View view) {
        HashMap<String, Object> pushParams = new HashMap<String, Object>();
        pushParams.put("baseUserId", publicUserData.getBaseUserId());
        ParseCloud.callFunctionInBackground("sendPush", pushParams);
        /*
        Intent intent = new Intent(this, NewChallengeActivity.class);
        // TODO:2 put opponent's user id
        startActivity(intent);
        */
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
