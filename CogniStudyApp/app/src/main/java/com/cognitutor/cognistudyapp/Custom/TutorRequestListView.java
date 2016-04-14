package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Kevin on 4/14/2016.
 */
public class TutorRequestListView extends ListView {

    private CardView mParentCardView;

    public TutorRequestListView(Context context) {
        super(context);
    }

    public TutorRequestListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TutorRequestListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setParentCardView(CardView cardView) {
        mParentCardView = cardView;
    }

    public CardView getParentCardView() {
        return mParentCardView;
    }
}
