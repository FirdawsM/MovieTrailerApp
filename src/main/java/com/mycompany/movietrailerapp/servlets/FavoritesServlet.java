package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;


public class FavoritesServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
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
        Integer userId = (Integer) session.getAttribute("userId");
        
        // Default to user 1 if not logged in (for demo purposes)
        if (userId == null) {
            userId = 1;
        }
        
        String action = request.getParameter("action");
        String movieIdStr = request.getParameter("movieId");
        String title = request.getParameter("title");
        String posterPath = request.getParameter("posterPath");
        
        JSONObject jsonResponse = new JSONObject();
        
        try {
            // Validate required parameters
            if (movieIdStr == null || movieIdStr.isEmpty()) {
                sendErrorResponse(jsonResponse, "Movie ID is required", HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(jsonResponse.toString());
                return;
            }
            
            if (action == null || action.isEmpty()) {
                sendErrorResponse(jsonResponse, "Action is required", HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(jsonResponse.toString());
                return;
            }
            
            int movieId = Integer.parseInt(movieIdStr);
            
            switch (action) {
                case "add":
                    if (title == null || title.isEmpty()) {
                        sendErrorResponse(jsonResponse, "Title is required", HttpServletResponse.SC_BAD_REQUEST);
                        break;
                    }
                    handleAddToFavorites(userId, movieId, title, posterPath, jsonResponse);
                    break;
                case "remove":
                    handleRemoveFromFavorites(userId, movieId, jsonResponse);
                    break;
                default:
                    sendErrorResponse(jsonResponse, "Invalid action", HttpServletResponse.SC_BAD_REQUEST);
            }
            
        } catch (NumberFormatException e) {
            sendErrorResponse(jsonResponse, "Invalid movie ID format", HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException e) {
            logError("Database error occurred", e);
            sendErrorResponse(jsonResponse, "Database error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            logError("Server error occurred", e);
            sendErrorResponse(jsonResponse, "Server error occurred", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        response.getWriter().print(jsonResponse.toString());
    }
    
    private void handleAddToFavorites(Integer userId, int movieId, String title, String posterPath, JSONObject jsonResponse) 
            throws SQLException {
        boolean success = addToFavorites(userId, movieId, title, posterPath);
        jsonResponse.put("success", success);
        jsonResponse.put("message", success ? 
            "Movie added to favorites" : "Movie is already in favorites");
    }
    
    private void handleRemoveFromFavorites(Integer userId, int movieId, JSONObject jsonResponse) 
            throws SQLException {
        boolean success = removeFromFavorites(userId, movieId);
        jsonResponse.put("success", success);
        jsonResponse.put("message", success ? 
            "Movie removed from favorites" : "Movie not found in favorites");
    }
    
    private void sendErrorResponse(JSONObject jsonResponse, String message, int statusCode) {
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        jsonResponse.put("statusCode", statusCode);
    }
    
    private void logError(String message, Exception e) {
        System.err.println(message);
        e.printStackTrace();
    }
    
    private boolean addToFavorites(Integer userId, Integer movieId, String title, String posterPath) throws SQLException {
        String checkQuery = "SELECT id FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        String insertQuery = "INSERT INTO user_favorites (user_id, movie_id, movie_title, poster_path, added_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = connectionManager.getConnection()) {
            // Check if already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, userId);
                checkStmt.setInt(2, movieId);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return false; // Already exists
                    }
                }
            }
            
            // Insert new favorite
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, movieId);
                insertStmt.setString(3, title);
                insertStmt.setString(4, posterPath != null ? posterPath : "");
                insertStmt.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
                
                return insertStmt.executeUpdate() > 0;
            }
        }
    }
    
    
    private boolean removeFromFavorites(Integer userId, Integer movieId) throws SQLException {
        String deleteQuery = "DELETE FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, movieId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // If browser requests /favorites directly, redirect to favorites.jsp UI
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains("text/html") && !"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
            response.sendRedirect("favorites.jsp");
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        Integer userId = (Integer) request.getSession().getAttribute("userId");
        if (userId == null) userId = 1;
        JSONObject jsonResponse = new JSONObject();
        try {
            String action = request.getParameter("action");
            String movieIdStr = request.getParameter("movieId");
            if ("remove".equals(action) && movieIdStr != null) {
                int movieId = Integer.parseInt(movieIdStr);
                boolean success = removeFromFavorites(userId, movieId);
                jsonResponse.put("success", success);
                jsonResponse.put("message", success ? "Movie removed from favorites" : "Movie not found in favorites");
            } else {
                jsonResponse.put("success", true);
                jsonResponse.put("favorites", getFavoritesForUser(userId));
            }
        } catch (Exception e) {
            jsonResponse.put("success", false);
            jsonResponse.put("message", e.getMessage());
        }
        response.getWriter().print(jsonResponse.toString());
    }

    private org.json.JSONArray getFavoritesForUser(Integer userId) throws SQLException {
        org.json.JSONArray arr = new org.json.JSONArray();
        String sql = "SELECT movie_id, movie_title, poster_path, added_date FROM user_favorites WHERE user_id = ? ORDER BY added_date DESC";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("movieId", rs.getInt("movie_id"));
                obj.put("title", rs.getString("movie_title"));
                obj.put("posterPath", rs.getString("poster_path"));
                obj.put("addedDate", rs.getTimestamp("added_date"));
                arr.put(obj);
            }
        }
        return arr;
    }
}