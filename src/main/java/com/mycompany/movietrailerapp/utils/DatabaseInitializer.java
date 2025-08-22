package com.mycompany.movietrailerapp.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Initializer utility to create required tables if they don't exist
 */
public class DatabaseInitializer {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializer.class.getName());
    private static DatabaseConnectionManager connectionManager;
    
    static {
        connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    /**
     * Initialize all required database tables
     */
    public static boolean initializeDatabase() {
        try {
            createUsersTable();
            createMoviesTable();
            createUserFavoritesTable();
            createUserHistoryTable();
            
            LOGGER.info("Database initialization completed successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database", e);
            return false;
        }
    }
    
    /**
     * Create users table with correct primary key name
     */
    private static void createUsersTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                email VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        
        executeSQL(sql, "users table");
    }
    
    /**
     * Create movies table
     */
    private static void createMoviesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS movies (
                id INT PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                description TEXT,
                trailer_url VARCHAR(500),
                poster_url VARCHAR(500),
                release_date DATE,
                genre VARCHAR(100),
                rating DECIMAL(3,1) DEFAULT 0.0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
            """;
        
        executeSQL(sql, "movies table");
    }
    
    /**
     * Create user_favorites table
     */
    private static void createUserFavoritesTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_favorites (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                movie_id INT NOT NULL,
                movie_title VARCHAR(255),
                poster_path VARCHAR(500),
                added_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                UNIQUE KEY unique_user_movie_favorite (user_id, movie_id),
                INDEX idx_user_favorites_user_id (user_id),
                INDEX idx_user_favorites_movie_id (movie_id)
            )
            """;
        
        executeSQL(sql, "user_favorites table");
    }
    
    /**
     * Create user_history table (CORRECTED to use watched_at)
     */
    private static void createUserHistoryTable() throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS user_history (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                movie_id INT NOT NULL,
                movie_title VARCHAR(255),
                poster_path VARCHAR(500),
                watched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
                UNIQUE KEY unique_user_movie_history (user_id, movie_id),
                INDEX idx_user_history_user_id (user_id),
                INDEX idx_user_history_movie_id (movie_id),
                INDEX idx_user_history_watched_at (watched_at)
            )
            """;
        
        executeSQL(sql, "user_history table");
    }
    
    /**
     * Execute SQL statement
     */
    private static void executeSQL(String sql, String description) throws SQLException {
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(sql);
                LOGGER.info("Successfully created/verified " + description);
            }
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Check if all required tables exist
     */
    public static boolean verifyTables() {
        String[] requiredTables = {"users", "movies", "user_favorites", "user_history"};
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            
            for (String tableName : requiredTables) {
                String sql = "SELECT 1 FROM " + tableName + " LIMIT 1";
                try (Statement stmt = connection.createStatement()) {
                    stmt.executeQuery(sql);
                    LOGGER.info("Table verified: " + tableName);
                } catch (SQLException e) {
                    LOGGER.warning("Table missing or inaccessible: " + tableName);
                    return false;
                }
            }
            
            LOGGER.info("All required tables verified successfully");
            return true;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error verifying tables", e);
            return false;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Get database status information
     */
    public static String getDatabaseStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Database Status:\n");
        
        try {
            if (connectionManager.testConnection()) {
                status.append("✓ Database connection: OK\n");
                
                if (verifyTables()) {
                    status.append("✓ Required tables: OK\n");
                } else {
                    status.append("✗ Required tables: MISSING\n");
                }
                
                status.append(connectionManager.getPoolStats());
                
            } else {
                status.append("✗ Database connection: FAILED\n");
            }
            
        } catch (Exception e) {
            status.append("✗ Database status check failed: ").append(e.getMessage());
        }
        
        return status.toString();
    }
}