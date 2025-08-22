package com.mycompany.movietrailerapp.listeners;

import com.mycompany.movietrailerapp.utils.DatabaseInitializer;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Application lifecycle listener that initializes the database on startup
 */
@WebListener
public class DatabaseInitializationListener implements ServletContextListener {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseInitializationListener.class.getName());
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("Initializing MovieTrailer application...");
        
        try {
            // Test database connection
            DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
            if (!connectionManager.testConnection()) {
                LOGGER.severe("Failed to connect to database. Please check your XAMPP MySQL server.");
                return;
            }
            
            LOGGER.info("Database connection established successfully");
            
            // Initialize database tables
            if (DatabaseInitializer.initializeDatabase()) {
                LOGGER.info("Database initialization completed successfully");
                
                // Verify all tables exist
                if (DatabaseInitializer.verifyTables()) {
                    LOGGER.info("All required database tables verified");
                } else {
                    LOGGER.warning("Some database tables may be missing");
                }
                
            } else {
                LOGGER.severe("Database initialization failed");
            }
            
            // Log database status
            LOGGER.info(DatabaseInitializer.getDatabaseStatus());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during application initialization", e);
        }
        
        LOGGER.info("MovieTrailer application initialization completed");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("Shutting down MovieTrailer application...");
        
        try {
            // Shutdown database connection pool
            DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
            connectionManager.shutdown();
            LOGGER.info("Database connection pool shutdown completed");
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during application shutdown", e);
        }
        
        LOGGER.info("MovieTrailer application shutdown completed");
    }
}