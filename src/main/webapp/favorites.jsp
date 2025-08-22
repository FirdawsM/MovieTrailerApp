<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>My Favorites</title>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css" />
  <link rel="stylesheet" href="css/favorites.css" />
</head>
<body>
  <nav class="navbar">
    <div class="logo">MOVIETRAILER</div>
    <div class="nav-links">
      <a href="dashboard"><i class="fas fa-home"></i> Home</a>
      <a href="favorites" style="color:#e50914;"><i class="fas fa-heart"></i> Favorites</a>
      <a href="history"><i class="fas fa-history"></i> History</a>
    </div>
    <img src="https://via.placeholder.com/40" alt="Profile" class="profile" />
  </nav>
  <div class="main-content">
    <div class="page-header">
      <h1><i class="fas fa-heart"></i> My Favorites <span id="favCount" style="font-size:0.85em;color:#b3b3b3;"></span></h1>
      <div id="status" style="color:#b3b3b3;"></div>
    </div>

    <section class="section">
      <div id="favoritesGrid" class="movie-grid"></div>
    </section>
  <script src="js/favorites.js"></script>
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
      loadFavorites();
    });

    function setStatus(msg) {
      document.getElementById('status').textContent = msg || '';
    }

    function escapeHtml(str) {
      const div = document.createElement('div');
      div.textContent = str || '';
      return div.innerHTML;
    }

    async function loadFavorites() {
      setStatus('Loading...');
      const grid = document.getElementById('favoritesGrid');
      grid.innerHTML = '<div class="loading-spinner" style="text-align:center;padding:20px;"><i class="fas fa-spinner fa-spin"></i> Loading favorites...</div>';

      try {
        const res = await fetch(APP_BASE + 'favorites?view=detailed', {
          headers: { 'Accept': 'application/json' }
        });
        const ct = (res.headers.get('content-type') || '').toLowerCase();
        if (!res.ok || ct.indexOf('application/json') === -1) {
          const text = await res.text();
          throw new Error('Non-JSON response (' + res.status + ')');
        }
        const data = await res.json();
        if (!data.success) throw new Error(data.message || 'Failed to load favorites');

        const list = (data.data && Array.isArray(data.data.favorites)) ? data.data.favorites : (data.favorites || []);
        renderFavorites(list);
        setStatus('');
      } catch (err) {
        console.error('Favorites load failed:', err);
        setStatus('Error loading favorites.');
        document.getElementById('favoritesGrid').innerHTML = '<div class="error-message">Failed to load favorites. Please try again later.</div>';
      }
    }

    function renderFavorites(list) {
      const grid = document.getElementById('favoritesGrid');
      grid.innerHTML = '';
      const countEl = document.getElementById('favCount');
      if (countEl) countEl.textContent = `(${list.length})`;

      if (!list.length) {
        grid.innerHTML = `
          <div class="empty-state">
            <i class="fas fa-heart-broken" style="font-size: 4rem; color: #666; margin-bottom: 20px;"></i>
            <h3>No favorites yet</h3>
            <p>Start adding movies to your favorites to see them here.</p>
            <a href="dashboard" class="btn btn-primary" style="margin-top: 20px; display: inline-block; text-decoration: none;">Browse Movies</a>
          </div>`;
        return;
      }

      list.forEach(fav => grid.appendChild(createFavoriteCard(fav)));
    }

    function escapeJsString(str) {
      return (str || '').replace(/'/g, "\\'");
    }

    function createFavoriteCard(fav) {
      const posterPath = fav.posterPath || '';
      let posterUrl = fav.fullPosterUrl || '';
      if (!posterUrl) {
        if (posterPath) {
          posterUrl = 'https://image.tmdb.org/t/p/w500' + (posterPath.startsWith('/') ? posterPath : '/' + posterPath);
        } else {
          posterUrl = 'https://via.placeholder.com/300x450?text=No+Poster';
        }
      }
    }