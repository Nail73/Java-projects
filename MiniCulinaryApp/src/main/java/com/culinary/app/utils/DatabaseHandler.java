package com.culinary.app.utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DatabaseHandler {
    private static final String URL = "jdbc:mysql://localhost:3306/culinary_app?useSSL=false&serverTimezone=UTC";
    private static final String USER = "";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void executeQuery(String query, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                // Преобразуем строки с запятыми в числа
                if (params[i] instanceof String) {
                    String str = (String) params[i];
                    if (str.contains(",")) {
                        params[i] = Double.parseDouble(str.replace(",", "."));
                    }
                }
                stmt.setObject(i + 1, params[i]);
            }
            stmt.execute();
        }
    }

    public static void executeQuery(String query, Consumer<ResultSet> resultConsumer, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                resultConsumer.accept(rs);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error executing query: " + query, e);
        }
    }

    public static class DatabaseException extends RuntimeException {
        public DatabaseException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static void executeInTransaction(Consumer<Connection> action) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                action.accept(conn);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new DatabaseException("Transaction failed", e);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error", e);
        }
    }

    public static double getProductCostFromAnalysis(String productCode) throws SQLException {
        String query = "SELECT total_cost_without_vat FROM total_cost_per_unit WHERE product_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total_cost_without_vat");
            }
        }
        return 0.0;
    }


    public static void updateProductCostInTransaction(String productCode) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                double cost = getProductCostFromAnalysis(productCode);
                executeQuery(String.valueOf(conn), "UPDATE products SET cost = ? WHERE code = ?", cost, productCode);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static List<Object[]> executeQuery(String sql,
                                              SQLFunction<ResultSet, List<Object[]>> processor, Object... params) throws SQLException {

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return processor.apply(rs);
            }
        }
    }

    public interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
