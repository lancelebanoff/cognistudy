package com.cognitutor.cognistudyapp.Activities;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.Fragments.BookmarkAndQuestionHistoryListFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.parse.ParseQuery;

public class BookmarksListActivity extends QuestionListActivity {

    @Override
    protected Class<? extends QuestionActivity> getTargetQuestionActivityClass() {
        return BookmarkedQuestionActivity.class;
    }

    @Override
    protected String getActivityName() {
        return Constants.IntentExtra.ParentActivity.BOOKMARKS_LIST_ACTIVITY;
    }

    @Override
    protected Class<? extends CogniFragment> getFragmentClass() {
        return BookmarkAndQuestionHistoryListFragment.class;
    }

    @Override
    protected ParseQuery<QuestionMetaObject> getSubjectAndCategoryQuery(String subject, String category) {
        return QuestionMetaObject.getSubjectAndCategoryQuery(Bookmark.class, subject, category)
                .fromLocalDatastore();
    }
}
