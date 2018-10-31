package com.codect.connections;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlConnection {
    public static SqlConnection getInstsance() {
        return new SqlConnection();
    }

    public Connection getConnectionFor(String connectionUrl, String driverClass) throws ClassNotFoundException, SQLException {
        Class.forName(driverClass);
        return DriverManager.getConnection(connectionUrl);

    }
}
