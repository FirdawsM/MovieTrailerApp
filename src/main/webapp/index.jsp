<%-- 
    Document   : index
    Created on : 4 Aug 2025, 12:57:48
    Author     : firda
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Movie Trailer App</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="css/index.css" />
</head>
<body>
    <nav class="navbar">
        <div class="logo">MOVIETRAILER</div>
        <div class="nav-links">
            <a href="dashboard"><i class="fas fa-home"></i> Home</a>
            <a href="favorites"><i class="fas fa-heart"></i> Favorites</a>
            <a href="history"><i class="fas fa-history"></i> History</a>
        </div>
        <div>
            <a href="login.jsp" class="btn btn-primary" style="margin-right:10px;">Login</a>
            <a href="register.jsp" class="btn btn-primary">Register</a>
        </div>
    </nav>
    <section class="hero">
        <h1>Welcome to Movie Trailer App</h1>
        <p>Discover, favorite, and keep track of the latest movies and TV shows. Watch trailers, manage your favorites, and never miss a trending title!</p>
        <a href="dashboard" class="btn-primary">Browse Movies</a>
    </section>
    <footer class="footer">
        &copy; 2025 MovieTrailer App. All rights reserved.
    </footer>
    <script src="js/index.js"></script>
</body>
</html>
