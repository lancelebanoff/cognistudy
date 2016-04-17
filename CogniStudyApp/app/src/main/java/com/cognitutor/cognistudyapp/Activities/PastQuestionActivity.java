package com.cognitutor.cognistudyapp.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.AnsweredQuestionActivity;
import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.QuestionFragment;
import com.cognitutor.cognistudyapp.Fragments.ResponseFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.AnsweredQuestionIds;
import com.cognitutor.cognistudyapp.R;

public class PastQuestionActivity extends AnsweredQuestionActivity {

    @Override
    protected String getQuestionAndResponsePinName() {
        return getChallengeId();
    }

    @Override
    protected String getQuestionTitle() {
        return getResources().getString(R.string.title_activity_past_question);
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }
}
