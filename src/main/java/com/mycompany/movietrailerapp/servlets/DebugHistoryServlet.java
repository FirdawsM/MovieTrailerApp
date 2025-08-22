package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;

@WebServlet("/debug-history")
public class DebugHistoryServlet extends HttpServlet {
    
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
            out.println("<head><title>Debug History</title></head>");
            out.println("<body>");
            out.println("<h1>Debug History Table</h1>");
            
            // Check table structure
            out.println("<h2>Table Structure:</h2>");
            checkTableStructure(out);
            
            // Test insert
            out.println("<h2>Test Insert:</h2>");
            testInsert(out);
            
            // Show current data
            out.println("<h2>Current Data:</h2>");
            showCurrentData(out);
            
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    private void checkTableStructure(PrintWriter out) {
        String query = "DESCRIBE user_history";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            out.println("<table border='1'>");
            out.println("<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>");
            
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getString("Field") + "</td>");
                out.println("<td>" + rs.getString("Type") + "</td>");
                out.println("<td>" + rs.getString("Null") + "</td>");
                out.println("<td>" + rs.getString("Key") + "</td>");
                out.println("<td>" + rs.getString("Default") + "</td>");
                out.println("<td>" + rs.getString("Extra") + "</td>");
                out.println("</tr>");
            }
            
            out.println("</table>");
            
        } catch (SQLException e) {
            out.println("<p>Error: " + e.getMessage() + "</p>");
        }
    }
    
    private void testInsert(PrintWriter out) {
        // First try with watch_date
        String query1 = "INSERT INTO user_history (user_id, movie_id, movie_title, poster_path, watch_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query1)) {
            
            stmt.setInt(1, 1);
            stmt.setInt(2, 12345);
            stmt.setString(3, "Test Movie 1");
            stmt.setString(4, "/test.jpg");
            stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
            
            int result = stmt.executeUpdate();
            out.println("<p>Insert with watch_date: " + (result > 0 ? "SUCCESS" : "FAILED") + "</p>");
            
        } catch (SQLException e) {
            out.println("<p>Insert with watch_date FAILED: " + e.getMessage() + "</p>");
            
            // Try with watched_at
            String query2 = "INSERT INTO user_history (user_id, movie_id, movie_title, poster_path, watched_at) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = connectionManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query2)) {
                
                stmt.setInt(1, 1);
                stmt.setInt(2, 12346);
                stmt.setString(3, "Test Movie 2");
                stmt.setString(4, "/test2.jpg");
                stmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                
                int result = stmt.executeUpdate();
                out.println("<p>Insert with watched_at: " + (result > 0 ? "SUCCESS" : "FAILED") + "</p>");
                
            } catch (SQLException e2) {
                out.println("<p>Insert with watched_at FAILED: " + e2.getMessage() + "</p>");
            }
        }
    }
    
    private void showCurrentData(PrintWriter out) {
        String query = "SELECT * FROM user_history ORDER BY id DESC LIMIT 10";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>User ID</th><th>Movie ID</th><th>Title</th><th>Poster</th><th>Date</th></tr>");
            
            while (rs.next()) {
                out.println("<tr>");
                out.println("<td>" + rs.getInt("id") + "</td>");
                out.println("<td>" + rs.getInt("user_id") + "</td>");
                out.println("<td>" + rs.getInt("movie_id") + "</td>");
                out.println("<td>" + rs.getString("movie_title") + "</td>");
                out.println("<td>" + rs.getString("poster_path") + "</td>");
                
                // Try both column names
                try {
                    out.println("<td>" + rs.getTimestamp("watch_date") + "</td>");
                } catch (SQLException e) {
                    try {
                        out.println("<td>" + rs.getTimestamp("watched_at") + "</td>");
                    } catch (SQLException e2) {
                        out.println("<td>No date column found</td>");
                    }
                }
                
                out.println("</tr>");
            }
            
            out.println("</table>");
            
        } catch (SQLException e) {
            out.println("<p>Error showing data: " + e.getMessage() + "</p>");
        }
    }
}