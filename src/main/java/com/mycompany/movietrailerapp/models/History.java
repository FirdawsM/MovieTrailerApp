package com.mycompany.movietrailerapp.models;

import java.sql.Timestamp;

/**
 * History model class representing user's watch history
 */
public class History {
    private Long id;
    private Long userId;
    private Integer movieId;
    private String movieTitle;
    private String posterPath;
    private Timestamp watchDate;
    
    // Default constructor
    public History() {}
    
    // Constructor with parameters
    public History(Long userId, Integer movieId, String movieTitle, String posterPath) {
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterPath = posterPath;
        this.watchDate = new Timestamp(System.currentTimeMillis());
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getMovieId() {
        return movieId;
    }
    
    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }
    
    public String getMovieTitle() {
        return movieTitle;
    }
    
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }
    
    public String getPosterPath() {
        return posterPath;
    }
    
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
    
    public Timestamp getWatchDate() {
        return watchDate;
    }
    
    public void setWatchDate(Timestamp watchDate) {
        this.watchDate = watchDate;
    }
    
    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", userId=" + userId +
                ", movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", watchDate=" + watchDate +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        History history = (History) obj;
        return userId.equals(history.userId) && movieId.equals(history.movieId);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode() + movieId.hashCode();
    }
}