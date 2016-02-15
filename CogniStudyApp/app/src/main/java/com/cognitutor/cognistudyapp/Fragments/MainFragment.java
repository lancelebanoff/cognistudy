package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Activities.QuestionActivity;
import com.cognitutor.cognistudyapp.Adapters.ChallengeQueryAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Lance on 12/27/2015.
 */

public class MainFragment extends CogniPushListenerFragment implements View.OnClickListener {

    private ChallengeQueryAdapter challengeRequestQueryAdapter;
    private ChallengeQueryAdapter yourTurnChallengeQueryAdapter;
    private ChallengeQueryAdapter theirTurnChallengeQueryAdapter;
    private ChallengeQueryAdapter pastChallengeQueryAdapter;
    private ListView challengeRequestListView;
    private ListView yourTurnListView;
    private ListView theirTurnListView;
    private ListView pastChallengeListView;

    public TextView txtChange;

    public static final MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO:2 Don't reload every time
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        Button b = (Button) rootView.findViewById(R.id.btnQuestion);
        b.setOnClickListener(this);

        b = (Button) rootView.findViewById(R.id.btnStartChallenge);
        b.setOnClickListener(this);

        b = (Button) rootView.findViewById(R.id.btnLogout);
        b.setOnClickListener(this);

        b = (Button) rootView.findViewById(R.id.btnDeleteUser);
        b.setOnClickListener(this);

        txtChange = (TextView) rootView.findViewById(R.id.txtChange);

        createChallengeRequestListView(rootView);
        createYourTurnListView(rootView);
        createTheirTurnListView(rootView);
        createPastChallengeListView(rootView);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        txtChange = null;
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getActivity().findViewById(R.id.viewpager);
        createChallengeRequestListView(rootView);
        createYourTurnListView(rootView);
        createTheirTurnListView(rootView);
        createPastChallengeListView(rootView);
    }

    private void createChallengeRequestListView(View rootView) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.accepted, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.curTurnUserId,
                PublicUserData.getPublicUserData().getBaseUserId()));
        challengeRequestQueryAdapter = new ChallengeQueryAdapter(getActivity(), keyValuePairs);

        challengeRequestListView = (ListView) rootView.findViewById(R.id.listChallengeRequests);
        challengeRequestListView.setFocusable(false);
        challengeRequestListView.setAdapter(challengeRequestQueryAdapter);
        challengeRequestQueryAdapter.loadObjects();

        challengeRequestQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                setListViewHeightBasedOnChildren(challengeRequestListView);
            }
        });
    }

    private void createYourTurnListView(View rootView) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.accepted, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.curTurnUserId,
                PublicUserData.getPublicUserData().getBaseUserId()));
        yourTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), keyValuePairs);

        yourTurnListView = (ListView) rootView.findViewById(R.id.listYourTurnChallenges);
        yourTurnListView.setFocusable(false);
        yourTurnListView.setAdapter(yourTurnChallengeQueryAdapter);
        yourTurnChallengeQueryAdapter.loadObjects();

        yourTurnChallengeQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                setListViewHeightBasedOnChildren(yourTurnListView);
            }
        });
    }

    private void createTheirTurnListView(View rootView) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.otherTurnUserId,
                PublicUserData.getPublicUserData().getBaseUserId()));
        theirTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), keyValuePairs);

        theirTurnListView = (ListView) rootView.findViewById(R.id.listTheirTurnChallenges);
        theirTurnListView.setFocusable(false);
        theirTurnListView.setAdapter(theirTurnChallengeQueryAdapter);
        theirTurnChallengeQueryAdapter.loadObjects();

        theirTurnChallengeQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {

            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                setListViewHeightBasedOnChildren(theirTurnListView);
            }
        });
    }

    private void createPastChallengeListView(View rootView) {
        List<Pair> keyValuePairs1 = new ArrayList<>();
        keyValuePairs1.add(new Pair<>(Challenge.Columns.hasEnded, true));
        keyValuePairs1.add(new Pair<>(Challenge.Columns.curTurnUserId,
                PublicUserData.getPublicUserData().getBaseUserId()));

        List<Pair> keyValuePairs2 = new ArrayList<>();
        keyValuePairs2.add(new Pair<>(Challenge.Columns.hasEnded, true));
        keyValuePairs2.add(new Pair<>(Challenge.Columns.otherTurnUserId,
                PublicUserData.getPublicUserData().getBaseUserId()));

        List<List<Pair>> keyValuePairsList = new ArrayList<>();
        keyValuePairsList.add(keyValuePairs1);
        keyValuePairsList.add(keyValuePairs2);
        pastChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), keyValuePairsList, true);

        pastChallengeListView = (ListView) rootView.findViewById(R.id.listPastChallenges);
        pastChallengeListView.setFocusable(false);
        pastChallengeListView.setAdapter(pastChallengeQueryAdapter);
        pastChallengeQueryAdapter.loadObjects();

        pastChallengeQueryAdapter.addOnQueryLoadListener(new ParseQueryAdapter.OnQueryLoadListener<ParseObject>() {
            @Override
            public void onLoading() {
            }

            @Override
            public void onLoaded(List<ParseObject> objects, Exception e) {
                setListViewHeightBasedOnChildren(pastChallengeListView);
            }
        });
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnQuestion:
                Intent intent = new Intent(getActivity(), QuestionActivity.class);
                intent.putExtra(Constants.IntentExtra.QUESTION_ID, "aSVEaMqEfB"); //TODO: Replace with desired questionId
                intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.MAIN_ACTIVITY);
//                eO4TCrdBdn
//                fF4lsHt2iW
                startActivity(intent);
                break;
            case R.id.btnStartChallenge:
                navigateToNewChallengeActivity();
                break;
            case R.id.btnLogout:
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e); return; }
                navigateToRegistrationActivity();
                break;
            case R.id.btnDeleteUser:
                String userId = ParseUser.getCurrentUser().getObjectId();
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e); return; }
                final HashMap<String, Object> params = new HashMap<>();
                params.put("userId", userId);
                ParseCloud.callFunctionInBackground("deleteStudent", params);
                navigateToRegistrationActivity();
        }
    }

    private void navigateToNewChallengeActivity() {
        Intent intent = new Intent(getActivity(), NewChallengeActivity.class);
        intent.putExtra(Constants.IntentExtra.USER1OR2, 1);
        startActivity(intent);
    }

    @Override
    public JSONObject getConditions() {
        JSONObject conditions = new JSONObject();
        try {
            conditions.put(Constants.NotificationData.ACTIVITY, Constants.NotificationData.Activity.MAIN_ACTIVITY);
        } catch (JSONException e) { e.printStackTrace(); }
        return conditions;
    }

    @Override
    public void onReceiveHandler() {
        if (txtChange.getText().equals("Test 1")) {
            txtChange.setText("Test 2!!!");
        }
        else {
            txtChange.setText("Test 1");
        }
    }
}
