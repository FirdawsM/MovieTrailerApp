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

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_USER_ID = "1";
    
    private DatabaseConnectionManager connectionManager;
    
    @Override
    public void init() throws ServletException {
        super.init();
        connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        
        HttpSession session = request.getSession();
        Integer userId = getUserIdFromSession(session);
        
        String action = request.getParameter("action");
        String movieIdStr = request.getParameter("movieId");
        
        JSONObject jsonResponse = new JSONObject();
        
        try {
            if (movieIdStr == null || movieIdStr.isEmpty()) {
                sendErrorResponse(jsonResponse, "Movie ID is required");
                response.getWriter().print(jsonResponse.toString());
                return;
            }
            
            int movieId = Integer.parseInt(movieIdStr);
            
            switch (action) {
                case "add":
                    handleAddToHistory(request, userId, movieId, jsonResponse);
                    break;
                case "remove":
                    handleRemoveFromHistory(userId, movieId, jsonResponse);
                    break;
                case "clear":
                    handleClearHistory(userId, jsonResponse);
                    break;
                default:
                    sendErrorResponse(jsonResponse, "Invalid action");
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(jsonResponse, "Invalid movie ID format");
        } catch (Exception e) {
            logError("Server error occurred", e);
            sendErrorResponse(jsonResponse, "Server error occurred");
        }
        
        response.getWriter().print(jsonResponse.toString());
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        HttpSession session = request.getSession();
        Integer userId = getUserIdFromSession(session);
        
        try (PrintWriter out = response.getWriter()) {
            renderHistoryPage(out, userId);
        }
    }
    
    private Integer getUserIdFromSession(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        return userId != null ? userId : Integer.parseInt(DEFAULT_USER_ID);
    }
    
    private void handleAddToHistory(HttpServletRequest request, Integer userId, int movieId, JSONObject jsonResponse) 
            throws SQLException {
        String title = request.getParameter("title");
        String posterPath = request.getParameter("posterPath");
        
        if (title == null || title.isEmpty()) {
            sendErrorResponse(jsonResponse, "Title is required");
            return;
        }
        
        boolean success = addToHistory(userId, movieId, title, posterPath);
        jsonResponse.put("success", success);
        jsonResponse.put("message", success ? 
            "Movie added to watch history" : "Movie updated in watch history");
    }
    
    private void handleRemoveFromHistory(Integer userId, int movieId, JSONObject jsonResponse) 
            throws SQLException {
        boolean success = removeFromHistory(userId, movieId);
        jsonResponse.put("success", success);
        jsonResponse.put("message", success ? 
            "Movie removed from history" : "Error removing movie");
    }
    
    private void handleClearHistory(Integer userId, JSONObject jsonResponse) 
            throws SQLException {
        boolean success = clearHistory(userId);
        jsonResponse.put("success", success);
        jsonResponse.put("message", success ? 
            "History cleared successfully" : "Error clearing history");
    }
    
    private void sendErrorResponse(JSONObject jsonResponse, String message) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
    }
    
    private void logError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }
    
    private boolean addToHistory(Integer userId, Integer movieId, String title, String posterPath) throws SQLException {
        String checkQuery = "SELECT id FROM user_history WHERE user_id = ? AND movie_id = ?";
        String updateQuery = "UPDATE user_history SET watched_at = ?, movie_title = ?, poster_path = ? WHERE user_id = ? AND movie_id = ?";
        String insertQuery = "INSERT INTO user_history (user_id, movie_id, movie_title, poster_path, watched_at) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection()) {
            // Check if already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, movieId);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Update existing entry
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                            updateStmt.setString(2, title);
                            updateStmt.setString(3, posterPath);
                            updateStmt.setInt(4, userId);
                            updateStmt.setInt(5, movieId);
                            return updateStmt.executeUpdate() > 0;
                        }
                    }
                }
            }
            
            // Insert new history entry
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, movieId);
                insertStmt.setString(3, title);
                insertStmt.setString(4, posterPath);
                insertStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                
                return insertStmt.executeUpdate() > 0;
            }
        }
    }
    
    private boolean removeFromHistory(Integer userId, Integer movieId) throws SQLException {
        String deleteQuery = "DELETE FROM user_history WHERE user_id = ? AND movie_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private boolean clearHistory(Integer userId) throws SQLException {
        String deleteQuery = "DELETE FROM user_history WHERE user_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            
            stmt.setInt(1, userId);
            return stmt.executeUpdate() >= 0;
        }
    }
    
    private void renderHistoryPage(PrintWriter out, Integer userId) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("<meta charset='UTF-8'>");
        out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("<title>Watch History - MovieTrailer</title>");
        out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'>");
        out.println("<link rel='stylesheet' href='css/history.css'>"); // External CSS
        out.println("</head>");
        out.println("<body>");
        
        generateNavBar(out);
        
        out.println("<div class='main-content'>");
        out.println("<section class='section'>");
        out.println("<div class='section-header'>");
        out.println("<h2 class='section-title'><i class='fas fa-history'></i> Watch History</h2>");
        out.println("<button id='clearHistoryBtn' class='btn btn-secondary' onclick='clearAllHistory()'>");
        out.println("<i class='fas fa-trash'></i> Clear All History</button>");
        out.println("</div>");
        
        displayUserHistory(out, userId);
        
        out.println("</section>");
        out.println("</div>");
        
        out.println("<footer>");
        out.println("<p>&copy; 2023 MovieTrailer App. All rights reserved.</p>");
        out.println("</footer>");
        
        out.println("<div id='notification' class='notification'></div>");
        out.println("<script src='js/history.js'></script>"); // External JS
        out.println("</body>");
        out.println("</html>");
    }
    
    private void displayUserHistory(PrintWriter out, Integer userId) {
        String query = "SELECT movie_id, movie_title, poster_path, watched_at FROM user_history " +
                      "WHERE user_id = ? ORDER BY watched_at DESC";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                out.println("<div class='movie-grid'>");
                
                boolean hasMovies = false;
                
                while (rs.next()) {
                    hasMovies = true;
                    renderHistoryItem(out, rs);
                }
                
                if (!hasMovies) {
                    renderEmptyState(out);
                }
                
                out.println("</div>");
            }
            
        } catch (SQLException e) {
            logError("Error loading watch history", e);
            renderErrorMessage(out);
        }
    }
    
    private void renderHistoryItem(PrintWriter out, ResultSet rs) throws SQLException {
        int movieId = rs.getInt("movie_id");
        String title = rs.getString("movie_title");
        String posterPath = rs.getString("poster_path");
        Timestamp watchDate = rs.getTimestamp("watched_at");
        
        out.println("<div class='history-card'>");
        if (posterPath != null && !posterPath.isEmpty()) {
            out.println("<img src='https://image.tmdb.org/t/p/w500" + posterPath + 
                         "' alt='" + escapeHtml(title) + "' class='movie-poster'>");
        } else {
            out.println("<div class='no-poster'><span>No Image</span></div>");
        }
        out.println("<div class='movie-info'>");
        out.println("<h3 class='movie-title'>" + escapeHtml(title) + "</h3>");
        out.println("<p class='watch-date'>Watched: " + formatDate(watchDate) + "</p>");
        out.println("<div class='movie-actions'>");
        out.println("<button class='action-btn watch-again' onclick='watchAgain(" + movieId + 
                   ", \"" + escapeJs(title) + "\", \"" + escapeJs(posterPath) + "\")'>");
        out.println("<i class='fas fa-play'></i> Watch Again</button>");
        out.println("<button class='action-btn remove' onclick='removeFromHistory(" + movieId + ", this)'>");
        out.println("<i class='fas fa-trash'></i> Remove</button>");
        out.println("</div>");
        out.println("</div>");
        out.println("</div>");
    }
    
    private void renderEmptyState(PrintWriter out) {
        out.println("<div class='empty-state'>");
        out.println("<i class='fas fa-history' style='font-size: 4rem; color: #666; margin-bottom: 20px;'></i>");
        out.println("<h3>No watch history yet</h3>");
        out.println("<p>Movies you watch will appear here so you can easily find them again.</p>");
        out.println("<a href='dashboard' class='btn btn-primary' style='margin-top: 20px; display: inline-block; text-decoration: none;'>Browse Movies</a>");
        out.println("</div>");
    }
    
    private void renderErrorMessage(PrintWriter out) {
        out.println("<div class='error-message'>");
        out.println("<p>Error loading watch history. Please try again later.</p>");
        out.println("</div>");
    }
    
    private String formatDate(Timestamp timestamp) {
        if (timestamp == null) return "Unknown";
        
        long now = System.currentTimeMillis();
        long diff = now - timestamp.getTime();
        
        // Convert to minutes, hours, days
        long minutes = diff / (1000 * 60);
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        } else if (days < 7) {
            return days + " days ago";
        } else {
            return timestamp.toString().substring(0, 10);
        }
    }
    
    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&#39;");
    }
    
    private String escapeJs(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("'", "\\'")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r");
    }
    
    private void generateNavBar(PrintWriter out) {
        out.println("<nav class='navbar'>");
        out.println("  <div class='logo'>MOVIETRAILER</div>");
        out.println("  <div class='nav-links'>");
        out.println("    <a href='dashboard'><i class='fas fa-home'></i> Home</a>");
        out.println("    <a href='#'><i class='fas fa-tv'></i> TV Shows</a>");
        out.println("    <a href='#'><i class='fas fa-film'></i> Movies</a>");
        out.println("    <a href='#'><i class='fas fa-fire'></i> Trending</a>");
        out.println("    <a href='favorites'><i class='fas fa-heart'></i> My Favorites</a>");
        out.println("    <a href='history' style='color: #e50914;'><i class='fas fa-history'></i> History</a>");
        out.println("  </div>");
        out.println("  <img src='https://via.placeholder.com/40' alt='Profile' class='profile'>");
        out.println("</nav>");
    }
}