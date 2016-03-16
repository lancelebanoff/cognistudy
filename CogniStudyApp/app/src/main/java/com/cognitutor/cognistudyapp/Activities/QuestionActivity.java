package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Adapters.AnswerAdapter;
import com.cognitutor.cognistudyapp.Custom.ClickableListItem;
import com.cognitutor.cognistudyapp.Custom.CogniMathView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionBundle;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionContents;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;

import org.apache.commons.io.IOUtils;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import io.github.kexanie.library.MathView;

public class QuestionActivity extends CogniActivity implements View.OnClickListener {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    private Intent mIntent;
    private ListView listView;
    private ActivityViewHolder avh;
    private Question mQuestion;
    private QuestionContents mQuestionContents;
    private AnswerAdapter answerAdapter;
    private Challenge mChallenge = null;
    private int mQuesAnsThisTurn = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        listView = (ListView) findViewById(R.id.listView);
        addComponents();
        avh.btnSetLatex.setOnClickListener(this);
        ClickableListItem.setQuestionAnswered(false);
        loadQuestion();

        loadChallenge();
    }

    public void loadQuestion() {

        try {
            mQuestion = Question.getQuestionWithContents(mIntent.getStringExtra(Constants.IntentExtra.QUESTION_ID));
        } catch(ParseException e) { handleParseError(e); return; }

        mQuestionContents = mQuestion.getQuestionContents();

        List<String> answers = mQuestionContents.getAnswers();
        answerAdapter = new AnswerAdapter(this, answers, Constants.AnswerLabelType.LETTER); //TODO: Choose letter or roman
        listView.setAdapter(answerAdapter);

        avh.mvQuestion.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                avh.txtModifyQuestion.setText("Done!");
                super.onPageFinished(view, url);
            }
        });

        avh.mvQuestion.setText(mQuestionContents.getQuestionText());
//        avh.mvQuestion.loadUrl("file:///android_asset/html/passage.html");
        avh.mvExplanation.setText(mQuestionContents.getExplanation());

        if(mQuestion.inBundle()) {
            QuestionBundle bundle = null;
            try {
                bundle = mQuestion.getQuestionBundle().fetchIfNeeded();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            avh.wvPassage.loadData(buildPassageHtml(bundle.getPassageText()), "text/html", "UTF-8");
        }
//        avh.wvPassage.loadData(
//                "<html><body>" +
//                        "You scored <u>192</u> points." +
//                        "</body></html>",
//                "text/html",
//                "UTF-8"
//        );
    }

    public void loadingFinished() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(0); // TODO:1 sleep so that user doesn't see mathview changing
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        avh.loadingFinished();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnSetLatex:
                setLatex();
                break;
        }
    }

    private void loadChallenge() {
        String challengeId = getChallengeId();
        Challenge.getChallengeInBackground(challengeId)
                .continueWith(new Continuation<Challenge, Void>() {
                    @Override
                    public Void then(Task<Challenge> task) throws Exception {
                        mChallenge = task.getResult();

                        return null;
                    }
                });
    }

    private String getChallengeId() {
        return mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID);
    }

    private void addComponents() {
        View header = getLayoutInflater().inflate(R.layout.header_question, listView, false);
        View footer = getLayoutInflater().inflate(R.layout.footer_question, listView, false);
        listView.addHeaderView(header, null, false);
        listView.addFooterView(footer, null, false);

        avh = new ActivityViewHolder();
        avh.showLoading();
//        avh.mvExplanation.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void setLatex() {
        String text = avh.txtModifyQuestion.getText().toString();
//        avh.mvQuestion.setText(text);
    }

    public static void createNewQuestion() {

        Question question = new Question(
        );
    }

    private boolean isSelectedAnswerCorrect() {
        return getSelectedAnswer() == getCorrectAnswer();
    }

    private int getSelectedAnswer() {
        return answerAdapter.getSelectedAnswer();
    }

    private int getCorrectAnswer() {
        return mQuestionContents.getCorrectIdx();
    }

    public void showAnswer(View view) {

        boolean isSelectedAnswerCorrect = isSelectedAnswerCorrect();
        if(isSelectedAnswerCorrect) {
            avh.txtCorrectIncorrect.setText("Correct!");
        }
        else {
            avh.txtCorrectIncorrect.setText("Incorrect!");
        }
        avh.vgPostAnswer.setVisibility(View.VISIBLE);
        ClickableListItem.setQuestionAnswered(true);

        // Switch Submit button to Continue button
        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
        viewSwitcher.setVisibility(View.INVISIBLE);

        createResponse(isSelectedAnswerCorrect);

        incrementAnalytics(mQuestion.getCategory(), isSelectedAnswerCorrect);

        if(mChallenge != null)
            incrementQuesAnsThisTurn(isSelectedAnswerCorrect);
    }

    private void createResponse(boolean isSelectedAnswerCorrect) {
        //TODO: Pin related objects
        //TODO: Implement rating
        final Response response = new Response(mQuestion, isSelectedAnswerCorrect, getSelectedAnswer(), Constants.QuestionRating.NOT_RATED);
        if(mChallenge != null) {
            String pinName = getChallengeId();
            ParseObjectUtils.pinThenSaveInBackground(pinName, response)
                    .continueWith(new Continuation<Void, Object>() {
                        @Override
                        public Object then(Task<Void> task) throws Exception {
                            PrivateStudentData.addResponse(response);
                            return null;
                        }
                    });
        }
        else {
            response.saveInBackground().continueWith(new Continuation<Void, Object>() {
                @Override
                public Object then(Task<Void> task) throws Exception {
                    PrivateStudentData.addResponse(response);
                    return null;
                }
            });
        }
    }

    private void incrementAnalytics(String category, boolean isSelectedAnswerCorrect) {
        //TODO: wait for incrementAll to finish when necessary
        StudentBlockStats.incrementAll(category, isSelectedAnswerCorrect);
        StudentTRollingStats.incrementAllInBackground(mQuestion, category, isSelectedAnswerCorrect);
    }

    private void incrementQuesAnsThisTurn(final boolean isSelectedAnswerCorrect) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(mChallenge == null) {} // Wait until challenge is loaded

                mQuesAnsThisTurn = mChallenge.incrementAndGetQuesAnsThisTurn();
                if(isSelectedAnswerCorrect) {
                    mChallenge.incrementCorrectAnsThisTurn();
                }
                if(mChallenge.getChallengeType().equals(Constants.ChallengeType.PRACTICE) &&
                        mChallenge.getQuesAnsThisTurn() == Constants.Questions.NUM_QUESTIONS_PER_TURN) {
                    mQuesAnsThisTurn = 0;
                    mChallenge.setQuesAnsThisTurn(0);
                    chooseThreeQuestionIds();
                }
                try {
                    mChallenge.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Switch Submit button to Continue button
                        ViewSwitcher viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher);
                        viewSwitcher.setVisibility(View.VISIBLE);
                        viewSwitcher.showNext();
                    }
                });
            }
        }).start();
    }

    private List<String> chooseThreeQuestionIds() {
        List<String> questionIds = new ArrayList<>();
        questionIds.add("aSVEaMqEfB");
        questionIds.add("fF4lsHt2iW");
        questionIds.add("eO4TCrdBdn");
        mChallenge.setThisTurnQuestionIds(questionIds);
        return questionIds;
    }

    public void navigateToNextActivity(View view) {
        String parentActivity = mIntent.getStringExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY);
        switch(parentActivity) {
            case Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY:
                if(mQuesAnsThisTurn == Constants.Questions.NUM_QUESTIONS_PER_TURN) {
                    navigateToBattleshipAttackActivity();
                } else {
                    String questionId = mChallenge.getThisTurnQuestionIds().get(mQuesAnsThisTurn);
                    navigateToNextQuestionActivity(questionId);
                }
                break;
            case Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY:
                navigateToParentActivity();
                break;
            case Constants.IntentExtra.ParentActivity.MAIN_ACTIVITY:
                navigateToParentActivity();
                break;
        }
    }

    private void navigateToParentActivity() {
        finish();
    }

    private void navigateToNextQuestionActivity(String questionId) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.CHALLENGE_ACTIVITY);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, questionId);
//        fF4lsHt2iW
//        eO4TCrdBdn
        startActivity(intent);
        finish();
    }

    private void navigateToBattleshipAttackActivity() {
        Intent intent = new Intent(this, BattleshipAttackActivity.class);
        intent.putExtra(Constants.IntentExtra.CHALLENGE_ID, mIntent.getStringExtra(Constants.IntentExtra.CHALLENGE_ID));
        intent.putExtra(Constants.IntentExtra.USER1OR2, mIntent.getIntExtra(Constants.IntentExtra.USER1OR2, -1));
        startActivity(intent);
        finish();
    }

    public void setBtnSubmitEnabled(boolean val) {
        avh.btnSubmit.setEnabled(val);
    }

    private class ActivityViewHolder {
        private RelativeLayout rlQuestionHeader;
        private ProgressBar progressBar;
        private WebView wvPassage;
        private CogniMathView mvQuestion;
        private EditText txtModifyQuestion;
        private Button btnSetLatex;
        private MathView mvExplanation;
        private ViewGroup vgPostAnswer;
        private TextView txtCorrectIncorrect;
        private Button btnSubmit;

        private ActivityViewHolder() {
            rlQuestionHeader = (RelativeLayout) findViewById(R.id.rlQuestionHeader);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            wvPassage = (WebView) findViewById(R.id.wvPassage);
            mvQuestion = (CogniMathView) findViewById(R.id.mvQuestion);
            txtModifyQuestion = (EditText) findViewById(R.id.txtModifyQuestion);
            btnSetLatex = (Button) findViewById(R.id.btnSetLatex);
            mvExplanation = (MathView) findViewById(R.id.mvExplanation);
            vgPostAnswer = (ViewGroup) findViewById(R.id.vgPostAnswer);
            txtCorrectIncorrect = (TextView) findViewById(R.id.txtCorrectIncorrect);
            btnSubmit = (Button) findViewById(R.id.btnSubmit);
        }

        private void showLoading() {
            rlQuestionHeader.setVisibility(View.INVISIBLE);
            listView.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        private void loadingFinished() {
            rlQuestionHeader.setVisibility(View.VISIBLE);
            listView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private String buildPassageHtml(String body) {

        String html = null;
        try { html = IOUtils.toString(getAssets().open("html/passage.html")); }
        catch (Exception e) {e.printStackTrace();}

        String css = null;
        try { css = IOUtils.toString(getAssets().open("css/question.css")); }
        catch (Exception e) {e.printStackTrace();}
        
        html = html.replace("$CSS$", css);

        return html.replace("$BODY$", body);
    }
}
