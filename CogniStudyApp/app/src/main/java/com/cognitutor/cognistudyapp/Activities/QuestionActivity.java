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
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.ChallengeUserData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionBundle;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionContents;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentBlockStats;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.StudentTRollingStats;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.apache.commons.io.IOUtils;

import java.util.List;

import bolts.Continuation;
import bolts.Task;
import io.github.kexanie.library.MathView;

public abstract class QuestionActivity extends CogniActivity implements View.OnClickListener {

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    protected Intent mIntent;
    private ListView listView;
    private ActivityViewHolder avh;
    protected Question mQuestion;
    protected Question mQuestionWithoutContents;
    protected QuestionContents mQuestionContents;
    private AnswerAdapter answerAdapter;
    private Response mResponse = null;

//    protected abstract void addComponents();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        listView = (ListView) findViewById(R.id.listView);
        addComponents();
        avh.btnSetLatex.setOnClickListener(this);
        ClickableListItem.setQuestionAnswered(false);
        //TODO: Move this
        try {
            loadResponse().waitForCompletion(); //If this is a past question
        } catch (InterruptedException e) { e.printStackTrace(); }
        loadQuestion();
    }

    public void loadQuestion() {

        int selectedAnswer = -1;
        if(mResponse != null) {
            selectedAnswer = mResponse.getSelectedAnswer();
        }

        try {
            String questionId = mIntent.getStringExtra(Constants.IntentExtra.QUESTION_ID);
            mQuestion = Question.getQuestionWithContents(questionId);
            mQuestionWithoutContents = Question.getQuestionWithoutContents(questionId);
        } catch(ParseException e) { handleParseError(e); return; }

        mQuestionContents = mQuestion.getQuestionContents();

        List<String> answers = mQuestionContents.getAnswers();
        answerAdapter = new AnswerAdapter(this, answers, Constants.AnswerLabelType.LETTER, selectedAnswer); //TODO: Choose letter or roman
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

        if(mResponse != null) {
            showAnswer(isSelectedAnswerCorrect());
        }
    }

    private void applyResponseToQuestion() {
        if(mResponse != null) {
            answerAdapter.selectAnswer(mResponse.getSelectedAnswer());
            showAnswer(isSelectedAnswerCorrect());
        }
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

    private Task<Object> loadResponse() {
        final String responseId = getResponseId();
        if(responseId == null)
            return CommonUtils.getCompletionTask(null);
        return QueryUtils.getFirstCacheElseNetworkInBackground(new QueryUtils.ParseQueryBuilder<Response>() {
            @Override
            public ParseQuery<Response> buildQuery() {
                return Response.getQuery().whereEqualTo("objectId", responseId);
            }
        }).continueWith(new Continuation<Response, Object>() {
            @Override
            public Object then(Task<Response> task) throws Exception {
                mResponse = task.getResult();
                return null;
            }
        });
    }

    private String getResponseId() {
        return mIntent.getStringExtra(Constants.IntentExtra.RESPONSE_ID);
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

    protected boolean isSelectedAnswerCorrect() {
        return getSelectedAnswer() == getCorrectAnswer();
    }

    protected int getSelectedAnswer() {
        return answerAdapter.getSelectedAnswer();
    }

    private int getCorrectAnswer() {
        return mQuestionContents.getCorrectIdx();
    }

    protected void showAnswer(boolean isSelectedAnswerCorrect) {

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
    }

    protected void navigateToParentActivity() {
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
