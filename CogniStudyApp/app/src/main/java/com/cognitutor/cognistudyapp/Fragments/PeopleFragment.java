package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import com.cognitutor.cognistudyapp.Activities.StudentProfileActivity;
import com.cognitutor.cognistudyapp.Activities.TutorProfileActivity;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Custom.PeopleQueryAdapter;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
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
//    private ListView listView;
    private RecyclerView recyclerView;
    private SearchView searchView;

    public static final PeopleFragment newInstance(PeopleListOnClickHandler onClickHandler) {
        PeopleFragment peopleFragment = new PeopleFragment();
        peopleFragment.onClickHandler = onClickHandler;
        return peopleFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        searchView.setQuery("", false);
        //TODO: Cache results of searches
        //TODO: Figure out how to save state of the fragment when user switches between far away fragments
//        searchView.onActionViewExpanded(); //This will put focus on the searchView without requestFocus()
//        searchView.requestFocus();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);

        searchView = (SearchView) rootView.findViewById(R.id.searchView);
        searchView.setQueryHint("Find users");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.length() == 0) {
                    resetResultsToDefault();
                    return true;
                }
                filter(newText);
                return true;
            }
        });

        peopleQueryAdapter = new PeopleQueryAdapter(getActivity(), onClickHandler);
        /*
        peopleQueryAdapter.setTextKey(PublicUserData.Columns.displayName);
        peopleQueryAdapter.setImageKey(PublicUserData.Columns.profilePic);
        */

//        listView = (ListView) rootView.findViewById(R.id.list);
//        listView.setAdapter(peopleQueryAdapter);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(peopleQueryAdapter);
        peopleQueryAdapter.loadObjects();

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
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

    private void search(String q) {
        peopleQueryAdapter.search(q);
    }

    private void filter(String q) {
        peopleQueryAdapter.getFilter().filter(q);
    }

    private void resetResultsToDefault() {
        peopleQueryAdapter.resetResultsToDefault();
    }
}

