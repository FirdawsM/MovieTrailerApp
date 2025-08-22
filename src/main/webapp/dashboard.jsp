<%-- 
    Document   : dashboard
    Created on : 4 Aug 2025, 12:58:36
    Author     : firda
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.mycompany.movietrailerapp.models.User" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - Movie Trailer App</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" type="text/css" href="css/style.css">
</head>
<body>
    <div class="dashboard-container">
        <!-- Sidebar Navigation -->
        <aside class="sidebar">
            <div class="user-profile">
                <div class="avatar">
                    <i class="fas fa-user"></i>
                </div>
                <h3><%= user.getUsername() %></h3>
                <p>Movie Enthusiast</p>
            </div>
            
            <nav class="main-nav">
                <ul>
                    <li class="active">
                        <a href="dashboard.jsp">
                            <i class="fas fa-home"></i>
                            <span>Dashboard</span>
                        </a>
                    </li>
                    <li>
                        <a href="favorites">
                            <i class="fas fa-heart"></i>
                            <span>My Favorites</span>
                            <span class="badge" id="favorites-count">0</span>
                        </a>
                    </li>
                    <li>
                        <a href="history">
                            <i class="fas fa-history"></i>
                            <span>Watch History</span>
                        </a>
                    </li>
                    <li>
                        <a href="recommendations.jsp">
                            <i class="fas fa-star"></i>
                            <span>Recommendations</span>
                        </a>
                    </li>
                    <li>
                        <a href="profile.jsp">
                            <i class="fas fa-user-cog"></i>
                            <span>Profile Settings</span>
                        </a>
                    </li>
                </ul>
            </nav>
            
            <div class="logout-section">
                <a href="login?logout=true" class="btn-logout">
                    <i class="fas fa-sign-out-alt"></i>
                    <span>Logout</span>
                </a>
            </div>
        </aside>

        <!-- Main Content Area -->
        <main class="main-content">
            <header class="main-header">
                <h1>Welcome Back, <%= user.getUsername() %>!</h1>
                <div class="search-container">
                    <form action="search" method="get" class="search-form">
                        <input type="text" name="query" placeholder="Search for movies..." required>
                        <button type="submit" class="btn-search">
                            <i class="fas fa-search"></i>
                        </button>
                    </form>
                </div>
            </header>
            
            <!-- Featured Trailers Section -->
            <section class="featured-section">
                <h2><i class="fas fa-fire"></i> Trending Now</h2>
                <div class="trailers-grid" id="trending-trailers">
                    <!-- Content will be loaded via JavaScript -->
                </div>
            </section>
            
            <!-- Recently Added Section -->
            <section class="recent-section">
                <h2><i class="fas fa-clock"></i> Recently Added</h2>
                <div class="trailers-grid" id="recent-trailers">
                    <!-- Content will be loaded via JavaScript -->
                </div>
            </section>
            
            <!-- Your Watchlist Section -->
            <section class="watchlist-section">
                <h2><i class="fas fa-bookmark"></i> Your Watchlist</h2>
                <div class="trailers-grid" id="watchlist-trailers">
                    <!-- Content will be loaded via JavaScript -->
                </div>
            </section>

            <!-- My Favorites Section -->
            <section class="favorites-section">
                <h2><i class="fas fa-heart"></i> My Favorites</h2>
                <div class="trailers-grid" id="dashboard-favorites-grid">
                    <!-- Content will be loaded via JavaScript -->
                </div>
            </section>
        </main>
    </div>
    
    <!-- Movie Trailer Modal -->
    <div class="modal" id="trailer-modal">
        <div class="modal-content">
            <span class="close-modal">&times;</span>
            <div class="modal-body">
                <iframe id="trailer-iframe" width="100%" height="500" frameborder="0" allowfullscreen></iframe>
                <div class="modal-actions">
                    <button class="btn btn-favorite" id="add-to-favorites">
                        <i class="far fa-heart"></i> Add to Favorites
                    </button>
                </div>
            </div>
        </div>
    </div>
    
    <script src="js/dashboard.js?v=4"></script>
</body>
</html>