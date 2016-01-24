package com.cognitutor.cognistudyapp.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.cognitutor.cognistudyapp.Custom.AnswerItem;
import com.cognitutor.cognistudyapp.Custom.Constants;
import com.cognitutor.cognistudyapp.R;

import java.util.ArrayList;
import java.util.List;

import io.github.kexanie.library.MathView;

/**
 * Created by Kevin on 1/21/2016.
 */
public class AnswerAdapter extends BaseAdapter {

    private final List<String> answers;
    private Activity context;
    private static String answerLabelType;
    private static List<RadioButton> radioButtonList;
    private static int selectedIdx;

    public AnswerAdapter(Activity context, List<String> answers, String labelType) {
        this.context = context;
        this.answers = answers;
        answerLabelType = labelType;
        radioButtonList = new ArrayList<RadioButton>();
    }

    public static String getAnswerLabelType() { return answerLabelType; }
    public static void selectAnswer(int position) {
        for(RadioButton button : radioButtonList)
            button.setChecked(button.getId() == position);
        selectedIdx = position;
    }
    public static void addRadioButton(RadioButton rb) {
        radioButtonList.add(rb);
    }
    public static int getSelectedAnswer() {
        return selectedIdx;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        AnswerViewHolder holder;
        String answer = getItem(position);
        LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            AnswerItem answerItem = new AnswerItem(context, answer, position);
            convertView = answerItem.getView();
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAnswer(position);
            }
        });
/*            convertView = vi.inflate(R.layout.list_item_answer, null);
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
        }*/

        return convertView;
    }

    private AnswerViewHolder createAnswerViewHolder(View v) {
        AnswerViewHolder holder = new AnswerViewHolder();
        holder.mvAnswer = (MathView) v.findViewById(R.id.mvAnswer);
        return holder;
    }

    private static class AnswerViewHolder {
        public TextView txtChoice;
        public MathView mvAnswer;
    }

}

