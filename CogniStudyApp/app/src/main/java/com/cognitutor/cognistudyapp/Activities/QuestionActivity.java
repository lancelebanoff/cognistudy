package com.cognitutor.cognistudyapp.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Adapters.AnswerAdapter;
import com.cognitutor.cognistudyapp.Custom.AnswerListView;
import com.cognitutor.cognistudyapp.Custom.BookmarkButton;
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
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionReport;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.apache.commons.io.IOUtils;

import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import io.github.kexanie.library.MathView;

public abstract class QuestionActivity extends CogniActivity implements View.OnClickListener {

    protected abstract String getQuestionAndResponsePinName();
    protected abstract String getQuestionTitle();

    /**
     * Extras:
     *      PARENT_ACTIVITY: string
     *      CHALLENGE_ID: String
     *      USER1OR2: int
     */
    protected Intent mIntent;
    private AnswerListView listView;
    private ActivityViewHolder avh;
    protected Question mQuestion;
    protected QuestionContents mQuestionContents;
    private AnswerAdapter answerAdapter;
    protected Response mResponse = null;
    private Bookmark mBookmark = null;
    private BookmarkOption mBookmarkOption = BookmarkOption.DO_NOTHING;

    enum BookmarkOption {
        DO_NOTHING, DO_BOOKMARK, UNDO_BOOKMARK
    }

    public static void setSubjectIcon(String subject, ImageView iv) {
        int icon;
        switch (subject) {
            case Constants.Subject.ENGLISH:
                icon = R.drawable.icon_english;
                break;
            case Constants.Subject.MATH:
                icon = R.drawable.icon_math;
                break;
            case Constants.Subject.SCIENCE:
                icon = R.drawable.icon_science;
                break;
            default:
                icon = R.drawable.icon_reading;
                break;
        }
        iv.setImageResource(icon);
    }

    public static void setResponseStatusIcon(ImageView ivResponseStatus, String responseStatus) {
        switch (responseStatus) {
            case Constants.ResponseStatusType.CORRECT:
                ivResponseStatus.setImageResource(R.drawable.ic_icon_correct);
                ivResponseStatus.setVisibility(View.VISIBLE);
                break;
            case Constants.ResponseStatusType.INCORRECT:
                ivResponseStatus.setImageResource(R.drawable.ic_icon_incorrect);
                ivResponseStatus.setVisibility(View.VISIBLE);
                break;
            default:
                ivResponseStatus.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        mIntent = getIntent();

        listView = (AnswerListView) findViewById(R.id.listView);
        addComponents();
        setOnClickListeners();
        ClickableListItem.setQuestionAnswered(false);
        //TODO: Move this
        try {
            loadResponseAndBookmark().waitForCompletion(); //If this is a past question, mResponse will be loaded
        } catch (InterruptedException e) { e.printStackTrace(); }
        loadQuestion();
        setBookmarkComponents();
        setTitle(getQuestionTitle());
    }

    @Override
    protected void onPause() {
        super.onPause();
        doOrUndoBookmark();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_question, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_report:
                onClick_menuReport();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getQuestionId() {
        return mIntent.getStringExtra(Constants.IntentExtra.QUESTION_ID);
    }

    protected String getQuestionMetaId() {
        return mIntent.getStringExtra(Constants.IntentExtra.QUESTION_META_ID);
    }

    public void loadQuestion() {

        int selectedAnswer = -1;
        if(mResponse != null) {
            selectedAnswer = mResponse.getSelectedAnswer();
        }

        try {
            Question.getQuestionWithContentsInBackground(getQuestionAndResponsePinName(), getQuestionId()).continueWith(new Continuation<Question, Object>() {
                @Override
                public Object then(Task<Question> task) throws Exception {
                    if(task.isFaulted()) {
                        Exception e = task.getError();
                        e.printStackTrace();
                        throw e;
                    }
                    mQuestion = task.getResult();
                    return null;
                }
            }).waitForCompletion();
        } catch (InterruptedException e) { e.printStackTrace(); }

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
        ImageView ivSubject = (ImageView) findViewById(R.id.ivSubject);
        setSubjectIcon(mQuestion.getSubject(), ivSubject);
        TextView txtCategory = (TextView) findViewById(R.id.txtCategory);
        txtCategory.setText(mQuestion.getCategory());
        avh.mvQuestion.setText(mQuestionContents.getQuestionText());
//        avh.mvQuestion.loadUrl("file:///android_asset/html/passage.html");
        avh.mvExplanation.setText(mQuestionContents.getExplanation());

        if(mQuestion.inBundle()) {
            int numInBundle = mQuestion.getNumberInBundle();
            avh.txtNumberInBundle.setText("Question " + numInBundle);
            avh.txtNumberInBundle.setVisibility(View.VISIBLE);

            QuestionBundle bundle = mQuestion.getQuestionBundle();
            try {
                bundle.fetchFromLocalDatastore();
            } catch (ParseException e) {
                e.printStackTrace();
                try {
                    bundle.fetchIfNeeded();
                } catch (ParseException e2) {
                    e2.printStackTrace();
                    Log.e("Fetching bundle", "Bundle should be in local datastore but was not");
                }
            }
            avh.wvPassage.loadData(buildPassageHtml(bundle.getPassageText()), "text/html", "UTF-8");
        }
        else {
            avh.txtNumberInBundle.setVisibility(View.INVISIBLE);
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
        }
    }

    private void doOrUndoBookmark() {
        if (mBookmarkOption == BookmarkOption.DO_BOOKMARK) {
            doBookmark();
        } else if (mBookmarkOption == BookmarkOption.UNDO_BOOKMARK) {
            undoBookmark();
        }
    }

    private Task<Bookmark> doBookmark() {
        return Task.callInBackground(new Callable<Bookmark>() {
            @Override
            public Bookmark call() throws Exception {
                int count = 0;
                while (mResponse == null) {
                    Thread.sleep(100);
                    count++;
                    if (count > 100) {
                        Log.e("doBookmark", "TIMEOUT");
                        return null;
                    }
                }
                final Bookmark bookmark = new Bookmark(mResponse);
                bookmark.getQuestion().fetchIfNeededInBackground().continueWithTask(new Continuation<ParseObject, Task<Bookmark>>() {
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
                }).waitForCompletion();
                return bookmark;
            }
        });
    }

    private Task<Void> undoBookmark() {
        ParseObject.unpinAllInBackground(mBookmark.getObjectId());
        return mBookmark.deleteEventually();
    }

    private void setOnClickListeners() {
        avh.btnSetLatex.setOnClickListener(this);
        avh.cbBookmark.setOnClickListener(getCbBookmarkOnClickListener());
    }

    private View.OnClickListener getCbBookmarkOnClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BookmarkButton cbBookmark = avh.cbBookmark;
                if(cbBookmark.isChecked() && mBookmark == null) {
                    mBookmarkOption = BookmarkOption.DO_BOOKMARK;
                }
                else if(!cbBookmark.isChecked() && mBookmark != null) {
                    mBookmarkOption = BookmarkOption.UNDO_BOOKMARK;
                }
                else {
                    mBookmarkOption = BookmarkOption.DO_NOTHING;
                }
            }
        };
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
        BookmarkButton cbBookmark = avh.cbBookmark;
        boolean isBookmarked = mBookmark != null;
        boolean isChecked = cbBookmark.isChecked();
        if((isBookmarked && !isChecked) || (!isBookmarked && isChecked)) {
            cbBookmark.setChecked(!isChecked);
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
            setResponseStatusIcon(avh.ivCorrectIncorrect, Constants.ResponseStatusType.CORRECT);
            avh.txtCorrectAnswer.setVisibility(View.GONE);
        }
        else {
            avh.txtCorrectIncorrect.setText("Incorrect!");
            avh.txtCorrectIncorrect.setTextColor(ContextCompat.getColor(this, R.color.red));
            setResponseStatusIcon(avh.ivCorrectIncorrect, Constants.ResponseStatusType.INCORRECT);
            setCorrectAnswerText();
        }
        avh.btnSubmit.setVisibility(View.GONE);
        avh.vgPostAnswer.setVisibility(View.VISIBLE);
        ClickableListItem.setQuestionAnswered(true);

        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.timedScrollToPosition(listView.getAdapter().getCount() - 1, 750);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(Constants.Loading.QUESTION_TUTORIAL_WAIT_TIME);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showTutorialDialogIfNeeded(Constants.Tutorial.ADDING_BOOKMARKS, null);
                    }
                });
            }
        }).start();
    }

    private void setCorrectAnswerText() {
        char correctLetter = (char) ('A' + mQuestionContents.getCorrectIdx());
        avh.txtCorrectAnswer.setText("Correct Answer: " + correctLetter);
        avh.txtCorrectAnswer.setVisibility(View.VISIBLE);
    }

    protected void navigateToParentActivity() {
        Intent intent = new Intent();
        finish();
    }

    public void navigateToParentActivity(View v) {
        navigateToParentActivity();
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
        private ImageView ivCorrectIncorrect;
        private TextView txtCorrectAnswer;
        private Button btnSubmit;
        private BookmarkButton cbBookmark;
        private TextView txtNumberInBundle;

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
            ivCorrectIncorrect = (ImageView) findViewById(R.id.ivCorrectIncorrect);
            txtCorrectAnswer = (TextView) findViewById(R.id.txtCorrectAnswer);
            btnSubmit = (Button) findViewById(R.id.btnSubmit);
            cbBookmark = (BookmarkButton) findViewById(R.id.cbBookmark);
            txtNumberInBundle = (TextView) findViewById(R.id.txtNumberInBundle);
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

    private void onClick_menuReport() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResourcesString(R.string.title_dialog_report_question));

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_report_question, null);

        builder.setView(view);

        final EditText editTextMessage = (EditText) view.findViewById(R.id.editTxtMessage);

        builder.setPositiveButton(getResourcesString(R.string.dialog_report_option_submit), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String message = editTextMessage.getText().toString();
                QuestionReport report = new QuestionReport(mQuestion.getObjectId(), message);
                report.saveInBackground().continueWith(new Continuation<Void, Object>() {
                    @Override
                    public Object then(Task<Void> task) throws Exception {
                        if(task.isFaulted()) {
                            String errorMsg = task.getError().getMessage();
                            if(errorMsg.equals("Too many reports in one day")) {
                                makeToast(getResourcesString(R.string.toast_report_question_error_too_many_reports));
                            }
                            else if(errorMsg.equals("User already reported this question")) {
                                makeToast(getResourcesString(R.string.toast_report_question_error_already_reported));
                            }
                        }
                        else {
                            makeToast(getResourcesString(R.string.toast_report_question_submitted));
                        }
                        return null;
                    }
                });
            }
        });
        builder.setNegativeButton(getResourcesString(R.string.dialog_report_option_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    private void makeToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getResourcesString(int id) {
        return getResources().getString(id);
    }

}
