package com.cognitutor.cognistudyapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BookmarksActivity extends CogniActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
    }

    public void navigateToPastQuestionActivity(View view) {
        Intent intent = new Intent(this, PastQuestionActivity.class);
        // TODO:1 put extras saying it came from bookmarks
        startActivity(intent);
    }
}
