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
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionContents;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import io.github.kexanie.library.MathView;

public class QuestionActivity extends CogniActivity implements View.OnClickListener {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     */
    private Intent mIntent;
    private ListView listView;
    private ActivityViewHolder avh;
    private Question question;
    private QuestionContents contents;
    private AnswerAdapter answerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        listView = (ListView) findViewById(R.id.listView);
        addComponents();
        avh.btnSetLatex.setOnClickListener(this);
        loadQuestion();
    }

    public void loadQuestion() {

        try {
            question = Question.getQuestionWithContents(mIntent.getStringExtra(Constants.IntentExtra.QUESTION_ID));
        } catch(ParseException e) { handleParseError(e); return; }

        contents = question.getQuestionContents();

        List<String> answers = contents.getAnswers();
        answerAdapter = new AnswerAdapter(this, answers, Constants.AnswerLabelType.LETTER); //TODO: Choose letter or roman
        listView.setAdapter(answerAdapter);
        avh.mvQuestion.setText(contents.getQuestionText());
        avh.mvExplanation.setText(contents.getExplanation());

        if(question.hasPassage()) {
            avh.wvPassage.loadData(contents.getPassage(), "text/html", "UTF-8");
        }
//        avh.wvPassage.loadData(
//                "<html><body>" +
//                        "You scored <u>192</u> points." +
//                        "</body></html>",
//                "text/html",
//                "UTF-8"
//        );
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
//        avh.mvExplanation.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setLatex() {
        String text = avh.txtModifyQuestion.getText().toString();
        avh.mvQuestion.setText(text);
    }

    public static void createNewQuestion() {

        Question question = new Question(
        );
    }

    private boolean isSelectedAnswerCorrect() {
        return answerAdapter.getSelectedAnswer() == contents.getCorrectIdx();
    }

    public void showAnswer(View view) {

        if(isSelectedAnswerCorrect()) {
            avh.txtCorrectIncorrect.setText("Correct!");
        }
        else {
            avh.txtCorrectIncorrect.setText("Incorrect!");
        }
        avh.vgPostAnswer.setVisibility(View.VISIBLE);

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
        private MathView mvExplanation;
        private ViewGroup vgPostAnswer;
        private TextView txtCorrectIncorrect;

        private ActivityViewHolder() {
            wvPassage = (WebView) findViewById(R.id.wvPassage);
            mvQuestion = (MathView) findViewById(R.id.mvQuestion);
            txtModifyQuestion = (EditText) findViewById(R.id.txtModifyQuestion);
            btnSetLatex = (Button) findViewById(R.id.btnSetLatex);
            mvExplanation = (MathView) findViewById(R.id.mvExplanation);
            vgPostAnswer = (ViewGroup) findViewById(R.id.vgPostAnswer);
            txtCorrectIncorrect = (TextView) findViewById(R.id.txtCorrectIncorrect);
        }
    }
}
