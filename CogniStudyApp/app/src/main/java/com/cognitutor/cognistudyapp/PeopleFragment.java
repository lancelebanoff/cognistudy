package com.cognitutor.cognistudyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lance on 12/27/2015.
 */
public class PeopleFragment extends Fragment {

    public static PeopleFragment newInstance() {
        return new PeopleFragment();
    }

    public PeopleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);
        return rootView;
    }
}

