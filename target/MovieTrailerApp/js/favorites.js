// Favorites page JS - Consistent with history.js

function setStatus(msg) {
  document.getElementById('status').textContent = msg || '';
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str || '';
  return div.innerHTML;
}

function escapeJsString(str) {
  return (str || '').replace(/'/g, "\\'");
}

async function loadFavorites() {
  setStatus('Loading...');
  const grid = document.getElementById('favoritesGrid');
  grid.innerHTML = '<div class="loading-spinner" style="text-align:center;padding:20px;"><i class="fas fa-spinner fa-spin"></i> Loading favorites...</div>';

  try {
    const res = await fetch('favorites?view=detailed', {
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
  const title = escapeHtml(fav.title || 'Untitled');
  const added = fav.addedDate ? new Date(fav.addedDate) : null;
  const addedText = added ? added.toLocaleDateString() : 'Unknown';

  const card = document.createElement('div');
  card.className = 'history-card';
  card.dataset.movieId = fav.movieId;
  card.innerHTML = `
    <img src="${posterUrl}" alt="${title}" class="movie-poster" onerror="this.src='https://via.placeholder.com/300x450?text=No+Poster'" />
    <div class="movie-info">
      <h3 class="movie-title">${title}</h3>
      <p class="watch-date">Added: ${addedText}</p>
      <div class="movie-actions">
        <button class="action-btn watch-again" onclick="playTrailer('${title}', ${fav.movieId}, '${escapeJsString(posterPath)}')">
          <i class="fas fa-play"></i> Play
        </button>
        <button class="action-btn remove" onclick="removeFavorite(${fav.movieId}, this)">
          <i class="fas fa-trash"></i> Remove
        </button>
      </div>
    </div>`;
  return card;
}

async function removeFavorite(movieId, btn) {
  setStatus('Removing...');
  try {
    const form = new URLSearchParams();
    form.set('action', 'remove');
    form.set('movieId', String(movieId));
    form.set('movie_id', String(movieId));
    const res = await fetch('favorites', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
      body: form.toString()
    });
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (!res.ok || ct.indexOf('application/json') === -1) {
      const text = await res.text();
      throw new Error('Non-JSON response (' + res.status + ')');
    }
    const json = await res.json();
    if (!json.success) throw new Error(json.message || 'Failed to remove favorite');

    const card = btn.closest('.history-card');
    if (card) card.remove();
    const left = document.querySelectorAll('.history-card').length;
    const countEl = document.getElementById('favCount');
    if (countEl) countEl.textContent = `(${left})`;
    if (left === 0) renderFavorites([]);
    setStatus('');
  } catch (err) {
    console.error('Remove failed:', err);
    setStatus('Failed to remove.');
    alert('Failed to remove from favorites.');
  }
}

function notify(msg) {
  let n = document.getElementById('notification');
  if (!n) {
    n = document.createElement('div');
    n.id = 'notification';
    n.className = 'notification';
    document.body.appendChild(n);
  }
  n.textContent = msg;
  n.style.display = 'block';
  setTimeout(() => { n.style.display = 'none'; }, 2500);
}

document.addEventListener('DOMContentLoaded', () => {
  loadFavorites();
});
