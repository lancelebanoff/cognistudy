package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.cognitutor.cognistudyapp.Activities.StudentProfileActivity;
import com.cognitutor.cognistudyapp.Activities.TutorProfileActivity;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.PeopleQueryAdapter;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Lance on 12/27/2015.
 */
public class PeopleFragment extends CogniFragment implements View.OnClickListener {

    private PeopleListOnClickHandler onClickHandler;
    private PeopleQueryAdapter peopleQueryAdapter;
    private ListView listView;
    private SearchView searchView;

    public static final PeopleFragment newInstance(PeopleListOnClickHandler onClickHandler) {
        PeopleFragment peopleFragment = new PeopleFragment();
        peopleFragment.onClickHandler = onClickHandler;
        return peopleFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);

        Button b = (Button) rootView.findViewById(R.id.btnSearch);
        b.setOnClickListener(this);

        searchView = (SearchView) rootView.findViewById(R.id.searchView);

        peopleQueryAdapter = new PeopleQueryAdapter(getActivity(), onClickHandler);
        /*
        peopleQueryAdapter.setTextKey(PublicUserData.Columns.displayName);
        peopleQueryAdapter.setImageKey(PublicUserData.Columns.profilePic);
        */

        listView = (ListView) rootView.findViewById(R.id.list);
        listView.setAdapter(peopleQueryAdapter);
        peopleQueryAdapter.loadObjects();

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnSearch:
                search(searchView.getQuery().toString());
        }
    }

    private void navigateToProfile() {
        // TODO:2 check which user type
        String userType = "student";
        if(userType.equals("student")) {
            navigateToStudentProfileActivity();
        }
        else if(userType.equals("tutor")) {
            navigateToTutorProfileActivity();
        }
    }

    private void navigateToStudentProfileActivity() {
        Intent intent = new Intent(getActivity(), StudentProfileActivity.class);
        startActivity(intent);
    }

    private void navigateToTutorProfileActivity() {
        Intent intent = new Intent(getActivity(), TutorProfileActivity.class);
        startActivity(intent);
    }

    public void search(String q) {

        q = q.replaceAll("\\s+", "");
        q = q.toLowerCase();

        peopleQueryAdapter.search(q);
    }
}

