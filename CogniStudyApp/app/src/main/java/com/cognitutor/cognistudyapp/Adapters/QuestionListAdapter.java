package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Question;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.QuestionMetaObject;
import com.cognitutor.cognistudyapp.ParseObjectSubclasses.Response;
import com.cognitutor.cognistudyapp.R;
import com.parse.ParseQuery;
import com.parse.ParseQueryAdapter;

/**
 * Created by Kevin on 3/18/2016.
 */
public class QuestionListAdapter extends CogniRecyclerAdapter<QuestionMetaObject, QuestionListAdapter.ViewHolder> {

    public QuestionListAdapter(Activity activity, final ParseQuery query) {
        super(activity, new ParseQueryAdapter.QueryFactory<QuestionMetaObject>() {
            public ParseQuery<QuestionMetaObject> create() {
                return query;
            }
        }, true); //TODO: Try true for hasStableIds
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView txtSubject;
        public TextView txtCategory;
        public TextView txtDate;
        public TextView txtResponseStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            txtSubject = (TextView) itemView.findViewById(R.id.txtSubject);
            txtCategory = (TextView) itemView.findViewById(R.id.txtCategory);
            txtDate = (TextView) itemView.findViewById(R.id.txtDate);
            txtResponseStatus = (TextView) itemView.findViewById(R.id.txtResponseStatus);
        }

        public void setOnClickListener(final String questionId, final String responseId) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    mOnClickHandler.onListItemClick(question);
                }
            });
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
        holder.txtSubject.setText(meta.getSubject());
        holder.txtCategory.setText(meta.getCategory());
        holder.txtDate.setText(meta.getDate());
        holder.txtResponseStatus.setText(meta.getResponseStatus());

        holder.setOnClickListener(meta.getQuestionId(), meta.getResponseId());
    }
}
