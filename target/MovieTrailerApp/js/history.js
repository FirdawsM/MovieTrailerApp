// History page interactions

async function clearAllHistory() {
  if (!confirm('Clear all watch history?')) return;
  try {
    const res = await fetch('history', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: 'action=clear&movieId=0'
    });
    const data = await res.json();
    if (data.success) {
      const grid = document.querySelector('.movie-grid');
      if (grid) grid.innerHTML = '';
      showEmptyState();
      notify('History cleared');
    } else {
      notify(data.message || 'Failed to clear history');
    }
  } catch (e) {
    console.error(e);
    notify('Error clearing history');
  }
}

async function removeFromHistory(movieId, btn) {
  try {
    const res = await fetch('history', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: `action=remove&movieId=${encodeURIComponent(movieId)}`
    });
    const data = await res.json();
    if (data.success) {
      const card = btn.closest('.history-card');
      if (card) card.remove();
      const anyLeft = document.querySelector('.history-card');
      if (!anyLeft) showEmptyState();
      notify('Removed from history');
    } else {
      notify(data.message || 'Failed to remove');
    }
  } catch (e) {
    console.error(e);
    notify('Error removing from history');
  }
}

function watchAgain(movieId, title, posterPath) {
  try {
    if (typeof playTrailer === 'function') {
      playTrailer(title, movieId, posterPath);
    } else {
      // Fallback to YouTube search
      window.open(`https://www.youtube.com/results?search_query=${encodeURIComponent(title + ' official trailer')}`, '_blank');
    }
  } catch (e) {
    console.error(e);
  }
}

function showEmptyState() {
  const grid = document.querySelector('.movie-grid');
  if (!grid) return;
  grid.outerHTML = `
    <div class="empty-state">
      <i class="fas fa-history" style="font-size: 4rem; color: #666; margin-bottom: 20px;"></i>
      <h3>No watch history yet</h3>
      <p>Movies you watch will appear here so you can easily find them again.</p>
      <a href="dashboard" class="btn btn-primary" style="margin-top: 20px; display: inline-block; text-decoration: none;">Browse Movies</a>
    </div>
  `;
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
  n.style.opacity = '1';
  setTimeout(() => { n.style.opacity = '0'; }, 2000);
}
