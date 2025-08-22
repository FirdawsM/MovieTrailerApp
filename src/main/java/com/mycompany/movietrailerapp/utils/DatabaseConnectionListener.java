package com.mycompany.movietrailerapp.utils;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Connection Listener for XAMPP MySQL
 * Initializes and manages database connection pool
 */
@WebListener
public class DatabaseConnectionListener implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionListener.class.getName());
    private DatabaseConnectionManager connectionManager;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            LOGGER.info("Initializing database connection pool...");
            
            // Initialize connection manager
            connectionManager = DatabaseConnectionManager.getInstance();
            
            // Test the connection
            if (connectionManager.testConnection()) {
                LOGGER.info("Database connection pool initialized successfully");
                LOGGER.info(connectionManager.getPoolStats());
                
                // Store connection manager in servlet context
                sce.getServletContext().setAttribute("connectionManager", connectionManager);
                
            } else {
                LOGGER.severe("Database connection test failed. Please check XAMPP MySQL service and database configuration.");
                // Don't throw exception, let the app start but log the error
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection pool", e);
            // Store the error in context for debugging
            sce.getServletContext().setAttribute("dbError", e.getMessage());
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down database connection pool...");
        
        try {
            if (connectionManager != null) {
                connectionManager.shutdown();
                LOGGER.info("Database connection pool shutdown completed");
            }
            
            // Clean up servlet context attributes
            sce.getServletContext().removeAttribute("connectionManager");
            sce.getServletContext().removeAttribute("dbError");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during database connection pool shutdown", e);
        }
        
        // Additional MySQL driver cleanup
        try {
            // Deregister JDBC drivers
            java.sql.DriverManager.getDrivers().asIterator().forEachRemaining(driver -> {
                try {
                    java.sql.DriverManager.deregisterDriver(driver);
                    LOGGER.info("Deregistered JDBC driver: " + driver.getClass().getName());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error deregistering JDBC driver: " + driver.getClass().getName(), e);
                }
            });
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during JDBC driver cleanup", e);
        }
    }
}