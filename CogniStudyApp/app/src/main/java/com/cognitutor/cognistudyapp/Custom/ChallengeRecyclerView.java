package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.cognitutor.cognistudyapp.Adapters.CogniRecyclerAdapter;

/**
 * Created by Kevin on 4/8/2016.
 */
public class ChallengeRecyclerView extends CogniRecyclerView{

    private CardView mParentCardView;

    public void setParentCardView(CardView cardView) {
        mParentCardView = cardView;
    }

    public CardView getParentCardView() {
        return mParentCardView;
    }

    public ChallengeRecyclerView(Context context) {
        super(context);
        init();
    }

    public ChallengeRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChallengeRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        ChallengeLayoutManager layoutManager = new ChallengeLayoutManager(getContext());
        setLayoutManager(layoutManager);
    }

    public class ChallengeLayoutManager extends LinearLayoutManager {
        public ChallengeLayoutManager(Context context) {
            super(context, LinearLayoutManager.VERTICAL, false);
        }

        public ChallengeLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public ChallengeLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public boolean canScrollVertically() {
            return false;
        }

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}

