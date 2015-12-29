package com.cognitutor.cognistudyapp;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ViewSwitcher;

public class QuestionActivity extends AppCompatActivity
        implements QuestionFragment.OnFragmentInteractionListener, ResponseFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

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

    public void navigateToBattleshipAttackActivity(View view) {
        Intent intent = new Intent(this, BattleshipAttackActivity.class);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
