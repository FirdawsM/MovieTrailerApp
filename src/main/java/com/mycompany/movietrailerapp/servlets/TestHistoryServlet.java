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
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;

@WebServlet("/test-history")
public class TestHistoryServlet extends HttpServlet {
    
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
            out.println("<head><title>Test History</title></head>");
            out.println("<body>");
            out.println("<h1>Test History Functionality</h1>");
            
            // Test 1: Check table structure
            out.println("<h2>1. Table Structure:</h2>");
            checkTableStructure(out);
            
            // Test 2: Test database connection
            out.println("<h2>2. Database Connection:</h2>");
            testConnection(out);
            
            // Test 3: Test manual insert
            out.println("<h2>3. Manual Insert Test:</h2>");
            testManualInsert(out);
            
            // Test 4: Show current data
            out.println("<h2>4. Current Data:</h2>");
            showCurrentData(out);
            
            // Test 5: Test the actual history endpoint
            out.println("<h2>5. Test History Endpoint:</h2>");
            out.println("<button onclick='testHistoryEndpoint()'>Test Add to History</button>");
            out.println("<div id='result'></div>");
            
            // JavaScript for testing
            out.println("<script>");
            out.println("function testHistoryEndpoint() {");
            out.println("  fetch('/MovieTrailerApp/history', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=add&movieId=12345&title=Test Movie&posterPath=/test.jpg'");
            out.println("  })");
            out.println("  .then(response => response.json())");
            out.println("  .then(data => {");
            out.println("    document.getElementById('result').innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';");
            out.println("  })");
            out.println("  .catch(error => {");
            out.println("    document.getElementById('result').innerHTML = '<p style=\"color:red\">Error: ' + error + '</p>';");
            out.println("  });");
            out.println("}");
            out.println("</script>");
            
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        // Test the exact same logic as HistoryServlet
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            userId = 1; // Default user
            session.setAttribute("userId", userId);
        }
        
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Test insert with detailed logging
            boolean success = testInsert(userId, 99999, "Test Movie from POST", "/test-post.jpg");
            jsonResponse.put("success", success);
            jsonResponse.put("message", success ? "Insert successful" : "Insert failed");
            jsonResponse.put("userId", userId);
            
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Error: " + e.getMessage());
            jsonResponse.put("error", e.getClass().getSimpleName());
        }
        
        response.getWriter().print(jsonResponse.toString());
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
            out.println("<p style='color:red'>Error: " + e.getMessage() + "</p>");
        }
    }
    
    private void testConnection(PrintWriter out) {
        try {
            boolean connected = connectionManager.testConnection();
            out.println("<p style='color:" + (connected ? "green" : "red") + "'>");
            out.println("Connection: " + (connected ? "SUCCESS" : "FAILED"));
            out.println("</p>");
        } catch (Exception e) {
            out.println("<p style='color:red'>Connection Error: " + e.getMessage() + "</p>");
        }
    }
    
    private void testManualInsert(PrintWriter out) {
        try {
            boolean success = testInsert(1, 88888, "Manual Test Movie", "/manual-test.jpg");
            out.println("<p style='color:" + (success ? "green" : "red") + "'>");
            out.println("Manual Insert: " + (success ? "SUCCESS" : "FAILED"));
            out.println("</p>");
        } catch (Exception e) {
            out.println("<p style='color:red'>Manual Insert Error: " + e.getMessage() + "</p>");
        }
    }
    
    private boolean testInsert(Integer userId, Integer movieId, String title, String posterPath) throws SQLException {
        String checkQuery = "SELECT id FROM user_history WHERE user_id = ? AND movie_id = ?";
        String insertQuery = "INSERT INTO user_history (user_id, movie_id, movie_title, poster_path, watched_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection()) {
            // Check if already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, movieId);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("Record already exists for user " + userId + ", movie " + movieId);
                        return true; // Already exists
                    }
                }
            }
            
            // Insert new record
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, movieId);
                insertStmt.setString(3, title);
                insertStmt.setString(4, posterPath);
                insertStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                
                int rowsAffected = insertStmt.executeUpdate();
                System.out.println("Insert result: " + rowsAffected + " rows affected");
                return rowsAffected > 0;
            }
        }
    }
    
    private void showCurrentData(PrintWriter out) {
        String query = "SELECT * FROM user_history ORDER BY id DESC LIMIT 10";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            out.println("<table border='1'>");
            out.println("<tr><th>ID</th><th>User ID</th><th>Movie ID</th><th>Title</th><th>Poster</th><th>Watched At</th></tr>");
            
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                out.println("<tr>");
                out.println("<td>" + rs.getInt("id") + "</td>");
                out.println("<td>" + rs.getInt("user_id") + "</td>");
                out.println("<td>" + rs.getInt("movie_id") + "</td>");
                out.println("<td>" + rs.getString("movie_title") + "</td>");
                out.println("<td>" + rs.getString("poster_path") + "</td>");
                out.println("<td>" + rs.getTimestamp("watched_at") + "</td>");
                out.println("</tr>");
            }
            
            if (!hasData) {
                out.println("<tr><td colspan='6'>No data found</td></tr>");
            }
            
            out.println("</table>");
            
        } catch (SQLException e) {
            out.println("<p style='color:red'>Error showing data: " + e.getMessage() + "</p>");
        }
    }
}