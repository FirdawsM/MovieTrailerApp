package com.mycompany.movietrailerapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Connection Manager with Connection Pooling for XAMPP MySQL
 */
public class DatabaseConnectionManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionManager.class.getName());
    
    // XAMPP default MySQL configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/movietrailerdb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USERNAME = "root";  // XAMPP default username
    private static final String DB_PASSWORD = "";      // XAMPP default password (empty)
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
    
    // Connection pool settings
    private static final int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    private static DatabaseConnectionManager instance;
    private BlockingQueue<Connection> connectionPool;
    private BlockingQueue<Connection> usedConnections;
    private volatile boolean isShutdown = false;
    
    private DatabaseConnectionManager() {
        initializeConnectionPool();
    }
    
    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConnectionManager();
        }
        return instance;
    }
    
    /**
     * Initialize the connection pool
     */
    private void initializeConnectionPool() {
        try {
            // Load MySQL driver
            Class.forName(DB_DRIVER);
            
            connectionPool = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
            usedConnections = new LinkedBlockingQueue<>(MAX_POOL_SIZE);
            
            // Create initial connections
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                connectionPool.offer(createConnection());
            }
            
            LOGGER.info("Database connection pool initialized with " + INITIAL_POOL_SIZE + " connections");
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
            throw new RuntimeException("Failed to load MySQL driver", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize connection pool", e);
            throw new RuntimeException("Failed to initialize connection pool", e);
        }
    }
    
    /**
     * Create a new database connection
     */
    private Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        connection.setAutoCommit(true);
        return connection;
    }
    
    /**
     * Get a connection from the pool
     */
    public Connection getConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("Connection pool has been shutdown");
        }
        
        Connection connection = connectionPool.poll();
        
        if (connection == null) {
            // No available connections, create a new one if under max limit
            if (usedConnections.size() < MAX_POOL_SIZE) {
                connection = createConnection();
                LOGGER.info("Created new connection. Total connections: " + (usedConnections.size() + 1));
            } else {
                throw new SQLException("Maximum connection pool size reached");
            }
        }
        
        // Check if connection is still valid
        if (connection != null && !isConnectionValid(connection)) {
            connection = createConnection();
            LOGGER.info("Created new connection to replace invalid connection");
        }
        
        usedConnections.offer(connection);
        return connection;
    }
    
    /**
     * Return a connection to the pool
     */
    public void releaseConnection(Connection connection) {
        if (connection == null || isShutdown) {
            return;
        }
        
        usedConnections.remove(connection);
        
        if (isConnectionValid(connection)) {
            connectionPool.offer(connection);
        } else {
            try {
                connection.close();
                // Create a new connection to maintain pool size
                connectionPool.offer(createConnection());
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to close invalid connection or create replacement", e);
            }
        }
    }
    
    /**
     * Check if connection is valid
     */
    private boolean isConnectionValid(Connection connection) {
        try {
            return connection != null && 
                   !connection.isClosed() && 
                   connection.isValid(CONNECTION_TIMEOUT / 1000);
        } catch (SQLException e) {
            return false;
        }
    }
    
    /**
     * Test database connectivity - FIXED to properly release connection
     */
    public boolean testConnection() {
        Connection connection = null;
        try {
            connection = getConnection();
            return connection != null && connection.isValid(5);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database connection test failed", e);
            return false;
        } finally {
            if (connection != null) {
                releaseConnection(connection);
            }
        }
    }
    
    /**
     * Get pool statistics
     */
    public String getPoolStats() {
        return String.format("Pool Stats - Available: %d, Used: %d, Total: %d", 
                connectionPool.size(), 
                usedConnections.size(),
                connectionPool.size() + usedConnections.size());
    }
    
    /**
     * Reset connection pool - ADDED to fix connection leaks
     */
    public synchronized void resetPool() {
        LOGGER.info("Resetting connection pool...");
        
        // Close all used connections
        while (!usedConnections.isEmpty()) {
            Connection connection = usedConnections.poll();
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing used connection during reset", e);
            }
        }
        
        // Close all pooled connections
        while (!connectionPool.isEmpty()) {
            Connection connection = connectionPool.poll();
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing pooled connection during reset", e);
            }
        }
        
        // Reinitialize the pool
        try {
            for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
                connectionPool.offer(createConnection());
            }
            LOGGER.info("Connection pool reset successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to reset connection pool", e);
        }
    }
    
    /**
     * Shutdown the connection pool
     */
    public synchronized void shutdown() {
        if (isShutdown) {
            return;
        }
        
        isShutdown = true;
        
        // Close all connections in the pool
        while (!connectionPool.isEmpty()) {
            Connection connection = connectionPool.poll();
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing pooled connection", e);
            }
        }
        
        // Close all used connections
        while (!usedConnections.isEmpty()) {
            Connection connection = usedConnections.poll();
            try {
                if (connection != null && !connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing used connection", e);
            }
        }
        
        // Clean up MySQL AbandonedConnectionCleanupThread
        try {
            Class<?> cleanupThreadClass = Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            java.lang.reflect.Method shutdownMethod = cleanupThreadClass.getMethod("checkedShutdown");
            shutdownMethod.invoke(null);
            LOGGER.info("MySQL AbandonedConnectionCleanupThread shutdown successfully");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to shutdown MySQL AbandonedConnectionCleanupThread", e);
        }
        
        LOGGER.info("Database connection pool shutdown complete");
    }
}