package com.prototypes;

import static com.pattern.PatternChecker.getInputValue;
import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JTextField;

public interface Validator
{
    boolean isEmpty();
    
    default boolean validateUsername(JTextField usernameField, ResourceBundle resources, JLabel usernameLabel)
    {
        String name = getInputValue(usernameField);

        if (!name.toLowerCase().matches("[A-Za-z][a-z]*")) {
            String text = resources.getString("validusername");
            usernameLabel.setForeground(Color.red);
            usernameLabel.setText(text);
            return false;
        }
        usernameLabel.setText("");
        return true;
    }
    
    default boolean validatePhoneNumber(JTextField contactField, ResourceBundle resources, JLabel contactLabel)
    {
        String phoneNumber = getInputValue(contactField);
        String msg = resources.getString("validnumber");

        if (!phoneNumber.matches("\\d{10}")) {
            contactLabel.setForeground(Color.red);
            contactLabel.setText(msg);
            return false;
        }
        contactLabel.setText("");
        return true;
    }
}