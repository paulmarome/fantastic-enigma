package com.window.dialog;

import java.awt.Component;
import java.util.ResourceBundle;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import static javax.swing.JOptionPane.*;

public class PopupPane
{
    private PopupPane() {}
    
    public static void errDialog(Component comp, String msg, String title){
        showMessageDialog(comp, msg, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void emptyDialog(Component comp, ResourceBundle bundle)
    {
        showMessageDialog(comp, bundle.getString("emptyfields"), bundle
                .getString("headermsg"), JOptionPane.ERROR_MESSAGE); 
    }
    
    public static void infoDialog(Component comp, String msg, String title) {
        showMessageDialog(comp, msg, title,JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static int confirmDialog(Component comp, String msg, String title) {
        return showConfirmDialog(comp, msg, title, YES_NO_CANCEL_OPTION);
    }
    
    public static String promptDialog(Component comp)
    {
        Object[] timeSlots = new Object[]{
            "Morning", "Early afternoon", "Late afternoon", 
            "Early evening", "Late evening"
        };

        ImageIcon icon = new ImageIcon("/stemsentertainmentcompany/images/info.png");
        
        return (String)JOptionPane.showInputDialog(comp, "Choose a viewing "
                + "time slot:", "Bookings", JOptionPane.PLAIN_MESSAGE, icon,
                    timeSlots, "Morning");
    }
}