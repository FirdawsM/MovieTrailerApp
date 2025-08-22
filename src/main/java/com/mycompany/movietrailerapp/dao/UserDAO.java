package com.mycompany.movietrailerapp.dao;

import com.mycompany.movietrailerapp.models.User;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDAO {
    
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());
    private DatabaseConnectionManager connectionManager;
    
    public UserDAO() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    /**
     * Create a new user in the database
     */
    public boolean createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, email, password, created_at) VALUES (?, ?, ?, NOW())";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername().trim());
                stmt.setString(2, user.getEmail().trim());
                stmt.setString(3, user.getPassword());
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setUserId(generatedKeys.getLong(1));
                        }
                    }
                    LOGGER.info("User created successfully: " + user.getUsername());
                    return true;
                }
                return false;
                
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user: " + user.getUsername(), e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Check if username already exists
     */
    public boolean userExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE TRIM(username) = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if user exists: " + username, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Check if email already exists
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE TRIM(email) = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, email.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking if email exists: " + email, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Authenticate user (for login) - FIXED VERSION
     */
    public User authenticateUser(String username, String password) throws SQLException {
        String sql = "SELECT user_id, username, email, password, created_at FROM users WHERE TRIM(username) = ? AND TRIM(password) = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                stmt.setString(2, password.trim());
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setUserId(rs.getLong("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setPassword(rs.getString("password"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        
                        LOGGER.info("User authenticated successfully: " + username);
                        return user;
                    } else {
                        LOGGER.warning("Authentication failed - invalid credentials for user: " + username);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + username, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(Long id) throws SQLException {
        String sql = "SELECT user_id, username, email, created_at FROM users WHERE user_id = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setUserId(rs.getLong("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by ID: " + id, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return null;
    }
    
    /**
     * Get user by username
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT user_id, username, email, created_at FROM users WHERE TRIM(username) = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, username.trim());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setUserId(rs.getLong("user_id"));
                        user.setUsername(rs.getString("username"));
                        user.setEmail(rs.getString("email"));
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting user by username: " + username, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return null;
    }
    
    /**
     * Get all users (for admin purposes)
     */
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT user_id, username, email, created_at FROM users ORDER BY created_at DESC";
        Connection connection = null;
        List<User> users = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                
                while (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getLong("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setEmail(rs.getString("email"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    users.add(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all users", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return users;
    }
    
    /**
     * Update user information
     */
    public boolean updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET username = ?, email = ?, updated_at = NOW() WHERE user_id = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, user.getUsername().trim());
                stmt.setString(2, user.getEmail().trim());
                stmt.setLong(3, user.getUserId());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("User updated successfully: " + user.getUsername());
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user: " + user.getUsername(), e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Update user password
     */
    public boolean updatePassword(String username, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ?, updated_at = NOW() WHERE TRIM(username) = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, newPassword);
                stmt.setString(2, username.trim());
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("Password updated successfully for user: " + username);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user: " + username, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Delete user
     */
    public boolean deleteUser(Long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    LOGGER.info("User deleted successfully: " + userId);
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user: " + userId, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return false;
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        return connectionManager.testConnection();
    }
    
    /**
     * Get connection pool statistics
     */
    public String getPoolStats() {
        return connectionManager.getPoolStats();
    }
    
    /**
     * Close method (now just for interface compatibility)
     */
    public void close() {
        // No need to close anything since we're using connection pooling
        // Connections are managed by the DatabaseConnectionManager
    }
}