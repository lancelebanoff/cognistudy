package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cognitutor.cognistudyapp.Activities.ConversationActivity;
import com.cognitutor.cognistudyapp.R;

/**
 * Created by Lance on 12/27/2015.
 */
public class MessagesFragment extends CogniFragment implements View.OnClickListener {

    public static final MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        Button b = (Button) rootView.findViewById(R.id.btnConversationFragment);
        b.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnConversationFragment:
                navigateToConversationActivity();
                break;
        }
    }

    public void navigateToConversationActivity() {
        Intent intent = new Intent(getActivity(), ConversationActivity.class);
        startActivity(intent);
    }
}

