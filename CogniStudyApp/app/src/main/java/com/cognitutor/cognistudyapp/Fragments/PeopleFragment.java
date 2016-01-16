package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.cognitutor.cognistudyapp.Custom.PeopleQueryAdapter;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.cognitutor.cognistudyapp.Activities.StudentProfileActivity;
import com.cognitutor.cognistudyapp.Activities.TutorProfileActivity;
import com.parse.ParseFile;
import com.parse.ParseImageView;
import com.parse.ParseObject;

/**
 * Created by Lance on 12/27/2015.
 */
public class PeopleFragment extends CogniFragment implements View.OnClickListener {

    private PeopleQueryAdapter peopleQueryAdapter;
    private ListView listView;

    public PeopleFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_people, container, false);

        peopleQueryAdapter = new PeopleQueryAdapter(getContext());
        peopleQueryAdapter.setTextKey(PublicUserData.Columns.displayName);
        peopleQueryAdapter.setImageKey(PublicUserData.Columns.profilePic);

        listView = (ListView) rootView.findViewById(R.id.list);
        listView.setAdapter(peopleQueryAdapter);
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
}

