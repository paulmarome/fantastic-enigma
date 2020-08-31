package files;

import com.ui.LoginFrame;
import com.ui.MenuFrame;
import com.ui.SignupFrame;
import com.ui.WelcomeFrame;
import com.window.location.FrameEvents;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Popup;
import javax.swing.PopupFactory;

public final class LanguageResources
{
    private static final String LANGUAGE = "language";
    private static final FileProcessor FILE_PROCESSOR = FileProcessor.createFile("preference");;

    private static final String ENGLISH  = "files/Language_en_US";
    private static final String SESOTHO  = "files/Language_nso_ZA";
    private static final String XHOSA    = "files/Language_xh_ZA";
    private static final String ZULU     = "files/Language_zu_ZA";

    private LanguageResources() {}

    public static String getENGLISH() {
        return ENGLISH;
    }

    public static String getSESOTHO() {
        return SESOTHO;
    }

    public static String getXHOSA() {
        return XHOSA;
    }

    public static String getZULU() {
        return ZULU;
    }

    private static void updateFrame(Object type, String file, String language)
    {
        if (type instanceof LoginFrame) {
            LoginFrame login = (LoginFrame) type;
            login.updateLanguagePreference(file, language);
        }
        else if (type instanceof SignupFrame) {
            SignupFrame signup = (SignupFrame) type;
            signup.updateLanguagePreference(file, language);
        }
        else {
            MenuFrame menu = (MenuFrame) type;
            menu.updateLanguagePreference(file, language);
        }
    }

    public static void setLanguageResourceFile(Object type, JComboBox choice)
    {
        FILE_PROCESSOR.loadFile();
        boolean langKey = FILE_PROCESSOR.getProperties().containsKey(LANGUAGE);
        String language = "";

        if (langKey) {
            String path = FILE_PROCESSOR.getProperties().get("file").toString();
            language = FILE_PROCESSOR.getProperties().get("language").toString();
            updateFrame(type, path, language);
        }
        else {
            WelcomeFrame.setDefaultResourceBundle(FILE_PROCESSOR);
            setLanguageResourceFile(type, choice);
        }
        choice.setSelectedItem(language);
    }

    public static void selectLanguage(JComboBox choiceList, int count, Object type, JPanel container, JPanel outer, JFrame frame)
    {
        int x = 578;
        int y = 345;

        PopupFactory factory = new PopupFactory();
        Popup popup = factory.getPopup(container, outer, x, y);

        String selectedLang = choiceList.getSelectedItem().toString();

        if (count > 2)
        {
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    String bundleName = locale(selectedLang);

                    updateFrame(type, bundleName, selectedLang);
                    outer.setVisible(false);
                    frame.setEnabled(true);
                    popup.hide();
                }
            }, 3000);

            FrameEvents.centerFrame(frame);
            outer.setVisible(true);
            frame.setEnabled(false);
            popup.show();
        }
    }

    public static void selectLanguage(JComboBox choiceList, int count, Object type, JProgressBar bar, JFrame frame)
    {
        String selectedLang = choiceList.getSelectedItem().toString();

        if (count > 2)
        {
            new Timer().schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    String bundleName = locale(selectedLang);
                    updateFrame(type, bundleName, selectedLang);
                    bar.setVisible(false);
                    frame.setEnabled(true);
                }
            }, 3000);

            bar.setVisible(true);
            frame.setEnabled(false);
        }
    }

    private static String locale(String selectedLang)
    {
        String bundle = "";

        switch (selectedLang)
        {
            case "English (default)": bundle = ENGLISH;
            break;

            case "Sesotho": bundle = SESOTHO;
            break;

            case "Xhosa": bundle = XHOSA;
            break;

            case "Zulu": bundle = ZULU;
            break;
        }

        /* Store the user's preferences to the properties file */
        FILE_PROCESSOR.getProperties().setProperty(LANGUAGE, selectedLang);
        FILE_PROCESSOR.getProperties().setProperty("file", bundle);
        FILE_PROCESSOR.saveFile();

        return bundle;
    }

    /**
     * @deprecated This method creates unnecessary {@code String} objects.
     *             Use the <code>convertCase</code> method for better performance
     *
     * @param username The name to capitalize
     * @return The converted username that meets the proper noun rules
     *         for capitalizing names
     *
     * <br>The expected output based on the input string is as follows:
     * <blockquote>
     * <pre>
     *     String firstCase = Marome.substring(0,1).toUppercase();
     *     String lastCase  = Marome.substring(1).toLowercase();
     *     firstCase contains <b>K</b>
     *     lastCase contains <b>gaugelo</b>
     * </pre>
     * </blockquote>
     *
     * @see #capitalizeString(java.lang.String)
     */
    @Deprecated
    public static String capitalize(String username)
    {
        /*
        String firstCase = username.substring(0, 1).toUpperCase();
        String lastCase = username.substring(1, username.length()).toLowerCase();
        return firstCase.concat(lastCase);
        */
        return username.substring(0, 1).toUpperCase()
                .concat(username.substring(1).toLowerCase());
    }

    public static String capitalizeString(String username){
        return new StringBuilder().append(username.substring(0, 1)
                .toUpperCase()).append(username.substring(1).toLowerCase()).toString();
    }
}