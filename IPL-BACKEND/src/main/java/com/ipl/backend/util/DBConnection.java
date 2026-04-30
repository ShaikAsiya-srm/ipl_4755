package com.ipl.backend.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Persistent H2 file database. Path is relative to the backend working directory.
    private static final String URL = "jdbc:h2:file:./data/ipldb;DB_CLOSE_ON_EXIT=FALSE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 JDBC Driver not found. Make sure it's in the classpath.", e);
        }
    }
}
