package com.mycompany.movietrailerapp.dao;

import com.mycompany.movietrailerapp.models.Favorite;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for managing user favorites
 */
public class FavoriteDAO {
    
    private static final Logger LOGGER = Logger.getLogger(FavoriteDAO.class.getName());
    private DatabaseConnectionManager connectionManager;
    
    public FavoriteDAO() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    /**
     * Add a movie to user's favorites
     */
    public boolean addToFavorites(Long userId, Integer movieId, String movieTitle, String posterPath) throws SQLException {
        String checkQuery = "SELECT id FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        String insertQuery = "INSERT INTO user_favorites (user_id, movie_id, movie_title, poster_path, added_date) VALUES (?, ?, ?, ?, NOW())";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            
            // Check if already exists
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setLong(1, userId);
                checkStmt.setInt(2, movieId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        LOGGER.info("Movie already in favorites: " + movieId + " for user: " + userId);
                        return false; // Already exists
                    }
                }
            }
            
            // Insert new favorite
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setLong(1, userId);
                insertStmt.setInt(2, movieId);
                insertStmt.setString(3, movieTitle);
                insertStmt.setString(4, posterPath);
                
                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Added movie to favorites: " + movieId + " for user: " + userId);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding movie to favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Remove a movie from user's favorites
     */
    public boolean removeFromFavorites(Long userId, Integer movieId) throws SQLException {
        String deleteQuery = "DELETE FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
                stmt.setLong(1, userId);
                stmt.setInt(2, movieId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Removed movie from favorites: " + movieId + " for user: " + userId);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing movie from favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Check if a movie is in user's favorites
     */
    public boolean isInFavorites(Long userId, Integer movieId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                stmt.setInt(2, movieId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if movie is in favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Get all favorites for a user
     */
    public List<Favorite> getUserFavorites(Long userId) throws SQLException {
        String query = "SELECT id, user_id, movie_id, movie_title, poster_path, added_date FROM user_favorites WHERE user_id = ? ORDER BY added_date DESC";
        
        Connection connection = null;
        List<Favorite> favorites = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Favorite favorite = new Favorite();
                        favorite.setId(rs.getLong("id"));
                        favorite.setUserId(rs.getLong("user_id"));
                        favorite.setMovieId(rs.getInt("movie_id"));
                        favorite.setMovieTitle(rs.getString("movie_title"));
                        favorite.setPosterPath(rs.getString("poster_path"));
                        favorite.setAddedDate(rs.getTimestamp("added_date"));
                        favorites.add(favorite);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return favorites;
    }
    
    /**
     * Get favorite movie IDs for a user (for quick lookup)
     */
    public List<Integer> getUserFavoriteIds(Long userId) throws SQLException {
        String query = "SELECT movie_id FROM user_favorites WHERE user_id = ?";
        
        Connection connection = null;
        List<Integer> favoriteIds = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        favoriteIds.add(rs.getInt("movie_id"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user favorite IDs", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return favoriteIds;
    }
    
    /**
     * Get count of favorites for a user
     */
    public int getFavoriteCount(Long userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user_favorites WHERE user_id = ?";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting favorite count", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return 0;
    }
    
    /**
     * Clear all favorites for a user
     */
    public boolean clearAllFavorites(Long userId) throws SQLException {
        String deleteQuery = "DELETE FROM user_favorites WHERE user_id = ?";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
                stmt.setLong(1, userId);
                
                int rowsAffected = stmt.executeUpdate();
                LOGGER.info("Cleared " + rowsAffected + " favorites for user: " + userId);
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing all favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        return connectionManager.testConnection();
    }
    
    /**
     * Close method (for interface compatibility)
     */
    public void close() {
        // Connection pooling handles this
    }
}