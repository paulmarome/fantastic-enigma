    package com.ui;

import com.classes.*;
import com.database.DatabaseConfiguration;
import com.database.Scripts;
import com.prototypes.Translator;
import com.prototypes.Validator;
import com.window.dialog.PopupPane;
import com.window.location.FrameEvents;
import files.FileProcessor;
import files.LanguageResources;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.*;
import java.util.Timer;
import java.util.Map.Entry;

import static com.lookandfeel.LookAndFeel.setLookAndFeel;
import static files.FileProcessor.getResourceBundle;
import static files.LanguageResources.capitalize;
import java.time.format.DateTimeFormatter;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class MenuFrame extends JFrame implements Translator, Validator
{
    private static String response;
    private static int clickCount = 0;
    private static int nextEntry = 0;
    private static int movieId = 0;
    private static int movieCount = 0;
    private static int ticketCount = 0;

    private static boolean updateName = false;
    private static boolean updateNumber = false;

    private final Map<String, Integer> POPULAR_MOVIES = new HashMap<>();
    private final Map<String, Integer> POPULAR_SESSION;
    private final FileProcessor FILES;

    private static final Scripts SCRIPT = new Scripts();
    private static final Set<String> UNIQUE_MOVIES = new HashSet<>();;
    private static final Set<String> PRICES = new HashSet<>();

    private DefaultTableModel model;
    private ResourceBundle resources;

    /**
     * Creates new form MenuFrame
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public MenuFrame()
    {
        this.POPULAR_SESSION = new HashMap<>();

        FILES = FileProcessor.createFile("preference");
        FILES.loadFile();
        resources = getResourceBundle(FILES);

        initComponents();
        FrameEvents.draggableWindow(this, (JPanel) this.getContentPane());
        FrameEvents.centerFrame(this);
        LanguageResources.setLanguageResourceFile(this, langComboBox);

        listFrequency();
        initializeDataFields();
        setTableData();
        bookingButtonActionPerformed();
        tabbedPaneListener();

        textFieldListener();
        textFieldListener(usernameField, phoneNoField);
        textFieldListener(movieField2);
        textFieldListener(hourField2);
        textFieldListener(minField2);
        textFieldListener(priceField2);
        textFieldListener(coverField2);

        menuComboListener(langCombo2);
        menuComboListener(ageCombo2);
        menuComboListener(genreCombo2);

        addMovieList();
        addAmountList();

        comboBoxListener();
        updateContentLayout(innerMainPanel);
        setUsername();
        prepareMovieTable();
    }

    private void exitLabelMouseClicked(MouseEvent evt) {
        getResourceBundle(FILES);
    }

    private void updateContentLayout(JPanel colorPanel, JPanel replacePanel)
    {
        JPanel[] jPanelArr = new JPanel[] {
            panelAdd, panelPurchase, panelMovies, panelBooking, panelPopular, panelSettings, 
            innerMainPanel, displayMoviesPanel, bookingsPanel, popularPanel, settingsPanel, 
            moviePanel, purchasePanel };   
        
        int mainPanelCount = 6;
        
        for (int index = 0, size = jPanelArr.length; index < size; index++) {
            if (index < mainPanelCount) resetColor(colorPanel, jPanelArr, index);
            else replaceComponent(replacePanel, jPanelArr, index);  
        }
    }
  
    private void updateContentLayout(JPanel defaultPanel) 
    {
        JPanel[] jPanelArr = new JPanel[] {
            innerMainPanel, displayMoviesPanel, bookingsPanel, popularPanel, settingsPanel,
            moviePanel, purchasePanel };
        
        for (int index = 1, size = jPanelArr.length; index < size; index++) {
            replaceComponent(defaultPanel, jPanelArr, index);
        }
    }
    
    private void resetColor(JPanel jPanel, JPanel[] jPanelComp, int index) {
        jPanelComp[index].setBackground(new Color(135, 112, 225));
        if (!jPanelComp[index].equals(jPanel)) {
            jPanelComp[index].setBackground(new Color(36, 91, 99));
        }
    }
    
    private void replaceComponent(JPanel jPanel, JPanel[] jPanelComp, int index) {
        /*
        if(!(jPanelComp[index]).equals(jPanel))jPanelComp[index].setVisible(false);
        else jPanelComp[index].setVisible(true);
         */     
        jPanelComp[index].setVisible(jPanelComp[index].equals(jPanel));
    }
    
    private void panelAddMousePressed(MouseEvent evt) {
        updateContentLayout(panelAdd, moviePanel);
    }
    private void panelPurchaseMousePressed(MouseEvent evt) {
        updateContentLayout(panelPurchase, purchasePanel);
    }

    private void panelBookingsMousePressed(MouseEvent evt) {
        updateContentLayout(panelBooking, bookingsPanel);
    }

    private void panelPopularMousePressed(MouseEvent evt) {
        updateContentLayout(panelPopular, popularPanel);
    }

    private void panelSettingsMousePressed(MouseEvent evt) {
        updateContentLayout(panelSettings, settingsPanel);
    }

    private void panelMoviesMousePressed(MouseEvent evt) {
        updateContentLayout(panelMovies, displayMoviesPanel);
    }

    
    private void submitBtnActionPerformed(ActionEvent evt) 
    {    
        if (isEmpty()){
            System.out.format("%s%n", "Please fill out all the fields!!");
        }
        else
        {
            String movieTitle = movieField2.getText();
            String lang = langCombo2.getSelectedItem().toString();
            String type = genreCombo2.getSelectedItem().toString();
            String age   = ageCombo2.getSelectedItem().toString();
            String hour = String.format("%s%s", hourField2.getText(), "h");
            String minute = String.format("%s%s", minField2.getText(), "m");

            if (checkRule())
            {
                if (!exists(movieTitle))
                {
                    double price = Double.parseDouble(priceField2.getText().trim());
                    LocalDate date = releaseDate2.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    InputStream image = null;

                    try {
                        image = new FileInputStream(coverField2.getText());
                    }
                    catch (FileNotFoundException ex) {
                        System.out.format("%s", ex);
                    }

                    String descr = getDescription();

                    if (DatabaseConfiguration.isConnected())
                    {
                        String password = LoginFrame.getPassword();
                        String hrMin = hour + minute;

                        int rowCount = SCRIPT.addMovie(movieTitle, lang, type, date, image, hrMin, age, price, descr, password);

                        if ((rowCount > 0)) {
                            PopupPane.infoDialog(this,resources.getString("movieinsert"),resources.getString("moviehead"));
                            displayButtonActionPerformed();
                        }
                        else {
                            String msg = resources.getString("movieerr");
                            PopupPane.infoDialog(this, msg, resources.getString("moviehead2"));
                        }
                    }

                    removeItem(comboMovie);
                    removeItem(comboAmount);
                }
                else {
                    PopupPane.infoDialog(this, resources.getString("moviedup"), resources.getString("duphead"));
                }
            }
        }
    }

    private void uploadBtnActionPerformed(ActionEvent evt) {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter imageFilter = new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes());
        fileChooser.setFileFilter(imageFilter);

        int var = fileChooser.showOpenDialog(MenuFrame.this);

        if (var == JFileChooser.APPROVE_OPTION)
        {
            File file = fileChooser.getSelectedFile();

            if (imageFilter.accept(file)) {
                coverField2.setText(file.getPath());
                coverField2.setBackground(Color.WHITE);
                coverField2.setForeground(Color.BLACK);
            }
        }
        else {
            PopupPane.infoDialog(this, resources.getString("cancelled"), resources.getString("cancelmsg"));
        }
    }

    private void continueButtonActionPerformed(ActionEvent evt) {
        if (custField.getText().isEmpty()) {
            PopupPane.emptyDialog(this, resources);
        }
        else
        {
            if (isVerified())
            {
                if (DatabaseConfiguration.isConnected())
                {
                    String name = LanguageResources.capitalize(custField.getText());
                    String contact = contactField.getText();
                    String movieTitle = comboMovie.getSelectedItem().toString();

                    double amount = Double.parseDouble(comboAmount.getSelectedItem().toString().replaceAll("R", ""));
                    bookingButtonActionPerformed();

                    response = PopupPane.promptDialog(this);
                    int count = initializeTicket(name, contact, amount, movieTitle);
                    dialogMessage(count);

                    listFrequency();
                    bookingButtonActionPerformed();
                }
                else {
                    PopupPane.errDialog(this, resources.getString("dbservererr"), resources.getString("dbservermsg"));
                }
            }
        }
    }

    private void exitLabelMouseMoved(MouseEvent evt) {
        exitLabel.setIcon(new ImageIcon(getClass().getResource("/com/images/close2.png")));
    }

    private void exitLabelMousePressed(MouseEvent evt) {
        FrameEvents.closeWindow(this, getResourceBundle(FILES));
    }

    private void exitLabelMouseExited(MouseEvent evt) {
        exitLabel.setIcon(new ImageIcon(getClass().getResource("/com/images/close24.png")));
    }

    private void bookingButtonActionPerformed() {
        resetBookingsTableStructure();
        prepareTicketTable();
    }

    private void saveButtonActionPerformed(ActionEvent evt) {
        LanguageDelayFrame delay = new LanguageDelayFrame();
        ResourceBundle bundle = getResourceBundle(FILES);

        String username = usernameField.getText().toLowerCase();
        String phoneNumber = phoneNoField.getText();
        String password = passwordField.getText();

        if (!(username.isEmpty()) && !(phoneNumber.isEmpty()))
        {
            if (updateName || updateNumber)
            {
                int result = SCRIPT.updateUserAccount(username, phoneNumber, password);

                saveButton.setEnabled(true);
                jPanel7.setEnabled(false);

                delay.setLocationRelativeTo(settingsPanel);
                delay.setVisible(true);
                usernameLabel.setText(LanguageResources.capitalizeString(username));

                new Timer().schedule(new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        saveButton.setEnabled(false);
                        delay.setVisible(false);
                        jPanel7.setEnabled(true);

                        if (result > 0) {
                            PopupPane.infoDialog(welcomePanel, bundle.getString("updateMsg"), 
                                    bundle.getString("updateTitle"));
                        }
                        else {
                            PopupPane.errDialog(welcomePanel, bundle.getString("updateErr"), 
                                    bundle.getString("updateTitle"));
                        }
                    }
                }, 850);
            }
        }
        else {
            LoginFrame.renderEmptyFields(usernameField, phoneNoField);
        }
    }

    private void exitLabel1MouseClicked(MouseEvent evt) {
        FrameEvents.minimizeWindow(this);
    }

    private void displayButtonActionPerformed() {
        resetMoviesTable();
        prepareMovieTable();
    }

    private void previousButtonActionPerformed(ActionEvent evt) {
        movieCount--;
        sortMovies();

        if (movieCount == 0) {
            previousButton.setEnabled(false);
        }
        else {
            previousButton.setEnabled(true);
        }
    }

    private void nextButtonActionPerformed(ActionEvent evt) {
        movieCount++;
        sortMovies();

        if (movieCount >= (nextEntry - 1)) {
            nextButton.setEnabled(false);
        }
        previousButton.setEnabled(true);
    }

    private void langComboBoxItemStateChanged(ItemEvent evt) {
        clickCount++;
        String language = FILES.getProperties().get("language").toString();

        if (language.equals("English (default)")) {
            clickCount++;
        }
        LanguageResources.selectLanguage(langComboBox, clickCount, this, progress, this);
    }

    private boolean validateDuration()
    {
        String hour = hourField2.getText();
        String minute = minField2.getText();
        String format = "\\d{1,2}";

        if (!((hour.matches(format)) && (minute.matches(format))))
        {
            String text = resources.getString("validdur");
            durLabel.setForeground(Color.red);
            durLabel.setText(text);
            return false;
        }
        durLabel.setText("");
        return true;
    }

    private boolean validatePrice()
    {
        String price = priceField2.getText().trim();

        if (!price.matches("^[0-9]+(\\.[0-9]{1,2})?$"))
        {
            String text = resources.getString("validprice");
            priceLabel.setForeground(Color.red);
            priceLabel.setText(text);
            return false;
        }
        priceLabel.setText("");
        return true;
    }

    private boolean isVerified() {
        return validateUsername(custField, resources, custLabel)
                & validatePhoneNumber(contactField, resources, contactLabel);
    }

    private boolean checkRule() {
        return validateDuration() & validatePrice();
    }

    private void setUsername() {
        usernameLabel.setText(LanguageResources.capitalize(LoginFrame.getloggedUser()));
    }

    private String getDescription()
    {
        String descr = textArea2.getText().isEmpty() ? "" : textArea2.getText();

        if (descr.length() > 300) {
            descr = descr.substring(0, 300);
        }
        else if (descr.length() < 1) {
            descr = "No Description provided";
        }
        return descr;
    }

    private void emptyFields()
    {
        JTextField[] fields = new JTextField[] {
            coverField2, priceField2, minField2, hourField2, movieField2 };

        boolean[] isEmpty = new boolean[] {            
            coverField2.getText().isEmpty(), priceField2.getText().isEmpty(),
            minField2.getText().isEmpty(), hourField2.getText().isEmpty(),
            movieField2.getText().isEmpty()
        };

        for (int index = 0; index < fields.length; index++) {
            if (isEmpty[index]) {
                fields[index].setBackground(Color.red);
            }
        }
        validFields(isEmpty, fields);
    }

    private void validFields(boolean[] isEmpty, JTextField[] field)
    {
        for (int index = 0; index < isEmpty.length; index++) {
            if (!isEmpty[index]){
                field[index].setBackground(Color.WHITE);
            }
        }
    }

    private void validateCombo()
    {
        JComboBox[] combo = new JComboBox[] { langCombo2, ageCombo2, genreCombo2 };

        boolean[] emptyList = new boolean[] { langCombo2.getSelectedIndex()  == 0, 
            ageCombo2.getSelectedIndex() == 0, genreCombo2.getSelectedIndex() == 0
        };

        for (int index = 0; index < combo.length; index++) {
            if (emptyList[index])
                combo[index].setForeground(Color.red);
        }

        for (int index = 0; index < emptyList.length; index++) {
            if (!emptyList[index]){
                combo[index].setForeground(Color.BLACK);
            }
        }
    }

    private void dialogMessage(int count)
    {
        if ((count > 0))
        {
            if ((response != null) && (response.length() > 0)) {
                JOptionPane.showMessageDialog(this, resources.getString("ticketmsg"), resources.getString("tickethead"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(this, resources.getString("ticketcancel"), resources.getString("tickethead2"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else {
            String msg = resources.getString("ticketerr");
            JOptionPane.showMessageDialog(this, msg, resources.getString("ticketerrhead"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] removeDuplicates(Collection<String> collection)
    {
        String[] titleList = new String[collection.size()];
        Iterator<String> iterator = collection.iterator();

        for (int list = 0; iterator.hasNext(); list++) {
            titleList[list] = iterator.next();
        }
        return titleList;
    }

    private void addMovieList()
    {
        LinkedList<Movie> movieList;

        if (DatabaseConfiguration.isConnected())
        {
            movieList = SCRIPT.getMovies();

            for (Movie movie : movieList) {
                UNIQUE_MOVIES.add(movie.getTitle());
            }

            comboMovie.setModel(new DefaultComboBoxModel(removeDuplicates(UNIQUE_MOVIES)));
        }
        else {
            comboMovie.remove(0);
        }
    }

    private void addAmountList()
    {
        LinkedList<Movie> priceList;

        if (DatabaseConfiguration.isConnected())
        {
            priceList = SCRIPT.getMovies();

            for (Movie movie : priceList) {
                if (PRICES.add(movie.getTitle())) {
                    comboAmount.addItem(setCurrency(movie.getPrice()));
                }
            }
        }
    }

    private void removeItem(JComboBox combo)
    {
        combo = (combo != comboMovie) ? comboAmount : comboMovie;

        for (int index = 1; index < combo.getItemCount(); index++) {
            combo.removeItemAt(index);
        }

        if (combo.equals(comboMovie)){
            addMovieList();
        }
        else {
            addAmountList();
        }
    }

    private boolean exists(String title) {
        LinkedList<Movie> dataList = SCRIPT.getMovies();
        return dataList.stream().anyMatch(data -> title.equalsIgnoreCase(data.getTitle()));
    }

    private boolean exists(String name, String contactNo, String title)
    {
        LinkedList<Booking> bookingList = new Scripts().getBookings();

        /**
         * The below code fragment can be replaced with:
         *
         * return IntStream.range(0, bookingList.size()).anyMatch(index ->
         *        bookingList.get(index).getName().equalsIgnoreCase(name) &&
         *        bookingList.get(index).getContactNumber().equalsIgnoreCase(
         *          contactNo) && bookingList.get(index).getId() == id);
         */
        for (Booking booking : bookingList)
        {
            if (booking.getName().equalsIgnoreCase(name))
            {
                if (booking.getContactNumber().equalsIgnoreCase(contactNo))
                {
                    int id = SCRIPT.getMovieById(title);

                    if (booking.getTicketId() == id)
                    {
                        movieId = id;
                        ticketCount = booking.getCount();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int initializeTicket(String name, String contact, double amount, String title)
    {
        int count;

        if (exists(name, contact, title)) {
            count = SCRIPT.updateTicketCount(++ticketCount, name, contact, movieId);
            SCRIPT.addBooking(response, name, contact, SCRIPT.getMovieById(title));
        }
        else {
            count = SCRIPT.addTicket(name, contact, amount, title);
            SCRIPT.addBooking(response, name, contact, SCRIPT.getMovieById(title));
        }
        return count;
    }

    private void setTableData()
    {
        model = (DefaultTableModel) loginJTable.getModel();

        /* Retrieve the user password */
        String password = LoginFrame.getPassword();

        /* Get the login data with a call to <code>getLoginActivity</code> */
        LinkedList<Activity> list = new Scripts().getLoginActivity(password);

        for (Activity listData : list){
            model.addRow(new Object[] {
                    formatDate(listData.getDate(), 1), listData.getTime()
            });
        }
    }

    private void prepareTicketTable()
    {
        model = (DefaultTableModel) bookingsTable.getModel();
        LinkedList<Ticket> ticket = SCRIPT.getTicket();

        for (Ticket ticketType : ticket) {
            model.addRow(new Object[] {
                ticketType.getTitle(), ticketType.getTicketNumber(), 
                ticketType.getName(), ticketType.getTime(), formatDate(ticketType.getDate(), 1)});
        }
    }

    private void prepareMovieTable()
    {
        model = (DefaultTableModel) moviesTable.getModel();
        LinkedList<Movie> movieList = SCRIPT.getMovieList();

        if (movieList.size() > 0)
        {
            displayMoviesPanel.setEnabled(false);

            for (Movie movie : movieList) {
                model.addRow(new Object[] { movie.getTitle(), movie.getLanguage(), formatDate(movie.getDate(), 1),
                             movie.getDuration(), setCurrency(movie.getPrice()),
                             movie.getDescription(), processImage(movie.getImage())});
            }
        }
    }

    private void resetLoginTableStructure() {
        loginJTable.getColumnModel().getColumn(0).setHeaderValue(resources.getString("datelabel"));
        loginJTable.getColumnModel().getColumn(1).setHeaderValue(resources.getString("timelabel"));
    }

    private void resetBookingsTableStructure()
    {
        model = (DefaultTableModel) bookingsTable.getModel();
        model.getDataVector().removeAllElements();

        bookingsTable.getColumnModel().getColumn(0).setHeaderValue(resources.getString("movietitle"));
        bookingsTable.getColumnModel().getColumn(1).setHeaderValue(resources.getString("ticket"));
        bookingsTable.getColumnModel().getColumn(2).setHeaderValue(resources.getString("customer"));
        bookingsTable.getColumnModel().getColumn(3).setHeaderValue(resources.getString("time"));
        bookingsTable.getColumnModel().getColumn(4).setHeaderValue(resources.getString("date"));
    }

    private void resetMoviesTable()
    {
        model = (DefaultTableModel) moviesTable.getModel();
        model.getDataVector().removeAllElements();
        
        moviesTable.getColumnModel().getColumn(0).setHeaderValue(resources.getString("title"));
        moviesTable.getColumnModel().getColumn(1).setHeaderValue(resources.getString("language"));
        moviesTable.getColumnModel().getColumn(2).setHeaderValue(resources.getString("dateres"));
        moviesTable.getColumnModel().getColumn(3).setHeaderValue(resources.getString("dur"));
        moviesTable.getColumnModel().getColumn(4).setHeaderValue(resources.getString("price"));
        moviesTable.getColumnModel().getColumn(5).setHeaderValue(resources.getString("desc"));
        moviesTable.getColumnModel().getColumn(6).setHeaderValue(resources.getString("cover"));
    }

    private void resetPopularMoviesTable()
    {
        model = (DefaultTableModel) sessionTable.getModel();
        model.getDataVector().removeAllElements();
        model.fireTableDataChanged();
        
        sessionTable.getColumnModel().getColumn(0).setHeaderValue(resources.getString("bookedsession"));
        sessionTable.getColumnModel().getColumn(1).setHeaderValue(resources.getString("views"));
    }

    private String formatDate(String dateTime, int type)
    {
        // SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(dateTime, format);

        /* Calendar calendar = Calendar.getInstance();calendar.setTime(date); */

        if (type == 1 && date != null) {
            // return String.format("%te  %tB  %tY", calendar, calendar, calendar);
            return String.format("%s  %s  %s", 
                    date.getDayOfWeek().getValue(), date.getMonth().toString(), date.getYear());
        }
        else  {
           //  return String.format("%tY", calendar);
            return String.format("%s", (date != null ? date.getYear() : null));
        }
    }

    private ImageIcon processImage(Blob imageType)
    {
        BufferedImage imageFile;
        ImageIcon image = null;

        int size;
        byte[] bytes;

        try
        {
            size = (int) imageType.length();
            bytes = imageType.getBytes(1, size);
            imageType.free();

            imageFile = ImageIO.read(new ByteArrayInputStream(bytes));
            image = new ImageIcon(imageFile);
        }
        catch (SQLException | IOException ex) {
            System.out.format("%n%s%n%n", ex);
        }
        return image;
    }

    private void initializeDataFields()
    {
        LinkedList<User> user = new Scripts().getUserAccount(LoginFrame.getPassword());
        usernameField.setText(user.get(0).getUsername());
        passwordField.setText(user.get(0).getPassword());
        passwordField.setEditable(false);
        idField.setText(user.get(0).getId());
        idField.setEditable(false);
        phoneNoField.setText(user.get(0).getContactNo());
    }

    private void disableButtons(Boolean state) {
        previousButton.setVisible(state);
        nextButton.setVisible(state);
    }

    private void tabbedPaneListener() {
        reportsPane.addChangeListener(new ChangeListenerImpl());
    }

    private void comboBoxListener() {
        LinkedList<Movie> movieList = SCRIPT.getMovies();
        comboMovie.addItemListener(new ItemListenerImpl(movieList));
    }

    private void removComponent(JComponent component)
    {
        JComponent[] componentList = new JComponent[] {
                moviePanel, purchasePanel, displayMoviesPanel,
                bookingsPanel, popularPanel, settingsPanel
        };

        for (int index = 0; index < componentList.length; index++) {
            if (!(componentList[index] == component)) {
                welcomePanel.remove(componentList[index]);
            }
        }
        welcomePanel.add(component);
        welcomePanel.setVisible(true);
        component.setVisible(true);
    }

    private void textFieldListener()
    {
        phoneNoField.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent key)
            {
                if (phoneNoField.getText().trim().length() > 9) {
                    phoneNoField.setBackground(Color.WHITE);
                }
            }
        });
    }

    private void textFieldListener(JTextField field)
    {
        field.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent event)
            {
                if (field.getText().trim().length() >= 1) {
                    field.setBackground(Color.WHITE);
                }
            }
        });
    }

    private void textFieldListener(JTextField textField1, JTextField textField2)
    {
        textField1.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent key) {
                updateName = true;
                saveButton.setEnabled(true);
            }
        });

        textField2.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent key) {
                updateNumber = true;
                saveButton.setEnabled(true);
            }
        });
    }

    private void menuComboListener(JComboBox comboList)
    {
        comboList.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(ItemEvent event) {
                if (event.getStateChange() == ItemEvent.SELECTED)
                    comboList.setForeground(Color.BLACK);
            }
        });
    }

    private String setCurrency(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

    private void listFrequency()
    {
        LinkedList<String> list = SCRIPT.getMovieByTitle();
        LinkedList<String> listView = SCRIPT.getMostBookedSession();      
        insertMovieData(list, POPULAR_MOVIES);
        insertMovieData(listView, POPULAR_SESSION);
        
        nextEntry = POPULAR_MOVIES.size();
        sortMovies();
        mostBookedSession();
    }

    private void insertMovieData(LinkedList<String> list, Map<String, Integer> map)
    {    
        if (!map.isEmpty()) {
            map.clear();
        }
        
        for (int index = 0; index < list.size(); index++)
        {
            String movie = list.get(index).toLowerCase();

            if (map.containsKey(movie)) {
                int frequency = map.get(movie);
                map.put(movie, frequency + 1);
            }
            else {
                map.put(movie, 1);
            }
        }
    }

    private List<Entry<String, Integer>> reverseByFrequency(Map<String, Integer> map)
    {
        List<Entry<String, Integer>> sortedList = new ArrayList<>(map.entrySet());
        sortedList.sort(new Comparator<Map.Entry<String, Integer>>()
        {
            @Override
            public int compare(Map.Entry<String, Integer> firstVal, Map.Entry<String, Integer> secondVal) {
                return firstVal.getValue().compareTo(secondVal.getValue());
            }
        }.reversed());
        return sortedList;
    }

    private void mostBookedSession()
    {
        model = (DefaultTableModel) sessionTable.getModel();
        model.getDataVector().removeAllElements();
        
        List<Entry<String, Integer>> sessionList = reverseByFrequency(POPULAR_SESSION);

        if (!sessionList.isEmpty())
        {
            for (Entry<String, Integer> session : sessionList){
                model.addRow(new Object[]{
                        capitalize(session.getKey()), session.getValue()
                });
            }
        }
    }

    private void sortMovies()
    {
        if (nextEntry == 0){
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
        }
        else
        {
            if (nextEntry > 1)
            {
                if (((movieCount < nextEntry) || (movieCount == 0)))
                {
                    defaultContent(reverseByFrequency(POPULAR_MOVIES), movieCount);
                    previousButton.setEnabled(false);
                    nextButton.setEnabled(true);
                }
            }
            else
            {
                defaultContent(reverseByFrequency(POPULAR_MOVIES), 0);
                previousButton.setEnabled(false);
                nextButton.setEnabled(false);
            }
        }
    }

    private void defaultContent(List<Entry<String, Integer>> sortedList, int index)
    {
        image5.setIcon(processImage(SCRIPT.getMovieCover(sortedList.get(index).getKey())));

        content4.setVisible(true);

        title.setText(String.format("%s (%s)", LanguageResources.capitalize(sortedList.get(index).getKey()),
                formatDate(SCRIPT.getMovieByDate(sortedList.get(index).getKey()), 0)));

        rating.setText(format(sortedList, index));
        genre.setText(SCRIPT.getMovieByGenre(sortedList.get(index).getKey()));
    }

    private String format(List<Entry<String, Integer>>list, int index) {
        return String.format("%.1f", Double.parseDouble(list.get(index).getValue().toString()));
    }

    @Override
    public boolean isEmpty()
    {
        String lang = langCombo2.getSelectedIndex() == 0 ? "" : "lang";
        String date = releaseDate2.getDate() == null ? "" : "date";
        String age = ageCombo2.getSelectedIndex() == 0 ? "" : "age";
        String type = genreCombo2.getSelectedIndex() == 0 ? "" : "genre";

        if (movieField2.getText().isEmpty() || lang.isEmpty() || date.isEmpty()
                || age.isEmpty() || hourField2.getText().isEmpty() || minField2.getText()
                .isEmpty() || type.isEmpty() || priceField2.getText().isEmpty()
                || coverField2.getText().isEmpty())
        {
            PopupPane.errDialog(this, resources.getString("emptyfields"),resources.getString("headermsg"));

            validateCombo();
            emptyFields();
            return true;
        }
        emptyFields();
        validateCombo();
        return false;
    }

    @Override
    public void updateLanguagePreference(String resource, String locale)
    {
        resources = ResourceBundle.getBundle(resource);

        titleId.setText(resources.getString("idLabel"));
        titlePhone.setText(resources.getString("phoneLabel"));
        jLabel3.setText(resources.getString("addmovielabel"));
        jLabel5.setText(resources.getString("purchaselabel"));
        jLabel7.setText(resources.getString("displaymovie"));
        jLabel9.setText(resources.getString("displaybookings"));
        jLabel11.setText(resources.getString("stats"));
        jLabel12.setText(resources.getString("settings"));

        titleName.setText(resources.getString("username"));
        titlePassword.setText(resources.getString("password"));

        addMovie.setText(resources.getString("addmovie"));
        langLabel.setText(resources.getString("language"));
        datelabel.setText(resources.getString("resdate"));
        ageLabel.setText(resources.getString("ageres"));
        duration.setText(resources.getString("dur"));
        genreLabel.setText(resources.getString("genre"));
        pricelab.setText(resources.getString("price"));
        cover.setText(resources.getString("cover"));
        desc.setText(resources.getString("desc"));

        purchaseLabel.setText(resources.getString("purchase"));
        custName.setText(resources.getString("customer"));
        contactlab.setText(resources.getString("contact"));
        movietitle.setText(resources.getString("movietitle"));
        movieLabel.setText(resources.getString("movietitle"));
        amountLabel.setText(resources.getString("amount"));

        displayLabel.setText(resources.getString("display"));
        bookingsLabel.setText(resources.getString("booking"));
        settingsLabel.setText(LanguageResources.capitalize(
                resources.getString("settings")));

        uploadBtn.setText(resources.getString("upload"));
        submitBtn.setText(resources.getString("submit"));
        continueButton.setText(resources.getString("submit"));
        displayButton.setText(resources.getString("refresh"));
        bookingButton.setText(resources.getString("refresh"));
        saveButton.setText(resources.getString("save"));

        langCombo2.setModel(new DefaultComboBoxModel(new String[] {
            resources.getString("selectlang"), "English", "Chinese", "Xhosa", "Zulu"}));

        ageCombo2.setModel(new DefaultComboBoxModel(new String[] {
                resources.getString("selectage"), "PG", "16", "18"}));

        genreCombo2.setModel(new DefaultComboBoxModel(new String[] {
            resources.getString("selectgenre"), "Action", "Adventure", "Animation", 
            "Comedy", "Drama", "Horror", "Romance", "Science Fiction"}));

        statsLabel.setText(resources.getString("report"));

        reportsPane.setTitleAt(0, resources.getString("popularmovies"));
        reportsPane.setTitleAt(1, resources.getString("popularsession"));

        settingsTabbedPane.setTitleAt(0, resources.getString("account"));
        settingsTabbedPane.setTitleAt(1, resources.getString("logact"));

        resetLoginTableStructure();
        bookingButtonActionPerformed();
        displayButtonActionPerformed();
        resetPopularMoviesTable();
        mostBookedSession();
    }

    private class ItemListenerImpl implements ItemListener
    {
        private final LinkedList<Movie> movieList;

        public ItemListenerImpl(LinkedList<Movie> movieList) {
            this.movieList = movieList;
        }

        @Override
        public void itemStateChanged(ItemEvent event)
        {
            String item = comboMovie.getSelectedItem().toString();

            for (Movie movie : movieList)
            {
                if (item.equalsIgnoreCase(movie.getTitle()))
                {
                    double price = movie.getPrice();

                    for (int inner = 1; inner < comboAmount.getItemCount(); inner++)
                    {
                        double listPrice = Double.parseDouble(comboAmount.getItemAt(inner).toString()
                                .replaceAll("R", "").trim());

                        if (listPrice == price) {
                            comboAmount.setSelectedIndex(inner);
                        }
                    }
                }
            }
        }
    }

    private class ChangeListenerImpl implements ChangeListener
    {
        public ChangeListenerImpl() {}

        @Override
        public void stateChanged(ChangeEvent evt)
        {
            JTabbedPane pane = (JTabbedPane) evt.getSource();
            int selectedPane = pane.getSelectedIndex();
            if (selectedPane == 1) disableButtons(false);
            else disableButtons(true);
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        sidePanel = new JPanel();
        imageLabel = new JLabel();
        usernameLabel = new JLabel();
        panelAdd = new JPanel();
        addSelector = new JPanel();
        jLabel3 = new JLabel();
        jLabel6 = new JLabel();
        panelPurchase = new JPanel();
        purchaseSelector = new JPanel();
        jLabel5 = new JLabel();
        jLabel8 = new JLabel();
        panelMovies = new JPanel();
        movieSelector = new JPanel();
        jLabel7 = new JLabel();
        jLabel10 = new JLabel();
        panelBooking = new JPanel();
        bookingSelector = new JPanel();
        jLabel9 = new JLabel();
        jLabel13 = new JLabel();
        panelPopular = new JPanel();
        popularSelector = new JPanel();
        jLabel11 = new JLabel();
        jLabel14 = new JLabel();
        panelSettings = new JPanel();
        settingsSelector = new JPanel();
        jLabel12 = new JLabel();
        jLabel15 = new JLabel();
        welcomePanel = new JPanel();
        innerMainPanel = new JPanel();
        mainImageLabel = new JLabel();
        displayMoviesPanel = new JPanel();
        displayLabel = new JLabel();
        jPanel8 = new JPanel();
        jScrollPane7 = new JScrollPane();
        moviesTable = new JTable();
        displayButton = new JButton();
        bookingsPanel = new JPanel();
        jScrollPane3 = new JScrollPane();
        bookingsTable = new JTable();
        jPanel4 = new JPanel();
        bookingsLabel = new JLabel();
        bookingButton = new JButton();
        popularPanel = new JPanel();
        jPanel9 = new JPanel();
        statsLabel = new JLabel();
        previousButton = new JButton();
        reportsPane = new JTabbedPane();
        jScrollPane11 = new JScrollPane();
        innerpane3 = new JPanel();
        image5 = new JLabel();
        content4 = new JPanel();
        title = new JLabel();
        genre = new JLabel();
        rating = new JLabel();
        jPanel10 = new JPanel();
        jScrollPane8 = new JScrollPane();
        sessionTable = new JTable();
        nextButton = new JButton();
        settingsPanel = new JPanel();
        jPanel5 = new JPanel();
        settingsLabel = new JLabel();
        settingsTabbedPane = new JTabbedPane();
        jPanel1 = new JPanel();
        jScrollPane6 = new JScrollPane();
        jPanel7 = new JPanel();
        usernameField = new JTextField();
        titleName = new JLabel();
        titlePassword = new JLabel();
        passwordField = new JPasswordField();
        titleId = new JLabel();
        idField = new JTextField();
        saveButton = new JButton();
        titlePhone = new JLabel();
        phoneNoField = new JTextField();
        final String[] languages = new String[] {
                "English (default)", "Sesotho", "Xhosa", "Zulu"
        };
        langComboBox = new JComboBox(languages);
        jPanel6 = new JPanel();
        jScrollPane5 = new JScrollPane();
        loginJTable = new JTable();
        progress = new JProgressBar();
        moviePanel = new JPanel();
        addMovie = new JLabel();
        jPanel2 = new JPanel();
        jScrollPane2 = new JScrollPane();
        innerMoviePanel = new JPanel();
        movietitle = new JLabel();
        langLabel = new JLabel();
        movieField2 = new JTextField();
        langCombo2 = new JComboBox();
        datelabel = new JLabel();
        releaseDate2 = new com.toedter.calendar.JDateChooser();
        ageLabel = new JLabel();
        ageCombo2 = new JComboBox();
        hourField2 = new JTextField();
        duration = new JLabel();
        minField2 = new JTextField();
        jLabel22 = new JLabel();
        jLabel23 = new JLabel();
        genreCombo2 = new JComboBox();
        genreLabel = new JLabel();
        priceField2 = new JTextField();
        pricelab = new JLabel();
        desc = new JLabel();
        jScrollPane4 = new JScrollPane();
        textArea2 = new JTextArea();
        cover = new JLabel();
        coverField2 = new JTextField();
        uploadBtn = new JButton();
        submitBtn = new JButton();
        durLabel = new JLabel();
        priceLabel = new JLabel();
        purchasePanel = new JPanel();
        purchaseLabel = new JLabel();
        jPanel3 = new JPanel();
        jScrollPane1 = new JScrollPane();
        purcPanel = new JPanel();
        custName = new JLabel();
        custField = new JTextField();
        contactlab = new JLabel();
        contactField = new JTextField();
        movieLabel = new JLabel();
        comboMovie = new JComboBox();
        amountLabel = new JLabel();
        comboAmount = new JComboBox();
        continueButton = new JButton();
        custLabel = new JLabel();
        contactLabel = new JLabel();
        jPanel13 = new JPanel();
        exitLabel = new JLabel();
        jLabel2 = new JLabel();
        exitLabel1 = new JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        sidePanel.setBackground(new Color(36, 91, 99));
        sidePanel.setForeground(new Color(26, 62, 66));
        sidePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sidePanel.setDebugGraphicsOptions(DebugGraphics.NONE_OPTION);

        imageLabel.setIcon(new ImageIcon(getClass().getResource("/com/images/userprofile.png"))); // NOI18N

        usernameLabel.setFont(new Font("Segoe UI Light", 1, 18)); // NOI18N
        usernameLabel.setForeground(new Color(255, 255, 255));
        usernameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        usernameLabel.setText("Paul");
        usernameLabel.setToolTipText("");

        panelAdd.setBackground(new Color(36, 91, 97));
        panelAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                panelAddMousePressed(evt);
            }
        });
        panelAdd.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        addSelector.setEnabled(false);
        addSelector.setOpaque(false);

        GroupLayout addSelectorLayout = new GroupLayout(addSelector);
        addSelector.setLayout(addSelectorLayout);
        addSelectorLayout.setHorizontalGroup(
                addSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        addSelectorLayout.setVerticalGroup(
                addSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        panelAdd.add(addSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 5, 40));

        jLabel3.setFont(new Font("Calibri Light", 1, 13)); // NOI18N
        jLabel3.setForeground(new Color(255, 255, 255));
        jLabel3.setText("ADD NEW MOVIE");
        panelAdd.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, 20));

        jLabel6.setIcon(new ImageIcon(getClass().getResource("/com/images/add.png"))); // NOI18N
        panelAdd.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 20));

        panelPurchase.setBackground(new Color(36, 91, 97));
        panelPurchase.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                panelPurchaseMousePressed(evt);
            }
        });
        panelPurchase.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        purchaseSelector.setEnabled(false);
        purchaseSelector.setOpaque(false);

        GroupLayout purchaseSelectorLayout = new GroupLayout(purchaseSelector);
        purchaseSelector.setLayout(purchaseSelectorLayout);
        purchaseSelectorLayout.setHorizontalGroup(
                purchaseSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        purchaseSelectorLayout.setVerticalGroup(
                purchaseSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        panelPurchase.add(purchaseSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 5, 40));

        jLabel5.setFont(new Font("Calibri Light", 1, 13)); // NOI18N
        jLabel5.setForeground(new Color(255, 255, 255));
        jLabel5.setText("PURCHASE TICKET");
        panelPurchase.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, 150, 20));

        jLabel8.setIcon(new ImageIcon(getClass().getResource("/com/images/purchase.png"))); // NOI18N
        panelPurchase.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 20, 20));

        panelMovies.setBackground(new Color(36, 91, 97));
        panelMovies.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                panelMoviesMousePressed(evt);
            }
        });
        panelMovies.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        movieSelector.setEnabled(false);
        movieSelector.setOpaque(false);

        GroupLayout movieSelectorLayout = new GroupLayout(movieSelector);
        movieSelector.setLayout(movieSelectorLayout);
        movieSelectorLayout.setHorizontalGroup(
                movieSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        movieSelectorLayout.setVerticalGroup(
                movieSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        panelMovies.add(movieSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 5, 40));

        jLabel7.setFont(new Font("Calibri Light", 1, 13)); // NOI18N
        jLabel7.setForeground(new Color(255, 255, 255));
        jLabel7.setText("DISPLAY MOVIES");
        panelMovies.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, 20));

        jLabel10.setIcon(new ImageIcon(getClass().getResource("/com/images/movies.png"))); // NOI18N
        panelMovies.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 20));

        panelBooking.setBackground(new Color(36, 91, 97));
        panelBooking.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                panelBookingsMousePressed(evt);
            }
        });
        panelBooking.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        bookingSelector.setEnabled(false);
        bookingSelector.setOpaque(false);

        GroupLayout bookingSelectorLayout = new GroupLayout(bookingSelector);
        bookingSelector.setLayout(bookingSelectorLayout);
        bookingSelectorLayout.setHorizontalGroup(
                bookingSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        bookingSelectorLayout.setVerticalGroup(
                bookingSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        panelBooking.add(bookingSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 5, 40));

        jLabel9.setFont(new Font("Calibri Light", 1, 13)); // NOI18N
        jLabel9.setForeground(new Color(255, 255, 255));
        jLabel9.setText("DISPLAY BOOKINGS");
        panelBooking.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, 20));

        jLabel13.setIcon(new ImageIcon(getClass().getResource("/com/images/booking.png"))); // NOI18N
        panelBooking.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 20));

        panelPopular.setBackground(new Color(36, 91, 97));
        panelPopular.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent evt) {
                panelPopularMousePressed(evt);
            }
        });
        panelPopular.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        popularSelector.setEnabled(false);
        popularSelector.setOpaque(false);

        GroupLayout popularSelectorLayout = new GroupLayout(popularSelector);
        popularSelector.setLayout(popularSelectorLayout);
        popularSelectorLayout.setHorizontalGroup(
                popularSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        popularSelectorLayout.setVerticalGroup(
                popularSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        panelPopular.add(popularSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 5, 40));

        jLabel11.setFont(new Font("Calibri Light", 1, 13)); // NOI18N
        jLabel11.setForeground(new Color(255, 255, 255));
        jLabel11.setText("STATISTICS");
        panelPopular.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, 20));

        jLabel14.setIcon(new ImageIcon(getClass().getResource("/com/images/popular.png"))); // NOI18N
        panelPopular.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 20));

        panelSettings.setBackground(new Color(36, 91, 97));
        panelSettings.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                panelSettingsMousePressed(evt);
            }
        });
        panelSettings.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        settingsSelector.setEnabled(false);
        settingsSelector.setOpaque(false);

        GroupLayout settingsSelectorLayout = new GroupLayout(settingsSelector);
        settingsSelector.setLayout(settingsSelectorLayout);
        settingsSelectorLayout.setHorizontalGroup(
                settingsSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        settingsSelectorLayout.setVerticalGroup(
                settingsSelectorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 40, Short.MAX_VALUE)
        );

        panelSettings.add(settingsSelector, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 5, 40));

        jLabel12.setFont(new Font("Calibri Light", 1, 13)); // NOI18N
        jLabel12.setForeground(new Color(255, 255, 255));
        jLabel12.setText("SETTINGS");
        panelSettings.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(40, 10, -1, 20));

        jLabel15.setIcon(new ImageIcon(getClass().getResource("/com/images/settings.png"))); // NOI18N
        panelSettings.add(jLabel15, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, 20));

        GroupLayout sidePanelLayout = new GroupLayout(sidePanel);
        sidePanel.setLayout(sidePanelLayout);
        sidePanelLayout.setHorizontalGroup(
                sidePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(panelAdd, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelPurchase, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelMovies, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelBooking, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(panelPopular, GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                        .addComponent(panelSettings, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(sidePanelLayout.createSequentialGroup()
                                .addGroup(sidePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(sidePanelLayout.createSequentialGroup()
                                                .addGap(43, 43, 43)
                                                .addComponent(usernameLabel, GroupLayout.PREFERRED_SIZE, 118, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(sidePanelLayout.createSequentialGroup()
                                                .addGap(77, 77, 77)
                                                .addComponent(imageLabel)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sidePanelLayout.setVerticalGroup(
                sidePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(sidePanelLayout.createSequentialGroup()
                                .addGap(23, 23, 23)
                                .addComponent(imageLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(usernameLabel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
                                .addGap(54, 54, 54)
                                .addComponent(panelAdd, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(panelPurchase, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(panelMovies, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(panelBooking, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)
                                .addComponent(panelPopular, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(panelSettings, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        welcomePanel.setBackground(new Color(36, 91, 99));
        welcomePanel.setForeground(new Color(36, 91, 99));

        innerMainPanel.setPreferredSize(new Dimension(627, 451));

        mainImageLabel.setIcon(new ImageIcon(getClass().getResource("/com/images/Welcome to The Stems Entertainment Group (1).png"))); // NOI18N

        GroupLayout innerMainPanelLayout = new GroupLayout(innerMainPanel);
        innerMainPanel.setLayout(innerMainPanelLayout);
        innerMainPanelLayout.setHorizontalGroup(
                innerMainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(innerMainPanelLayout.createSequentialGroup()
                                .addGap(54, 54, 54)
                                .addComponent(mainImageLabel, GroupLayout.PREFERRED_SIZE, 517, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(56, Short.MAX_VALUE))
        );
        innerMainPanelLayout.setVerticalGroup(
                innerMainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(innerMainPanelLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addComponent(mainImageLabel, GroupLayout.PREFERRED_SIZE, 352, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(54, Short.MAX_VALUE))
        );

        displayMoviesPanel.setPreferredSize(new Dimension(627, 451));

        displayLabel.setFont(new Font("Segoe UI Emoji", 1, 18)); // NOI18N
        displayLabel.setText("Display Movies");

        jPanel8.setBackground(new Color(204, 204, 204));
        jPanel8.setPreferredSize(new Dimension(0, 2));

        GroupLayout jPanel8Layout = new GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
                jPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel8Layout.setVerticalGroup(
                jPanel8Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        moviesTable.setFont(new Font("Calibri Light", 1, 14)); // NOI18N
        moviesTable.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        resources.getString("movietitle"), resources.getString("language"), resources.getString("dateres"), resources.getString("dur"), resources.getString("price"), resources.getString("desc"), resources.getString("cover")
                }
        ) {
            Class[] types = new Class [] {
                    java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, ImageIcon.class
            };
            boolean[] canEdit = new boolean [] {
                    false, false, false, false, false, false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        
        moviesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        moviesTable.setIntercellSpacing(new Dimension(15, 8));
        moviesTable.setRowHeight(200);
        jScrollPane7.setViewportView(moviesTable);
        
        if (moviesTable.getColumnModel().getColumnCount() > 0) {
            moviesTable.getColumnModel().getColumn(0).setMinWidth(180);
            moviesTable.getColumnModel().getColumn(0).setPreferredWidth(180);
            moviesTable.getColumnModel().getColumn(0).setMaxWidth(180);
            moviesTable.getColumnModel().getColumn(1).setMinWidth(100);
            moviesTable.getColumnModel().getColumn(1).setPreferredWidth(100);
            moviesTable.getColumnModel().getColumn(1).setMaxWidth(100);
            moviesTable.getColumnModel().getColumn(2).setMinWidth(160);
            moviesTable.getColumnModel().getColumn(2).setPreferredWidth(160);
            moviesTable.getColumnModel().getColumn(2).setMaxWidth(160);
            moviesTable.getColumnModel().getColumn(3).setMinWidth(90);
            moviesTable.getColumnModel().getColumn(3).setPreferredWidth(90);
            moviesTable.getColumnModel().getColumn(3).setMaxWidth(90);
            moviesTable.getColumnModel().getColumn(4).setMinWidth(90);
            moviesTable.getColumnModel().getColumn(4).setPreferredWidth(90);
            moviesTable.getColumnModel().getColumn(4).setMaxWidth(90);
            moviesTable.getColumnModel().getColumn(5).setMinWidth(250);
            moviesTable.getColumnModel().getColumn(5).setPreferredWidth(250);
            moviesTable.getColumnModel().getColumn(5).setMaxWidth(250);
            moviesTable.getColumnModel().getColumn(6).setMinWidth(230);
            moviesTable.getColumnModel().getColumn(6).setPreferredWidth(230);
            moviesTable.getColumnModel().getColumn(6).setMaxWidth(230);
        }

        displayButton.setFont(new Font("Calibri", 1, 14)); // NOI18N
        displayButton.setText("Refresh");
        displayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                displayButtonActionPerformed();
            }
        });

        GroupLayout displayMoviesPanelLayout = new GroupLayout(displayMoviesPanel);
        displayMoviesPanel.setLayout(displayMoviesPanelLayout);
        displayMoviesPanelLayout.setHorizontalGroup(
                displayMoviesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(displayMoviesPanelLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(displayMoviesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(displayButton, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(displayMoviesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(jPanel8, GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)
                                                .addComponent(displayLabel, GroupLayout.PREFERRED_SIZE, 241, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(jScrollPane7, GroupLayout.DEFAULT_SIZE, 536, Short.MAX_VALUE)))
                                .addContainerGap(46, Short.MAX_VALUE))
        );
        displayMoviesPanelLayout.setVerticalGroup(
                displayMoviesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(displayMoviesPanelLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(displayLabel, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(38, 38, 38)
                                .addComponent(jScrollPane7, GroupLayout.PREFERRED_SIZE, 268, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(displayButton, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(28, Short.MAX_VALUE))
        );

        bookingsPanel.setPreferredSize(new Dimension(627, 451));

        bookingsTable.setFont(new Font("Calibri Light", 0, 12)); // NOI18N
        bookingsTable.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        resources.getString("ticket"), resources.getString("customer"), resources.getString("movietitle"), resources.getString("time"), resources.getString("date")
                }
        ) {
            Class[] types = new Class [] {
                    java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                    false, false, false, false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        bookingsTable.setIntercellSpacing(new Dimension(8, 6));
        bookingsTable.setRowHeight(18);
        jScrollPane3.setViewportView(bookingsTable);
        if (bookingsTable.getColumnModel().getColumnCount() > 0) {
            bookingsTable.getColumnModel().getColumn(0).setPreferredWidth(46);
        }

        jPanel4.setBackground(new Color(204, 204, 204));

        GroupLayout jPanel4Layout = new GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
                jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
                jPanel4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        bookingsLabel.setFont(new Font("Segoe UI Emoji", 1, 18)); // NOI18N
        bookingsLabel.setText("Bookings");

        bookingButton.setFont(new Font("Calibri", 1, 14)); // NOI18N
        bookingButton.setText("Refresh");
        bookingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                bookingButtonActionPerformed();
            }
        });

        GroupLayout bookingsPanelLayout = new GroupLayout(bookingsPanel);
        bookingsPanel.setLayout(bookingsPanelLayout);
        bookingsPanelLayout.setHorizontalGroup(
                bookingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(bookingsPanelLayout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(bookingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(bookingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                .addComponent(bookingsLabel)
                                                .addComponent(jPanel4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 537, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(bookingButton, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(45, Short.MAX_VALUE))
        );
        bookingsPanelLayout.setVerticalGroup(
                bookingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(bookingsPanelLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(bookingsLabel, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jPanel4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                                .addComponent(jScrollPane3, GroupLayout.PREFERRED_SIZE, 264, GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(bookingButton, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(29, 29, 29))
        );

        popularPanel.setPreferredSize(new Dimension(627, 451));

        jPanel9.setBackground(new Color(204, 204, 204));
        jPanel9.setPreferredSize(new Dimension(538, 2));

        GroupLayout jPanel9Layout = new GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
                jPanel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 536, Short.MAX_VALUE)
        );
        jPanel9Layout.setVerticalGroup(
                jPanel9Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        statsLabel.setFont(new Font("Segoe UI Emoji", 1, 18)); // NOI18N
        statsLabel.setText("Management Reporting");

        previousButton.setFont(new Font("Calibri", 1, 13)); // NOI18N
        previousButton.setText("<<");
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        content4.setBorder(BorderFactory.createLineBorder(new Color(153, 153, 153)));

        title.setFont(new Font("Segoe UI Semibold", 0, 13)); // NOI18N
        title.setHorizontalAlignment(SwingConstants.CENTER);

        genre.setFont(new Font("Segoe UI Semibold", 0, 12)); // NOI18N
        genre.setHorizontalAlignment(SwingConstants.CENTER);

        rating.setFont(new Font("Segoe UI Semibold", 0, 12)); // NOI18N
        rating.setHorizontalAlignment(SwingConstants.CENTER);

        content4.setVisible(false);

        GroupLayout content4Layout = new GroupLayout(content4);
        content4.setLayout(content4Layout);
        content4Layout.setHorizontalGroup(
                content4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(rating, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(genre, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(title, GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
        );
        content4Layout.setVerticalGroup(
                content4Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(content4Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(title)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(genre)
                                .addGap(9, 9, 9)
                                .addComponent(rating)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout innerpane3Layout = new GroupLayout(innerpane3);
        innerpane3.setLayout(innerpane3Layout);
        innerpane3Layout.setHorizontalGroup(
                innerpane3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(innerpane3Layout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addComponent(image5, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                                .addGap(20, 20, 20)
                                .addComponent(content4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(40, 40, 40))
        );
        innerpane3Layout.setVerticalGroup(
                innerpane3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(innerpane3Layout.createSequentialGroup()
                                .addGap(14, 14, 14)
                                .addGroup(innerpane3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(image5, GroupLayout.PREFERRED_SIZE, 267, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(content4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(3, 3, 3))
        );

        jScrollPane11.setViewportView(innerpane3);

        reportsPane.addTab("Popular Movies", jScrollPane11);

        sessionTable.setFont(new Font("Calibri Light", 0, 12)); // NOI18N
        sessionTable.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        "Most Booked Session", "Count"
                }
        ) 
        {
            boolean[] canEdit = new boolean [] {
                    false, false
            };

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        sessionTable.setIntercellSpacing(new Dimension(8, 6));
        sessionTable.setRowHeight(18);
        jScrollPane8.setViewportView(sessionTable);

        GroupLayout jPanel10Layout = new GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
                jPanel10Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel10Layout.createSequentialGroup()
                                .addGap(34, 34, 34)
                                .addComponent(jScrollPane8, GroupLayout.PREFERRED_SIZE, 461, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(36, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
                jPanel10Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                                .addContainerGap(29, Short.MAX_VALUE)
                                .addComponent(jScrollPane8, GroupLayout.PREFERRED_SIZE, 237, GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26))
        );

        reportsPane.addTab("Popular Movie Session ", jPanel10);

        nextButton.setFont(new Font("Calibri", 1, 13)); // NOI18N
        nextButton.setText(">>");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        GroupLayout popularPanelLayout = new GroupLayout(popularPanel);
        popularPanel.setLayout(popularPanelLayout);
        popularPanelLayout.setHorizontalGroup(
                popularPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(popularPanelLayout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addGroup(popularPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addGroup(popularPanelLayout.createSequentialGroup()
                                                .addComponent(previousButton, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(nextButton, GroupLayout.PREFERRED_SIZE, 76, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(reportsPane, GroupLayout.PREFERRED_SIZE, 536, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jPanel9, GroupLayout.PREFERRED_SIZE, 536, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(statsLabel, GroupLayout.PREFERRED_SIZE, 325, GroupLayout.PREFERRED_SIZE))
                                .addGap(44, 44, 44))
        );
        popularPanelLayout.setVerticalGroup(
                popularPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(popularPanelLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(statsLabel, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addComponent(reportsPane, GroupLayout.PREFERRED_SIZE, 320, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(popularPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(previousButton)
                                        .addComponent(nextButton))
                                .addContainerGap(12, Short.MAX_VALUE))
        );

        settingsPanel.setPreferredSize(new Dimension(627, 451));

        jPanel5.setBackground(new Color(204, 204, 204));

        GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
                jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
                jPanel5Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        settingsLabel.setFont(new Font("Segoe UI Emoji", 1, 18)); // NOI18N
        settingsLabel.setText("Settings");

        settingsTabbedPane.setBackground(new Color(255, 255, 255));
        settingsTabbedPane.setFont(new Font("Tahoma", 0, 12)); // NOI18N

        jPanel1.setBackground(new Color(255, 255, 255));

        jScrollPane6.setBorder(null);

        jPanel7.setBackground(new Color(255, 255, 255));
        jPanel7.setForeground(new Color(255, 255, 255));

        usernameField.setFont(new Font("Calibri", 0, 14)); // NOI18N
        usernameField.setMargin(new Insets(6, 8, 2, 2));

        titleName.setFont(new Font("Calibri", 1, 16)); // NOI18N
        titleName.setText(resources.getString("username"));

        titlePassword.setFont(new Font("Calibri", 1, 16)); // NOI18N
        titlePassword.setText(resources.getString("password"));

        passwordField.setEditable(false);
        passwordField.setFont(new Font("Calibri", 0, 14)); // NOI18N
        passwordField.setMargin(new Insets(8, 8, 4, 8));

        titleId.setFont(new Font("Calibri", 1, 16)); // NOI18N
        titleId.setText(resources.getString("idLabel"));

        idField.setFont(new Font("Calibri", 0, 14)); // NOI18N
        idField.setMargin(new Insets(8, 8, 4, 6));

        saveButton.setFont(new Font("Tahoma", 1, 13)); // NOI18N
        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        titlePhone.setFont(new Font("Calibri", 1, 16)); // NOI18N
        titlePhone.setText(resources.getString("phoneLabel"));

        phoneNoField.setFont(new Font("Calibri", 0, 14)); // NOI18N
        phoneNoField.setMargin(new Insets(8, 8, 4, 2));

        langComboBox.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        langComboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        langComboBox.setMinimumSize(new Dimension(115, 19));
        langComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent evt) {
                langComboBoxItemStateChanged(evt);
            }
        });

        GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
                jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(0, 0, 0)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, 104, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(langComboBox, GroupLayout.PREFERRED_SIZE, 134, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(phoneNoField, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel7Layout.createSequentialGroup()
                                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(titleId, GroupLayout.DEFAULT_SIZE, 143, Short.MAX_VALUE)
                                                        .addComponent(titlePhone, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(idField, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE)
                                        .addGroup(GroupLayout.Alignment.LEADING, jPanel7Layout.createSequentialGroup()
                                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(titleName, GroupLayout.DEFAULT_SIZE, 139, Short.MAX_VALUE)
                                                        .addComponent(titlePassword, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(14, 14, 14)
                                                .addComponent(usernameField, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE))))
        );
        jPanel7Layout.setVerticalGroup(
                jPanel7Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(usernameField, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(titleName))
                                .addGap(20, 20, 20)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(passwordField, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(titlePassword))
                                .addGap(20, 20, 20)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(idField, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(titleId))
                                .addGap(20, 20, 20)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(phoneNoField, GroupLayout.PREFERRED_SIZE, 34, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(titlePhone))
                                .addGap(30, 30, 30)
                                .addGroup(jPanel7Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(langComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane6.setViewportView(jPanel7);

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(32, 32, 32)
                                .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 462, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(33, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addComponent(jScrollPane6, GroupLayout.PREFERRED_SIZE, 218, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(37, Short.MAX_VALUE))
        );

        settingsTabbedPane.addTab("Account", jPanel1);

        jPanel6.setBackground(new Color(255, 255, 255));

        loginJTable.setFont(new Font("Calibri Light", 0, 12)); // NOI18N
        loginJTable.setModel(new DefaultTableModel(
                new Object [][] {

                },
                new String [] {
                        resources.getString("datelabel"), resources.getString("timelabel")
                }
        ) 
        {
            Class[] types = new Class [] {
                    java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                    false, false
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        loginJTable.setIntercellSpacing(new Dimension(8, 6));
        loginJTable.setRowHeight(18);
        jScrollPane5.setViewportView(loginJTable);

        GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
                jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel6Layout.createSequentialGroup()
                                .addGap(33, 33, 33)
                                .addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 461, GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33))
        );
        jPanel6Layout.setVerticalGroup(
                jPanel6Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(jScrollPane5, GroupLayout.PREFERRED_SIZE, 206, GroupLayout.PREFERRED_SIZE)
                                .addGap(46, 46, 46))
        );

        settingsTabbedPane.addTab("Login Activity", jPanel6);

        progress.setIndeterminate(true);
        progress.setPreferredSize(new Dimension(530, 8));

        GroupLayout settingsPanelLayout = new GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
                settingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGap(43, 43, 43)
                                .addGroup(settingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(settingsLabel)
                                        .addComponent(settingsTabbedPane)
                                        .addComponent(jPanel5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(progress, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(52, Short.MAX_VALUE))
        );
        settingsPanelLayout.setVerticalGroup(
                settingsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addGap(25, 25, 25)
                                .addComponent(settingsLabel, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(39, 39, 39)
                                .addComponent(settingsTabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(progress, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(29, Short.MAX_VALUE))
        );

        settingsTabbedPane.getAccessibleContext().setAccessibleName("account");
        progress.setVisible(false);

        moviePanel.setPreferredSize(new Dimension(627, 451));

        addMovie.setFont(new Font("Segoe UI Emoji", 1, 18)); // NOI18N
        addMovie.setText("Add Movie");

        jPanel2.setBackground(new Color(204, 204, 204));

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jScrollPane2.setBorder(null);

        innerMoviePanel.setBackground(new Color(252, 255, 253));
        innerMoviePanel.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));

        movietitle.setFont(new Font("Calibri", 1, 16)); // NOI18N
        movietitle.setText("Movie Title ");

        langLabel.setFont(new Font("Calibri", 1, 16)); // NOI18N
        langLabel.setText("Language");

        movieField2.setMargin(new Insets(2, 8, 2, 2));

        langCombo2.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        langCombo2.setModel(new DefaultComboBoxModel(new String[] { resources.getString("selectlang"), "English", "Chinese", "Xhosa", "Zulu" }));

        datelabel.setFont(new Font("Calibri", 1, 16)); // NOI18N
        datelabel.setText("Release Date");

        releaseDate2.setFont(new Font("Tahoma", 0, 12)); // NOI18N

        ageLabel.setFont(new Font("Calibri", 1, 16)); // NOI18N
        ageLabel.setText("Age Restriction");

        ageCombo2.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        ageCombo2.setModel(new DefaultComboBoxModel(new String[] { resources.getString("selectage"), "PG", "16", "18" }));

        hourField2.setMargin(new Insets(2, 8, 2, 2));

        duration.setFont(new Font("Calibri", 1, 16)); // NOI18N
        duration.setText("Duration");

        minField2.setMargin(new Insets(2, 8, 2, 2));

        jLabel22.setFont(new Font("Segoe UI Light", 0, 12)); // NOI18N
        jLabel22.setText("HR(S)");

        jLabel23.setFont(new Font("Segoe UI Light", 0, 12)); // NOI18N
        jLabel23.setText("MIN");

        genreCombo2.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        genreCombo2.setModel(new DefaultComboBoxModel(new String[] { resources.getString("selectgenre"), "Action", "Adventure", "Animation", "Comedy", "Drama", "Horror", "Romance", "Science Fiction" }));

        genreLabel.setFont(new Font("Calibri", 1, 16)); // NOI18N
        genreLabel.setText("Genre");

        priceField2.setMargin(new Insets(2, 8, 2, 2));

        pricelab.setFont(new Font("Calibri", 1, 16)); // NOI18N
        pricelab.setText("Price");

        desc.setFont(new Font("Calibri", 1, 16)); // NOI18N
        desc.setText("Description");

        textArea2.setColumns(20);
        textArea2.setRows(5);
        jScrollPane4.setViewportView(textArea2);

        cover.setFont(new Font("Calibri", 1, 16)); // NOI18N
        cover.setText("Cover");

        coverField2.setEnabled(false);
        coverField2.setMargin(new Insets(2, 8, 2, 2));

        uploadBtn.setFont(new Font("Calibri", 1, 14)); // NOI18N
        uploadBtn.setText("Upload");
        uploadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                uploadBtnActionPerformed(evt);
            }
        });

        submitBtn.setFont(new Font("Calibri", 1, 16)); // NOI18N
        submitBtn.setText("Submit");
        submitBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                submitBtnActionPerformed(evt);
            }
        });

        GroupLayout innerMoviePanelLayout = new GroupLayout(innerMoviePanel);
        innerMoviePanel.setLayout(innerMoviePanelLayout);
        innerMoviePanelLayout.setHorizontalGroup(
                innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                .addGap(26, 26, 26)
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(GroupLayout.Alignment.TRAILING, innerMoviePanelLayout.createSequentialGroup()
                                                                .addGap(2, 2, 2)
                                                                .addComponent(pricelab, GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED))
                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addComponent(priceField2, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(hourField2, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE))
                                                                .addGap(18, 20, Short.MAX_VALUE))
                                                        .addGroup(GroupLayout.Alignment.TRAILING, innerMoviePanelLayout.createSequentialGroup()
                                                                .addGap(0, 0, Short.MAX_VALUE)
                                                                .addComponent(priceLabel, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(28, 28, 28)))
                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(minField2, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                .addGap(2, 2, 2)
                                                                .addComponent(cover, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                .addComponent(coverField2, GroupLayout.PREFERRED_SIZE, 229, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(uploadBtn, GroupLayout.PREFERRED_SIZE, 83, GroupLayout.PREFERRED_SIZE)))
                                                .addGap(32, 32, 32))
                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                .addGap(44, 44, 44)
                                                                .addComponent(jLabel22)
                                                                .addGap(110, 110, 110)
                                                                .addComponent(jLabel23))
                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                                                .addComponent(releaseDate2, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                .addComponent(movieField2, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 255, GroupLayout.PREFERRED_SIZE))
                                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                                .addGap(1, 1, 1)
                                                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                        .addComponent(datelabel, GroupLayout.PREFERRED_SIZE, 205, GroupLayout.PREFERRED_SIZE)
                                                                                        .addComponent(duration, GroupLayout.PREFERRED_SIZE, 142, GroupLayout.PREFERRED_SIZE)))
                                                                        .addComponent(movietitle, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE))
                                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                                .addGap(45, 45, 45)
                                                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                                        .addComponent(ageCombo2, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                                        .addComponent(langCombo2, 0, 160, Short.MAX_VALUE)
                                                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                                                .addGap(2, 2, 2)
                                                                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                                        .addComponent(genreLabel, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)
                                                                                                        .addComponent(langLabel, GroupLayout.PREFERRED_SIZE, 115, GroupLayout.PREFERRED_SIZE)))
                                                                                        .addComponent(genreCombo2, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                                                        .addGroup(GroupLayout.Alignment.TRAILING, innerMoviePanelLayout.createSequentialGroup()
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                .addComponent(ageLabel, GroupLayout.PREFERRED_SIZE, 158, GroupLayout.PREFERRED_SIZE))))
                                                        .addComponent(desc, GroupLayout.PREFERRED_SIZE, 166, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 257, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(submitBtn, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                                .addGap(4, 4, 4)
                                                                .addComponent(durLabel, GroupLayout.PREFERRED_SIZE, 242, GroupLayout.PREFERRED_SIZE)))
                                                .addGap(0, 32, Short.MAX_VALUE))))
        );
        innerMoviePanelLayout.setVerticalGroup(
                innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                .addGap(19, 19, 19)
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(langLabel)
                                        .addComponent(movietitle))
                                .addGap(0, 0, 0)
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(langCombo2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(movieField2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                .addComponent(datelabel)
                                                .addGap(0, 0, 0)
                                                .addComponent(releaseDate2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                .addComponent(ageLabel)
                                                .addGap(0, 0, 0)
                                                .addComponent(ageCombo2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                .addComponent(duration)
                                                .addGap(0, 0, 0)
                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(minField2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(hourField2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)))
                                        .addGroup(innerMoviePanelLayout.createSequentialGroup()
                                                .addComponent(genreLabel)
                                                .addGap(0, 0, 0)
                                                .addComponent(genreCombo2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)))
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel22)
                                        .addComponent(jLabel23))
                                .addGap(2, 2, 2)
                                .addComponent(durLabel)
                                .addGap(14, 14, 14)
                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.TRAILING, innerMoviePanelLayout.createSequentialGroup()
                                                .addGroup(innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(pricelab)
                                                        .addComponent(cover))
                                                .addGap(0, 0, 0)
                                                .addComponent(priceField2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(GroupLayout.Alignment.TRAILING, innerMoviePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                .addComponent(coverField2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(uploadBtn, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(priceLabel)
                                .addGap(18, 18, 18)
                                .addComponent(desc)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane4, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(submitBtn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        jScrollPane2.setViewportView(innerMoviePanel);

        GroupLayout moviePanelLayout = new GroupLayout(moviePanel);
        moviePanel.setLayout(moviePanelLayout);
        moviePanelLayout.setHorizontalGroup(
                moviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(moviePanelLayout.createSequentialGroup()
                                .addGap(47, 47, 47)
                                .addGroup(moviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(addMovie)
                                        .addComponent(jPanel2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE))
                                .addContainerGap(43, Short.MAX_VALUE))
        );
        moviePanelLayout.setVerticalGroup(
                moviePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(moviePanelLayout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(addMovie, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(33, 33, 33)
                                .addComponent(jScrollPane2, GroupLayout.PREFERRED_SIZE, 317, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(44, Short.MAX_VALUE))
        );

        purchasePanel.setPreferredSize(new Dimension(627, 451));

        purchaseLabel.setFont(new Font("Segoe UI Emoji", 1, 18)); // NOI18N
        purchaseLabel.setText("Purchase Ticket");

        jPanel3.setBackground(new Color(204, 204, 204));
        jPanel3.setPreferredSize(new Dimension(0, 2));

        GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 2, Short.MAX_VALUE)
        );

        jScrollPane1.setBorder(null);

        purcPanel.setBackground(new Color(252, 255, 253));
        purcPanel.setBorder(BorderFactory.createLineBorder(new Color(204, 204, 204)));

        custName.setFont(new Font("Calibri", 1, 16)); // NOI18N
        custName.setText("Customer Name");

        custField.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        custField.setMargin(new Insets(2, 8, 2, 2));

        contactlab.setFont(new Font("Calibri", 1, 16)); // NOI18N
        contactlab.setText("Contact Number");

        contactField.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        contactField.setMargin(new Insets(2, 8, 2, 2));

        movieLabel.setFont(new Font("Calibri", 1, 16)); // NOI18N
        movieLabel.setText("Movie Title");

        comboMovie.setFont(new Font("Tahoma", 0, 12)); // NOI18N

        amountLabel.setFont(new Font("Calibri", 1, 16)); // NOI18N
        amountLabel.setText("Amount");

        comboAmount.setFont(new Font("Tahoma", 0, 12)); // NOI18N
        comboAmount.setModel(new DefaultComboBoxModel(new String[] {  }));
        comboAmount.setAutoscrolls(true);
        comboAmount.setEnabled(false);

        continueButton.setFont(new Font("Calibri", 1, 16)); // NOI18N
        continueButton.setText("Submit");
        continueButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                continueButtonActionPerformed(evt);
            }
        });

        GroupLayout purcPanelLayout = new GroupLayout(purcPanel);
        purcPanel.setLayout(purcPanelLayout);
        purcPanelLayout.setHorizontalGroup(
                purcPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(purcPanelLayout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addGroup(purcPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(purcPanelLayout.createSequentialGroup()
                                                .addGroup(purcPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addComponent(custLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(GroupLayout.Alignment.LEADING, purcPanelLayout.createSequentialGroup()
                                                                .addGroup(purcPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(continueButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 93, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(contactlab, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 233, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(custName, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 236, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(movieLabel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE)
                                                                        .addComponent(amountLabel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 144, GroupLayout.PREFERRED_SIZE))
                                                                .addGap(0, 0, Short.MAX_VALUE)))
                                                .addGap(15, 15, 15))
                                        .addGroup(purcPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                .addComponent(comboAmount, GroupLayout.Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(comboMovie, GroupLayout.Alignment.LEADING, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(contactField, GroupLayout.Alignment.LEADING)
                                                .addComponent(contactLabel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 290, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(custField, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 375, GroupLayout.PREFERRED_SIZE)))
                                .addGap(63, 63, 63))
        );
        purcPanelLayout.setVerticalGroup(
                purcPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(purcPanelLayout.createSequentialGroup()
                                .addGap(30, 30, 30)
                                .addComponent(custName)
                                .addGap(0, 0, 0)
                                .addComponent(custField, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(custLabel)
                                .addGap(10, 10, 10)
                                .addComponent(contactlab)
                                .addGap(0, 0, 0)
                                .addComponent(contactField, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(2, 2, 2)
                                .addComponent(contactLabel)
                                .addGap(18, 18, 18)
                                .addComponent(movieLabel)
                                .addGap(3, 3, 3)
                                .addComponent(comboMovie, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(amountLabel)
                                .addGap(3, 3, 3)
                                .addComponent(comboAmount, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)
                                .addComponent(continueButton, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(20, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(purcPanel);

        GroupLayout purchasePanelLayout = new GroupLayout(purchasePanel);
        purchasePanel.setLayout(purchasePanelLayout);
        purchasePanelLayout.setHorizontalGroup(
                purchasePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(purchasePanelLayout.createSequentialGroup()
                                .addGap(51, 51, 51)
                                .addGroup(purchasePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(jPanel3, GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
                                        .addComponent(purchaseLabel)
                                        .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 522, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(54, Short.MAX_VALUE))
        );
        purchasePanelLayout.setVerticalGroup(
                purchasePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(purchasePanelLayout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addComponent(purchaseLabel, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(32, 32, 32)
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 311, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(43, Short.MAX_VALUE))
        );

        GroupLayout welcomePanelLayout = new GroupLayout(welcomePanel);
        welcomePanel.setLayout(welcomePanelLayout);
        welcomePanelLayout.setHorizontalGroup(
                welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(welcomePanelLayout.createSequentialGroup()
                                        .addComponent(innerMainPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 12, Short.MAX_VALUE)))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(welcomePanelLayout.createSequentialGroup()
                                        .addComponent(displayMoviesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(welcomePanelLayout.createSequentialGroup()
                                        .addComponent(bookingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 3, Short.MAX_VALUE)))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(popularPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(welcomePanelLayout.createSequentialGroup()
                                        .addComponent(settingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(welcomePanelLayout.createSequentialGroup()
                                        .addComponent(moviePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 3, Short.MAX_VALUE)))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(purchasePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
        welcomePanelLayout.setVerticalGroup(
                welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 452, Short.MAX_VALUE)
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(innerMainPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(displayMoviesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(welcomePanelLayout.createSequentialGroup()
                                        .addComponent(bookingsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(popularPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(settingsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(moviePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(welcomePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(purchasePanel, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        jPanel13.setBackground(new Color(64, 107, 115));
        jPanel13.setBorder(BorderFactory.createLineBorder(new Color(0, 73, 99), 0));

        exitLabel.setIcon(new ImageIcon(getClass().getResource("/com/images/close24.png"))); // NOI18N
        exitLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent evt) {
                exitLabelMouseMoved(evt);
            }
        });
        exitLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                exitLabelMouseClicked(evt);
            }
            
            @Override
            public void mouseExited(MouseEvent evt) {
                exitLabelMouseExited(evt);
            }
            
            @Override
            public void mousePressed(MouseEvent evt) {
                exitLabelMousePressed(evt);
            }
        });

        jLabel2.setFont(new Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setForeground(new Color(255, 255, 255));
        jLabel2.setText("Stems Entertainment");

        exitLabel1.setIcon(new ImageIcon(getClass().getResource("/com/images/minimize.png"))); // NOI18N
        exitLabel1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exitLabel1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                exitLabel1MouseClicked(evt);
            }
        });

        GroupLayout jPanel13Layout = new GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
                jPanel13Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel13Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(exitLabel1)
                                .addGap(12, 12, 12)
                                .addComponent(exitLabel)
                                .addGap(15, 15, 15))
        );
        jPanel13Layout.setVerticalGroup(
                jPanel13Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel13Layout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addGroup(jPanel13Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(exitLabel1)
                                        .addComponent(jLabel2)
                                        .addComponent(exitLabel))
                                .addGap(12, 12, 12))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(sidePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel13, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(welcomePanel, GroupLayout.PREFERRED_SIZE, 628, GroupLayout.PREFERRED_SIZE)))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel13, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, 0)
                                .addComponent(welcomePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                        .addComponent(sidePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>

    /**
     * Main method begins execution
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        setLookAndFeel("Windows");

        /* Create and display the form */
        EventQueue.invokeLater(() -> {
            new MenuFrame().setVisible(true);
        });
    }

    // Variables declaration - do not modify
    private JLabel addMovie;
    private JPanel addSelector;
    private JComboBox ageCombo2;
    private JLabel ageLabel;
    private JLabel amountLabel;
    private JButton bookingButton;
    private JPanel bookingSelector;
    private JLabel bookingsLabel;
    private JPanel bookingsPanel;
    private JTable bookingsTable;
    private JComboBox comboAmount;
    private JComboBox comboMovie;
    private JTextField contactField;
    private JLabel contactLabel;
    private JLabel contactlab;
    private JPanel content4;
    private JButton continueButton;
    private JLabel cover;
    private JTextField coverField2;
    private JTextField custField;
    private JLabel custLabel;
    private JLabel custName;
    private JLabel datelabel;
    private JLabel desc;
    private JButton displayButton;
    private JLabel displayLabel;
    private JPanel displayMoviesPanel;
    private JLabel durLabel;
    private JLabel duration;
    private JLabel exitLabel;
    private JLabel exitLabel1;
    private JLabel genre;
    private JComboBox genreCombo2;
    private JLabel genreLabel;
    private JTextField hourField2;
    private JTextField idField;
    private JLabel image5;
    private JLabel imageLabel;
    private JPanel innerMainPanel;
    private JPanel innerMoviePanel;
    private JPanel innerpane3;
    private JLabel jLabel10;
    private JLabel jLabel11;
    private JLabel jLabel12;
    private JLabel jLabel13;
    private JLabel jLabel14;
    private JLabel jLabel15;
    private JLabel jLabel2;
    private JLabel jLabel22;
    private JLabel jLabel23;
    private JLabel jLabel3;
    private JLabel jLabel5;
    private JLabel jLabel6;
    private JLabel jLabel7;
    private JLabel jLabel8;
    private JLabel jLabel9;
    private JPanel jPanel1;
    private JPanel jPanel10;
    private JPanel jPanel13;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private JPanel jPanel4;
    private JPanel jPanel5;
    private JPanel jPanel6;
    private JPanel jPanel7;
    private JPanel jPanel8;
    private JPanel jPanel9;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane11;
    private JScrollPane jScrollPane2;
    private JScrollPane jScrollPane3;
    private JScrollPane jScrollPane4;
    private JScrollPane jScrollPane5;
    private JScrollPane jScrollPane6;
    private JScrollPane jScrollPane7;
    private JScrollPane jScrollPane8;
    private JComboBox langCombo2;
    private static JComboBox langComboBox;
    private JLabel langLabel;
    private JTable loginJTable;
    private JLabel mainImageLabel;
    private JTextField minField2;
    private JTextField movieField2;
    private JLabel movieLabel;
    private JPanel moviePanel;
    private JPanel movieSelector;
    private JTable moviesTable;
    private JLabel movietitle;
    private JButton nextButton;
    private JPanel panelAdd;
    private JPanel panelBooking;
    private JPanel panelMovies;
    private JPanel panelPopular;
    private JPanel panelPurchase;
    private JPanel panelSettings;
    private JPasswordField passwordField;
    private JTextField phoneNoField;
    private JPanel popularPanel;
    private JPanel popularSelector;
    private JButton previousButton;
    private JTextField priceField2;
    private JLabel priceLabel;
    private JLabel pricelab;
    private JProgressBar progress;
    private JPanel purcPanel;
    private JLabel purchaseLabel;
    private JPanel purchasePanel;
    private JPanel purchaseSelector;
    private JLabel rating;
    private com.toedter.calendar.JDateChooser releaseDate2;
    private JTabbedPane reportsPane;
    private JButton saveButton;
    private JTable sessionTable;
    private JLabel settingsLabel;
    private JPanel settingsPanel;
    private JPanel settingsSelector;
    private JTabbedPane settingsTabbedPane;
    private JPanel sidePanel;
    private JLabel statsLabel;
    private JButton submitBtn;
    private JTextArea textArea2;
    private JLabel title;
    private JLabel titleId;
    private JLabel titleName;
    private JLabel titlePassword;
    private JLabel titlePhone;
    private JButton uploadBtn;
    private JTextField usernameField;
    private JLabel usernameLabel;
    private JPanel welcomePanel;
    // End of variables declaration
}