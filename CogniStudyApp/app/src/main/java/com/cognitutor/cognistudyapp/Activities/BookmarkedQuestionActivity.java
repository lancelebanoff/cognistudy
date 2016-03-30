package com.cognitutor.cognistudyapp.Activities;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Bookmark;

/**
 * Created by Kevin on 3/23/2016.
 */
public class BookmarkedQuestionActivity extends QuestionActivity {

    @Override
    protected String getQuestionAndResponsePinName() {
        return getQuestionMetaId();
    }
}
