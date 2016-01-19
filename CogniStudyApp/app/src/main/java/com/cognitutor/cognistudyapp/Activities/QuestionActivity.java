package com.cognitutor.cognistudyapp.Activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.QuestionFragment;
import com.cognitutor.cognistudyapp.Fragments.ResponseFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.R;

public class QuestionActivity extends CogniActivity
        implements QuestionFragment.OnFragmentInteractionListener, ResponseFragment.OnFragmentInteractionListener {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        Fragment fragment = new QuestionFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();

    }

    public static void createNewQuestion() {

        Question question = new Question(
        );
    }

    public void showAnswer(View view) {
        // Replace QuestionFragment with ResponseFragment
        Fragment fragment = new ResponseFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .commit();

        // Switch Submit button to Continue button
        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.showNext();
    }

    public void navigateToNextActivity(View view) {
        String parentActivity = mIntent.getStringExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY);
        switch(parentActivity) {
            case Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY:
                navigateToBattleshipAttackActivity();
                break;
            case Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY:
                navigateToParentActivity();
                break;
        }
    }

    private void navigateToParentActivity() {
        finish();
    }

    private void navigateToBattleshipAttackActivity() {
        Intent intent = new Intent(this, BattleshipAttackActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
