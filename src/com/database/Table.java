package com.database;

import files.FileProcessor;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Table 
{
    private static ResultSet results;
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final int STEP = 0; 
 
    static
    {
        final FileProcessor file  = FileProcessor.createFile("preference");
        file.loadFile(); 
        
//        USERNAME = file.getProperties().get("dbusername").toString();
//        PASSWORD = file.getProperties().get("dbpassword").toString();
//        STEP = Integer.parseInt(file.getProperties().get("step").toString());
    }
    
    /**
     * Needs reevaluation (code refactoring) to remove redundancy
     * 
     * @see com.database.Scripts#query(java.lang.String) 
     */
    @Deprecated
    private static boolean multipleScripts(String scripts)
    {
        final FileProcessor file  = FileProcessor.createFile("preference");
        file.loadFile();  
        
        PreparedStatement tables;
        
        try {
            tables = DatabaseConfiguration.getConnection().configConnection(USERNAME, PASSWORD, STEP)
                    .prepareStatement(scripts);
            tables.execute();
            return true;
        }
        catch (SQLException sqlErr) {
            Scripts.databaseStatusError(sqlErr);
            return false;
        }
    }
    
    public static boolean createTables()
    {   
        /* This is a trivial approach for storing complex queries */
        final String[] TABLE_CREATION_SCRIPTS = new String[]
        {
            "CREATE TABLE IF NOT EXISTS userreg ("
                + "REG_ID INT NOT NULL PRIMARY KEY, USERNAME VARCHAR(25) NOT NULL, "
                + "ID_NUMBER CHAR(13) NOT NULL, PHONE_NUMBER VARCHAR(25) NOT NULL, "
                + "GENDER CHAR NOT NULL, COMPANY VARCHAR(30) NOT NULL, DATE_CREATED "
                + "DATE NOT NULL, CONSTRAINT userreg_ID_NUMBER_uindex UNIQUE "
                + "(ID_NUMBER));",
            
            "CREATE TABLE IF NOT EXISTS userlogin ("
                + "ID INT NOT NULL PRIMARY KEY, PASSWORD VARCHAR(200) NOT NULL, "
                + "USER_ID INT NULL, CONSTRAINT USERLOGIN_PASSWORD_uindex UNIQUE "
                + "(PASSWORD), CONSTRAINT USER_HAS FOREIGN KEY (USER_ID) REFERENCES "
                + "userreg (REG_ID) ON DELETE CASCADE);",
            
            "CREATE TABLE IF NOT EXISTS accstatus ("
                + "ID INT AUTO_INCREMENT PRIMARY KEY, DATE DATE, TIME TIME NOT NULL, "
                + "LOGIN_ID INT NOT NULL, CONSTRAINT LOGINSESSION_SESSION_ID_uindex "
                + "UNIQUE (ID), CONSTRAINT HASID FOREIGN KEY (LOGIN_ID) REFERENCES "
                + "userlogin (ID) ON DELETE CASCADE);",
            
            "CREATE TABLE IF NOT EXISTS movies ("
                + "MOVIE_ID INT AUTO_INCREMENT PRIMARY KEY, TITLE VARCHAR(50) "
                + "NOT NULL, LANG VARCHAR(15) NOT NULL, DATE_RELEASED DATE "
                + "NOT NULL, GENRE VARCHAR(50) NOT NULL, COVER LONGBLOB NULL, "
                + "DURATION VARCHAR(9) NOT NULL, AGE VARCHAR(5) NOT NULL, "
                + "PRICE DECIMAL(5, 2) NOT NULL, MOVIE_DESCR VARCHAR(300) DEFAULT "
                + "'No Description provided' NULL, EMP_ID INT NULL, CONSTRAINT "
                + "movies_TITLE_uindex UNIQUE (TITLE), CONSTRAINT ADDEDBY FOREIGN "
                + "KEY(EMP_ID) REFERENCES userlogin (ID) ON DELETE CASCADE);",
            
            "CREATE TABLE IF NOT EXISTS tickets ("
                + "TICKET_NUMBER  INT AUTO_INCREMENT PRIMARY KEY, CUST_NAME "
                + "VARCHAR(30) NOT NULL, CONTACT_NUMBER VARCHAR(13) NOT NULL, "
                + "AMOUNT DECIMAL(8, 2) NOT NULL, MOVIE_ID INT NULL, PURCH_DATE "
                + "DATE NOT NULL, PURCH_TIME TIME NOT NULL, NUMBER_OF_TICKETS "
                + "int(11) NOT NULL, CONSTRAINT ISFOR FOREIGN KEY (MOVIE_ID) "
                + "REFERENCES movies (MOVIE_ID) ON DELETE CASCADE);",
            
            "CREATE TABLE IF NOT EXISTS bookings ("
                + "ID INT AUTO_INCREMENT PRIMARY KEY, TIME_BOOKED VARCHAR(20) "
                + "NOT NULL, TICKET_ID INT NULL, CONSTRAINT HASA FOREIGN KEY "
                + "(TICKET_ID) REFERENCES tickets (TICKET_NUMBER) ON DELETE "
                + "CASCADE);"
        };

        for (String script : TABLE_CREATION_SCRIPTS) { 
            multipleScripts(script);
        }
        return true;
    }
    
    public static int count()
    {
        ArrayList<String> tableList = new ArrayList<>();
        PreparedStatement selectTables;
  
        try
        {
            selectTables = DatabaseConfiguration.getConnection()
                    .configConnection(USERNAME, PASSWORD, STEP)
                    .prepareStatement("SHOW TABLES");
            
            results = selectTables.executeQuery();
            
            while (results.next()) {
                tableList.add(results.getObject(1).toString()); 
            }
        }
        catch(SQLException sqlError) {
            Scripts.databaseStatusError(sqlError);
        }
        return tableList.size();
    }
}