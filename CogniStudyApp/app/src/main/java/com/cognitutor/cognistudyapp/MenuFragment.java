package com.cognitutor.cognistudyapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Lance on 12/27/2015.
 */
public class MenuFragment extends Fragment {

    public static MenuFragment newInstance() {
        return new MenuFragment();
    }

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);
        return rootView;
    }
}
