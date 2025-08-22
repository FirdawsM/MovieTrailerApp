package com.mycompany.movietrailerapp.servlets;

import com.mycompany.movietrailerapp.utils.DatabaseInitializer;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet for checking database status and initializing tables if needed
 * Access via: /database-status
 */
@WebServlet("/database-status")
public class DatabaseStatusServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseStatusServlet.class.getName());
    private DatabaseConnectionManager connectionManager;
    
    @Override
    public void init() throws ServletException {
        super.init();
        connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Database Status - MovieTrailer App</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; background-color: #f5f5f5; }");
            out.println(".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
            out.println("h1 { color: #333; border-bottom: 2px solid #e50914; padding-bottom: 10px; }");
            out.println(".status { padding: 15px; margin: 10px 0; border-radius: 5px; }");
            out.println(".success { background-color: #d4edda; border: 1px solid #c3e6cb; color: #155724; }");
            out.println(".error { background-color: #f8d7da; border: 1px solid #f5c6cb; color: #721c24; }");
            out.println(".warning { background-color: #fff3cd; border: 1px solid #ffeaa7; color: #856404; }");
            out.println(".info { background-color: #d1ecf1; border: 1px solid #bee5eb; color: #0c5460; }");
            out.println("pre { background: #f8f9fa; padding: 15px; border-radius: 5px; overflow-x: auto; }");
            out.println(".btn { display: inline-block; padding: 10px 20px; margin: 10px 5px; text-decoration: none; border-radius: 5px; font-weight: bold; }");
            out.println(".btn-primary { background-color: #e50914; color: white; }");
            out.println(".btn-secondary { background-color: #6c757d; color: white; }");
            out.println("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
            out.println("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }");
            out.println("th { background-color: #f8f9fa; font-weight: bold; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<div class='container'>");
            out.println("<h1>🎬 MovieTrailer App - Database Status</h1>");
            
            // Test database connection
            boolean connectionOk = testDatabaseConnection(out);
            
            if (connectionOk) {
                // Check tables
                checkDatabaseTables(out);
                
                // Show table counts
                showTableCounts(out);
                
                // Show connection pool stats
                showConnectionPoolStats(out);
            }
            
            // Action buttons
            out.println("<div style='margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;'>");
            out.println("<h3>Actions</h3>");
            out.println("<a href='?action=init' class='btn btn-primary'>Initialize Database Tables</a>");
            out.println("<a href='?action=verify' class='btn btn-secondary'>Verify Tables</a>");
            out.println("<a href='dashboard' class='btn btn-secondary'>Back to Dashboard</a>");
            out.println("</div>");
            
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("init".equals(action)) {
            boolean success = DatabaseInitializer.initializeDatabase();
            response.setContentType("application/json");
            try (PrintWriter out = response.getWriter()) {
                out.println("{\"success\": " + success + ", \"message\": \"" + 
                           (success ? "Database initialized successfully" : "Database initialization failed") + "\"}");
            }
        } else {
            doGet(request, response);
        }
    }
    
    private boolean testDatabaseConnection(PrintWriter out) {
        out.println("<h2>🔌 Database Connection Test</h2>");
        
        try {
            boolean connected = connectionManager.testConnection();
            
            if (connected) {
                out.println("<div class='status success'>");
                out.println("✅ <strong>Database Connection: SUCCESS</strong><br>");
                out.println("Successfully connected to MySQL database");
                out.println("</div>");
                return true;
            } else {
                out.println("<div class='status error'>");
                out.println("❌ <strong>Database Connection: FAILED</strong><br>");
                out.println("Unable to connect to MySQL database. Please check:");
                out.println("<ul>");
                out.println("<li>XAMPP MySQL server is running</li>");
                out.println("<li>Database 'movietrailerdb' exists</li>");
                out.println("<li>Connection settings in DatabaseConnectionManager</li>");
                out.println("</ul>");
                out.println("</div>");
                return false;
            }
            
        } catch (Exception e) {
            out.println("<div class='status error'>");
            out.println("❌ <strong>Database Connection: ERROR</strong><br>");
            out.println("Exception: " + e.getMessage());
            out.println("</div>");
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
    
    private void checkDatabaseTables(PrintWriter out) {
        out.println("<h2>📋 Database Tables Status</h2>");
        
        String[] requiredTables = {"users", "movies", "user_favorites", "user_history"};
        boolean allTablesExist = true;
        
        out.println("<table>");
        out.println("<tr><th>Table Name</th><th>Status</th><th>Description</th></tr>");
        
        for (String tableName : requiredTables) {
            boolean exists = checkTableExists(tableName);
            allTablesExist = allTablesExist && exists;
            
            out.println("<tr>");
            out.println("<td><strong>" + tableName + "</strong></td>");
            
            if (exists) {
                out.println("<td><span style='color: green;'>✅ EXISTS</span></td>");
            } else {
                out.println("<td><span style='color: red;'>❌ MISSING</span></td>");
            }
            
            out.println("<td>" + getTableDescription(tableName) + "</td>");
            out.println("</tr>");
        }
        
        out.println("</table>");
        
        if (allTablesExist) {
            out.println("<div class='status success'>");
            out.println("✅ <strong>All required tables exist</strong>");
            out.println("</div>");
        } else {
            out.println("<div class='status warning'>");
            out.println("⚠️ <strong>Some tables are missing</strong><br>");
            out.println("Click 'Initialize Database Tables' to create missing tables.");
            out.println("</div>");
        }
    }
    
    private boolean checkTableExists(String tableName) {
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (Statement stmt = connection.createStatement()) {
                stmt.executeQuery("SELECT 1 FROM " + tableName + " LIMIT 1");
                return true;
            }
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    private String getTableDescription(String tableName) {
        switch (tableName) {
            case "users": return "User accounts and authentication";
            case "movies": return "Movie information and metadata";
            case "user_favorites": return "User's favorite movies";
            case "user_history": return "User's watch history";
            default: return "Unknown table";
        }
    }
    
    private void showTableCounts(PrintWriter out) {
        out.println("<h2>📊 Table Statistics</h2>");
        
        String[] tables = {"users", "movies", "user_favorites", "user_history"};
        
        out.println("<table>");
        out.println("<tr><th>Table</th><th>Record Count</th></tr>");
        
        for (String table : tables) {
            int count = getTableCount(table);
            out.println("<tr>");
            out.println("<td><strong>" + table + "</strong></td>");
            out.println("<td>" + (count >= 0 ? count : "N/A") + "</td>");
            out.println("</tr>");
        }
        
        out.println("</table>");
    }
    
    private int getTableCount(String tableName) {
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting count for table: " + tableName, e);
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return -1;
    }
    
    private void showConnectionPoolStats(PrintWriter out) {
        out.println("<h2>🏊 Connection Pool Status</h2>");
        
        out.println("<div class='status info'>");
        out.println("<strong>Pool Statistics:</strong><br>");
        out.println("<pre>" + connectionManager.getPoolStats() + "</pre>");
        out.println("</div>");
    }
}