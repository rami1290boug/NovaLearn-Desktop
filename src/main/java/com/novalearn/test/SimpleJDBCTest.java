package com.novalearn.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleJDBCTest {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3307/novalearn?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String password = "";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT 1");
            if (rs.next()) {
                System.out.println("JDBC Connection Successful! Result: " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
