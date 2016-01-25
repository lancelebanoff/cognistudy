package com.cognitutor.cognistudyapp.Activities;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Adapters.AnswerAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.RoundedImageView;
import com.cognitutor.cognistudyapp.Fragments.QuestionFragment;
import com.cognitutor.cognistudyapp.Fragments.ResponseFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.R;

import java.util.ArrayList;
import java.util.List;

import io.github.kexanie.library.MathView;

public class QuestionActivity extends CogniActivity implements View.OnClickListener {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;
    private ListView listView;
    private ActivityViewHolder avh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        listView = (ListView) findViewById(R.id.listView);
        addComponents();

        List<String> answers = new ArrayList<String>();
        answers.add("$$x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}$$");
        answers.add("$$x = {-b \\sqrt{b^2-4ac} \\over 2a}$$");
        listView.setAdapter(new AnswerAdapter(this, answers, Constants.AnswerLabelType.LETTER));

        avh.btnSetLatex.setOnClickListener(this);
        avh.txtModifyQuestion.setText(avh.mvQuestion.getText());
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSetLatex:
                setLatex();
                break;
        }
    }

    private void addComponents() {
        View header = getLayoutInflater().inflate(R.layout.header_question, listView, false);
        View footer = getLayoutInflater().inflate(R.layout.footer_question, listView, false);
        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null, false);

        avh = new ActivityViewHolder();
    }

    @Override
    public void onResume() {
        super.onResume();

        avh.mvQuestion.setText(
                "When \\(a \\ne 0\\), there are two solutions to \\(ax^2 + bx + c = 0\\)" +
                        "and they are $$x = {-b \\pm \\sqrt{b^2-4ac} \\over 2a}.$$"
        );

        avh.wvPassage.loadData(
                "<html><body>" +
                        "You scored <u>192</u> points." +
                        "</body></html>",
                "text/html",
                "UTF-8"
        );
    }

    private void setLatex() {
        avh.mvQuestion.setText(avh.txtModifyQuestion.getText().toString());
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

    private class ActivityViewHolder {
        private WebView wvPassage;
        private MathView mvQuestion;
        private EditText txtModifyQuestion;
        private Button btnSetLatex;

        private ActivityViewHolder() {
            wvPassage = (WebView) findViewById(R.id.wvPassage);
            mvQuestion = (MathView) findViewById(R.id.mvQuestion);
            txtModifyQuestion = (EditText) findViewById(R.id.txtModifyQuestion);
            btnSetLatex = (Button) findViewById(R.id.btnSetLatex);
        }
    }
}
