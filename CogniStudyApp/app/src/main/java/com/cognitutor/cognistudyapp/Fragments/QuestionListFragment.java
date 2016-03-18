package com.cognitutor.cognistudyapp.Fragments;

import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;
import com.rey.material.widget.Spinner;

import java.util.Arrays;

/**
 * Created by Kevin on 3/17/2016.
 */
public class QuestionListFragment extends CogniFragment {

    private Spinner mSpSubjects;
    private Spinner mSpCategories;
    private LinearLayout mSpinnerLayout;
    private ListType mListType;

    public enum ListType {
        BOOKMARKS, SUGGESTED_QUESTIONS, CHALLENGE_QUESTIONS
    }

    public static final QuestionListFragment newInstance(ListType type) {
        QuestionListFragment questionListFragment = new QuestionListFragment();
        questionListFragment.mListType = type;
        return questionListFragment;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpinnerLayout = (LinearLayout) getView().findViewById(R.id.spinnerLayout);
        initializeSpinners();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_question_list, container, false);
    }

    private void initializeSpinners() {
        initializeSubjectSpinner();
        initializeCategorySpinner(null);
    }

    private void initializeSubjectSpinner() {
        mSpSubjects = (Spinner) getView().findViewById(R.id.spSubjectsQL);

        String[] subjects = Constants.Subject.getSubjectsPlusAll();
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, subjects);
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
        if(subject != null) {
            String[] categories = Constants.SubjectToCategory.get(subject);
            categoriesPlusAll = new String[categories.length + 1];
            System.arraycopy(categories, 0, categoriesPlusAll, 1, categories.length);
        } else {
            categoriesPlusAll = new String[1];
        }
        categoriesPlusAll[0] = "All Categories";

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mSpCategories = new Spinner(getContext());
        mSpCategories.applyStyle(R.style.Material_Widget_Spinner);
        ArrayAdapter<String> newCategoriesAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categoriesPlusAll);
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
        getAndDisplay(subject, null);
    }

    private void getAndDisplayCategory() {
        getAndDisplay(getSelectedSubject(), getSelectedCategory());
    }

    private void getAndDisplay(String subject, String category) {
        switch (mListType) {
            case BOOKMARKS:
                getAndDisplayBookmarks(subject, category);
                break;
            case SUGGESTED_QUESTIONS:
                getAndDisplaySuggestedQuestions(subject, category);
                break;
            case CHALLENGE_QUESTIONS:
                getAndDisplayChallengeQuestions(subject, category);
                break;
        }
    }

    private void getAndDisplayBookmarks(String subject, String category) {

    }

    private void getAndDisplaySuggestedQuestions(String subject, String category) {

    }

    private void getAndDisplayChallengeQuestions(String subject, String category) {

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
