package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.cognitutor.cognistudyapp.Adapters.AnswerAdapter;
import com.cognitutor.cognistudyapp.Custom.ClickableListItem;
import com.cognitutor.cognistudyapp.Custom.CogniMathView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.CommonUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionBundle;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionContents;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.SuggestedQuestion;
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
    private Bookmark mBookmark = null;

//    protected abstract void addComponents();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        listView = (ListView) findViewById(R.id.listView);
        addComponents();
        setOnClickListeners();
        ClickableListItem.setQuestionAnswered(false);
        //TODO: Move this
        try {
            loadResponseAndBookmark().waitForCompletion(); //If this is a past question, mResponse will be loaded
        } catch (InterruptedException e) { e.printStackTrace(); }
        loadQuestion();
        setBookmarkComponents();
    }

    public String getQuestionId() {
        return mIntent.getStringExtra(Constants.IntentExtra.QUESTION_ID);
    }

    public void loadQuestion() {

        int selectedAnswer = -1;
        if(mResponse != null) {
            selectedAnswer = mResponse.getSelectedAnswer();
        }

        try {
            String questionId = getQuestionId();
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

        TextView txtSubject = (TextView) findViewById(R.id.txtSubject);
        txtSubject.setText(mQuestion.getSubject());
        TextView txtCategory = (TextView) findViewById(R.id.txtCategory);
        txtCategory.setText(mQuestion.getCategory());
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

//        SuggestedQuestion.createSuggestedQuestion(mQuestion); //TODO: DELETE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    }

    public void loadingFinished() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(Constants.Loading.QUESTION_LOADING_TIME); // Wait extra time for MathView to load
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
            case R.id.vsBookmark:
                ViewSwitcher viewSwitcher = avh.vsBookmark;
                if(viewSwitcher.getCurrentView().getId() == R.id.ivDoBookmark) {
                    doBookmark();
                }
                else {
                    undoBookmark();
                }
                avh.vsBookmark.showNext();
                break;
        }
    }

    private Task<Bookmark> doBookmark() {
        final Bookmark bookmark = new Bookmark(mResponse);
        return bookmark.getQuestion().fetchIfNeededInBackground().continueWithTask(new Continuation<ParseObject, Task<Bookmark>>() {
            @Override
            public Task<Bookmark> then(Task<ParseObject> task) throws Exception {
                return ParseObjectUtils.saveThenPinWithObjectIdInBackground(bookmark)
                        .continueWith(new Continuation<Void, Bookmark>() {
                            @Override
                            public Bookmark then(Task<Void> task) throws Exception {
                                PrivateStudentData.addBookmarkAndSaveEventually(bookmark).waitForCompletion();
                                mBookmark = bookmark;
                                return bookmark;
                            }
                        });
            }
        });
    }

    private Task<Void> undoBookmark() {
        ParseObject.unpinAllInBackground(mBookmark.getObjectId());
        return mBookmark.deleteEventually();
    }

    private void setOnClickListeners() {
        avh.btnSetLatex.setOnClickListener(this);
        avh.vsBookmark.setOnClickListener(this);
    }

    private Task<Object> loadResponseAndBookmark() {
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
                if (mResponse != null) {
                    return loadBookmark();
                } else {
                    return null;
                }
            }
        });
    }

    private String getResponseId() {
        return mIntent.getStringExtra(Constants.IntentExtra.RESPONSE_ID);
    }

    private Task<Object> loadBookmark() {
        return Bookmark.getQueryWithResponseId(getResponseId())
            .getFirstInBackground()
            .continueWith(new Continuation<Bookmark, Object>() {
                @Override
                public Object then(Task<Bookmark> task) throws Exception {
                    mBookmark = task.getResult();
                    return null;
                }
            });
    }

    private void setBookmarkComponents() {
        ViewSwitcher viewSwitcher = avh.vsBookmark;
        if(mBookmark != null) {
            if(viewSwitcher.getCurrentView().getId() == R.id.ivDoBookmark) {
                viewSwitcher.showNext();
            }
        }
        else {
            if(viewSwitcher.getCurrentView().getId() == R.id.ivUndoBookmark) {
                viewSwitcher.showNext();
            }
        }
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
            avh.txtCorrectIncorrect.setTextColor(ContextCompat.getColor(this, R.color.green));
        }
        else {
            avh.txtCorrectIncorrect.setText("Incorrect!");
            avh.txtCorrectIncorrect.setTextColor(ContextCompat.getColor(this, R.color.red));
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
        private LinearLayout rlQuestionHeader;
        private ProgressBar progressBar;
        private WebView wvPassage;
        private CogniMathView mvQuestion;
        private EditText txtModifyQuestion;
        private Button btnSetLatex;
        private MathView mvExplanation;
        private ViewGroup vgPostAnswer;
        private TextView txtCorrectIncorrect;
        private Button btnSubmit;
        private ViewSwitcher vsBookmark;

        private ActivityViewHolder() {
            rlQuestionHeader = (LinearLayout) findViewById(R.id.rlQuestionHeader);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            wvPassage = (WebView) findViewById(R.id.wvPassage);
            mvQuestion = (CogniMathView) findViewById(R.id.mvQuestion);
            txtModifyQuestion = (EditText) findViewById(R.id.txtModifyQuestion);
            btnSetLatex = (Button) findViewById(R.id.btnSetLatex);
            mvExplanation = (MathView) findViewById(R.id.mvExplanation);
            vgPostAnswer = (ViewGroup) findViewById(R.id.vgPostAnswer);
            txtCorrectIncorrect = (TextView) findViewById(R.id.txtCorrectIncorrect);
            btnSubmit = (Button) findViewById(R.id.btnSubmit);
            vsBookmark = (ViewSwitcher) findViewById(R.id.vsBookmark);
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
