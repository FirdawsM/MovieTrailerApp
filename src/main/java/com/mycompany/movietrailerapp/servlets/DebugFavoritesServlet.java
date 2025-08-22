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

@WebServlet("/debug-favorites")
public class DebugFavoritesServlet extends HttpServlet {
    
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
            out.println("<title>Debug Favorites</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println("button { padding: 10px 20px; margin: 10px; background: #e50914; color: white; border: none; border-radius: 5px; cursor: pointer; }");
            out.println(".success { color: green; }");
            out.println(".error { color: red; }");
            out.println(".info { color: blue; }");
            out.println("pre { background: #f5f5f5; padding: 15px; border-radius: 5px; overflow-x: auto; }");
            out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            out.println("th { background-color: #f2f2f2; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>❤️ Debug Favorites Functionality</h1>");
            
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
            out.println("</div>");
            
            // Check table structure
            out.println("<h2>📋 Table Structure:</h2>");
            checkTableStructure(out);
            
            // Get a sample movie for testing
            JSONObject sampleMovie = getSampleMovie();
            if (sampleMovie != null) {
                out.println("<div class='info'>");
                out.println("<h2>Sample Movie for Testing:</h2>");
                out.println("<p><strong>ID:</strong> " + sampleMovie.getInt("id") + "</p>");
                out.println("<p><strong>Title:</strong> " + sampleMovie.getString("title") + "</p>");
                out.println("</div>");
                
                // Test buttons
                out.println("<h2>Test Actions:</h2>");
                out.println("<button onclick='testAddFavorite(" + sampleMovie.getInt("id") + ", \"" + escapeJs(sampleMovie.getString("title")) + "\")'>❤️ Test Add to Favorites</button>");
                out.println("<button onclick='testRemoveFavorite(" + sampleMovie.getInt("id") + ")'>💔 Test Remove from Favorites</button>");
                out.println("<button onclick='testDirectInsert(" + sampleMovie.getInt("id") + ", \"" + escapeJs(sampleMovie.getString("title")) + "\")'>📊 Test Direct Insert</button>");
                out.println("<button onclick='checkFavorites()'>🔍 Check Favorites</button>");
            }
            
            // Show current favorites
            out.println("<h2>📋 Current Favorites:</h2>");
            showCurrentFavorites(out, userId);
            
            out.println("<div id='result' style='margin-top: 20px;'></div>");
            
            // JavaScript for testing
            out.println("<script>");
            
            // Test add to favorites
            out.println("function testAddFavorite(movieId, title) {");
            out.println("  document.getElementById('result').innerHTML = '<p>Testing add to favorites...</p>';");
            out.println("  ");
            out.println("  fetch('favorites', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=add&movieId=' + movieId + '&title=' + encodeURIComponent(title) + '&posterPath=/test.jpg'");
            out.println("  })");
            out.println("  .then(response => {");
            out.println("    console.log('Response status:', response.status);");
            out.println("    return response.text();");
            out.println("  })");
            out.println("  .then(text => {");
            out.println("    console.log('Raw response:', text);");
            out.println("    try {");
            out.println("      const data = JSON.parse(text);");
            out.println("      console.log('Parsed response:', data);");
            out.println("      let resultHtml = '<h3>Add to Favorites Test Result:</h3>';");
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
            
            // Test remove from favorites
            out.println("function testRemoveFavorite(movieId) {");
            out.println("  document.getElementById('result').innerHTML = '<p>Testing remove from favorites...</p>';");
            out.println("  ");
            out.println("  fetch('favorites', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=remove&movieId=' + movieId");
            out.println("  })");
            out.println("  .then(response => response.json())");
            out.println("  .then(data => {");
            out.println("    let resultHtml = '<h3>Remove from Favorites Test Result:</h3>';");
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
            
            // Test direct insert
            out.println("function testDirectInsert(movieId, title) {");
            out.println("  document.getElementById('result').innerHTML = '<p>Testing direct database insert...</p>';");
            out.println("  ");
            out.println("  fetch('debug-favorites', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=test&movieId=' + movieId + '&title=' + encodeURIComponent(title)");
            out.println("  })");
            out.println("  .then(response => response.json())");
            out.println("  .then(data => {");
            out.println("    let resultHtml = '<h3>Direct Insert Test Result:</h3>';");
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
            
            // Check favorites
            out.println("function checkFavorites() {");
            out.println("  location.reload();");
            out.println("}");
            
            out.println("</script>");
            
            out.println("<hr>");
            out.println("<h3>📋 Instructions:</h3>");
            out.println("<ol>");
            out.println("<li><strong>Check table structure</strong> - Should have movie_title, poster_path, added_date columns</li>");
            out.println("<li><strong>Test Add to Favorites</strong> - Tests the favorites endpoint</li>");
            out.println("<li><strong>Test Remove from Favorites</strong> - Tests removing favorites</li>");
            out.println("<li><strong>Test Direct Insert</strong> - Tests direct database insert</li>");
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
    
    private void checkTableStructure(PrintWriter out) {
        String query = "DESCRIBE user_favorites";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                out.println("<table>");
                out.println("<tr><th>Field</th><th>Type</th><th>Null</th><th>Key</th><th>Default</th><th>Extra</th></tr>");
                
                boolean hasMovieTitle = false;
                boolean hasPosterPath = false;
                boolean hasAddedDate = false;
                
                while (rs.next()) {
                    String field = rs.getString("Field");
                    out.println("<tr>");
                    out.println("<td>" + field + "</td>");
                    out.println("<td>" + rs.getString("Type") + "</td>");
                    out.println("<td>" + rs.getString("Null") + "</td>");
                    out.println("<td>" + rs.getString("Key") + "</td>");
                    out.println("<td>" + rs.getString("Default") + "</td>");
                    out.println("<td>" + rs.getString("Extra") + "</td>");
                    out.println("</tr>");
                    
                    if ("movie_title".equals(field)) hasMovieTitle = true;
                    if ("poster_path".equals(field)) hasPosterPath = true;
                    if ("added_date".equals(field)) hasAddedDate = true;
                }
                
                out.println("</table>");
                
                // Check for missing columns
                if (!hasMovieTitle || !hasPosterPath || !hasAddedDate) {
                    out.println("<div class='error'>");
                    out.println("<h3>⚠️ Missing Columns Detected:</h3>");
                    out.println("<ul>");
                    if (!hasMovieTitle) out.println("<li>❌ <strong>movie_title</strong> column is missing</li>");
                    if (!hasPosterPath) out.println("<li>❌ <strong>poster_path</strong> column is missing</li>");
                    if (!hasAddedDate) out.println("<li>❌ <strong>added_date</strong> column is missing (or named differently)</li>");
                    out.println("</ul>");
                    out.println("<p><strong>Solution:</strong> Run the SQL script to add missing columns:</p>");
                    out.println("<pre>ALTER TABLE user_favorites \nADD COLUMN movie_title VARCHAR(255) AFTER movie_id,\nADD COLUMN poster_path VARCHAR(500) AFTER movie_title;</pre>");
                    out.println("</div>");
                } else {
                    out.println("<div class='success'>");
                    out.println("<p>✅ All required columns are present</p>");
                    out.println("</div>");
                }
                
            }
        } catch (SQLException e) {
            out.println("<p class='error'>Error checking table structure: " + e.getMessage() + "</p>");
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
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
    
    private void showCurrentFavorites(PrintWriter out, Integer userId) {
        String query = "SELECT * FROM user_favorites WHERE user_id = ? ORDER BY id DESC LIMIT 10";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    
                    out.println("<table>");
                    out.println("<tr><th>ID</th><th>User ID</th><th>Movie ID</th><th>Movie Title</th><th>Poster Path</th><th>Added Date</th></tr>");
                    
                    boolean hasData = false;
                    while (rs.next()) {
                        hasData = true;
                        out.println("<tr>");
                        out.println("<td>" + rs.getInt("id") + "</td>");
                        out.println("<td>" + rs.getInt("user_id") + "</td>");
                        out.println("<td>" + rs.getInt("movie_id") + "</td>");
                        try {
                            out.println("<td>" + rs.getString("movie_title") + "</td>");
                        } catch (SQLException e) {
                            out.println("<td>N/A</td>");
                        }
                        try {
                            out.println("<td>" + rs.getString("poster_path") + "</td>");
                        } catch (SQLException e) {
                            out.println("<td>N/A</td>");
                        }
                        try {
                            out.println("<td>" + rs.getTimestamp("added_date") + "</td>");
                        } catch (SQLException e) {
                            try {
                                out.println("<td>" + rs.getTimestamp("added_at") + "</td>");
                            } catch (SQLException e2) {
                                out.println("<td>N/A</td>");
                            }
                        }
                        out.println("</tr>");
                    }
                    
                    if (!hasData) {
                        out.println("<tr><td colspan='6'>No favorites found</td></tr>");
                    }
                    
                    out.println("</table>");
                }
            }
        } catch (SQLException e) {
            out.println("<p class='error'>Error showing favorites: " + e.getMessage() + "</p>");
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    private boolean testDirectInsert(Integer userId, Integer movieId, String title) {
        String insertQuery = "INSERT INTO user_favorites (user_id, movie_id, movie_title, poster_path, added_date) VALUES (?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE added_date = NOW()";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(insertQuery)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, movieId);
                stmt.setString(3, title);
                stmt.setString(4, "/test.jpg");
                
                int rowsAffected = stmt.executeUpdate();
                System.out.println("Direct favorites insert result: " + rowsAffected + " rows affected");
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            System.err.println("Direct favorites insert error: " + e.getMessage());
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