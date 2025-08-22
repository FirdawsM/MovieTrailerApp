package com.mycompany.movietrailerapp.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuration manager for API keys and external service settings
 */
public class APIConfig {
    private static APIConfig instance;
    private Properties properties;
    
    private APIConfig() {
        loadProperties();
    }
    
    public static synchronized APIConfig getInstance() {
        if (instance == null) {
            instance = new APIConfig();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("api-config.properties")) {
            
            if (input != null) {
                properties.load(input);
            } else {
                // Fallback to default values if properties file not found
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Error loading API configuration: " + e.getMessage());
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        // Set default API keys (replace with your actual keys)
        properties.setProperty("tmdb.api.key", "6a9daee329adb7a4eefa450d1270aee7");
        properties.setProperty("youtube.api.key", "AIzaSyCzvwLpyN4uBuXgirSuNmrCOo7kco6Wc30");
        properties.setProperty("tmdb.base.url", "https://api.themoviedb.org/3");
        properties.setProperty("tmdb.image.base.url", "https://image.tmdb.org/t/p/w500");
        properties.setProperty("youtube.base.url", "https://www.googleapis.com/youtube/v3");
    }
    
    public String getTMDBApiKey() {
        return properties.getProperty("tmdb.api.key");
    }
    
    public String getYouTubeApiKey() {
        return properties.getProperty("youtube.api.key");
    }
    
    public String getTMDBBaseUrl() {
        return properties.getProperty("tmdb.base.url");
    }
    
    public String getTMDBImageBaseUrl() {
        return properties.getProperty("tmdb.image.base.url");
    }
    
    public String getYouTubeBaseUrl() {
        return properties.getProperty("youtube.base.url");
    }
    
    public boolean isYouTubeApiConfigured() {
        String apiKey = getYouTubeApiKey();
        return apiKey != null && !apiKey.isEmpty() && !"AIzaSyCzvwLpyN4uBuXgirSuNmrCOo7kco6Wc30".equals(apiKey);
    }
}