package com.lookandfeel;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class LookAndFeel
{
  /**
     * Set the Nimbus look and feel once the thread
     * completes execution successfully. 
     * @param interfaceType
     */
    public static void setLookAndFeel(String interfaceType)
    {
        try
        {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (interfaceType.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } 
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException 
                | UnsupportedLookAndFeelException ex) {
            System.out.format("%s%n%n", ex);
        }
    } 
}