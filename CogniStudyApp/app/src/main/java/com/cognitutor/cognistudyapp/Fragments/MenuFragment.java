package com.cognitutor.cognistudyapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.cognitutor.cognistudyapp.Activities.AchievementsActivity;
import com.cognitutor.cognistudyapp.Activities.BookmarksActivity;
import com.cognitutor.cognistudyapp.Activities.HelpActivity;
import com.cognitutor.cognistudyapp.R;
import com.cognitutor.cognistudyapp.Activities.RegistrationActivity;
import com.cognitutor.cognistudyapp.Activities.SettingsActivity;
import com.cognitutor.cognistudyapp.Activities.ShopActivity;
import com.cognitutor.cognistudyapp.Activities.SuggestedQuestionsActivity;
import com.parse.ParseException;

/**
 * Created by Lance on 12/27/2015.
 */
public class MenuFragment extends CogniFragment implements View.OnClickListener {

    public MenuFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_menu, container, false);

        int[] allButtonIds = {
                R.id.btnSuggestedQuestions,
                R.id.btnBookmarks,
                R.id.btnAchievements,
                R.id.btnShop,
                R.id.btnSettings,
                R.id.btnHelp,
                R.id.btnLogout
        };
        for(int buttonId : allButtonIds) {
            Button b = (Button) rootView.findViewById(buttonId);
            b.setOnClickListener(this);
        }

        return rootView;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnSuggestedQuestions:
                navigateToActivity(SuggestedQuestionsActivity.class);
                break;
            case R.id.btnBookmarks:
                navigateToActivity(BookmarksActivity.class);
                break;
            case R.id.btnAchievements:
                navigateToActivity(AchievementsActivity.class);
                break;
            case R.id.btnShop:
                navigateToActivity(ShopActivity.class);
                break;
            case R.id.btnSettings:
                navigateToActivity(SettingsActivity.class);
                break;
            case R.id.btnHelp:
                navigateToActivity(HelpActivity.class);
                break;
            case R.id.btnLogout:
                promptLogout();
                break;
        }
    }

    private void navigateToActivity(Class activityClass) {
        Intent intent = new Intent(getActivity(), activityClass);
        startActivity(intent);
    }

    private void promptLogout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Are you sure you want to log out from CogniStudy?");
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e); return; }
                navigateToRegistrationActivity();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}