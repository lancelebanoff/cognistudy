package com.cognitutor.cognistudyapp.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.cognitutor.cognistudyapp.Activities.MainActivity;
import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Adapters.ChallengeQueryAdapter;
import com.cognitutor.cognistudyapp.Adapters.TutorRequestAdapter;
import com.cognitutor.cognistudyapp.Custom.ChallengeRecyclerView;
import com.cognitutor.cognistudyapp.Custom.CogniButton;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.ParseObjectUtils;
import com.cognitutor.cognistudyapp.Custom.TutorRequestListView;
import com.cognitutor.cognistudyapp.Custom.UserUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.R;
import com.parse.FindCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import bolts.Continuation;
import bolts.Task;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Lance on 12/27/2015.
 */

public class MainFragment extends CogniPushListenerFragment implements View.OnClickListener {

    private final int NUM_ADAPTERS = 4;

    private TutorRequestAdapter tutorRequestAdapter;
    private ChallengeQueryAdapter challengeRequestQueryAdapter;
    private ChallengeQueryAdapter yourTurnChallengeQueryAdapter;
    private ChallengeQueryAdapter theirTurnChallengeQueryAdapter;
    private ChallengeQueryAdapter pastChallengeQueryAdapter;
    private TutorRequestListView tutorRequestListView;
    private ChallengeRecyclerView challengeRequestListView;
    private ChallengeRecyclerView yourTurnListView;
    private ChallengeRecyclerView theirTurnListView;
    private ChallengeRecyclerView pastChallengeListView;
    private AtomicInteger mNumAdaptersLoaded;

    public static ArrayAdapter<ParseObject> answeredQuestionIdAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private ArrayList<ChallengeQueryAdapter> adapterList;
    private GifImageView mGifArrow;

    public static final MainFragment newInstance() {
        return new MainFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapterList = new ArrayList<>();
        mNumAdaptersLoaded = new AtomicInteger(0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO:2 Don't reload every time
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        CogniButton b = (CogniButton) rootView.findViewById(R.id.btnStartChallenge);
        b.setOnClickListener(this);

        createAllListViews(rootView);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        int density = getResources().getDisplayMetrics().densityDpi;
        String TAG = "Display size";
        switch(density)
        {
            case DisplayMetrics.DENSITY_LOW:
                Log.d(TAG, "LDPI");
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                Log.d(TAG, "MDPI");
                break;
            case DisplayMetrics.DENSITY_HIGH:
                Log.d(TAG, "HDPI");
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                Log.d(TAG, "XHDPI");
                break;
        }

        loadChallengesAndTutorRequests();
        showOrHideArrow();
        setSwipeRefreshLayout(getView());
        initializeBroadcastReceiver();
    }

    private void loadChallengesAndTutorRequests() {
        mNumAdaptersLoaded.set(0);
        loadChallengesFromNetwork();
        tutorRequestAdapter.loadTutorRequests(true);
    }

    private void createAllListViews(final View rootView) {
        endChallengesThatRanOutOfTime();
        createTutorRequestListView(rootView);
        createChallengeRequestListView(rootView);
        createYourTurnListView(rootView);
        createTheirTurnListView(rootView);
        createPastChallengeListView(rootView);
    }

    private void endChallengesThatRanOutOfTime() { //TODO: Where should this go if not in refresh?
        List<ParseQuery<Challenge>> queries = new ArrayList<ParseQuery<Challenge>>();
        queries.add(Challenge.getQuery()
                .whereEqualTo(Challenge.Columns.curTurnUserId, UserUtils.getCurrentUserId()));
        queries.add(Challenge.getQuery()
                .whereEqualTo(Challenge.Columns.otherTurnUserId, UserUtils.getCurrentUserId()));
        ParseQuery.or(queries).findInBackground(new FindCallback<Challenge>() {
            @Override
            public void done(List<Challenge> challenges, ParseException error) {
                for (Challenge challenge : challenges) {
                    if (!challenge.getHasEnded() && !challenge.getChallengeType().equals(Constants.ChallengeType.ONE_PLAYER)) {
                        Calendar calendarCurrentDate = Calendar.getInstance(); // Today
                        calendarCurrentDate.setTime(new Date());

                        Calendar calendarEndDate = Calendar.getInstance(); // 3 days past time last played
                        calendarEndDate.setTime(challenge.getTimeLastPlayed());
                        calendarEndDate.add(Calendar.DATE, Constants.ChallengeAttribute.NUM_DAYS_PER_TURN);

                        if (calendarCurrentDate.compareTo(calendarEndDate) == 1) {
                            challenge.setHasEnded(true);
                            Date endDate = calendarEndDate.getTime();
                            challenge.setEndDate(endDate);
                            challenge.setTimeLastPlayed(endDate);
                            try {
                                challenge.save();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            // Delete unaccepted challenges
                            if (!challenge.getAccepted()) {
                                final HashMap<String, Object> params = new HashMap<>();
                                params.put(Challenge.Columns.objectId, challenge.getObjectId());
                                try {
                                    ParseCloud.callFunction(Constants.CloudCodeFunction.DELETE_CHALLENGE, params);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    private void createTutorRequestListView(final View rootView) {
        final Activity activity = getActivity();
        final MainFragment fragment = this;
        tutorRequestListView = (TutorRequestListView) rootView.findViewById(R.id.listTutorRequests);
        CardView parentCardView = (CardView) rootView.findViewById(R.id.cvTutorRequests);
        tutorRequestListView.setParentCardView(parentCardView);
        tutorRequestAdapter = new TutorRequestAdapter(activity, fragment, tutorRequestListView);
        tutorRequestListView.setFocusable(false);
        tutorRequestListView.setAdapter(tutorRequestAdapter);
    }

    private void createChallengeRequestListView(final View rootView) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.accepted, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.curTurnUserId, UserUtils.getCurrentUserId()));
        challengeRequestListView = (ChallengeRecyclerView) rootView.findViewById(R.id.listChallengeRequests);
        CardView parentCardView = (CardView) rootView.findViewById(R.id.cvChallengeRequests);
        challengeRequestListView.setParentCardView(parentCardView);
        challengeRequestQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, challengeRequestListView, keyValuePairs);
        createListView(challengeRequestListView, challengeRequestQueryAdapter);
    }

    private void createYourTurnListView(final View rootView) {
        final List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.accepted, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.curTurnUserId, UserUtils.getCurrentUserId()));
        yourTurnListView = (ChallengeRecyclerView) rootView.findViewById(R.id.listYourTurnChallenges);
        CardView parentCardView = (CardView) rootView.findViewById(R.id.cvYourTurnChallenges);
        yourTurnListView.setParentCardView(parentCardView);
        yourTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, yourTurnListView, keyValuePairs);
        createListView(yourTurnListView, yourTurnChallengeQueryAdapter);
    }

    private void createTheirTurnListView(final View rootView) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.otherTurnUserId, UserUtils.getCurrentUserId()));
        theirTurnListView = (ChallengeRecyclerView) rootView.findViewById(R.id.listTheirTurnChallenges);
        CardView parentCardView = (CardView) rootView.findViewById(R.id.cvTheirTurnChallenges);
        theirTurnListView.setParentCardView(parentCardView);
        theirTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, theirTurnListView, keyValuePairs);
        createListView(theirTurnListView, theirTurnChallengeQueryAdapter);
    }

    private void createPastChallengeListView(final View rootView) {
        List<Pair> keyValuePairs1 = new ArrayList<>();
        keyValuePairs1.add(new Pair<>(Challenge.Columns.hasEnded, true));
        keyValuePairs1.add(new Pair<>(Challenge.Columns.curTurnUserId, UserUtils.getCurrentUserId()));

        List<Pair> keyValuePairs2 = new ArrayList<>();
        keyValuePairs2.add(new Pair<>(Challenge.Columns.hasEnded, true));
        keyValuePairs2.add(new Pair<>(Challenge.Columns.otherTurnUserId, UserUtils.getCurrentUserId()));

        List<List<Pair>> keyValuePairsList = new ArrayList<>();
        keyValuePairsList.add(keyValuePairs1);
        keyValuePairsList.add(keyValuePairs2);

        pastChallengeListView = (ChallengeRecyclerView) rootView.findViewById(R.id.listPastChallenges);
        CardView parentCardView = (CardView) rootView.findViewById(R.id.cvPastChallenges);
        pastChallengeListView.setParentCardView(parentCardView);
        pastChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, pastChallengeListView, keyValuePairsList, true);
        createListView(pastChallengeListView, pastChallengeQueryAdapter);
    }

    private void createListView(final ChallengeRecyclerView recyclerView, final ChallengeQueryAdapter adapter) {
        recyclerView.setFocusable(false);
        recyclerView.setAdapter(adapter);
        adapterList.add(adapter);
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
                loadChallengesAndTutorRequests();
                if (mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        }, 1000);
    }

    private void loadChallengesFromNetwork() {
        for (ChallengeQueryAdapter adapter : adapterList) {
            adapter.loadFromNetwork().continueWith(new Continuation<List<Challenge>, Void>() {
                @Override
                public Void then(Task<List<Challenge>> task) throws Exception {
                    int numAdaptersLoaded = mNumAdaptersLoaded.incrementAndGet();
                    if (numAdaptersLoaded == NUM_ADAPTERS) {
                        ((MainActivity) getActivity()).challengesFinishedLoading = true;
                    }
                    return null;
                }
            });
        }
    }

    private void showOrHideArrow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(Constants.Loading.CHALLENGE_ARROW_WAIT_TIME);
                while (!allAdaptersExist()) {

                }
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (allAdaptersAreEmpty()) {
                                showArrowGif();
                            }
                        }
                    });
                }
            }
        }).start();
    }

    private void showArrowGif() {
        RelativeLayout rlContent = (RelativeLayout) getActivity().findViewById(R.id.rlContentMain);
        if (mGifArrow != null) {
            rlContent.removeView(mGifArrow);
        }

        mGifArrow = new GifImageView(getActivity());
        mGifArrow.setImageResource(R.drawable.animation_bouncing_arrow);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.start_challenge_arrow_width),
                (int) getResources().getDimension(R.dimen.start_challenge_arrow_height)
        );
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.btnStartChallenge);
        mGifArrow.setLayoutParams(layoutParams);

        rlContent.addView(mGifArrow);
        mGifArrow.invalidate();
    }

    private boolean allAdaptersExist() {
        return challengeRequestQueryAdapter != null &&
                yourTurnChallengeQueryAdapter != null &&
                theirTurnChallengeQueryAdapter != null &&
                pastChallengeQueryAdapter != null;
    }

    private boolean allAdaptersAreEmpty() {
        return challengeRequestQueryAdapter.getItemCount() == 0 &&
                yourTurnChallengeQueryAdapter.getItemCount() == 0 &&
                theirTurnChallengeQueryAdapter.getItemCount() == 0 &&
                pastChallengeQueryAdapter.getItemCount() == 0;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnStartChallenge:
                ParseObjectUtils.logPinnedObjects(false);
                navigateToNewChallengeActivity();
                break;
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
            conditions.put(Constants.NotificationData.FRAGMENT, Constants.NotificationData.Fragment.MAIN_FRAGMENT);
        } catch (JSONException e) { e.printStackTrace(); }
        return conditions;
    }

    @Override
    public void onReceiveHandler() {
        loadChallengesAndTutorRequests();
    }

    // Refreshes challenge list when other activity finishes
    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                if (intent.getExtras().containsKey(Constants.IntentExtra.REFRESH_CHALLENGE_LIST)) {
                    for(ChallengeQueryAdapter adapter : adapterList) {
                        adapter.loadObjects(); //TODO: Does this work?
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Constants.IntentExtra.REFRESH_CHALLENGE_LIST);
        getActivity().registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public void onPause() {
        if (mGifArrow != null) {
            RelativeLayout rlContent = (RelativeLayout) getActivity().findViewById(R.id.rlContentMain);
            rlContent.removeView(mGifArrow);
        }

        super.onPause();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}