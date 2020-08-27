package com.pattern;

import javax.swing.JTextField;

public class PatternChecker
{
    public static String getInputValue(JTextField text) {
        int size = text.getText().length();
        return size > 0 ? text.getText().trim() : "";
    }
}