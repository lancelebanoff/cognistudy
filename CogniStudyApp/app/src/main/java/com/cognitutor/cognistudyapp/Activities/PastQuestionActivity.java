package com.cognitutor.cognistudyapp.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.QuestionFragment;
import com.cognitutor.cognistudyapp.Fragments.ResponseFragment;
import com.cognitutor.cognistudyapp.R;

public class PastQuestionActivity extends QuestionActivity {

    @Override
    protected String getQuestionAndResponsePinName() {
        return getChallengeId();
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }
}
