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

import com.cognitutor.cognistudyapp.Activities.QuestionActivity;
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
    private QuestionActivity quesActivity;
    private String answerLabelType;
    private List<RadioButton> radioButtonList;
    private int selectedIdx;

    public AnswerAdapter(Activity context, List<String> answers, String labelType) {
        this.quesActivity = (QuestionActivity) context;
        this.answers = answers;
        answerLabelType = labelType;
        radioButtonList = new ArrayList<RadioButton>();
    }

    public String getAnswerLabelType() { return answerLabelType; }
    public void selectAnswer(int position) {

        quesActivity.setBtnSubmitEnabled(true);
        for(RadioButton button : radioButtonList)
            button.setChecked(button.getId() == position);
        selectedIdx = position;
    }
    public void addRadioButton(RadioButton rb) {
        radioButtonList.add(rb);
    }
    public int getSelectedAnswer() {
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
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        String answer = getItem(position);

        AnswerItem answerItem;
        if (view == null) {
            answerItem = new AnswerItem(quesActivity, this, answer, position);
            view = answerItem.getView();
            view.setTag(answerItem);
        }
        else {
            answerItem = (AnswerItem) view.getTag();
        }

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectAnswer(position);
            }
        });
        return view;
    }
}

