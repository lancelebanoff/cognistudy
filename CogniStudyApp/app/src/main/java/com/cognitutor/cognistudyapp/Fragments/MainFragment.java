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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.cognitutor.cognistudyapp.Activities.MainActivity;
import com.cognitutor.cognistudyapp.Activities.NewChallengeActivity;
import com.cognitutor.cognistudyapp.Adapters.ChallengeQueryAdapter;
import com.cognitutor.cognistudyapp.Adapters.TutorRequestAdapter;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Challenge;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PrivateStudentData;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.PublicUserData;
import com.cognitutor.cognistudyapp.R;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import bolts.Continuation;
import bolts.Task;
import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Lance on 12/27/2015.
 */

public class MainFragment extends CogniPushListenerFragment implements View.OnClickListener {

    private TutorRequestAdapter tutorRequestAdapter;
    private ChallengeQueryAdapter challengeRequestQueryAdapter;
    private ChallengeQueryAdapter yourTurnChallengeQueryAdapter;
    private ChallengeQueryAdapter theirTurnChallengeQueryAdapter;
    private ChallengeQueryAdapter pastChallengeQueryAdapter;
    private ListView tutorRequestListView;
    private ListView challengeRequestListView;
    private ListView yourTurnListView;
    private ListView theirTurnListView;
    private ListView pastChallengeListView;

    public static ArrayAdapter<ParseObject> answeredQuestionIdAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BroadcastReceiver mBroadcastReceiver;
    private GifImageView mGifArrow;

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

        Button b = (Button) rootView.findViewById(R.id.btnStartChallenge);
        b.setOnClickListener(this);

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

        createAllListViews(getView());
        showOrHideArrow();
        setSwipeRefreshLayout(getView());
        initializeBroadcastReceiver();
    }

    private void createAllListViews(final View rootView) {
        PublicUserData.getPublicUserDataInBackground().continueWith(new Continuation<PublicUserData, Void>() {
            @Override
            public Void then(Task<PublicUserData> task) throws Exception {
                PublicUserData publicUserData = task.getResult();
                endChallengesThatRanOutOfTime(publicUserData);
                createTutorRequestListView(rootView);
                createChallengeRequestListView(rootView, publicUserData);
                createYourTurnListView(rootView, publicUserData);
                createTheirTurnListView(rootView, publicUserData);
                createPastChallengeListView(rootView, publicUserData);
                ((MainActivity) getActivity()).challengesFinishedLoading = true;
                return null;
            }
        });
    }

    private void endChallengesThatRanOutOfTime(PublicUserData publicUserData) {
        List<ParseQuery<Challenge>> queries = new ArrayList<ParseQuery<Challenge>>();
        queries.add(Challenge.getQuery()
                .whereEqualTo(Challenge.Columns.curTurnUserId, publicUserData.getBaseUserId()));
        queries.add(Challenge.getQuery()
                .whereEqualTo(Challenge.Columns.otherTurnUserId, publicUserData.getBaseUserId()));
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
                            challenge.setEndDate(calendarEndDate.getTime());
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

        PrivateStudentData.getPrivateStudentDataInBackground().continueWith(new Continuation<PrivateStudentData, Void>() {
            @Override
            public Void then(Task<PrivateStudentData> task) throws Exception {
                task.getResult().fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        PrivateStudentData privateStudentData = (PrivateStudentData) object;
                        List<PublicUserData> tutorRequests = privateStudentData.getTutorRequests();
                        tutorRequestAdapter = new TutorRequestAdapter(activity, fragment, tutorRequests);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tutorRequestListView = (ListView) rootView.findViewById(R.id.listTutorRequests);
                                tutorRequestListView.setFocusable(false);
                                tutorRequestListView.setAdapter(tutorRequestAdapter);

                                View parentCardView = (View) tutorRequestListView.getParent().getParent();
                                if (tutorRequestAdapter.getCount() == 0) {
                                    parentCardView.setVisibility(View.GONE);
                                } else {
                                    parentCardView.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                });
                return null;
            }
        });
    }

    private void createChallengeRequestListView(final View rootView, PublicUserData publicUserData) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.accepted, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.curTurnUserId, publicUserData.getBaseUserId()));
        challengeRequestQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairs);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void createYourTurnListView(final View rootView, PublicUserData publicUserData) {
        final List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.accepted, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.curTurnUserId, publicUserData.getBaseUserId()));
        yourTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairs);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void createTheirTurnListView(final View rootView, PublicUserData publicUserData) {
        List<Pair> keyValuePairs = new ArrayList<>();
        keyValuePairs.add(new Pair<>(Challenge.Columns.activated, true));
        keyValuePairs.add(new Pair<>(Challenge.Columns.hasEnded, false));
        keyValuePairs.add(new Pair<>(Challenge.Columns.otherTurnUserId, publicUserData.getBaseUserId()));
        theirTurnChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairs);


        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    private void createPastChallengeListView(final View rootView, PublicUserData publicUserData) {
        List<Pair> keyValuePairs1 = new ArrayList<>();
        keyValuePairs1.add(new Pair<>(Challenge.Columns.hasEnded, true));
        keyValuePairs1.add(new Pair<>(Challenge.Columns.curTurnUserId, publicUserData.getBaseUserId()));

        List<Pair> keyValuePairs2 = new ArrayList<>();
        keyValuePairs2.add(new Pair<>(Challenge.Columns.hasEnded, true));
        keyValuePairs2.add(new Pair<>(Challenge.Columns.otherTurnUserId, publicUserData.getBaseUserId()));

        List<List<Pair>> keyValuePairsList = new ArrayList<>();
        keyValuePairsList.add(keyValuePairs1);
        keyValuePairsList.add(keyValuePairs2);
        pastChallengeQueryAdapter = new ChallengeQueryAdapter(getActivity(), this, keyValuePairsList, true);

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
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
        });
    }

    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        if (listAdapter.getCount() > 0) {
            if (mGifArrow != null) {
                RelativeLayout rlContent = (RelativeLayout) getActivity().findViewById(R.id.rlContentMain);
                rlContent.removeView(mGifArrow);
            }
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

    private void showOrHideArrow() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(Constants.Loading.CHALLENGE_ARROW_WAIT_TIME);
                while (!allAdaptersExist()) {

                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (allAdaptersAreEmpty()) {
                            showArrowGif();
                        }
                    }
                });
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
    }

    private boolean allAdaptersExist() {
        return challengeRequestQueryAdapter != null &&
                yourTurnChallengeQueryAdapter != null &&
                theirTurnChallengeQueryAdapter != null &&
                pastChallengeQueryAdapter != null;
    }

    private boolean allAdaptersAreEmpty() {
        return challengeRequestQueryAdapter.getCount() == 0 &&
                yourTurnChallengeQueryAdapter.getCount() == 0 &&
                theirTurnChallengeQueryAdapter.getCount() == 0 &&
                pastChallengeQueryAdapter.getCount() == 0;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.btnStartChallenge:
                navigateToNewChallengeActivity();
                break;
//            case R.id.btnViewLocalDatastore:
//                ParseObjectUtils.logPinnedObjects(false);
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
        refresh();
    }

    // Refreshes challenge list when other activity finishes
    private void initializeBroadcastReceiver() {
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context arg0, Intent intent) {
                if (intent.getExtras().containsKey(Constants.IntentExtra.REFRESH_CHALLENGE_LIST)) {
                    createAllListViews(getView());
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
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(mBroadcastReceiver);
        super.onDestroyView();
    }
}