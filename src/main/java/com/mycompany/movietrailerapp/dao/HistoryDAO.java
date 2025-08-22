package com.mycompany.movietrailerapp.dao;

import com.mycompany.movietrailerapp.models.History;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for managing user watch history
 */
public class HistoryDAO {
    
    private static final Logger LOGGER = Logger.getLogger(HistoryDAO.class.getName());
    private DatabaseConnectionManager connectionManager;
    
    public HistoryDAO() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    /**
     * Add a movie to user's watch history (or update if exists)
     */
    public boolean addToHistory(Long userId, Integer movieId, String movieTitle, String posterPath) throws SQLException {
        String checkQuery = "SELECT id FROM user_history WHERE user_id = ? AND movie_id = ?";
        String updateQuery = "UPDATE user_history SET watched_at = NOW(), movie_title = ?, poster_path = ? WHERE user_id = ? AND movie_id = ?";
        String insertQuery = "INSERT INTO user_history (user_id, movie_id, movie_title, poster_path, watched_at) VALUES (?, ?, ?, ?, NOW())";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            
            // Check if already exists
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setLong(1, userId);
                checkStmt.setInt(2, movieId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Update existing entry
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, movieTitle);
                            updateStmt.setString(2, posterPath);
                            updateStmt.setLong(3, userId);
                            updateStmt.setInt(4, movieId);
                            
                            int rowsAffected = updateStmt.executeUpdate();
                            if (rowsAffected > 0) {
                                LOGGER.info("Updated movie in history: " + movieId + " for user: " + userId);
                                return true;
                            }
                        }
                    }
                }
            }
            
            // Insert new history entry
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setLong(1, userId);
                insertStmt.setInt(2, movieId);
                insertStmt.setString(3, movieTitle);
                insertStmt.setString(4, posterPath);
                
                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Added movie to history: " + movieId + " for user: " + userId);
                    return true;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding movie to history", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Remove a movie from user's watch history
     */
    public boolean removeFromHistory(Long userId, Integer movieId) throws SQLException {
        String deleteQuery = "DELETE FROM user_history WHERE user_id = ? AND movie_id = ?";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
                stmt.setLong(1, userId);
                stmt.setInt(2, movieId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Removed movie from history: " + movieId + " for user: " + userId);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing movie from history", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Check if a movie is in user's watch history
     */
    public boolean isInHistory(Long userId, Integer movieId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user_history WHERE user_id = ? AND movie_id = ?";
        
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
            LOGGER.log(Level.SEVERE, "Error checking if movie is in history", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Get all watch history for a user
     */
    public List<History> getUserHistory(Long userId) throws SQLException {
        String query = "SELECT id, user_id, movie_id, movie_title, poster_path, watched_at FROM user_history WHERE user_id = ? ORDER BY watched_at DESC";
        
        Connection connection = null;
        List<History> historyList = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        History history = new History();
                        history.setId(rs.getLong("id"));
                        history.setUserId(rs.getLong("user_id"));
                        history.setMovieId(rs.getInt("movie_id"));
                        history.setMovieTitle(rs.getString("movie_title"));
                        history.setPosterPath(rs.getString("poster_path"));
                        history.setWatchDate(rs.getTimestamp("watched_at"));
                        historyList.add(history);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user history", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return historyList;
    }
    
    /**
     * Get history movie IDs for a user (for quick lookup)
     */
    public List<Integer> getUserHistoryIds(Long userId) throws SQLException {
        String query = "SELECT movie_id FROM user_history WHERE user_id = ?";
        
        Connection connection = null;
        List<Integer> historyIds = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        historyIds.add(rs.getInt("movie_id"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user history IDs", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return historyIds;
    }
    
    /**
     * Get count of history entries for a user
     */
    public int getHistoryCount(Long userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM user_history WHERE user_id = ?";
        
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
            LOGGER.log(Level.SEVERE, "Error getting history count", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return 0;
    }
    
    /**
     * Clear all watch history for a user
     */
    public boolean clearAllHistory(Long userId) throws SQLException {
        String deleteQuery = "DELETE FROM user_history WHERE user_id = ?";
        
        Connection connection = null;
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
                stmt.setLong(1, userId);
                
                int rowsAffected = stmt.executeUpdate();
                LOGGER.info("Cleared " + rowsAffected + " history entries for user: " + userId);
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing all history", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Get recent watch history (limited number of entries)
     */
    public List<History> getRecentHistory(Long userId, int limit) throws SQLException {
        String query = "SELECT id, user_id, movie_id, movie_title, poster_path, watched_at FROM user_history WHERE user_id = ? ORDER BY watched_at DESC LIMIT ?";
        
        Connection connection = null;
        List<History> historyList = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, userId);
                stmt.setInt(2, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        History history = new History();
                        history.setId(rs.getLong("id"));
                        history.setUserId(rs.getLong("user_id"));
                        history.setMovieId(rs.getInt("movie_id"));
                        history.setMovieTitle(rs.getString("movie_title"));
                        history.setPosterPath(rs.getString("poster_path"));
                        history.setWatchDate(rs.getTimestamp("watched_at"));
                        historyList.add(history);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting recent history", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return historyList;
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