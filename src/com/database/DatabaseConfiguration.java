package com.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfiguration
{    
    private static Connection connectToAddress;
    private static DatabaseConfiguration databaseInstance;
    private static boolean isConnected;
    private static final String DB_NAME = "stems_movies";
    private final String DEFAULT_USER = "root";

    private DatabaseConfiguration() { }

    public static DatabaseConfiguration getConnection() {
        if (databaseInstance == null) synchronized (DatabaseConfiguration.class) {
            databaseInstance = new DatabaseConfiguration();
        }
        return databaseInstance;
    }

    public Connection configConnection() {
        return configConnection(DEFAULT_USER, "", 0);
    }
    
    public Connection configConnection(String username, String password, int step)
    {
        String url = "jdbc:mysql://localhost";
        final String CREATE_DATABASE = String.format("CREATE SCHEMA IF NOT EXISTS %s", DB_NAME);

        if (step == 0)
        {
            try
            {
                if (connectToAddress == null) {
                    establishConnection(url, DEFAULT_USER, "", CREATE_DATABASE);
                    isConnected = true;
                }
                return connectToAddress;
            } 
            catch (SQLException sqlErr) {
                System.out.format("%s%n", sqlErr);
            }
        }
        else
        {
            password = (password == null) ? "" : password;
            try {
                establishConnection(url, username, password, CREATE_DATABASE);
                isConnected = true;
            }
            catch (SQLException sqlError2) {
                isConnected = false;
                System.out.format("%s", sqlError2);
            }
        }
        return connectToAddress;
    }

    /**
     * @param url      Specifies a database url of the form
     * @param username Specifies the database user on whose behalf the
     *                 connection is being made
     * @param password Specifies the database password
     * @param sql      Specifies the sql creation script
     * @throws SQLException If a database access error occurs
     */
    private void establishConnection(String url, String username, String password, String sql) throws SQLException
    {
        /* Test the MYSQL database connection using default values */
        connectToAddress = DriverManager.getConnection(url, username, password);
        connectToAddress.prepareStatement(sql).execute();

        /* Reconfigure the database @code DriverManager */
        url = String.format("%s/%s", url, DB_NAME);
        connectToAddress = DriverManager.getConnection(url, username, password);
    }

    public static boolean isConnected() {
        return isConnected;
    }
    /**
     * Deallocate resources that an object has used by manually closing the
     * {@code Connection} and {@code PreparedStatement}, which are considered
     * limited resources; before closing, the method verifies if the database
     * connection was established by checking {@code isConnected} status value.
     */
    public static void closeConnection()
    {
        if (isConnected || (connectToAddress != null)) {
            try {
                connectToAddress.close();
            }
            catch (SQLException sqlException) {
                System.out.format("%s", sqlException);
            }
            finally {
                isConnected = false;
            }
        }
    }
}