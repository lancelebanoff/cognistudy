package com.cognitutor.cognistudyapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Adapters.ChallengeQueryAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQueryAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public static ArrayAdapter<ParseObject> answeredQuestionIdAdapter;
    private ListView answeredQuestionIdsListView;
    private SwipeRefreshLayout mSwipeRefreshLayout;

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

        b = (Button) rootView.findViewById(R.id.btnViewLocalDatastore);
        b.setOnClickListener(this);

        createAllListViews(rootView);
        setSwipeRefreshLayout(rootView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getActivity().findViewById(R.id.viewpager);
        createAllListViews(rootView);
    }

    private void createAllListViews(View rootView) {
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
        challengeRequestQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairs);

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
        yourTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairs);

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
        theirTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairs);

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
        pastChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairsList, true);

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

        View parentCardView = (View) listView.getParent().getParent();
        if(listAdapter.getCount() == 0) {
            parentCardView.setVisibility(View.GONE);
        } else {
            parentCardView.setVisibility(View.VISIBLE);
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

    private void setSwipeRefreshLayout(View rootView) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
    }

    public void refresh() {
        final View rootView = getView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                createAllListViews(rootView);
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnQuestion:
                DateUtils.generateRandomStats(DateUtils.BlockType.MONTH);
//                Intent intent = new Intent(getActivity(), QuestionActivity.class);
//                intent.putExtra(Constants.IntentExtra.QUESTION_ID, "eO4TCrdBdn"); //TODO: Replace with desired questionId
//                intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, Constants.IntentExtra.ParentActivity.MAIN_ACTIVITY);
//                //aSVEaMqEfB
//                //fF4lsHt2iW
//                //zpyHpKMb5S
//                startActivity(intent);
                break;
            case R.id.btnStartChallenge:
                navigateToNewChallengeActivity();
                break;
            case R.id.btnLogout:
                try {
                    logout();
                } catch (ParseException e) { handleParseError(e);
                    return; }
                navigateToRegistrationActivity();
                break;
            case R.id.btnViewLocalDatastore:
                ParseObjectUtils.logPinnedObjects(false);
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
    }
}
