package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Activities.ChallengeQuestionActivity;
import com.cognitutor.cognistudyapp.Activities.QuestionActivity;
import com.cognitutor.cognistudyapp.Activities.QuestionHistoryActivity;
import com.cognitutor.cognistudyapp.Activities.SuggestedQuestionsListActivity;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.Custom.DateUtils;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Kevin on 3/18/2016.
 */
public class QuestionListAdapter extends CogniRecyclerAdapter<QuestionMetaObject, QuestionListAdapter.ViewHolder> {

    private Class<? extends QuestionActivity> mTargetQuestionActivityClass;

    public QuestionListAdapter(Activity activity, Class<? extends QuestionActivity> targetQuestionActivityClass, final ParseQuery query) {
        super(activity, new ParseQueryAdapter.QueryFactory<QuestionMetaObject>() {
            public ParseQuery<QuestionMetaObject> create() {
                return query;
            }
        }, true); //TODO: Try true for hasStableIds
        mTargetQuestionActivityClass = targetQuestionActivityClass;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public ImageView ivSubject;
        public TextView txtCategory;
        public TextView txtDate;
        public ImageView ivResponseStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            ivSubject = (ImageView) itemView.findViewById(R.id.ivSubject);
            txtCategory = (TextView) itemView.findViewById(R.id.txtCategory);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            ivResponseStatus = (ImageView) itemView.findViewById(R.id.ivResponseStatus);
        }

        public void setOnClickListener(final String questionId, final String responseId) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToQuestionActivity(questionId, responseId);
                }
            });
        }

        private void setSubjectIcon(String subject) {
            int icon;
            switch (subject) {
                case Constants.Subject.ENGLISH:
                    icon = R.drawable.icon_english;
                    break;
                case Constants.Subject.MATH:
                    icon = R.drawable.icon_math;
                    break;
                case Constants.Subject.SCIENCE:
                    icon = R.drawable.icon_science;
                    break;
                default:
                    icon = R.drawable.icon_reading;
                    break;
            }
            ivSubject.setImageResource(icon);
        }

        private void setResponseStatusIcon(String responseStatus) {
            int icon;
            switch (responseStatus) {
                case Constants.ResponseStatusType.CORRECT:
                    icon = R.drawable.ic_icon_correct;
                    break;
                case Constants.ResponseStatusType.INCORRECT:
                    icon = R.drawable.ic_icon_incorrect;
                    break;
                default:
                    icon = R.drawable.ic_icon_unanswered;
                    break;
            }
            ivResponseStatus.setImageResource(icon);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_question, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final QuestionMetaObject meta = mItems.get(position);
        holder.setSubjectIcon(meta.getSubject());
        holder.txtCategory.setText(meta.getCategory());
        holder.txtCategory.setTypeface(null, Typeface.BOLD);
        holder.txtDate.setText(getDateToDisplay(meta));
        holder.setResponseStatusIcon(meta.getResponseStatus());

        holder.setOnClickListener(meta.getQuestionId(), meta.getResponseId());
    }

    private String getDateToDisplay(QuestionMetaObject meta) {
        Date questionDate = meta.getDate();
        Date currentDate = new Date();
        String timeBetween = DateUtils.getTimeBetween(questionDate, currentDate);
        if(timeBetween.contains("day")) {
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM d", Locale.US);
            return formatter.format(questionDate);
        }
        return timeBetween + " ago";
    }

    private void navigateToQuestionActivity(String questionId, String responseId) {
        Intent intent = new Intent(mActivity, mTargetQuestionActivityClass); //TODO: Change based on type of question
        intent.putExtra(Constants.IntentExtra.ParentActivity.PARENT_ACTIVITY, getParentActivityConstant());
        intent.putExtra(Constants.IntentExtra.QUESTION_ID, questionId);
        intent.putExtra(Constants.IntentExtra.RESPONSE_ID, responseId);
        mActivity.startActivity(intent);
    }

    private String getParentActivityConstant() {
        if(mActivity instanceof QuestionHistoryActivity)
            return Constants.IntentExtra.ParentActivity.QUESTION_HISTORY_ACTIVITY;
        else if(mActivity instanceof SuggestedQuestionsListActivity)
            return Constants.IntentExtra.ParentActivity.SUGGESTED_QUESTIONS_ACTIVITY;
        else {
            return Constants.IntentExtra.ParentActivity.BOOKMARKS_LIST_ACTIVITY;
        }
    }
}
