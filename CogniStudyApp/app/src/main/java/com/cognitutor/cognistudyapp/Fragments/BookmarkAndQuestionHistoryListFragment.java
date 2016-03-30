package com.cognitutor.cognistudyapp.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 3/28/2016.
 */
public class BookmarkAndQuestionHistoryListFragment extends CogniFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question_list, container, false);
    }
}
