package com.cognitutor.cognistudyapp.Fragments;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

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
    }

    private void initializeSpinners() {
        mSpSubjects = (Spinner) getView().findViewById(R.id.spSubjectsQL);
        mSpCategories = (Spinner) getView().findViewById(R.id.spCategoriesQL);

        String[] subjects = Constants.Subject.getSubjectsPlusAll();
        ArrayAdapter<String> subjectsAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, subjects);
        subjectsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpSubjects.setAdapter(subjectsAdapter);

        String[] categories = {""};
        ArrayAdapter<String> categoriesAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categories);
        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpCategories.setAdapter(categoriesAdapter);

        Spinner.OnItemSelectedListener subjectListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                getAndDisplaySubject();
            }
        };

        Spinner.OnItemSelectedListener categoriesListener = new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(Spinner parent, View view, int position, long id) {
                getAndDisplayCategory();
            }
        };
        mSpSubjects.setOnItemSelectedListener(subjectListener);
        mSpSubjects.setAnimation(null);
        mSpCategories.setOnItemSelectedListener(categoriesListener);
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
        String[] categories = Constants.SubjectToCategory.get(subject);
        String[] categoriesPlusAll = new String[categories.length + 1];
        categoriesPlusAll[0] = "All";
        System.arraycopy(categories, 0, categoriesPlusAll, 1, categories.length);
        ArrayAdapter<String> newCategoriesAdapter =
                new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categoriesPlusAll);
        mSpCategories.setAdapter(newCategoriesAdapter);
    }

    private String getSelectedCategory() {
        return mSpCategories.getAdapter().getItem(mSpCategories.getSelectedItemPosition()).toString();
    }

    private String getSelectedSubject() {
        return mSpSubjects.getAdapter().getItem(mSpSubjects.getSelectedItemPosition()).toString();
    }
}
