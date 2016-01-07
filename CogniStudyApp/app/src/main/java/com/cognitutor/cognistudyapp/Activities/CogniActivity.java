package com.cognitutor.cognistudyapp.Activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import com.cognitutor.cognistudyapp.R;

public class CogniActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cogni);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Makes the ActionBar "up" button mimic the Android "back" button
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return(super.onOptionsItemSelected(item));
    }
}
