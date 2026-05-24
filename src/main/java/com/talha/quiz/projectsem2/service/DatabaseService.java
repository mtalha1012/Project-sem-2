package com.talha.quiz.projectsem2.service;

import com.talha.quiz.projectsem2.util.LoadProperties;

import java.sql.*;
import java.util.Objects;
import java.util.Properties;

public class DatabaseService {
    // Attributes
    private static final ClassLoader classLoader = DatabaseService.class.getClassLoader();
    private static final Properties DB_PROPERTIES = LoadProperties.loadProperties(
            "db.properties", classLoader);
    private static final Properties USER_QUERIES = LoadProperties.loadProperties(
            "user-queries.properties", classLoader);
    private static final Properties GRAPH_QUERIES = LoadProperties.loadProperties(
            "graph-queries.properties", classLoader);
    private static final Properties NODE_QUERIES = LoadProperties.loadProperties(
            "node-queries.properties", classLoader);
    private static final Properties EDGE_QUERIES = LoadProperties.loadProperties(
            "edge-queries.properties", classLoader);
    private static final Properties GRAPH_PAIR_QUERIES = LoadProperties.loadProperties(
            "graph-pair-queries.properties", classLoader);

    private Connection connection;
    private static DatabaseService instance;

    // Constructor
    private DatabaseService() {
        try {
            openConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to open database connection", e);
        }
        ensureMigrations();
    }

    public static DatabaseService getInstance() {
        return Objects.requireNonNullElseGet(instance, () ->
                instance = new DatabaseService());
    }

    public Connection getConnection() {
        return connection;
    }

    private void openConnection() throws SQLException {
        connection = DriverManager.getConnection(
                DB_PROPERTIES.getProperty("db.url"),
                DB_PROPERTIES.getProperty("db.user"),
                DB_PROPERTIES.getProperty("db.password"));
    }

    private void ensureMigrations() {
        try (Statement st = connection.createStatement()) {
            // graphs.user_id was added after the initial table; add if absent
            ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'graphs' AND COLUMN_NAME = 'user_id'");
            rs.next();
            boolean columnMissing = rs.getInt(1) == 0;
            rs.close();
            if (columnMissing) {
                st.executeUpdate(
                    "ALTER TABLE graphs " +
                    "ADD COLUMN user_id INT, " +
                    "ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE");
            }

            // graph_pairs table for saving paired graphs per user
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS graph_pairs (" +
                "  id INT AUTO_INCREMENT PRIMARY KEY," +
                "  name VARCHAR(255) NOT NULL," +
                "  user_id INT," +
                "  graph_a_id INT," +
                "  graph_b_id INT," +
                "  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (graph_a_id) REFERENCES graphs(id) ON DELETE CASCADE," +
                "  FOREIGN KEY (graph_b_id) REFERENCES graphs(id) ON DELETE CASCADE" +
                ")");

        } catch (SQLException e) {
            throw new RuntimeException("Schema migration failed", e);
        }
    }

    public void closeConnection() throws SQLException {
        connection.close();
    }

    private String getQuery(Properties queries, String key) {
        String query = queries.getProperty(key);
        if (query == null) throw new RuntimeException("Query not found: " + key);
        return query;
    }

    public String getUserQuery(String key) { return getQuery(USER_QUERIES, key); }
    public String getGraphQuery(String key) { return getQuery(GRAPH_QUERIES, key); }
    public String getNodeQuery(String key) { return getQuery(NODE_QUERIES, key); }
    public String getEdgeQuery(String key) { return getQuery(EDGE_QUERIES, key); }
    public String getGraphPairQuery(String key) { return getQuery(GRAPH_PAIR_QUERIES, key); }
}
