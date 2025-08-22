package com.mycompany.movietrailerapp.models;

import java.sql.Timestamp;

/**
 * Favorite model class representing user's favorite movies
 */
public class Favorite {
    private Long id;
    private Long userId;
    private Integer movieId;
    private String movieTitle;
    private String posterPath;
    private Timestamp addedDate;
    
    // Default constructor
    public Favorite() {}
    
    // Constructor with parameters
    public Favorite(Long userId, Integer movieId, String movieTitle, String posterPath) {
        this.userId = userId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterPath = posterPath;
        this.addedDate = new Timestamp(System.currentTimeMillis());
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
    
    public Timestamp getAddedDate() {
        return addedDate;
    }
    
    public void setAddedDate(Timestamp addedDate) {
        this.addedDate = addedDate;
    }
    
    @Override
    public String toString() {
        return "Favorite{" +
                "id=" + id +
                ", userId=" + userId +
                ", movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", posterPath='" + posterPath + '\'' +
                ", addedDate=" + addedDate +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Favorite favorite = (Favorite) obj;
        return userId.equals(favorite.userId) && movieId.equals(favorite.movieId);
    }
    
    @Override
    public int hashCode() {
        return userId.hashCode() + movieId.hashCode();
    }
}