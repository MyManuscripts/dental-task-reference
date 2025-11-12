package org.example.dao;
import java.lang.Class;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;


public class DatabaseConnection {
    private static final String URL = "jdbc:sqlanywhere:Server=d4w;DBN=d4w;UID=dba;PWD=sql";
    //    - Server: d4w (Server name)
    //    - DBN: d4w (Database name)
    //    - UID: dba (User ID)
    //    - PWD: sql (Password)


    static {
        try {
            Class.forName("sap.jdbc4.sqlanywhere.IDriver");
        }catch (ClassNotFoundException e) {
            throw new RuntimeException("SQL Anywhere JDBC driver not found", e);
        }
    }

    public static Connection getConnection () throws SQLException {
        return DriverManager.getConnection(URL);
    }

}
