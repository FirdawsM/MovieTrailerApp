package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mycompany.movietrailerapp.config.ApiKeys;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;


public class DashboardServlet extends HttpServlet {
    
private static final String TMDB_API_KEY = ApiKeys.TMDB_API_KEY;
private static final String YOUTUBE_API_KEY = ApiKeys.YOUTUBE_API_KEY;
    private static final String TMDB_BASE_URL = "https://api.themoviedb.org/3";
    private static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    
    private DatabaseConnectionManager connectionManager;
    
    @Override
    public void init() throws ServletException {
        super.init();
        connectionManager = DatabaseConnectionManager.getInstance();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        HttpSession session = request.getSession();
        
        // FIXED: Use actual logged-in user ID, don't default to 1
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            // No user logged in, redirect to login
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        // Log which user is accessing dashboard
        String username = (String) session.getAttribute("username");
        System.out.println("Dashboard accessed by user ID: " + userId + " (username: " + username + ")");
        
        try (PrintWriter out = response.getWriter()) {
            // Fetch user's favorites and history using ACTUAL user ID
            Set<Integer> userFavorites = getUserFavorites(userId);
            Set<Integer> userHistory = getUserHistory(userId);

            // Fetch data from TMDB API
            JSONObject trendingMovies = fetchFromTMDB("/trending/movie/day");
            JSONObject popularMovies = fetchFromTMDB("/movie/popular");
            JSONObject topRatedMovies = fetchFromTMDB("/movie/top_rated");
            JSONObject nowPlayingMovies = fetchFromTMDB("/movie/now_playing");

            // Save movies from TMDB to database if not already present
            com.mycompany.movietrailerapp.dao.MovieDAO movieDao = new com.mycompany.movietrailerapp.dao.MovieDAO();
            try {
                org.json.JSONArray[] movieArrays = {
                    trendingMovies != null ? trendingMovies.optJSONArray("results") : null,
                    popularMovies != null ? popularMovies.optJSONArray("results") : null,
                    topRatedMovies != null ? topRatedMovies.optJSONArray("results") : null,
                    nowPlayingMovies != null ? nowPlayingMovies.optJSONArray("results") : null
                };
                for (org.json.JSONArray arr : movieArrays) {
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            org.json.JSONObject movie = arr.getJSONObject(i);
                            int movieId = movie.getInt("id");
                            if (!movieDao.movieExists(movieId)) {
                                movieDao.insertMovie(movie);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Generate HTML
            out.println("<!DOCTYPE html>");
            out.println("<html lang='en'>");
            out.println("<head>");
            out.println("<meta charset='UTF-8'>");
            out.println("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            out.println("<title>MovieTrailer - Dashboard</title>");
            out.println("<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'>");
            out.println("<style>");
            generateCSS(out);
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");

            // Navigation Bar
            generateNavBar(out, username);

            // Main Content
            out.println("<div class='main-content'>");

            // Hero Section
            generateHeroSection(out, trendingMovies, userId, userFavorites, userHistory);

            // Movie Sections
            generateMovieSection(out, "Trending Now", "fas fa-fire", trendingMovies, userId, userFavorites, userHistory);
            generateMovieSection(out, "Popular Movies", "fas fa-star", popularMovies, userId, userFavorites, userHistory);
            generateMovieSection(out, "Top Rated", "fas fa-trophy", topRatedMovies, userId, userFavorites, userHistory);
            generateMovieSection(out, "Now Playing", "fas fa-ticket-alt", nowPlayingMovies, userId, userFavorites, userHistory);

            out.println("</div>"); // Close main-content

            // YouTube Player Modal
            generateYouTubeModal(out);

            // Footer
            out.println("<footer>");
            out.println("  <p>&copy; 2023 MovieTrailer App. All rights reserved.</p>");
            out.println("</footer>");

            // JavaScript for AJAX calls
            generateJavaScript(out);

            out.println("</body>");
            out.println("</html>");
        }
    }
    
    private void generateCSS(PrintWriter out) {
        out.println("  * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Arial', sans-serif; }");
        out.println("  body { background-color: #141414; color: #fff; }");
        out.println("  .navbar { display: flex; justify-content: space-between; align-items: center; padding: 20px 50px; background: linear-gradient(to bottom, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0) 100%); position: fixed; width: 100%; z-index: 100; }");
        out.println("  .logo { color: #e50914; font-size: 2rem; font-weight: bold; }");
        out.println("  .nav-links { display: flex; gap: 20px; }");
        out.println("  .nav-links a { color: #fff; text-decoration: none; font-size: 1rem; }");
        out.println("  .nav-links a:hover { color: #b3b3b3; }");
        out.println("  .profile { width: 40px; height: 40px; border-radius: 4px; }");
        out.println("  .main-content { padding-top: 100px; }");
        out.println("  .hero { position: relative; height: 60vh; background-size: cover; background-position: center; display: flex; flex-direction: column; justify-content: center; padding: 0 50px; }");
        out.println("  .hero-content { max-width: 600px; z-index: 2; }");
        out.println("  .hero-title { font-size: 3rem; margin-bottom: 20px; }");
        out.println("  .hero-description { max-width: 100%; margin-bottom: 20px; line-height: 1.5; }");
        out.println("  .hero-buttons { display: flex; gap: 15px; }");
        out.println("  .btn { padding: 10px 25px; border-radius: 4px; border: none; font-size: 1rem; cursor: pointer; }");
        out.println("  .btn-primary { background-color: #e50914; color: white; }");
        out.println("  .btn-secondary { background-color: rgba(109, 109, 110, 0.7); color: white; }");
        out.println("  .section { margin: 30px 50px; }");
        out.println("  .section-title { font-size: 1.5rem; margin-bottom: 15px; }");
        out.println("  .movie-row { display: flex; gap: 10px; overflow-x: auto; padding: 10px 0; }");
        out.println("  .movie-row::-webkit-scrollbar { display: none; }");
        out.println("  .movie-card { min-width: 200px; transition: transform 0.3s; position: relative; cursor: pointer; }");
        out.println("  .movie-card:hover { transform: scale(1.05); }");
        out.println("  .movie-poster { width: 100%; border-radius: 4px; }");
        out.println("  .movie-title { margin-top: 8px; font-size: 0.9rem; }");
        out.println("  .movie-rating { position: absolute; top: 10px; right: 10px; background-color: rgba(0,0,0,0.7); padding: 3px 6px; border-radius: 4px; font-size: 0.8rem; }");
        out.println("  .movie-actions { position: absolute; bottom: 10px; left: 10px; right: 10px; display: flex; gap: 5px; opacity: 0; transition: opacity 0.3s; }");
        out.println("  .movie-card:hover .movie-actions { opacity: 1; }");
        out.println("  .action-btn { background-color: rgba(0,0,0,0.8); border: none; color: white; padding: 5px 8px; border-radius: 50%; cursor: pointer; font-size: 0.9rem; }");
        out.println("  .action-btn:hover { background-color: #e50914; }");
        out.println("  .action-btn.active { background-color: #e50914; }");
        out.println("  .overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: linear-gradient(to top, rgba(0,0,0,0.9) 0%, rgba(0,0,0,0) 50%); }");
        out.println("  footer { text-align: center; padding: 30px; color: #808080; }");
        out.println("  .notification { position: fixed; top: 100px; right: 20px; background-color: #e50914; color: white; padding: 10px 20px; border-radius: 4px; z-index: 1000; opacity: 0; transition: opacity 0.3s; }");
        out.println("  .notification.show { opacity: 1; }");
        out.println("  .user-info { color: #b3b3b3; font-size: 0.9rem; }");
        
        // YouTube Modal CSS
        out.println("  .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; overflow: auto; background-color: rgba(0,0,0,0.9); }");
        out.println("  .modal-content { position: relative; background-color: #141414; margin: 5% auto; padding: 0; width: 90%; max-width: 800px; border-radius: 10px; }");
        out.println("  .modal-header { padding: 20px; border-bottom: 1px solid #333; display: flex; justify-content: space-between; align-items: center; }");
        out.println("  .modal-title { margin: 0; }");
        out.println("  .close { color: #aaa; font-size: 28px; font-weight: bold; cursor: pointer; }");
        out.println("  .close:hover, .close:focus { color: white; }");
        out.println("  .modal-body { padding: 0; }");
        out.println("  .video-container { position: relative; padding-bottom: 56.25%; height: 0; overflow: hidden; }");
        out.println("  .video-container iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; }");
        out.println("  .loading { text-align: center; padding: 50px; }");
        out.println("  .error-message { text-align: center; padding: 50px; color: #e50914; }");
    }
    
    private void generateYouTubeModal(PrintWriter out) {
        out.println("<div id='trailerModal' class='modal'>");
        out.println("  <div class='modal-content'>");
        out.println("    <div class='modal-header'>");
        out.println("      <h3 id='modalTitle' class='modal-title'>Movie Trailer</h3>");
        out.println("      <span class='close' onclick='closeTrailerModal()'>&times;</span>");
        out.println("    </div>");
        out.println("    <div class='modal-body'>");
        out.println("      <div id='loadingMessage' class='loading'>");
        out.println("        <i class='fas fa-spinner fa-spin'></i> Loading trailer...");
        out.println("      </div>");
        out.println("      <div id='errorMessage' class='error-message' style='display: none;'>");
        out.println("        <i class='fas fa-exclamation-triangle'></i> Trailer not available");
        out.println("      </div>");
        out.println("      <div id='videoContainer' class='video-container' style='display: none;'>");
        out.println("      </div>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("</div>");
    }
    
    private void generateNavBar(PrintWriter out, String username) {
        out.println("<nav class='navbar'>");
        out.println("  <div class='logo'>MOVIETRAILER</div>");
        out.println("  <div class='nav-links'>");
        out.println("    <a href='dashboard'><i class='fas fa-home'></i> Home</a>");
        out.println("    <a href='#'><i class='fas fa-tv'></i> TV Shows</a>");
        out.println("    <a href='#'><i class='fas fa-film'></i> Movies</a>");
        out.println("    <a href='#'><i class='fas fa-fire'></i> Trending</a>");
        out.println("    <a href='favorites'><i class='fas fa-heart'></i> My Favorites</a>");
        out.println("    <a href='history'><i class='fas fa-history'></i> History</a>");
        out.println("  </div>");
        out.println("  <div style='display: flex; align-items: center; gap: 15px;'>");
        if (username != null) {
            out.println("    <span class='user-info'>Welcome, " + username + "</span>");
        }
        out.println("    <a href='login?logout=true' style='color: #fff; text-decoration: none;'><i class='fas fa-sign-out-alt'></i> Logout</a>");
        out.println("  </div>");
        out.println("</nav>");
    }
    
    private void generateHeroSection(PrintWriter out, JSONObject trendingMovies, Integer userId, Set<Integer> userFavorites, Set<Integer> userHistory) {
        if (trendingMovies != null) {
            JSONArray trendingResults = trendingMovies.getJSONArray("results");
            if (trendingResults.length() > 0) {
                JSONObject featuredMovie = trendingResults.getJSONObject(0);
                String backdropPath = featuredMovie.optString("backdrop_path", "");
                int movieId = featuredMovie.getInt("id");
                
                out.println("<section class='hero' style=\"background: linear-gradient(rgba(0,0,0,0.5), rgba(0,0,0,0.5)), url('" + TMDB_IMAGE_BASE_URL + backdropPath + "')\">");
                out.println("  <div class='overlay'></div>");
                out.println("  <div class='hero-content'>");
                out.println("    <h1 class='hero-title'>" + featuredMovie.getString("title") + "</h1>");
                out.println("    <p class='hero-description'>" + truncateText(featuredMovie.getString("overview"), 200) + "</p>");
                out.println("    <div class='hero-buttons'>");
                out.println("      <button class='btn btn-primary' onclick='playTrailer(" + movieId + ", \"" + escapeString(featuredMovie.getString("title")) + "\", \"" + escapeString(featuredMovie.optString("poster_path", "")) + "\")'><i class='fas fa-play'></i> Play Trailer</button>");
                out.println("      <button class='btn btn-secondary'><i class='fas fa-info-circle'></i> More Info</button>");
                out.println("    </div>");
                out.println("  </div>");
                out.println("</section>");
            }
        }
    }
    
    private void generateMovieSection(PrintWriter out, String title, String icon, JSONObject moviesData, Integer userId, Set<Integer> userFavorites, Set<Integer> userHistory) {
        out.println("<section class='section'>");
        out.println("  <h2 class='section-title'><i class='" + icon + "'></i> " + title + "</h2>");
        out.println("  <div class='movie-row'>");
        if (moviesData != null) {
            JSONArray results = moviesData.getJSONArray("results");
            for (int i = 0; i < Math.min(10, results.length()); i++) {
                JSONObject movie = results.getJSONObject(i);
                out.println(generateMovieCard(movie, userId, userFavorites, userHistory));
            }
        }
        out.println("  </div>");
        out.println("</section>");
    }
    
    private void generateJavaScript(PrintWriter out) {
        out.println("<script>");
        
        // Add to favorites function
        out.println("function addToFavorites(movieId, title, posterPath) {");
        out.println("  console.log('Adding to favorites for current user');");
        out.println("  fetch('favorites', {");
        out.println("    method: 'POST',");
        out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("    body: 'action=add&movieId=' + movieId + '&title=' + encodeURIComponent(title) + '&posterPath=' + encodeURIComponent(posterPath)");
        out.println("  })");
        out.println("  .then(response => response.json())");
        out.println("  .then(data => {");
        out.println("    console.log('Favorites response:', data);");
        out.println("    if (data.success) {");
        out.println("      const btn = document.querySelector('#fav-' + movieId);");
        out.println("      btn.classList.add('active');");
        out.println("      btn.setAttribute('onclick', 'removeFromFavorites(' + movieId + ')');");
        out.println("      showNotification('Added to favorites!');");
        out.println("    } else {");
        out.println("      showNotification(data.message || 'Error adding to favorites');");
        out.println("    }");
        out.println("  })");
        out.println("  .catch(error => {");
        out.println("    console.error('Error:', error);");
        out.println("    showNotification('Error adding to favorites');");
        out.println("  });");
        out.println("}");
        out.println("");
        
        // Remove from favorites function
        out.println("function removeFromFavorites(movieId) {");
        out.println("  console.log('Removing from favorites for current user');");
        out.println("  fetch('favorites', {");
        out.println("    method: 'POST',");
        out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("    body: 'action=remove&movieId=' + movieId");
        out.println("  })");
        out.println("  .then(response => response.json())");
        out.println("  .then(data => {");
        out.println("    console.log('Remove favorites response:', data);");
        out.println("    if (data.success) {");
        out.println("      const btn = document.querySelector('#fav-' + movieId);");
        out.println("      btn.classList.remove('active');");
        out.println("      const title = btn.getAttribute('data-title');");
        out.println("      const posterPath = btn.getAttribute('data-poster');");
        out.println("      btn.setAttribute('onclick', 'addToFavorites(' + movieId + ', \"' + title + '\", \"' + posterPath + '\")');");
        out.println("      showNotification('Removed from favorites!');");
        out.println("    }");
        out.println("  })");
        out.println("  .catch(error => {");
        out.println("    console.error('Error:', error);");
        out.println("    showNotification('Error removing from favorites');");
        out.println("  });");
        out.println("}");
        out.println("");
        
        // Play trailer function (enhanced)
        out.println("function playTrailer(movieId, title, posterPath) {");
        out.println("  console.log('Adding to history for current user');");
        out.println("  // Add to history first");
        out.println("  fetch('history', {");
        out.println("    method: 'POST',");
        out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
        out.println("    body: 'action=add&movieId=' + movieId + '&title=' + encodeURIComponent(title) + '&posterPath=' + encodeURIComponent(posterPath)");
        out.println("  })");
        out.println("  .then(response => response.json())");
        out.println("  .then(data => {");
        out.println("    console.log('History response:', data);");
        out.println("    if (data.success) {");
        out.println("      const histBtn = document.querySelector('#hist-' + movieId);");
        out.println("      if (histBtn) histBtn.classList.add('active');");
        out.println("    }");
        out.println("  });");
        out.println("");
        out.println("  // Open trailer modal");
        out.println("  openTrailerModal(movieId, title);");
        out.println("}");
        out.println("");
        
        // Trailer modal functions
        out.println("function openTrailerModal(movieId, title) {");
        out.println("  const modal = document.getElementById('trailerModal');");
        out.println("  const modalTitle = document.getElementById('modalTitle');");
        out.println("  const loadingMessage = document.getElementById('loadingMessage');");
        out.println("  const errorMessage = document.getElementById('errorMessage');");
        out.println("  const videoContainer = document.getElementById('videoContainer');");
        out.println("");
        out.println("  modalTitle.textContent = title + ' - Trailer';");
        out.println("  modal.style.display = 'block';");
        out.println("  loadingMessage.style.display = 'block';");
        out.println("  errorMessage.style.display = 'none';");
        out.println("  videoContainer.style.display = 'none';");
        out.println("  videoContainer.innerHTML = '';");
        out.println("");
        out.println("  // Fetch trailer from in-app servlet with TMDB first, YouTube fallback");
        out.println("  fetch('trailer?title=' + encodeURIComponent(title) + '&movieId=' + movieId)");
        out.println("    .then(response => response.json())");
        out.println("    .then(data => {");
        out.println("      loadingMessage.style.display = 'none';");
        out.println("      if (data.success && (data.embedUrl || data.videoId)) {");
        out.println("        const src = data.embedUrl ? (data.embedUrl + '?autoplay=1') : ('https://www.youtube.com/embed/' + data.videoId + '?autoplay=1');");
        out.println("        videoContainer.innerHTML = '<iframe src=\"' + src + '\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>';");
        out.println("        videoContainer.style.display = 'block';");
        out.println("      } else {");
        out.println("        const query = encodeURIComponent(title + ' official trailer');");
        out.println("        videoContainer.innerHTML = '<iframe src=\"https://www.youtube.com/embed?listType=search&list=' + query + '\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>';");
        out.println("        videoContainer.style.display = 'block';");
        out.println("      }");
        out.println("    })");
        out.println("    .catch(error => {");
        out.println("      console.error('Error fetching trailer:', error);");
        out.println("      loadingMessage.style.display = 'none';");
        out.println("      const query = encodeURIComponent(title + ' official trailer');");
        out.println("      videoContainer.innerHTML = '<iframe src=\"https://www.youtube.com/embed?listType=search&list=' + query + '\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture\" allowfullscreen></iframe>';");
        out.println("      videoContainer.style.display = 'block';");
        out.println("    });");
        out.println("}");
        out.println("");
        
        out.println("function closeTrailerModal() {");
        out.println("  const modal = document.getElementById('trailerModal');");
        out.println("  const videoContainer = document.getElementById('videoContainer');");
        out.println("  modal.style.display = 'none';");
        out.println("  videoContainer.innerHTML = ''; // Stop video playback");
        out.println("}");
        out.println("");
        
        // Close modal when clicking outside
        out.println("window.onclick = function(event) {");
        out.println("  const modal = document.getElementById('trailerModal');");
        out.println("  if (event.target == modal) {");
        out.println("    closeTrailerModal();");
        out.println("  }");
        out.println("}");
        out.println("");
        
        // Notification function
        out.println("function showNotification(message) {");
        out.println("  const notification = document.createElement('div');");
        out.println("  notification.className = 'notification show';");
        out.println("  notification.textContent = message;");
        out.println("  document.body.appendChild(notification);");
        out.println("  setTimeout(() => {");
        out.println("    notification.remove();");
        out.println("  }, 3000);");
        out.println("}");
        
        out.println("</script>");
    }
    
    // Fixed API call method
    private JSONObject fetchFromTMDB(String endpoint) throws IOException {
        try {
            
            URL url = new URL(TMDB_BASE_URL + endpoint + "?api_key=" + TMDB_API_KEY);
         
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }
            
            StringBuilder response = new StringBuilder();
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNext()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            
            return new JSONObject(response.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String generateMovieCard(JSONObject movie, Integer userId, Set<Integer> userFavorites, Set<Integer> userHistory) {
        String posterPath = movie.optString("poster_path", "");
        String title = movie.getString("title");
        double rating = movie.getDouble("vote_average");
        int movieId = movie.getInt("id");
        
        boolean isFavorite = userFavorites.contains(movieId);
        boolean isWatched = userHistory.contains(movieId);
        
        StringBuilder card = new StringBuilder();
        card.append("<div class='movie-card'>");
        if (!posterPath.isEmpty()) {
            card.append("<img src='" + TMDB_IMAGE_BASE_URL + posterPath + "' alt='" + title + "' class='movie-poster'>");
        } else {
            card.append("<div style='width: 200px; height: 300px; background-color: #333; display: flex; align-items: center; justify-content: center;'>");
            card.append("<span>No Image</span>");
            card.append("</div>");
        }
        
        card.append("<div class='movie-rating'><i class='fas fa-star'></i> " + String.format("%.1f", rating) + "</div>");
        
        // Action buttons
        card.append("<div class='movie-actions'>");
        
        // Play button
        card.append("<button class='action-btn' onclick='playTrailer(" + movieId + ", \"" + escapeString(title) + "\", \"" + escapeString(posterPath) + "\")'>");
        card.append("<i class='fas fa-play'></i></button>");
        
        // Favorite button (fixed with proper data attributes)
        String favoriteClass = isFavorite ? "action-btn active" : "action-btn";
        String favoriteAction = isFavorite ? "removeFromFavorites(" + movieId + ")" : "addToFavorites(" + movieId + ", \"" + escapeString(title) + "\", \"" + escapeString(posterPath) + "\")";
        card.append("<button id='fav-" + movieId + "' class='" + favoriteClass + "' ");
        card.append("data-title='" + escapeString(title) + "' data-poster='" + escapeString(posterPath) + "' ");
        card.append("onclick='" + favoriteAction + "'>");
        card.append("<i class='fas fa-heart'></i></button>");
        
        // History indicator button
        String historyClass = isWatched ? "action-btn active" : "action-btn";
        card.append("<button id='hist-" + movieId + "' class='" + historyClass + "'>");
        card.append("<i class='fas fa-history'></i></button>");
        card.append("</div>");
        
        card.append("<div class='movie-title'>" + title + "</div>");
        card.append("</div>");
        
        return card.toString();
    }
    
    private Set<Integer> getUserFavorites(Integer userId) {
        Set<Integer> favorites = new HashSet<>();
        String query = "SELECT movie_id FROM user_favorites WHERE user_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                favorites.add(rs.getInt("movie_id"));
            }
            
            System.out.println("Loaded " + favorites.size() + " favorites for user " + userId);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return favorites;
    }
    
    private Set<Integer> getUserHistory(Integer userId) {
        Set<Integer> history = new HashSet<>();
        String query = "SELECT movie_id FROM user_history WHERE user_id = ?";
        
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                history.add(rs.getInt("movie_id"));
            }
            
            System.out.println("Loaded " + history.size() + " history entries for user " + userId);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return history;
    }
    
    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"").replace("'", "\\'").replace("\n", "\\n").replace("\r", "\\r");
    }
    
    private String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}