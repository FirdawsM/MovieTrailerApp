package com.mycompany.movietrailerapp.models;

import java.sql.Date;
import java.sql.Timestamp;

public class Movie {
    
    private Long id;
    private String title;
    private String description;
    private String trailerUrl;
    private String posterUrl;
    private Date releaseDate;
    private String genre;
    private Double rating;
    private Timestamp createdAt;
    
    // Default constructor
    public Movie() {
    }
    
    // Constructor with essential fields
    public Movie(String title, String description, String trailerUrl, String posterUrl, 
                Date releaseDate, String genre, Double rating) {
        this.title = title;
        this.description = description;
        this.trailerUrl = trailerUrl;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.rating = rating;
    }
    
    // Constructor with all fields
    public Movie(Long id, String title, String description, String trailerUrl, String posterUrl, 
                Date releaseDate, String genre, Double rating, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.trailerUrl = trailerUrl;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.genre = genre;
        this.rating = rating;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getTrailerUrl() {
        return trailerUrl;
    }
    
    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }
    
    public String getPosterUrl() {
        return posterUrl;
    }
    
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
    
    public Date getReleaseDate() {
        return releaseDate;
    }
    
    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public Double getRating() {
        return rating;
    }
    
    public void setRating(Double rating) {
        this.rating = rating;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    // Utility methods
    public String getFormattedRating() {
        if (rating != null) {
            return String.format("%.1f", rating);
        }
        return "N/A";
    }
    
    public String getRatingStars() {
        if (rating != null) {
            int fullStars = (int) Math.floor(rating / 2);
            boolean hasHalfStar = (rating % 2) >= 1;
            
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < fullStars; i++) {
                stars.append("★");
            }
            if (hasHalfStar) {
                stars.append("☆");
            }
            while (stars.length() < 5) {
                stars.append("☆");
            }
            return stars.toString();
        }
        return "☆☆☆☆☆";
    }
    
    public String getShortDescription() {
        if (description != null && description.length() > 100) {
            return description.substring(0, 97) + "...";
        }
        return description;
    }
    
    public String getYouTubeVideoId() {
        if (trailerUrl != null) {
            // Extract YouTube video ID from various YouTube URL formats
            String videoId = null;
            
            // Handle youtube.com/watch?v=VIDEO_ID
            if (trailerUrl.contains("watch?v=")) {
                videoId = trailerUrl.substring(trailerUrl.indexOf("watch?v=") + 8);
            }
            // Handle youtu.be/VIDEO_ID
            else if (trailerUrl.contains("youtu.be/")) {
                videoId = trailerUrl.substring(trailerUrl.indexOf("youtu.be/") + 9);
            }
            // Handle youtube.com/embed/VIDEO_ID
            else if (trailerUrl.contains("embed/")) {
                videoId = trailerUrl.substring(trailerUrl.indexOf("embed/") + 6);
            }
            
            // Remove additional parameters if present
            if (videoId != null && videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            if (videoId != null && videoId.contains("?")) {
                videoId = videoId.substring(0, videoId.indexOf("?"));
            }
            
            return videoId;
        }
        return null;
    }
    
    public String getEmbedUrl() {
        String videoId = getYouTubeVideoId();
        if (videoId != null) {
            return "https://www.youtube.com/embed/" + videoId;
        }
        return trailerUrl; // Return original URL as fallback
    }
    
    public String getThumbnailUrl() {
        String videoId = getYouTubeVideoId();
        if (videoId != null) {
            return "https://img.youtube.com/vi/" + videoId + "/hqdefault.jpg";
        }
        return posterUrl; // Return poster URL as fallback
    }
    
    public int getReleaseYear() {
        if (releaseDate != null) {
            return releaseDate.toLocalDate().getYear();
        }
        return 0;
    }
    
    public boolean isHighRated() {
        return rating != null && rating >= 8.0;
    }
    
    public boolean isRecentRelease() {
        if (releaseDate != null) {
            return releaseDate.toLocalDate().getYear() >= (java.time.LocalDate.now().getYear() - 2);
        }
        return false;
    }
}