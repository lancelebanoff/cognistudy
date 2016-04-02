package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.parse.ParseQuery;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/31/2016.
 */
public class NewConversationActivity extends PeopleFragmentActivity {


    @Override
    protected PeopleListOnClickHandler getPeopleListOnClickHandler() {
        return new PeopleListOnClickHandler() {
            @Override
            public void onListItemClick(PublicUserData publicUserData) {
                navigateToChatActivity(publicUserData.getBaseUserId(), publicUserData.getDisplayName());
            }
        };
    }

    private void navigateToChatActivity(String baseUserId, String displayName) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Constants.IntentExtra.BASEUSERID, baseUserId);
        intent.putExtra(Constants.IntentExtra.CONVERSANT_DISPLAY_NAME, displayName);
        startActivity(intent);
        finish();
    }
}
