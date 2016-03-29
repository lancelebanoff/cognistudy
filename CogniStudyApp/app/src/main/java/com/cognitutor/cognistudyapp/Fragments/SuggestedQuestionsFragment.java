package com.cognitutor.cognistudyapp.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.R;

import org.json.JSONObject;

/**
 * Created by Kevin on 3/28/2016.
 */
public class SuggestedQuestionsFragment extends CogniPushListenerFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question_list, container, false);
    }

    @Override
    public void onReceiveHandler() {

    }

    @Override
    public JSONObject getConditions() {
        return null;
    }
}
