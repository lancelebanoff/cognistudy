package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.cognitutor.cognistudyapp.Activities.StudentProfileActivity;
import com.cognitutor.cognistudyapp.Activities.TutorProfileActivity;
import com.cognitutor.cognistudyapp.Custom.CogniRecyclerView;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Adapters.PeopleQueryAdapter;
import com.cognitutor.cognistudyapp.R;

import java.util.List;

/**
 * Created by Lance on 12/27/2015.
 */
public class PeopleFragment extends CogniFragment {

    private PeopleListOnClickHandler onClickHandler;
    private PeopleQueryAdapter peopleQueryAdapter;
//    private ListView listView;
    private CogniRecyclerView recyclerView;
    private SearchView searchView;
    private boolean mIgnoreTutors;

    public static final PeopleFragment newInstance(PeopleListOnClickHandler onClickHandler, boolean ignoreTutors) {
        PeopleFragment peopleFragment = new PeopleFragment();
        peopleFragment.onClickHandler = onClickHandler;
        peopleFragment.mIgnoreTutors = ignoreTutors;
        return peopleFragment;
    }

    //Used when a challenge finishes loading and being pinned so that the opponent shows up in the people list
    public void updateList() {
        String q = searchView.getQuery().toString();
        if(q.length() == 0) {
            peopleQueryAdapter.resetResultsToDefault();
        }
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

        peopleQueryAdapter = new PeopleQueryAdapter(getActivity(), onClickHandler, mIgnoreTutors);

        recyclerView = (CogniRecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(peopleQueryAdapter);
        peopleQueryAdapter.loadObjects();

        return rootView;
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

