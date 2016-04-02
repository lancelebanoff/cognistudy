package com.cognitutor.cognistudyapp.Fragments;

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
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Conversation;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;

/**
 * Created by Lance on 12/27/2015.
 */
public class ConversationsFragment extends CogniFragment implements View.OnClickListener{

    private ConversationAdapter mConversationAdapter;
    private CogniRecyclerView mRecyclerView;

    public static final ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_conversations, container, false);

        mConversationAdapter = new ConversationAdapter(getActivity());

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
        startActivity(intent);
    }
}

