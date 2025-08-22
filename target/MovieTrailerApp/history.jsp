<%-- 
    Document   : history
    Created on : 4 Aug 2025, 12:59:08
    Author     : firda
--%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Watch History</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
    <link rel="stylesheet" href="css/history.css" />
</head>
<body>
    <nav class="navbar">
        <div class="logo">MOVIETRAILER</div>
        <div class="nav-links">
            <a href="dashboard"><i class="fas fa-home"></i> Home</a>
            <a href="favorites"><i class="fas fa-heart"></i> Favorites</a>
            <a href="history" style="color:#e50914;"><i class="fas fa-history"></i> History</a>
        </div>
        <img src="https://via.placeholder.com/40" alt="Profile" class="profile" />
    </nav>
    <div class="main-content">
        <div class="page-header">
            <h1><i class="fas fa-history"></i> Watch History <span id="historyCount" style="font-size:0.85em;color:#b3b3b3;"></span></h1>
            <button class="btn btn-danger" onclick="clearAllHistory()" style="float:right;margin-top:-40px;">Clear All History</button>
            <div id="status" style="color:#b3b3b3;"></div>
        </div>
        <section class="section">
            <div class="movie-grid"></div>
        </section>
        <script src="js/history.js"></script>
    </div>

    <!-- Trailer Modal (same IDs as dashboard) -->
    <div class="modal" id="trailer-modal" style="display:none;">
        <div class="modal-content">
            <span class="close-modal" style="position:absolute;right:12px;top:8px;cursor:pointer;font-size:24px;color:#aaa">&times;</span>
            <div class="modal-body" style="padding:0;">
                <iframe id="trailer-iframe" width="100%" height="500" frameborder="0"
                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>
            </div>
        </div>
    </div>

    <div id="notification" class="notification" style="display:none;"></div>

    <script>
        const APP_BASE = '<%= request.getContextPath() %>/';
    </script>
    <script src="js/dashboard.js?v=4"></script>
    <script>
        document.addEventListener('DOMContentLoaded', () => {
            const closeBtn = document.querySelector('#trailer-modal .close-modal');
            if (closeBtn) closeBtn.addEventListener('click', closeVideoModal);
            // You may want to call a function to load history here if needed
        });
    </script>
</body>
</html>
