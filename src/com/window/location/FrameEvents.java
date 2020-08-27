package com.window.location;

import com.database.DatabaseConfiguration;
import com.window.dialog.PopupPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public final class FrameEvents 
{
    private static int x;
    private static int y;
   
    /**
     * Don't let anyone instantiate this class.
     */
    private FrameEvents() {}
   
    /**
     * The original author can be found in the description below:
     * @author Jan Bodnar 
     *  (Editor)
     * <a href="https://stackoverflow.com/questions/9543320/how-to-position-the-form-in-the-center-screen/9543339"></a>
     * 
     * @param root the JFrame component
     */
    public static void centerFrame(JFrame root)
    {
        Dimension windowSize = root.getSize();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int dx = centerPoint.x - windowSize.width / 2;
        int dy = centerPoint.y - windowSize.height / 2;
        root.setLocation(dx, dy);
    }
    
    /**
     * @param comp
     * @param bundle
     */
    public static void closeWindow(Component comp, ResourceBundle bundle)
    {
        String message = bundle.getString("exit2");
        String title = bundle.getString("select");      
        int choice =  PopupPane.confirmDialog(comp, message, title);

        if(choice == JOptionPane.YES_OPTION) {
            DatabaseConfiguration.closeConnection();
            System.exit(0); 
        }
    }
    
    public static void minimizeWindow(JFrame frame) {
        frame.setState(Frame.ICONIFIED);
    }
    
    public static void draggableWindow(JFrame frame, JPanel panel)
    {
        panel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent event){
                x = event.getX();
                y = event.getY();
            }
        });
       
        panel.addMouseMotionListener(new MouseMotionAdapter()
        {
           @Override
           public void mouseDragged(MouseEvent event)
           {
               int x_coord = event.getXOnScreen() - x;
               int y_coord = event.getYOnScreen() - y;
               frame.setLocation(x_coord, y_coord);
           }
        });
    }
}