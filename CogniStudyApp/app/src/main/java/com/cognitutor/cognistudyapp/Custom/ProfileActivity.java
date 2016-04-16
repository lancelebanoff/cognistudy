package com.cognitutor.cognistudyapp.Custom;

import android.content.Intent;
import android.view.View;

import com.cognitutor.cognistudyapp.Activities.ChatActivity;
import com.cognitutor.cognistudyapp.Activities.CogniActivity;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;

/**
 * Created by Kevin on 4/16/2016.
 */
public class ProfileActivity extends CogniActivity {

    protected PublicUserData mPublicUserData;

    public void onClick_btnMessage(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.IntentExtra.BASEUSERID, mPublicUserData.getBaseUserId());
        intent.putExtra(Constants.IntentExtra.CONVERSANT_DISPLAY_NAME, mPublicUserData.getDisplayName());
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.MAIN_ACTIVITY);
        ChatActivity.setConversantPud(mPublicUserData);
        startActivity(intent);
    }
}
