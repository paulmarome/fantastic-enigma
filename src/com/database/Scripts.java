package com.database;

import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import com.classes.Activity;
import com.classes.Booking;
import com.classes.Detector;
import com.classes.Movie;
import com.classes.Ticket;
import com.classes.User;
import files.FileProcessor;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;

public class Scripts
{
    private static int ticketNumber = 0; 
    
    private final FileProcessor file = FileProcessor.createFile("preference");
    private final DatabaseConfiguration configureDatabase;
    private PreparedStatement selectUser;
  
    public Scripts() {
        this.configureDatabase = DatabaseConfiguration.getConnection();
    }
      
    public static int getTicketNumber() {
        return ticketNumber;
    }
    
    private static int randomIdGenerator(int random)
    {
        try
        {
            SecureRandom firstRandomId = SecureRandom.getInstance("WINDOWS-PRNG");
            SecureRandom offset = SecureRandom.getInstance("WINDOWS-PRNG");
            return firstRandomId.nextInt(random) + offset.nextInt(90);
        } 
        catch (NoSuchAlgorithmException ex) {
            databaseStatusError(ex);
        }
        return -1;
    }
   
    private PreparedStatement query(String sql) throws SQLException
    {   
        file.loadFile();
        PreparedStatement statement;
        int step = Integer.parseInt(file.getProperties().get("step").toString());
        
        if (step == 1) {
            String username = file.getProperties().getProperty("dbusername");
            String password = file.getProperties().getProperty("dbpassword");
            password = (password == null) ? "" : password;          
            statement = configureDatabase.configConnection(username, password, 1).prepareStatement(sql);
        } 
        else {
            statement = configureDatabase.configConnection().prepareStatement(sql);
        }
        return statement;
    }
    
    public Map<String, String> getUserAccount()
    {
        HashMap<String, String> userTable = new HashMap<>();       
        ResultSet results = null;
        
        try
        {
            selectUser = query(
                    "SELECT USERNAME, PASSWORD " +
                    "FROM userlogin INNER JOIN userreg " +
                    "ON userlogin.USER_ID = userreg.REG_ID");
            
            results = selectUser.executeQuery();
            
            while (results.next()) {
                String password = results.getString("PASSWORD");
                String user = results.getString("USERNAME").toLowerCase();
                userTable.put(password, user);             
            }
        }
        catch (SQLException sqlError){
            databaseStatusError(sqlError);
        }
        return userTable;
    }
    
    public LinkedList<User> getUserAccount(String password)
    {
        LinkedList<User> user = new LinkedList<>();
        selectUser = null;
        ResultSet results = null;
        
        try
        {
            selectUser = query(
                    "SELECT USERNAME, PASSWORD, ID_NUMBER, " +
                    "PHONE_NUMBER FROM userreg " +
                    "INNER JOIN userlogin " +
                    "ON userreg.REG_ID = userlogin.USER_ID " +
                    "WHERE userlogin.PASSWORD = ?");
           
            selectUser.setString(1, password);
            results = selectUser.executeQuery();
            
            while(results.next()) {
                user.add(new User(results.getString("USERNAME"), results.getString("PASSWORD"),
                                  results.getString("ID_NUMBER"), results.getString("PHONE_NUMBER")));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return user;
    }
    
    public LinkedList<Activity> getLoginActivity(String password)
    {
        LinkedList<Activity> loginHistory = new LinkedList<>();
        PreparedStatement selectLoginActivity;   
        ResultSet results = null;
        
        try
        {
            selectLoginActivity = query(
                    "SELECT DATE, TIME FROM accstatus " +
                    "WHERE LOGIN_ID = (SELECT ID FROM userlogin " +
                    "WHERE PASSWORD = ?)");
            
            selectLoginActivity.setString(1, password);
            results = selectLoginActivity.executeQuery();
            
            while (results.next()) {
                loginHistory.add(new Activity(results.getDate("DATE").toString(), results.getTime("TIME").toString()));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return loginHistory;
    }
    
    public LinkedList<Ticket> getTicket()
    {
        PreparedStatement selectTicket;
        ResultSet results = null;
        LinkedList<Ticket> tickets = new LinkedList<>();
                
        try
        {
            selectTicket = query(
                    "SELECT TICKET_NUMBER, CUST_NAME, TITLE, PURCH_TIME, "
                  + "PURCH_DATE FROM tickets "
                  + "INNER JOIN movies "
                  + "ON TICKETS.MOVIE_ID = movies.MOVIE_ID");
            
            results = selectTicket.executeQuery();
            
            while (results.next()) {
                tickets.add(new Ticket(
                        results.getInt("TICKET_NUMBER"), results.getString("CUST_NAME"), results.getString("TITLE"),
                        results.getString("PURCH_TIME"), results.getString("PURCH_DATE")));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return tickets;
    }
    
    public int getMovieById(String title)
    {
        PreparedStatement selectMovieByTitle; 
        ResultSet results = null;
        int movieId = 0;
        
        try
        {
            selectMovieByTitle = query("SELECT MOVIE_ID FROM movies WHERE TITLE = ?");
            
            selectMovieByTitle.setString(1, title);
            results = selectMovieByTitle.executeQuery();
            
            while (results.next()) {
                movieId = results.getInt("MOVIE_ID");
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return movieId;
    }
    
    public LinkedList<Movie> getMovieList()
    {
        PreparedStatement selectMovie; 
        ResultSet results = null;
        LinkedList<Movie> movieList = new LinkedList<>();
        
        try
        {
            selectMovie = query(
                    "SELECT TITLE, LANG, DATE_RELEASED, COVER, "
                  + "DURATION, PRICE, MOVIE_DESCR "
                  + "FROM movies");
            
            results = selectMovie.executeQuery();
            
            while (results.next()) {
                movieList.add(new Movie(
                        results.getString("TITLE"), results.getString("LANG"), results.getString("DATE_RELEASED"),
                        results.getBlob("COVER"), results.getString("DURATION"), results.getDouble("PRICE"),
                        results.getString("MOVIE_DESCR")));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return movieList;
    }
 
    public int logSession(LocalDate date, LocalTime time, String password)
    {
        PreparedStatement insertLoginSession;        
        int count = 0;       
        
        try
        {
            insertLoginSession = query(
                    "INSERT INTO accstatus (DATE, TIME, LOGIN_ID) "
                  + "VALUES (?, ?, (SELECT ID FROM userlogin "
                  + "WHERE PASSWORD = ?))");
            
            insertLoginSession.setDate(1, Date.valueOf(date));
            insertLoginSession.setTime(2, Time.valueOf(time));
            insertLoginSession.setString(3, password);
            
            count = insertLoginSession.executeUpdate();
        }
        catch(SQLException sqlError) {
            sqlError.printStackTrace();
            databaseStatusError(sqlError);
        }
        return count;
    }
            
    public ArrayList<Detector> authenticate()
    {
        ArrayList<Detector> dataList = new ArrayList<>();
        ResultSet results = null;
        
        try 
        {
            selectUser = query(
                    "SELECT USERNAME, ID_NUMBER, PASSWORD FROM " 
                  + "userlogin INNER JOIN userreg ON " 
                  + "userlogin.USER_ID = userreg.REG_ID");
            
            results = selectUser.executeQuery();
            
            while(results.next()) {
                dataList.add(new Detector(results.getString("ID_NUMBER"), results.getString("PASSWORD")));
            }
        }
        catch(SQLException retrievalError) {
            databaseStatusError(retrievalError);
        }
        return dataList;
    }
    
    public LinkedList<String> getMovieByTitle()
    {    
        LinkedList<String> title = new LinkedList<>();
        PreparedStatement selectMovieByTitle;
        ResultSet results = null;
        
        try
        {
            selectMovieByTitle = query(
                    "SELECT TITLE FROM movies "
                  + "INNER JOIN tickets ON movies.MOVIE_ID = tickets.MOVIE_ID");
            
            results = selectMovieByTitle.executeQuery();
            
            while(results.next()) {
                title.add(results.getString("Title"));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return title;
    }
    
    public String getMovieByGenre(String title)
    {    
        String genre = "";
        PreparedStatement selectMovieByGenre;
        ResultSet results = null;
        
        try
        {
            selectMovieByGenre = query(
                    "SELECT GENRE FROM movies "
                  + "INNER JOIN tickets ON movies.MOVIE_ID = tickets.MOVIE_ID "
                  + "WHERE TITLE = ? LIMIT 1");
            
            selectMovieByGenre.setString(1, title);
            results = selectMovieByGenre.executeQuery();
            
            while(results.next()) {
                genre = results.getString("GENRE");
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return genre;
    }
    
    public String getMovieByDate(String title)
    {    
        String date = "";
        PreparedStatement selectMovieByDate;
        ResultSet results = null;
        
        try
        {
            selectMovieByDate = query("SELECT DATE_RELEASED FROM movies WHERE TITLE = ?");
            
            selectMovieByDate.setString(1, title);
            results = selectMovieByDate.executeQuery();
            
            while(results.next()) {
                date = results.getString("DATE_RELEASED");
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return date;
    }
    
    public Blob getMovieCover(String title)
    {
        PreparedStatement selectMovieByCover;
        Blob image =  null;
        ResultSet results = null;
        
        try
        {
            selectMovieByCover = query("SELECT COVER FROM movies WHERE TITLE = ?");
            
            selectMovieByCover.setString(1, title);
            results = selectMovieByCover.executeQuery();
            
            while (results.next()) {
                image = results.getBlob("COVER");
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return image;
    }
    
    public LinkedList<Movie> getMovies() 
    {
        LinkedList<Movie> movieList = new LinkedList<>();
        PreparedStatement selectMovies;
        ResultSet results = null;
        
        try
        {
            selectMovies = query("SELECT Title, PRICE FROM movies");
            results = selectMovies.executeQuery();
            
            while(results.next()) {
                movieList.add(new Movie(results.getString("Title"), results.getDouble("PRICE")));
            }
        }
        catch(SQLException sqlError) {
            databaseStatusError(sqlError);
        }
        return movieList;
    }
    
    public LinkedList<Booking> getBookings()
    {
        PreparedStatement selectBookings;
        LinkedList<Booking> bookings = null;
        ResultSet results = null;
        
        try
        {
            bookings = new LinkedList<>();
            selectBookings = query(
                    "SELECT MOVIE_ID, CUST_NAME, CONTACT_NUMBER, "
                  + "NUMBER_OF_TICKETS "
                  + "FROM tickets");
            
            results = selectBookings.executeQuery();
        
            while (results.next()) {
                bookings.add(new Booking(results.getInt("MOVIE_ID"), results.getString("CUST_NAME"),
                        results.getString("CONTACT_NUMBER"), results.getInt("NUMBER_OF_TICKETS")));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return bookings;
    }
  
    public LinkedList<String> getMostBookedSession()
    {
        PreparedStatement selectBookingTime;
        LinkedList<String> timeBooked = null;
        ResultSet results = null;
        
        try
        {
            timeBooked = new LinkedList<>();
            selectBookingTime = query("SELECT TIME_BOOKED FROM bookings");
            results = selectBookingTime.executeQuery();
        
            while (results.next()) {
                timeBooked.add(results.getString("TIME_BOOKED"));
            }
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return timeBooked;
    }
    
    public int addUser(String username, String idNumber, String phoneNumber,
            String password, char gender, LocalDate dateCreated)
    {
        PreparedStatement insertNewUser;
        int numberOfRows = 0;

        try 
        {
            String genderVal = String.valueOf(gender);
            int newUserId = randomIdGenerator(950);
            int userLoginId = randomIdGenerator(320);

            insertNewUser = query(
                    "INSERT INTO userreg (REG_ID, USERNAME," +
                    "ID_NUMBER, PHONE_NUMBER, GENDER, COMPANY," +
                    "DATE_CREATED) VALUES(?, ?, ?, ?, ?, ?, ?)");
            
            insertNewUser.setInt(1, newUserId);
            insertNewUser.setString(2, username);
            insertNewUser.setString(3, idNumber);
            insertNewUser.setString(4, phoneNumber);
            insertNewUser.setString(5, genderVal);
            insertNewUser.setString(6, "Stems Entertainment Group");
            insertNewUser.setDate(7, Date.valueOf(dateCreated));

            numberOfRows = insertNewUser.executeUpdate();
            insertLoginData(userLoginId, password, newUserId);
        } 
        catch (SQLException sqlException) {
            databaseStatusError(sqlException);
        }
        return numberOfRows;
    } 
    
    private void insertLoginData(int loginId, String password, int userId) 
    {
        PreparedStatement insertUserLogin;
        
        try 
        {
            insertUserLogin = query("INSERT INTO userlogin (ID,PASSWORD,USER_ID) VALUES(?, ?, ?)");
            
            insertUserLogin.setInt(1, loginId);
            insertUserLogin.setString(2, password);
            insertUserLogin.setInt(3, userId);

            int count = insertUserLogin.executeUpdate();
            System.out.format("%s%n%s %d%n", "Insertion was successful.", "Row count =", count);
        } 
        catch (SQLException sqlError) {
            databaseStatusError(sqlError);
        }
    }
        
    public int addMovie(String title, String lang, String genre, LocalDate date,
            InputStream image, String dur, String age, double price, 
            String movieDescr, String password)
    {
        PreparedStatement insertMovie;
        int row = 0;

        try 
        {
            insertMovie = query(
                    "INSERT INTO movies (TITLE, LANG, GENRE,"
                  + " DATE_RELEASED, COVER, DURATION, AGE, PRICE, "
                  + "MOVIE_DESCR, EMP_ID) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, "
                  + "(SELECT ID FROM userlogin "
                  + "WHERE PASSWORD = ?))");
            
            insertMovie.setString(1, title);
            insertMovie.setString(2, lang);
            insertMovie.setString(3, genre);
            insertMovie.setDate(4, Date.valueOf(date));
            insertMovie.setBlob(5, image);
            insertMovie.setString(6, dur);
            insertMovie.setString(7, age);
            insertMovie.setDouble(8, price);
            insertMovie.setString(9, movieDescr);
            insertMovie.setString(10, password);

            row = insertMovie.executeUpdate();
            System.out.format("%s %d%n", "Status update for Query =", row);
        } 
        catch (SQLException sqlError) {
            System.out.println(sqlError);
            databaseStatusError(sqlError);
        }
        return row;
    }

    public int addTicket(String custName, String contact, double amount, String id) 
    {
        PreparedStatement insertTicket;
        int count = 0;
        
        try 
        {
            insertTicket = query(
                  "INSERT INTO tickets (TICKET_NUMBER, CUST_NAME, "
                + "CONTACT_NUMBER, AMOUNT, MOVIE_ID, PURCH_DATE, "
                + "PURCH_TIME, NUMBER_OF_TICKETS) "
                + "VALUES(?, ?, ?, ?, (SELECT MOVIE_ID FROM movies "
                + "WHERE TITLE = ?), ?, ?, ?)");
            
            ticketNumber = randomIdGenerator(988) + new SecureRandom().nextInt(221);
            
            insertTicket.setInt(1, ticketNumber);
            insertTicket.setString(2, custName);
            insertTicket.setString(3, contact);
            insertTicket.setDouble(4, amount);
            insertTicket.setString(5, id);
            insertTicket.setDate(6, Date.valueOf(LocalDate.now()));
            insertTicket.setTime(7, java.sql.Time.valueOf(LocalTime.now()));
            insertTicket.setInt(8, 1);

            count = insertTicket.executeUpdate();
            System.out.format("%n%s %d%n", "Rows affected =", count);
        } 
        catch (SQLException sqlError) {
            databaseStatusError(sqlError);
        }
        return count;
    }
    
    public int addBooking(String time, String name, String contact, int movieId)
    {
        PreparedStatement insertBooking;
        int count = 0;
        
        try
        {
            insertBooking = query(
                    "INSERT INTO bookings (ID, TIME_BOOKED, TICKET_ID) "
                  + "VALUES (?, ?, (SELECT tickets.TICKET_NUMBER FROM tickets "
                  + "WHERE tickets.CUST_NAME = ? AND tickets.CONTACT_NUMBER = ? "
                  + "AND tickets.MOVIE_ID = ?))");
            
            insertBooking.setInt(1, randomIdGenerator(1232) + new SecureRandom().nextInt(201));
            insertBooking.setString(2, time);
            insertBooking.setString(3, name);
            insertBooking.setString(4, contact);
            insertBooking.setInt(5, movieId);
            
            count = insertBooking.executeUpdate();
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return count;
    }
    
    public int updateUserAccount(String username, String phoneNumber, String password)
    {
        PreparedStatement updateUserAccount;
        int rowCount = 0;
        
        try
        {
            updateUserAccount = query(
                    "UPDATE userreg SET USERNAME = ?, PHONE_NUMBER = ? "
                  + "WHERE REG_ID = "
                  + "(SELECT userlogin.USER_ID FROM userlogin WHERE PASSWORD = ?) ");
            
            updateUserAccount.setString(1, username);
            updateUserAccount.setString(2, phoneNumber);
            updateUserAccount.setString(3, password);
            
            rowCount = updateUserAccount.executeUpdate();
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return rowCount;
    }
  
    public int updateTicketCount(int count, String name, String contactNumber, int id)
    {
        PreparedStatement updateBookings;
        int rows = 0;
        
        try
        {
            updateBookings = query(
                    "UPDATE tickets SET NUMBER_OF_TICKETS = ?"
                  + " WHERE CUST_NAME = ? AND CONTACT_NUMBER = ? "
                  + "AND MOVIE_ID = ?");
            
            updateBookings.setInt(1, count);
            updateBookings.setString(2, name);
            updateBookings.setString(3, contactNumber);
            updateBookings.setInt(4, id);
            
            rows = updateBookings.executeUpdate();
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return rows;
    }
   
    public int deleteLoginActivity(String password)
    {
        PreparedStatement deleteLoginActivity;
        int count = 0;
        
        try
        {
            deleteLoginActivity = query(
                    "DELETE FROM accstatus "
                  + "WHERE LOGIN_ID = "
                  + "(SELECT ID FROM userlogin WHERE PASSWORD = ?)");
            
            deleteLoginActivity.setString(1, password);
            count = deleteLoginActivity.executeUpdate();
        }
        catch (SQLException sqlErr) {
            databaseStatusError(sqlErr);
        }
        return count;
    }
    
    /**
     * Locates the class, line, and method if an exception occurs 
     * during execution of an SQL query.
     *
     * @param exception Error thrown by the compiler.
     */
    public static void databaseStatusError(Exception exception )
    {
        StackTraceElement[] errorStack = exception.getStackTrace();

        System.out.printf("%-59s%-43s%-14s%-14s%n", "CLASS", "FILE", "LINE", "METHOD");

        for (StackTraceElement error : errorStack) {
            System.out.printf("%-59s%-43s%-14s%-14s%n", error.getClassName(),
                    error.getFileName(), error.getLineNumber(), error.getMethodName());
        }
    }
}