package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.CogniRecyclerView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.rey.material.widget.Spinner;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Kevin on 3/18/2016.
 */
public abstract class QuestionListActivity extends CogniActivity {

    protected Spinner mSpSubjects;
    protected Spinner mSpCategories;
    protected ViewFlipper mViewFlipper;
    protected LinearLayout mSpinnerLayout;
    protected ListType mListType;
    protected QuestionListAdapter mAdapter;
    protected CogniRecyclerView mQuestionList;
    protected Intent mIntent;

    public enum ListType {
        BOOKMARKS, SUGGESTED_QUESTIONS, CHALLENGE_QUESTIONS
    }

    protected abstract void getAndDisplay(String subject, String category);
    protected abstract String getActivityName();

    public void navigateToQuestionActivity(View view) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, getActivityName());
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);

        mIntent = getIntent();
        mViewFlipper = (ViewFlipper) findViewById(R.id.vfCategoriesSpinner);
        mSpinnerLayout = (LinearLayout) findViewById(R.id.spinnerLayout);
        mQuestionList = (CogniRecyclerView) findViewById(R.id.rvQuestionList);
        mQuestionList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        initializeSpinners();
//        getAndDisplay(Constants.Subject.ALL_SUBJECTS, Constants.Category.ALL_CATEGORIES);
    }

    private void initializeSpinners() {
        initializeSubjectSpinner();
        initializeCategorySpinners();
    }

    private void initializeSubjectSpinner() {
        mSpSubjects = (Spinner) findViewById(R.id.spSubjectsQL);

        String[] subjects = Constants.Subject.getSubjectsPlusAll();
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, subjects);
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

    private void initializeCategorySpinners() {

        String[] subjectsPlusAll = Constants.Subject.getSubjectsPlusAll();
        for(String subject : subjectsPlusAll) {
            String[] categoriesPlusAll;
            if (subject.equals(Constants.Subject.ALL_SUBJECTS)) {
                categoriesPlusAll = new String[1];
            } else {
                String[] categories = Constants.SubjectToCategory.get(subject);
                categoriesPlusAll = new String[categories.length + 1];
                System.arraycopy(categories, 0, categoriesPlusAll, 1, categories.length);
            }
            categoriesPlusAll[0] = "All Categories";

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            Spinner catSpinner = new Spinner(this);
            catSpinner.applyStyle(R.style.Material_Widget_Spinner);
            ArrayAdapter<String> categoriesAdapter =
                    new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categoriesPlusAll);
            categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            catSpinner.setAdapter(categoriesAdapter);

            Spinner.OnItemSelectedListener categoriesListener = new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(Spinner parent, View view, int position, long id) {
                    getAndDisplayCategory();
                }
            };
            catSpinner.setOnItemSelectedListener(categoriesListener);

            mViewFlipper.addView(catSpinner, params);
        }
        setCategoriesSpinner(Constants.Subject.ALL_SUBJECTS);
    }

    private void setCategoriesSpinner(String subject) {
        String[] subjectsPlusAll = Constants.Subject.getSubjectsPlusAll();
        int i;
        for(i=0; i<subjectsPlusAll.length; i++) {
            if(subject.equals(subjectsPlusAll[i]))
                break;
        }
        mViewFlipper.setDisplayedChild(i);
        mSpCategories = (Spinner) mViewFlipper.getChildAt(i);
        mSpCategories.setSelection(0);
        mSpCategories.performItemClick(mSpCategories.getChildAt(0), 0, 0);
        mSpCategories.setAnimation(null);
    }

    private void getAndDisplaySubject() {
        String subject = getSelectedSubject();
        setCategoriesSpinner(subject);
        getAndDisplay(subject, Constants.Category.ALL_CATEGORIES);
    }

    private void getAndDisplayCategory() {
        getAndDisplay(getSelectedSubject(), getSelectedCategory());
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
