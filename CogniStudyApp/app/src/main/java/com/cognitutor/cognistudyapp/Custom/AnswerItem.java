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
    private MathView mvAnswer;
    private RadioButton rb;
    private static int id = 100;

    public AnswerItem(Context context, String answer, int position) {

        view = View.inflate(context, R.layout.list_item_answer, null);

        mvAnswer = (MathView) view.findViewById(R.id.mvAnswer);
        mvAnswer.setText(answer);
        mvAnswer.setClickable(false);
        mvAnswer.setFocusable(false);
        mvAnswer.setFocusableInTouchMode(false);

        view.setId(id);

//        rb = (RadioButton) view.findViewById(R.id.radioButton);
//        rb.setId(position);
//        rb.setClickable(false);
//        rb.setFocusable(false);
//        rb.setFocusableInTouchMode(false);
//        AnswerAdapter.addRadioButton(rb);
//
//        if(AnswerAdapter.getAnswerLabelType().equals(Constants.AnswerLabelType.LETTER)) {
//            rb.setText(Letters.values()[position].toString());
//        }
//        else {
//            rb.setText(RomanNumerals.values()[position].toString());
//        }

//        rb.setOnCheckedChangeListener(null);
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
