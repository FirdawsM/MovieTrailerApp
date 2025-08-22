package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;

@WebServlet("/debug-play")
public class DebugPlayTrailerServlet extends HttpServlet {
    
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
            out.println("<title>Debug Play Trailer</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println("button { padding: 10px 20px; margin: 10px; background: #e50914; color: white; border: none; border-radius: 5px; cursor: pointer; }");
            out.println(".success { color: green; }");
            out.println(".error { color: red; }");
            out.println(".info { color: blue; }");
            out.println("pre { background: #f5f5f5; padding: 15px; border-radius: 5px; overflow-x: auto; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>🎬 Debug Play Trailer Functionality</h1>");
            
            // Get session info
            HttpSession session = request.getSession();
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                userId = 1;
                session.setAttribute("userId", userId);
            }
            
            out.println("<div class='info'>");
            out.println("<h2>Session Information:</h2>");
            out.println("<p><strong>User ID:</strong> " + userId + "</p>");
            out.println("<p><strong>Session ID:</strong> " + session.getId() + "</p>");
            out.println("</div>");
            
            // Check if we have movies in database
            int movieCount = getMovieCount();
            out.println("<div class='" + (movieCount > 0 ? "success" : "error") + "'>");
            out.println("<h2>Movies in Database:</h2>");
            out.println("<p><strong>Count:</strong> " + movieCount + "</p>");
            if (movieCount == 0) {
                out.println("<p>⚠️ No movies found! You need to visit the dashboard first to load movies from TMDB.</p>");
            }
            out.println("</div>");
            
            // Get a sample movie for testing
            if (movieCount > 0) {
                JSONObject sampleMovie = getSampleMovie();
                if (sampleMovie != null) {
                    out.println("<div class='info'>");
                    out.println("<h2>Sample Movie for Testing:</h2>");
                    out.println("<p><strong>ID:</strong> " + sampleMovie.getInt("id") + "</p>");
                    out.println("<p><strong>Title:</strong> " + sampleMovie.getString("title") + "</p>");
                    out.println("</div>");
                    
                    // Test buttons
                    out.println("<h2>Test Actions:</h2>");
                    out.println("<button onclick='testPlayTrailer(" + sampleMovie.getInt("id") + ", \"" + escapeJs(sampleMovie.getString("title")) + "\")'>🎯 Test Play Trailer</button>");
                    out.println("<button onclick='testHistoryDirect(" + sampleMovie.getInt("id") + ", \"" + escapeJs(sampleMovie.getString("title")) + "\")'>📊 Test History Direct</button>");
                    out.println("<button onclick='checkHistory()'>🔍 Check History</button>");
                }
            }
            
            out.println("<div id='result' style='margin-top: 20px;'></div>");
            
            // JavaScript for testing
            out.println("<script>");
            
            // Test play trailer (same as dashboard)
            out.println("function testPlayTrailer(movieId, title) {");
            out.println("  document.getElementById('result').innerHTML = '<p>Testing playTrailer function...</p>';");
            out.println("  ");
            out.println("  // This is the exact same code as in dashboard");
            out.println("  fetch('history', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=add&movieId=' + movieId + '&title=' + encodeURIComponent(title) + '&posterPath=/test.jpg'");
            out.println("  })");
            out.println("  .then(response => {");
            out.println("    console.log('Response status:', response.status);");
            out.println("    console.log('Response headers:', response.headers);");
            out.println("    return response.text();");
            out.println("  })");
            out.println("  .then(text => {");
            out.println("    console.log('Raw response:', text);");
            out.println("    try {");
            out.println("      const data = JSON.parse(text);");
            out.println("      console.log('Parsed response:', data);");
            out.println("      let resultHtml = '<h3>Play Trailer Test Result:</h3>';");
            out.println("      if (data.success) {");
            out.println("        resultHtml += '<p class=\"success\">✅ SUCCESS: ' + data.message + '</p>';");
            out.println("      } else {");
            out.println("        resultHtml += '<p class=\"error\">❌ FAILED: ' + data.message + '</p>';");
            out.println("      }");
            out.println("      resultHtml += '<h4>Full Response:</h4><pre>' + JSON.stringify(data, null, 2) + '</pre>';");
            out.println("      document.getElementById('result').innerHTML = resultHtml;");
            out.println("    } catch (e) {");
            out.println("      document.getElementById('result').innerHTML = '<p class=\"error\">❌ Invalid JSON response: ' + text + '</p>';");
            out.println("    }");
            out.println("  })");
            out.println("  .catch(error => {");
            out.println("    console.error('Fetch error:', error);");
            out.println("    document.getElementById('result').innerHTML = '<p class=\"error\">❌ Network Error: ' + error + '</p>';");
            out.println("  });");
            out.println("}");
            
            // Test history endpoint directly
            out.println("function testHistoryDirect(movieId, title) {");
            out.println("  document.getElementById('result').innerHTML = '<p>Testing history endpoint directly...</p>';");
            out.println("  ");
            out.println("  fetch('debug-play', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=test&movieId=' + movieId + '&title=' + encodeURIComponent(title)");
            out.println("  })");
            out.println("  .then(response => response.json())");
            out.println("  .then(data => {");
            out.println("    let resultHtml = '<h3>Direct History Test Result:</h3>';");
            out.println("    if (data.success) {");
            out.println("      resultHtml += '<p class=\"success\">✅ SUCCESS: ' + data.message + '</p>';");
            out.println("    } else {");
            out.println("      resultHtml += '<p class=\"error\">❌ FAILED: ' + data.message + '</p>';");
            out.println("    }");
            out.println("    resultHtml += '<pre>' + JSON.stringify(data, null, 2) + '</pre>';");
            out.println("    document.getElementById('result').innerHTML = resultHtml;");
            out.println("  })");
            out.println("  .catch(error => {");
            out.println("    document.getElementById('result').innerHTML = '<p class=\"error\">❌ Error: ' + error + '</p>';");
            out.println("  });");
            out.println("}");
            
            // Check history
            out.println("function checkHistory() {");
            out.println("  window.open('test-history', '_blank');");
            out.println("}");
            
            out.println("</script>");
            
            out.println("<hr>");
            out.println("<h3>📋 Instructions:</h3>");
            out.println("<ol>");
            out.println("<li><strong>Test Play Trailer</strong> - Tests the exact same code as dashboard</li>");
            out.println("<li><strong>Test History Direct</strong> - Tests direct database insert</li>");
            out.println("<li><strong>Check History</strong> - Opens detailed database view</li>");
            out.println("<li><strong>Check browser console</strong> - Look for detailed logs</li>");
            out.println("</ol>");
            
            out.println("</body>");
            out.println("</html>");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        String action = request.getParameter("action");
        JSONObject jsonResponse = new JSONObject();
        
        if ("test".equals(action)) {
            // Test direct database insert
            HttpSession session = request.getSession();
            Integer userId = (Integer) session.getAttribute("userId");
            if (userId == null) {
                userId = 1;
                session.setAttribute("userId", userId);
            }
            
            String movieIdStr = request.getParameter("movieId");
            String title = request.getParameter("title");
            
            try {
                int movieId = Integer.parseInt(movieIdStr);
                boolean success = testDirectInsert(userId, movieId, title);
                
                jsonResponse.put("success", success);
                jsonResponse.put("message", success ? "Direct insert successful" : "Direct insert failed");
                jsonResponse.put("userId", userId);
                jsonResponse.put("movieId", movieId);
                jsonResponse.put("title", title);
                
            } catch (Exception e) {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Error: " + e.getMessage());
                jsonResponse.put("error", e.getClass().getSimpleName());
            }
        }
        
        response.getWriter().print(jsonResponse.toString());
    }
    
    private int getMovieCount() {
        String query = "SELECT COUNT(*) FROM movies";
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting movie count: " + e.getMessage());
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return 0;
    }
    
    private JSONObject getSampleMovie() {
        String query = "SELECT id, title FROM movies LIMIT 1";
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    JSONObject movie = new JSONObject();
                    movie.put("id", rs.getInt("id"));
                    movie.put("title", rs.getString("title"));
                    return movie;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting sample movie: " + e.getMessage());
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return null;
    }
    
    private boolean testDirectInsert(Integer userId, Integer movieId, String title) {
        String insertQuery = "INSERT INTO user_history (user_id, movie_id, movie_title, poster_path, watched_at) VALUES (?, ?, ?, ?, NOW())";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, movieId);
                stmt.setString(3, title);
                stmt.setString(4, "/test.jpg");
                
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Direct insert result: " + rowsAffected + " rows affected");
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Direct insert error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    private String escapeJs(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("'", "\\'")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r");
    }
}