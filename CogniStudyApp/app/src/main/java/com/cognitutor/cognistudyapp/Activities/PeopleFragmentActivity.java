package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.PeopleListOnClickHandler;
import com.cognitutor.cognistudyapp.Fragments.PeopleFragment;
import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 3/31/2016.
 */
public abstract class PeopleFragmentActivity extends CogniActivity {

    protected Intent mIntent;

    protected abstract PeopleListOnClickHandler getPeopleListOnClickHandler();
    protected abstract boolean getIgnoreTutors();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people_fragment);
        mIntent = getIntent();

        createPeopleFragment();
    }

    private void createPeopleFragment() {
        PeopleFragment fragment = PeopleFragment.newInstance(getPeopleListOnClickHandler(), getIgnoreTutors());

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragment)
                .commit();
    }
}
