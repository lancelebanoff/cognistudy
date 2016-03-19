package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.CogniRecyclerView;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;
import com.rey.material.widget.Spinner;

/**
 * Created by Kevin on 3/18/2016.
 */
public abstract class QuestionListActivity extends CogniActivity {

    protected Spinner mSpSubjects;
    protected Spinner mSpCategories;
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
        mSpinnerLayout = (LinearLayout) findViewById(R.id.spinnerLayout);
        mQuestionList = (CogniRecyclerView) findViewById(R.id.rvQuestionList);
        initializeSpinners();
    }

    private void initializeSpinners() {
        initializeSubjectSpinner();
        initializeCategorySpinner(Constants.Subject.ALL_SUBJECTS);
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

    private void initializeCategorySpinner(String subject) {
        if(mSpCategories != null) {
            mSpinnerLayout.removeView(mSpCategories);
        }

        String[] categoriesPlusAll;
        if(subject.equals(Constants.Subject.ALL_SUBJECTS)) {
            categoriesPlusAll = new String[1];
        } else {
            String[] categories = Constants.SubjectToCategory.get(subject);
            categoriesPlusAll = new String[categories.length + 1];
            System.arraycopy(categories, 0, categoriesPlusAll, 1, categories.length);
        }
        categoriesPlusAll[0] = "All Categories";

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mSpCategories = new Spinner(this);
        mSpCategories.applyStyle(R.style.Material_Widget_Spinner);
        ArrayAdapter<String> newCategoriesAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoriesPlusAll);
        newCategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCategories.setAdapter(newCategoriesAdapter);

        Spinner.OnItemSelectedListener categoriesListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                getAndDisplayCategory();
            }
        };
        mSpCategories.setOnItemSelectedListener(categoriesListener);

        mSpinnerLayout.addView(mSpCategories, params);
        mSpCategories.setSelection(0);
        mSpCategories.performItemClick(mSpCategories.getChildAt(0), 0, 0);

        mSpCategories.setAnimation(null);
    }

    private void getAndDisplaySubject() {
        String subject = getSelectedSubject();
        setCategories(subject);
        getAndDisplay(subject, Constants.Category.ALL_CATEGORIES);
    }

    private void getAndDisplayCategory() {
        getAndDisplay(getSelectedSubject(), getSelectedCategory());
    }

    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(ParseQuery<QuestionMetaObject> query, String subject, String category) {
        if(!subject.equals(Constants.Subject.ALL_SUBJECTS)) {
            query.whereEqualTo(Question.Columns.subject, subject);
        }
        if(!category.equals(Constants.Category.ALL_CATEGORIES)) {
            query.whereEqualTo(Question.Columns.category, category);
        }
        return query;
    }

    private void setCategories(String subject) {
        initializeCategorySpinner(subject);
    }

    private String getSelectedCategory() {
        return mSpCategories.getAdapter().getItem(mSpCategories.getSelectedItemPosition()).toString();
    }

    private String getSelectedSubject() {
        return mSpSubjects.getAdapter().getItem(mSpSubjects.getSelectedItemPosition()).toString();
    }
}
