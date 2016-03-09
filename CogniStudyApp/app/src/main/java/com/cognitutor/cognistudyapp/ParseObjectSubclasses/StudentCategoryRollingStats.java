package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.parse.ParseClassName;

/**
 * Created by Kevin on 1/18/2016.
 */
@ParseClassName("StudentCategoryRollingStats")
public class StudentCategoryRollingStats extends StudentTRollingStats {

    public class Columns {
        public static final String category = "category";
        public static final String answeredQuestionIds = "answeredQuestionIds";
    }

    public StudentCategoryRollingStats() {}

    public String getCategory() { return getString(Columns.category); }
    /**
     * Calls saveInBackground() after creation
     * @param baseUserId
     * @param category
     */
    public StudentCategoryRollingStats(String baseUserId, String category) {
        super(baseUserId);
        put(Columns.category, category);
        put(Columns.answeredQuestionIds, new AnsweredQuestionIds(category));
        saveInBackground();
    }

    public AnsweredQuestionIds getAnsweredQuestionIds() { return (AnsweredQuestionIds) getParseObject(Columns.answeredQuestionIds); }

    public void addAnsweredQuestionIdAndSaveEventually(final Question question) {
        final AnsweredQuestionIds answeredQuestionIds = (AnsweredQuestionIds) getParseObject(Columns.answeredQuestionIds);
        if(question.inBundle())
            answeredQuestionIds.addBundleAnsweredQuestionIdAndSaveEventually(question.getObjectId());
        else
            answeredQuestionIds.addSingleAnsweredQuestionIdAndSaveEventually(question.getObjectId());
    }

    public void addSingleAnsweredQuestionIdAndSaveEventually(final String questionId) {
        final AnsweredQuestionIds answeredQuestionIds = (AnsweredQuestionIds) getParseObject(Columns.answeredQuestionIds);
        answeredQuestionIds.addSingleAnsweredQuestionIdAndSaveEventually(questionId);
    }

    public void addBundleAnsweredQuestionIdAndSaveEventually(final String questionId) {
        final AnsweredQuestionIds answeredQuestionIds = (AnsweredQuestionIds) getParseObject(Columns.answeredQuestionIds);
        answeredQuestionIds.addBundleAnsweredQuestionIdAndSaveEventually(questionId);
    }

    @Override
    public String toString() {
        return String.format("%-23s", getCategory()) + " | " + super.toString();
    }
}
