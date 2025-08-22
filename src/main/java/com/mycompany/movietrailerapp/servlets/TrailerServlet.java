package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.annotation.WebServlet;


@WebServlet("/trailer")
public class TrailerServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(TrailerServlet.class.getName());
    private static final String YOUTUBE_API_KEY = "AIzaSyCzvwLpyN4uBuXgirSuNmrCOo7kco6Wc30";
    private static final String YOUTUBE_SEARCH_URL = "https://www.googleapis.com/youtube/v3/search";
    private static final String TMDB_API_KEY = "6a9daee329adb7a4eefa450d1270aee7";
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final int API_TIMEOUT_MS = 5000;
    private static final int MAX_RETRIES = 2;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String title = request.getParameter("title");
        String movieId = request.getParameter("movieId");
        
        if (title == null || title.isEmpty()) {
            sendErrorResponse(response, "Title parameter is required", HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        try {
            // Try TMDB by provided movieId first
            String videoId = fetchFromTMDB(movieId);
            
            // If not found by ID, search TMDB by title to resolve an ID, then fetch videos
            if (videoId == null) {
                String tmdbId = searchTMDBIdByTitle(title);
                if (tmdbId != null) {
                    videoId = fetchFromTMDB(tmdbId);
                }
            }
            
            // Fallback to YouTube search if TMDB fails
            if (videoId == null) {
                videoId = searchYouTubeTrailerWithRetry(title);
            }
            
            JSONObject jsonResponse = new JSONObject();
            if (videoId != null) {
                jsonResponse.put("success", true)
                          .put("videoId", videoId)
                          .put("embedUrl", "https://www.youtube.com/embed/" + videoId)
                          .put("title", title);
            } else {
                sendErrorResponse(response, "Trailer not found for: " + title, HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            response.getWriter().print(jsonResponse.toString());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error searching for trailer: " + title, e);
            sendErrorResponse(response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
    
    private String fetchFromTMDB(String movieId) {
        if (movieId == null) return null;
        
        try {
            URL url = new URL(TMDB_BASE_URL + "/movie/" + movieId + "/videos?api_key=" + TMDB_API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(API_TIMEOUT_MS);
            
            if (conn.getResponseCode() != 200) {
                return null;
            }
            
            String response = readResponse(conn);
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray results = jsonResponse.getJSONArray("results");
            
            // Prefer YouTube Trailer/Teaser (ignore language). Fallback to first YouTube or first available key
            String firstKey = null;
            String firstYouTubeKey = null;
            for (int i = 0; i < results.length(); i++) {
                JSONObject video = results.getJSONObject(i);
                String key = video.optString("key", null);
                String site = video.optString("site", "");
                String type = video.optString("type", "");
                if (firstKey == null && key != null) firstKey = key;
                if (firstYouTubeKey == null && key != null && "YouTube".equalsIgnoreCase(site)) firstYouTubeKey = key;
                if ("YouTube".equalsIgnoreCase(site) && key != null && ("Trailer".equalsIgnoreCase(type) || "Teaser".equalsIgnoreCase(type))) {
                    return key;
                }
            }
            if (firstYouTubeKey != null) return firstYouTubeKey;
            return firstKey;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to fetch from TMDB", e);
        }
        return null;
    }
    
    private String searchYouTubeTrailerWithRetry(String movieTitle) throws IOException {
        String videoId = null;
        int attempt = 0;
        
        while (attempt < MAX_RETRIES && videoId == null) {
            try {
                if (attempt > 0) {
                    TimeUnit.SECONDS.sleep(1); // Rate limiting
                }
                
                videoId = searchYouTubeTrailer(movieTitle);
                attempt++;
                
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IOException("Search interrupted", ie);
            }
        }
        return videoId;
    }
    
    private String searchYouTubeTrailer(String movieTitle) throws IOException {
        String[] searchQueries = {
            movieTitle + " official trailer",
            movieTitle + " trailer",
            movieTitle + " movie"
        };
        
        for (String query : searchQueries) {
            String videoId = searchYouTube(query);
            if (videoId != null) {
                return videoId;
            }
        }
        return null;
    }
    
    private String searchYouTube(String searchQuery) throws IOException {
        URL url = new URL(YOUTUBE_SEARCH_URL + 
                         "?part=snippet&maxResults=1&q=" + 
                         java.net.URLEncoder.encode(searchQuery, "UTF-8") + 
                         "&key=" + YOUTUBE_API_KEY + 
                         "&type=video&videoEmbeddable=true");
        
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(API_TIMEOUT_MS);
        
        try {
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                LOGGER.warning("YouTube API response: " + responseCode);
                return null;
            }
            
            String response = readResponse(conn);
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray items = jsonResponse.getJSONArray("items");
            
            if (items.length() > 0) {
                return items.getJSONObject(0)
                           .getJSONObject("id")
                           .getString("videoId");
            }
        } finally {
            conn.disconnect();
        }
        return null;
    }
    
    private String searchTMDBIdByTitle(String title) {
        try {
            URL url = new URL(TMDB_BASE_URL + "/search/movie?api_key=" + TMDB_API_KEY + "&query=" + java.net.URLEncoder.encode(title, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(API_TIMEOUT_MS);
            if (conn.getResponseCode() != 200) {
                return null;
            }
            String response = readResponse(conn);
            JSONObject json = new JSONObject(response);
            JSONArray results = json.optJSONArray("results");
            if (results != null && results.length() > 0) {
                int id = results.getJSONObject(0).optInt("id", 0);
                return id > 0 ? String.valueOf(id) : null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed TMDB title search", e);
        }
        return null;
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        StringBuilder response = new StringBuilder();
        try (Scanner scanner = new Scanner(conn.getInputStream())) {
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
        }
        return response.toString();
    }
    
    private void sendErrorResponse(HttpServletResponse response, String message, int statusCode) 
            throws IOException {
        response.setStatus(statusCode);
        JSONObject errorResponse = new JSONObject()
            .put("success", false)
            .put("error", message);
        response.getWriter().print(errorResponse.toString());
    }
}