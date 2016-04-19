package com.cognitutor.cognistudyapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.Activities.QuestionListActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kevin on 3/28/2016.
 */
public class SuggestedQuestionsListFragment extends CogniPushListenerFragment {

    QuestionListActivity mParentActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mParentActivity = (QuestionListActivity) getActivity();
    }

    @Override
    public void onReceiveHandler() {
        mParentActivity.getAndDisplayFromSelections(false);
    }

    @Override
    public JSONObject getConditions() {
        JSONObject conditions = new JSONObject();
        try {
            conditions.put(Constants.NotificationData.FRAGMENT, Constants.NotificationData.Fragment.SUGGESTED_QUESTIONS_LIST_FRAGMENT);
        } catch (JSONException e) { e.printStackTrace(); }
        return conditions;
    }
}
