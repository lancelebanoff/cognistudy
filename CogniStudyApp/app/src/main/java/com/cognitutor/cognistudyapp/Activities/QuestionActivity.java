package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Fragments.QuestionFragment;
import com.cognitutor.cognistudyapp.Fragments.ResponseFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.R;

import java.util.List;

import io.github.kexanie.library.MathView;

public class QuestionActivity extends CogniActivity implements View.OnClickListener {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;
    private ActViewHolder actViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        Button b = (Button) findViewById(R.id.btnSetLatex);
        b.setOnClickListener(this);

        actViewHolder = new ActViewHolder();

        actViewHolder.txtModifyQuestion.setText(actViewHolder.mvQuestion.getText());
    }

    @Override
    public void onResume() {
        super.onResume();

        MathView mathView = (MathView) findViewById(R.id.mathView);
        mathView.setText(
                "When \\(a \\ne 0\\), there are two solutions to \\(ax^2 + bx + c = 0\\)" +
                        "and they are $$x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}.$$"
        );

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.loadData(
                "<html><body>" +
                        "You scored <u>192</u> points." +
                        "</body></html>",
                "text/html",
                "UTF-8"
        );
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSetLatex:
                actViewHolder.mvQuestion.setText(actViewHolder.txtModifyQuestion.getText().toString());
                break;
        }
    }

    public static void createNewQuestion() {

        Question question = new Question(
        );
    }

    public void showAnswer(View view) {
        // Replace QuestionFragment with ResponseFragment

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

    private class ActViewHolder {
        private MathView mvQuestion;
        private EditText txtModifyQuestion;

        private ActViewHolder() {
            mvQuestion = (MathView) findViewById(R.id.mvQuestion);
            txtModifyQuestion = (EditText) findViewById(R.id.txtModifyQuestion);
        }
    }
}
