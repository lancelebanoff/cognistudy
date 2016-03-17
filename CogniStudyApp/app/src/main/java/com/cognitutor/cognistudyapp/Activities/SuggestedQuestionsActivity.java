package com.cognitutor.cognistudyapp.Activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.View;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.QuestionListFragment;
import com.cognitutor.cognistudyapp.R;

public class SuggestedQuestionsActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_questions);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = QuestionListFragment.newInstance(QuestionListFragment.ListType.SUGGESTED_QUESTIONS);
        fragmentTransaction.add(R.id.suggestedQuestionsLayout, fragment).commit();
    }

    public void navigateToQuestionActivity(View view) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY);
        startActivity(intent);
    }
}
