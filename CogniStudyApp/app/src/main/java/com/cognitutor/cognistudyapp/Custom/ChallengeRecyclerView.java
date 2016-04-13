package com.cognitutor.cognistudyapp.Custom;

import android.app.Activity;
import android.content.Context;
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
//        ChallengeLayoutManager layoutManager = new ChallengeLayoutManager(getContext());
//        setLayoutManager(layoutManager);
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
//    public class ChallengeLayoutManager extends RecyclerView.LayoutManager {

        @Override
        public RecyclerView.LayoutParams generateDefaultLayoutParams() {
            return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        @Override
        public void onMeasure(Recycler recycler, State state, int widthSpec, int heightSpec) {
            super.onMeasure(recycler, state, widthSpec, heightSpec);
        }

        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            super.onLayoutChildren(recycler, state);

            RecyclerView.Adapter listAdapter = getAdapter();
            if (listAdapter == null) {
                // pre-condition
                return;
            }

            View parentCardView = (View) getParent().getParent();
            if(listAdapter.getItemCount() == 0) {
                parentCardView.setVisibility(View.GONE);
            } else {
                parentCardView.setVisibility(View.VISIBLE);
            }

            int totalHeight = 0;
            for(int i=0; i<getChildCount(); i++) {
                final View child = getChildAt(i);
                child.measure(0, 0);
                totalHeight += child.getMeasuredHeight();
            }

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = totalHeight;
            setLayoutParams(params);
        }
    }
}

