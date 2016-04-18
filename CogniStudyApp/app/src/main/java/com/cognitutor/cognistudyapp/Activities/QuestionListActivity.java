package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.CogniRecyclerView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;
import com.rey.material.widget.Spinner;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import bolts.Continuation;
import bolts.Task;

/**
 * Created by Kevin on 3/18/2016.
 */
public abstract class QuestionListActivity extends CogniActivity {

    public static final int REQUEST_CODE = 1;

    protected Spinner mSpSubjects;
    protected Spinner mSpCategories;
    protected QuestionListAdapter mAdapter;
    protected CogniRecyclerView mQuestionList;
    protected Intent mIntent;
    protected Fragment mFragment;
    private TextView mTxtNoResults;

    protected abstract Class<? extends QuestionActivity> getTargetQuestionActivityClass();
    protected abstract String getActivityName();
    protected abstract Class<? extends CogniFragment> getFragmentClass();
    protected abstract ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(String subject, String category);

    private boolean onResumeFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_question_list);
        if(savedInstanceState == null) {
            try {
                mFragment = getFragmentClass().newInstance();
            } catch (Exception e) { e.printStackTrace(); }
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.questionListFragmentContainer, mFragment).commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        onResumeFinished = false;
        if(mAdapter == null) {
            mIntent = getIntent();
            mQuestionList = (CogniRecyclerView) findViewById(R.id.rvQuestionList);
            mSpCategories = (Spinner) findViewById(R.id.spCategoriesQL);
            initializeSpinners();

            mAdapter = createQuestionListAdapter();
            mQuestionList.setAdapter(mAdapter);
        }
    }

    //This was made a method so that QuestionHistoryActivity could override the method and use the alternate constructor
    // for QuestionListAdapter, which passes an additional intent extra to PastQuestionActivity containing the challenge id
    protected QuestionListAdapter createQuestionListAdapter() {
        return new QuestionListAdapter(this, getTargetQuestionActivityClass(),
                getSubjectAndCategoryQuery(Constants.Subject.ALL_SUBJECTS, Constants.Category.ALL_CATEGORIES));
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {
        super.onSupportActionModeStarted(mode);
    }

    @Override
    public void onResume() {
        super.onResume();
        getAndDisplay(Constants.Subject.ALL_SUBJECTS, Constants.Category.ALL_CATEGORIES);
        onResumeFinished = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            if(data.hasExtra(Constants.IntentExtra.UPDATE_OBJECT_ID_IN_LIST)) {
                String objectId = data.getStringExtra(Constants.IntentExtra.UPDATE_OBJECT_ID_IN_LIST);
                mAdapter.notifyObjectIdChanged(objectId);
            }
        }
    }

    public void getAndDisplayFromSelections() {
        //The category spinner's onItemSelected was getting called in initializeSpinners, before onResume(), where
        // getAndDisplay is called. This was causing the items to blink. No need to call getAndDisplay before onResume().
        if(onResumeFinished)
            getAndDisplay(getSelectedSubject(), getSelectedCategory());
    }

    //This method is overridden in SuggestedQuestionsListActivity
    protected void getAndDisplay(String subject, String category) {

        ParseQuery<QuestionMetaObject> query = getSubjectAndCategoryQuery(subject, category);
        query.findInBackground()
                .continueWith(new Continuation<List<QuestionMetaObject>, Object>() {
                    @Override
                    public Object then(Task<List<QuestionMetaObject>> task) throws Exception {
                        final List<QuestionMetaObject> list;
                        List<QuestionMetaObject> result = task.getResult();
                        if(result == null)
                            list = new ArrayList<>();
                        else
                            list = result;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mTxtNoResults == null)
                                    mTxtNoResults = (TextView) findViewById(R.id.txtNoResults);
                                if(list.size() == 0)
                                    mTxtNoResults.setVisibility(View.VISIBLE);
                                else
                                    mTxtNoResults.setVisibility(View.GONE);
                                mAdapter.onDataLoaded(list);
                            }
                        });
                        return null;
                    }
                });
    }

    private void initializeSpinners() {
        initializeSubjectSpinner();
        initializeCategorySpinner();
    }

    private void initializeSubjectSpinner() {
        mSpSubjects = (Spinner) findViewById(R.id.spSubjectsQL);

        String[] subjects = Constants.Subject.getSubjectsPlusAll();
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjects);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSubjects.setAdapter(subjectsAdapter);

        Spinner.OnItemSelectedListener subjectListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                getAndDisplaySubject();
            }
        };

        mSpSubjects.setOnItemSelectedListener(subjectListener);
        mSpSubjects.setAnimation(null);
    }

    private void initializeCategorySpinner() {

        Spinner.OnItemSelectedListener categoriesListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                getAndDisplayFromSelections();
            }
        };
        mSpCategories.setOnItemSelectedListener(categoriesListener);
        mSpCategories.setAnimation(null);

        setCategoriesSpinner(Constants.Subject.ALL_SUBJECTS);
    }

    private void setCategoriesSpinner(String subject) {

        String[] categoriesPlusAll;
        if (subject.equals(Constants.Subject.ALL_SUBJECTS)) {
            categoriesPlusAll = new String[1];
        } else {
            String[] categories = Constants.SubjectToCategory.get(subject);
            categoriesPlusAll = new String[categories.length + 1];
            System.arraycopy(categories, 0, categoriesPlusAll, 1, categories.length);
        }
        categoriesPlusAll[0] = Constants.Category.ALL_CATEGORIES;
        ArrayAdapter<String> categoriesAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesPlusAll);
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCategories.setAdapter(categoriesAdapter);
        mSpCategories.applyStyle(R.style.Material_Widget_Spinner);
        mSpCategories.setSelection(0);
    }

    private void getAndDisplaySubject() {
        String subject = getSelectedSubject();
        setCategoriesSpinner(subject);
        getAndDisplay(subject, Constants.Category.ALL_CATEGORIES);
    }

    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(ParseQuery<QuestionMetaObject> query, String subject, String category) {
        ParseQuery<Question> innerQuery = Question.getQuery();
        boolean includeInnerQuery = false;
        if(!subject.equals(Constants.Subject.ALL_SUBJECTS)) {
            innerQuery.whereEqualTo(Question.Columns.subject, subject);
            includeInnerQuery = true;
        }
        if(!category.equals(Constants.Category.ALL_CATEGORIES)) {
            innerQuery.whereEqualTo(Question.Columns.category, category);
            includeInnerQuery = true;
        }
        if(includeInnerQuery) {
            query.whereMatchesQuery("question", innerQuery);
        }
        return query;
    }

    private String getSelectedCategory() {
        return mSpCategories.getAdapter().getItem(mSpCategories.getSelectedItemPosition()).toString();
    }

    private String getSelectedSubject() {
        return mSpSubjects.getAdapter().getItem(mSpSubjects.getSelectedItemPosition()).toString();
    }
}
