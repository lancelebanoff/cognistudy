package com.cognitutor.cognistudyapp.Custom;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cognitutor.cognistudyapp.Adapters.AnswerAdapter;
import com.cognitutor.cognistudyapp.R;

import io.github.kexanie.library.MathView;

/**
 * Created by Kevin on 1/24/2016.
 */
public class AnswerItem {

    private View view;
    private CogniMathView mvAnswer;
    private RadioButton rb;

    public AnswerItem(Context context, AnswerAdapter parent, String answer, int position) {

        view = View.inflate(context, R.layout.list_item_answer, null);

        mvAnswer = (CogniMathView) view.findViewById(R.id.mvAnswer);
        mvAnswer.setText(answer);

        rb = (RadioButton) view.findViewById(R.id.radioButton);
        rb.setId(position);
        parent.addRadioButton(rb);

        if(parent.getAnswerLabelType().equals(Constants.AnswerLabelType.LETTER)) {
            rb.setText(Letters.values()[position].toString());
        }
        else {
            rb.setText(RomanNumerals.values()[position].toString());
        }

        rb.setOnCheckedChangeListener(null);
    }

    public View getView() {
        return view;
    }

    private static enum Letters {
        A, B, C, D, E
    }

    private static enum RomanNumerals {
        I, II, III, IV, V
    }
}
