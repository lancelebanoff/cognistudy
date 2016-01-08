package com.cognitutor.cognistudyapp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.cognitutor.cognistudyapp.Custom.ErrorHandler;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseUser;

public class CogniActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cogni);

        Intent callingIntent = getIntent();
        Bundle extrasBundle = callingIntent.getExtras();
        if(extrasBundle != null && extrasBundle.containsKey("toastMessage")) {
            Toast.makeText(getApplicationContext(), extrasBundle.getString("toastMessage"), Toast.LENGTH_LONG).show();
        }
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

    public void handleParseError(String errorMsg, ParseException e) {
        e.printStackTrace();
        String action = ErrorHandler.determineAction(e);
        switch (action) {
            case ErrorHandler.Action.LOGOUT:
                ParseUser.logOut();
                Intent intent = new Intent(this, RegistrationActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("toastMessage", ErrorHandler.ErrorMsg.LOGIN_AGAIN);
                startActivity(intent);
                break;
            case ErrorHandler.Action.SHOW_MESSAGE:
                Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
    }
}
