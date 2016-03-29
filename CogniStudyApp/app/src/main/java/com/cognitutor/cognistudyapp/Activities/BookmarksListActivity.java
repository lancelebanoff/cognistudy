package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cognitutor.cognistudyapp.Adapters.QuestionListAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Fragments.CogniFragment;
import com.cognitutor.cognistudyapp.Fragments.PastQuestionsFragment;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.R;

public class BookmarksListActivity extends QuestionListActivity {

    @Override
    protected Class<? extends QuestionMetaObject> getTargetMetaClass() {
        return Bookmark.class;
    }

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
        return PastQuestionsFragment.class;
    }
}
