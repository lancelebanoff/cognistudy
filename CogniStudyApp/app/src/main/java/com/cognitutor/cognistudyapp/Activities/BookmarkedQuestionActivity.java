package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;

import com.cognitutor.cognistudyapp.Custom.AnsweredQuestionActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;
import com.cognitutor.cognistudyapp.R;

/**
 * Created by Kevin on 3/23/2016.
 */
public class BookmarkedQuestionActivity extends AnsweredQuestionActivity {

    @Override
    protected String getQuestionAndResponsePinName() {
        return getQuestionMetaId();
    }

    @Override
    protected String getQuestionTitle() {
        return getResources().getString(R.string.title_activity_bookmarked_question);
    }
}
