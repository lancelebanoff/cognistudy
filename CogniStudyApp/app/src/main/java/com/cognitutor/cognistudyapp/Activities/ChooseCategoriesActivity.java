package com.cognitutor.cognistudyapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;

public class ChooseCategoriesActivity extends CogniActivity {

    private ListView lvCategories;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_categories);

        displayCategories();
    }

    private void displayCategories() {
        lvCategories = (ListView) findViewById(R.id.lvCategories);
        String[] categoryNames = Constants.getAllConstants(Constants.Category.class);
        mAdapter = new ArrayAdapter<>(this, R.layout.checkbox_category, categoryNames);
        lvCategories.setAdapter(mAdapter);
    }

    public void onClick_Continue(View view) {
        finish();
    }
}
