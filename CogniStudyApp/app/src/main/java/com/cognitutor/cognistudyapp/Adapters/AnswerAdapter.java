package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;

import java.util.List;

import io.github.kexanie.library.MathView;

/**
 * Created by Kevin on 1/21/2016.
 */
public class AnswerAdapter extends BaseAdapter {

    private final List<String> answers;
    private Activity context;
    private String answerLabelType;

    private AnswerAdapter(Activity context, List<String> answers, String answerLabelType) {
        this.context = context;
        this.answers = answers;
        this.answerLabelType = answerLabelType;
    }

    @Override
    public int getCount() {
        if(answers != null)
            return answers.size();
        else return 0;
    }

    @Override
    public String getItem(int position) {
        if(answers != null)
            return answers.get(position);
        else return null;
    }

    @Override
    public long getItemId(int position) { return position; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AnswerViewHolder holder;
        String answer = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.list_item_answer, null);
            holder = createAnswerViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AnswerViewHolder) convertView.getTag();
        }

        holder.mvAnswer.setText(answer);
        if(answerLabelType.equals(Constants.AnswerLabelType.LETTER)) {
            holder.txtChoice.setText(Letters.values()[position].toString());
        }
        else {
            holder.txtChoice.setText(RomanNumerals.values()[position].toString());
        }

        return convertView;
    }

    private AnswerViewHolder createAnswerViewHolder(View v) {
        AnswerViewHolder holder = new AnswerViewHolder();
        holder.txtChoice = (TextView) context.findViewById(R.id.txtChoice);
        holder.mvAnswer = (MathView) context.findViewById(R.id.mathView);
        return holder;
    }

    private static class AnswerViewHolder {
        public TextView txtChoice;
        public MathView mvAnswer;
    }

    private static enum Letters {
        A, B, C, D, E
    }

    private static enum RomanNumerals {
        I, II, III, IV, V
    }
}

