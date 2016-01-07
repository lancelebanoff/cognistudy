package com.cognitutor.cognistudyapp.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Fragments.QuestionFragment;
import com.cognitutor.cognistudyapp.Fragments.ResponseFragment;
import com.cognitutor.cognistudyapp.R;

public class PastQuestionActivity extends CogniActivity
        implements QuestionFragment.OnFragmentInteractionListener, ResponseFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_question);

        Fragment fragment = new QuestionFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();
    }

    public void showAnswer(View view) {
        // Replace QuestionFragment with ResponseFragment
        Fragment fragment = new ResponseFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();

        // Switch Submit button to Continue button
        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.showNext();
    }

    public void navigateToPreviousActivity(View view) {
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
