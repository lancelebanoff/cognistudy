package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.QuestionListFragment;
import com.cognitutor.cognistudyapp.R;

public class QuestionHistoryActivity extends CogniActivity {

    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_history);
        mIntent = getIntent();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = QuestionListFragment.newChallengeQuestionsIntance(getChallengeId());
        fragmentTransaction.add(R.id.fragmentQuestionHistoryContainer, fragment).commit();
    }

    public void navigateToPastQuestionActivity(View view) {
        Intent intent = new Intent(this, PastQuestionActivity.class);
        startActivity(intent);
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }
}
