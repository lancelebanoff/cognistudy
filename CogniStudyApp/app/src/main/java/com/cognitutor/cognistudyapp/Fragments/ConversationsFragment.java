package com.cognitutor.cognistudyapp.Fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.Activities.NewConversationActivity;
import com.cognitutor.cognistudyapp.Adapters.ConversationAdapter;
import com.cognitutor.cognistudyapp.Custom.CogniRecyclerView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lance on 12/27/2015.
 */
public class ConversationsFragment extends CogniPushListenerFragment implements View.OnClickListener{

    private ConversationAdapter mConversationAdapter;
    private CogniRecyclerView mRecyclerView;
    public static final int REQUEST_CODE = 1;

    public static final ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    public void notifyObjectIdChanged(String objectId) {
        mConversationAdapter.notifyObjectIdChanged(objectId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversations, container, false);

        mConversationAdapter = new ConversationAdapter(getActivity(), this);

        mRecyclerView = (CogniRecyclerView) rootView.findViewById(R.id.rvConversations);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mConversationAdapter);
        mConversationAdapter.loadObjects();

        FloatingActionButton fabNewConversation = (FloatingActionButton) rootView.findViewById(R.id.fabNewConversation);
        fabNewConversation.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCacheThenNetwork();
    }

    private void loadCacheThenNetwork() {
        QueryUtils.findCacheThenNetworkInBackgroundPinWithObjId(mConversationAdapter, new QueryUtils.ParseQueryBuilder<Conversation>() {
            @Override
            public ParseQuery<Conversation> buildQuery() {
                return Conversation.getQueryForCurrentUserConversations();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fabNewConversation:
                createNewConversation();
        }
    }

    public void createNewConversation() {
        Intent intent = new Intent(getActivity(), NewConversationActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    public void onReceiveHandler() {
        loadCacheThenNetwork();
    }

    @Override
    public JSONObject getConditions() {
        JSONObject conditions = new JSONObject();
        try {
            conditions.put(Constants.NotificationData.FRAGMENT, Constants.NotificationData.Fragment.CONVERSATIONS_FRAGMENT);
        } catch (JSONException e) { e.printStackTrace(); }
        return conditions;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if(data.hasExtra(Constants.IntentExtra.UPDATE_OBJECT_ID_IN_LIST)) {
                String objectId = data.getStringExtra(Constants.IntentExtra.UPDATE_OBJECT_ID_IN_LIST);
                notifyObjectIdChanged(objectId);
            }
        }
    }
}

