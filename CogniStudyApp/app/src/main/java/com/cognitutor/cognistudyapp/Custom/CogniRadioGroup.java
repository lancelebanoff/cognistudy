package com.cognitutor.cognistudyapp.Custom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lance on 3/13/2016.
 */
public class CogniRadioGroup {

    private List<CogniRadioButton> buttons;

    public CogniRadioGroup() {
        buttons = new ArrayList<>();
    }

    public void add(CogniRadioButton button) {
        buttons.add(button);
        button.setGroup(this);
    }

    public void setChosenButton(CogniRadioButton clickedButton) {
        for (CogniRadioButton button : buttons) {
            if (button.equals(clickedButton)) {
                button.setChecked(true);
            } else {
                button.setChecked(false);
            }
        }
    }

    public List<CogniRadioButton> getRadioButtons() {
        return buttons;
    }
}
