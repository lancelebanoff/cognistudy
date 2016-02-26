package com.cognitutor.cognistudyapp.ParseObjectSubclasses;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.QueryUtils;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import bolts.Task;

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

    public void addAnsweredQuestionIdAndSaveEventually(final String questionId) {
        final AnsweredQuestionIds answeredQuestionIds = (AnsweredQuestionIds) getParseObject(Columns.answeredQuestionIds);
        answeredQuestionIds.fetchFromLocalDatastoreInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    answeredQuestionIds.addAnsweredQuestionIdAndSaveEventually(questionId);
                    return;
                }
                //TODO: What to do when internet is down
                answeredQuestionIds.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        answeredQuestionIds.pinInBackground(Constants.PinNames.CurrentUser);
                        answeredQuestionIds.addAnsweredQuestionIdAndSaveEventually(questionId);
                    }
                });
            }
        });
    }

    @Override
    public String toString() {
        return String.format("%-23s", getCategory()) + " | " + super.toString();
    }
}
