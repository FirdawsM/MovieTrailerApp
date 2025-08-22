// (Removed duplicate and misplaced method definitions. The correct implementations are inside the class body.)
package com.mycompany.movietrailerapp.dao;

import com.mycompany.movietrailerapp.models.Movie;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieDAO {
    
    private static final Logger LOGGER = Logger.getLogger(MovieDAO.class.getName());
    private DatabaseConnectionManager connectionManager;
    
    public MovieDAO() {
        this.connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    /**
     * Get latest movies
     */
    public List<Movie> getLatestMovies(int limit) throws SQLException {
        String sql = "SELECT * FROM movies ORDER BY created_at DESC LIMIT ?";
        Connection connection = null;
        List<Movie> movies = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(mapResultSetToMovie(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting latest movies", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return movies;
    }
    
    /**
     * Get popular movies (by rating)
     */
    public List<Movie> getPopularMovies(int limit) throws SQLException {
        String sql = "SELECT * FROM movies WHERE rating >= 7.0 ORDER BY rating DESC, created_at DESC LIMIT ?";
        Connection connection = null;
        List<Movie> movies = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(mapResultSetToMovie(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting popular movies", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return movies;
    }
    
    /**
     * Get featured movies
     */
    public List<Movie> getFeaturedMovies(int limit) throws SQLException {
        String sql = "SELECT * FROM movies WHERE rating >= 8.0 ORDER BY rating DESC LIMIT ?";
        Connection connection = null;
        List<Movie> movies = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(mapResultSetToMovie(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting featured movies", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return movies;
    }
    
    /**
     * Search movies by title or description
     */
    public List<Movie> searchMovies(String query) throws SQLException {
        String sql = "SELECT * FROM movies WHERE title LIKE ? OR description LIKE ? ORDER BY title";
        Connection connection = null;
        List<Movie> movies = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                String searchPattern = "%" + query + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(mapResultSetToMovie(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching movies", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return movies;
    }
    
    /**
     * Get filtered movies
     */
    public List<Movie> getFilteredMovies(String genre, String year, String rating) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM movies WHERE 1=1");
        List<Object> parameters = new ArrayList<>();
        
        if (genre != null && !genre.trim().isEmpty() && !"all".equals(genre)) {
            sql.append(" AND genre = ?");
            parameters.add(genre);
        }
        
        if (year != null && !year.trim().isEmpty() && !"all".equals(year)) {
            sql.append(" AND YEAR(release_date) = ?");
            parameters.add(Integer.parseInt(year));
        }
        
        if (rating != null && !rating.trim().isEmpty() && !"all".equals(rating)) {
            sql.append(" AND rating >= ?");
            parameters.add(Double.parseDouble(rating));
        }
        
        sql.append(" ORDER BY rating DESC, created_at DESC");
        
        Connection connection = null;
        List<Movie> movies = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < parameters.size(); i++) {
                    stmt.setObject(i + 1, parameters.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(mapResultSetToMovie(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting filtered movies", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return movies;
    }
    
    /**
     * Get user's favorite movies
     */
    public List<Movie> getUserFavorites(Long userId) throws SQLException {
        String sql = "SELECT m.* FROM movies m " +
                    "JOIN user_favorites uf ON m.id = uf.movie_id " +
                    "WHERE uf.user_id = ? ORDER BY uf.added_date DESC";
        Connection connection = null;
        List<Movie> movies = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        movies.add(mapResultSetToMovie(rs));
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
        return movies;
    }
    
    /**
     * Get movie by ID
     */
    public Movie getMovieById(Long id) throws SQLException {
        String sql = "SELECT * FROM movies WHERE id = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToMovie(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting movie by ID: " + id, e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return null;
    }
    
    /**
     * Add movie to user favorites
     */
    public boolean addToFavorites(Long userId, Long movieId) throws SQLException {
        String sql = "INSERT IGNORE INTO user_favorites (user_id, movie_id, added_date) VALUES (?, ?, NOW())";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                stmt.setLong(2, movieId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding movie to favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Remove movie from user favorites
     */
    public boolean removeFromFavorites(Long userId, Long movieId) throws SQLException {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                stmt.setLong(2, movieId);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing movie from favorites", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
    }
    
    /**
     * Check if movie is in user's favorites
     */
    public boolean isInFavorites(Long userId, Long movieId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_favorites WHERE user_id = ? AND movie_id = ?";
        Connection connection = null;
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, userId);
                stmt.setLong(2, movieId);
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
     * Get all genres
     */
    public List<String> getAllGenres() throws SQLException {
        String sql = "SELECT DISTINCT genre FROM movies WHERE genre IS NOT NULL ORDER BY genre";
        Connection connection = null;
        List<String> genres = new ArrayList<>();
        
        try {
            connection = connectionManager.getConnection();
            try (PreparedStatement stmt = connection.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    genres.add(rs.getString("genre"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting genres", e);
            throw e;
        } finally {
            if (connection != null) {
                connectionManager.releaseConnection(connection);
            }
        }
        return genres;
    }
    
    /**
     * Map ResultSet to Movie object
     */
    private Movie mapResultSetToMovie(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getLong("id"));
        movie.setTitle(rs.getString("title"));
        movie.setDescription(rs.getString("description"));
        movie.setTrailerUrl(rs.getString("trailer_url"));
        movie.setPosterUrl(rs.getString("poster_url"));
        movie.setReleaseDate(rs.getDate("release_date"));
        movie.setGenre(rs.getString("genre"));
        movie.setRating(rs.getDouble("rating"));
        movie.setCreatedAt(rs.getTimestamp("created_at"));
        return movie;
    }
    
    /**
     * Close method
     */
    public void close() {
        // Connection pooling handles this
    }

    public List<Movie> getUserFavorites(int userId) {
        throw new UnsupportedOperationException("Unimplemented method 'getUserFavorites'");
    }

    // Check if a movie exists in the database by TMDB id
    public boolean movieExists(int movieId) throws SQLException {
        String sql = "SELECT id FROM movies WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movieId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Insert a movie from TMDB JSON into the database
    public void insertMovie(org.json.JSONObject movie) throws SQLException {
        String sql = "INSERT INTO movies (id, title, description, trailerUrl, posterUrl, rating, created_at) VALUES (?, ?, ?, ?, ?, ?, NOW())";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, movie.getInt("id"));
            stmt.setString(2, movie.getString("title"));
            stmt.setString(3, movie.optString("overview", ""));
            stmt.setString(4, ""); // trailerUrl can be set if available
            stmt.setString(5, movie.optString("poster_path", ""));
            stmt.setDouble(6, movie.optDouble("vote_average", 0.0));
            stmt.executeUpdate();
        }
    }
}