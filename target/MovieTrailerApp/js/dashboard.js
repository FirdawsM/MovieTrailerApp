// Enhanced dashboard.js with improved favorites functionality

// Toggle favorite status with enhanced feedback and error handling
async function toggleFavorite(movieId, title, posterPath, button) {
    if (!movieId || !title) {
        showNotification('Missing movie information', 'error');
        return;
    }
    
    const isFavorite = button.classList.contains('active');
    const action = isFavorite ? 'remove' : 'add';
    
    // Add loading state to button
    const originalContent = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    button.disabled = true;
    
    try {
        console.log(`Attempting to ${action} movie: ${title} (ID: ${movieId})`);
        
        const response = await fetch('favorites', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            body: `action=${action}&movieId=${movieId}&title=${encodeURIComponent(title)}&posterPath=${encodeURIComponent(posterPath || '')}`
        });
        
        console.log('Response status:', response.status);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('Response data:', data);
        
        if (data.success) {
            // Update button state
            button.classList.toggle('active');
            
            // Show success notification
            showNotification(data.message || (isFavorite ? 'Removed from favorites' : 'Added to favorites'), 'success');
            
            // Update all buttons with the same movie ID (hero section, movie cards, etc.)
            updateAllFavoriteButtons(movieId, !isFavorite);
            
        } else {
            throw new Error(data.message || 'Operation failed');
        }
        
    } catch (error) {
        console.error('Favorite error:', error);
        showNotification(error.message || 'Failed to update favorites', 'error');
        
        // Reset button state on error
        // Don't toggle the class since the operation failed
        
    } finally {
        // Restore button content and enable
        button.innerHTML = originalContent;
        button.disabled = false;
    }
}

// Update all favorite buttons for a specific movie
function updateAllFavoriteButtons(movieId, isFavorite) {
    const buttons = document.querySelectorAll(`[id*="fav-${movieId}"], [id*="hero-fav-${movieId}"]`);
    buttons.forEach(btn => {
        if (isFavorite) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

// Check if movie is in favorites (useful for initial page load)
async function checkFavoriteStatus(movieId) {
    try {
        const response = await fetch(`favorites?action=check&movieId=${movieId}`);
        const data = await response.json();
        return data.success ? data.isFavorite : false;
    } catch (error) {
        console.error('Error checking favorite status:', error);
        return false;
    }
}

// Initialize favorite buttons on page load
async function initializeFavoriteButtons() {
    const favoriteButtons = document.querySelectorAll('[onclick*="toggleFavorite"]');
    
    for (const button of favoriteButtons) {
        try {
            // Extract movie ID from onclick attribute
            const onclickAttr = button.getAttribute('onclick');
            const movieIdMatch = onclickAttr.match(/toggleFavorite\((\d+)/);
            
            if (movieIdMatch) {
                const movieId = movieIdMatch[1];
                const isFavorite = await checkFavoriteStatus(movieId);
                
                if (isFavorite) {
                    button.classList.add('active');
                }
            }
        } catch (error) {
            console.error('Error initializing favorite button:', error);
        }
    }
}

// Enhanced notification function with better styling
function showNotification(message, type = 'success') {
    // Remove any existing notifications
    const existingNotifications = document.querySelectorAll('.notification');
    existingNotifications.forEach(notif => notif.remove());
    
    const notification = document.createElement('div');
    notification.className = `notification ${type} show`;
    
    const icon = type === 'success' ? 'check-circle' : 
                 type === 'error' ? 'exclamation-circle' : 
                 'info-circle';
    
    notification.innerHTML = `
        <i class="fas fa-${icon}"></i>
        <span>${message}</span>
        <button class="notification-close" onclick="this.parentElement.remove()">
            <i class="fas fa-times"></i>
        </button>
    `;
    
    document.body.appendChild(notification);
    
    // Auto-remove after delay
    setTimeout(() => {
        if (notification.parentElement) {
            notification.classList.remove('show');
            setTimeout(() => {
                if (notification.parentElement) {
                    notification.remove();
                }
            }, 300);
        }
    }, 5000);
}

// Enhanced trailer playback function (keeping your existing logic)
async function playTrailer(movieTitle, movieId, posterPath) {
    const modal = document.getElementById('trailer-modal');
    const iframe = document.getElementById('trailer-iframe');

    try {
        // Show modal and reset iframe
        modal.style.display = 'block';
        if (iframe) iframe.src = 'about:blank';

        // Add to watch history
        const historyResponse = await fetch('history', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `action=add&movieId=${movieId}&title=${encodeURIComponent(movieTitle)}&posterPath=${encodeURIComponent(posterPath || '')}`
        });

        if (!historyResponse.ok) {
            throw new Error('Failed to add to history');
        }

        const historyData = await historyResponse.json();
        if (historyData.success) {
            const historyBtn = document.getElementById(`hist-${movieId}`) || document.getElementById(`hero-hist-${movieId}`);
            if (historyBtn) historyBtn.classList.add('active');

            // Fetch trailer from servlet (relative path)
            const trailerResponse = await fetch(`trailer?title=${encodeURIComponent(movieTitle)}&movieId=${movieId}`);
            if (!trailerResponse.ok) {
                throw new Error('Trailer service unavailable');
            }

            const trailerData = await trailerResponse.json();
            if (iframe) {
                if (trailerData.success && trailerData.embedUrl) {
                    iframe.src = `${trailerData.embedUrl}?autoplay=1`;
                } else {
                    // Keep playback within site by embedding YouTube search results
                    const query = encodeURIComponent(movieTitle + ' official trailer');
                    iframe.src = `https://www.youtube.com/embed?listType=search&list=${query}`;
                }
            }
        } else {
            throw new Error(historyData.message || 'Error adding to history');
        }
    } catch (error) {
        console.error('Trailer error:', error);
        // Keep user on site: embed YouTube search results into the iframe
        const iframe = document.getElementById('trailer-iframe');
        const modal = document.getElementById('trailer-modal');
        if (modal) modal.style.display = 'block';
        if (iframe) {
            const query = encodeURIComponent(movieTitle + ' official trailer');
            iframe.src = `https://www.youtube.com/embed?listType=search&list=${query}`;
        }
    }
}

// Helper function to sanitize HTML content
function sanitizeHTML(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// Helper function for fallback UI
function showFallbackTrailerUI(movieTitle) {
    // Always keep user on site: embed YouTube search results in modal iframe
    const modal = document.getElementById('trailer-modal');
    const iframe = document.getElementById('trailer-iframe');
    if (modal) modal.style.display = 'block';
    if (iframe) {
        const query = encodeURIComponent(movieTitle + ' official trailer');
        iframe.src = `https://www.youtube.com/embed?listType=search&list=${query}`;
    }
}

// Close video modal and clean up
function closeVideoModal() {
    const modal = document.getElementById('trailer-modal');
    const iframe = document.getElementById('trailer-iframe');
    if (iframe) iframe.src = '';
    if (modal) modal.style.display = 'none';
}

// Close modal when clicking outside
window.addEventListener('click', (event) => {
    const modal = document.getElementById('trailer-modal');
    if (event.target === modal) {
        closeVideoModal();
    }
});

// Close modal with Escape key
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
        closeVideoModal();
    }
});

// Wire up close button in modal if present
document.addEventListener('DOMContentLoaded', () => {
    const closeBtn = document.querySelector('#trailer-modal .close-modal');
    if (closeBtn) closeBtn.addEventListener('click', closeVideoModal);
});

// Initialize favorites when page loads
document.addEventListener('DOMContentLoaded', () => {
    console.log('Initializing favorite buttons...');
    initializeFavoriteButtons();
});

// JavaScript functions to properly display favorites

// Load and display favorites on the favorites page
async function loadFavoritesPage() {
    const container = document.getElementById('favoritesContainer') || document.querySelector('.favorites-container');
    
    if (!container) {
        console.error('Favorites container not found');
        return;
    }
    
    try {
        // Show loading state
        container.innerHTML = `
            <div class="loading-spinner">
                <i class="fas fa-spinner fa-spin"></i>
                <p>Loading your favorites...</p>
            </div>
        `;
        
        // Fetch detailed favorites
        const response = await fetch('favorites?view=detailed');
        const data = await response.json();
        
        if (data.success) {
            const favs = data.data && Array.isArray(data.data.favorites) ? data.data.favorites : [];
            if (favs.length > 0) {
                displayFavorites(favs, container);
            } else {
                displayEmptyFavorites(container);
            }
        } else {
            throw new Error(data.message || 'Failed to load favorites');
        }
        
    } catch (error) {
        console.error('Error loading favorites:', error);
        container.innerHTML = `
            <div class="error-message">
                <i class="fas fa-exclamation-triangle"></i>
                <p>Failed to load favorites: ${error.message}</p>
                <button onclick="loadFavoritesPage()" class="retry-btn">
                    <i class="fas fa-refresh"></i> Retry
                </button>
            </div>
        `;
    }
}

// Display favorites in a grid layout
function displayFavorites(favorites, container) {
    const favoritesHtml = favorites.map(movie => `
        <div class="favorite-card" data-movie-id="${movie.movieId}">
            <div class="movie-poster">
                <img src="${movie.fullPosterUrl || 'https://via.placeholder.com/300x450?text=No+Poster'}" 
                     alt="${movie.title}"
                     onerror="this.src='https://via.placeholder.com/300x450?text=No+Poster'">
                <div class="movie-overlay">
                    <button class="play-btn" onclick="playTrailer('${escapeHtml(movie.title)}', ${movie.movieId}, '${movie.posterPath}')">
                        <i class="fas fa-play"></i>
                    </button>
                </div>
            </div>
            <div class="movie-info">
                <h3 class="movie-title">${movie.title}</h3>
                <div class="movie-actions">
                    <button class="favorite-btn active" 
                            onclick="toggleFavorite(${movie.movieId}, '${escapeHtml(movie.title)}', '${movie.posterPath}', this)"
                            title="Remove from favorites">
                        <i class="fas fa-heart"></i>
                    </button>
                    <span class="added-date">Added ${formatDate(movie.addedDate)}</span>
                </div>
            </div>
        </div>
    `).join('');
    
    container.innerHTML = `
        <div class="favorites-header">
            <h2><i class="fas fa-heart"></i> My Favorites (${favorites.length})</h2>
            <div class="favorites-controls">
                <button onclick="clearAllFavorites()" class="clear-all-btn">
                    <i class="fas fa-trash"></i> Clear All
                </button>
            </div>
        </div>
        <div class="favorites-grid">
            ${favoritesHtml}
        </div>
    `;
}

// Display empty favorites state
function displayEmptyFavorites(container) {
    container.innerHTML = `
        <div class="empty-favorites">
            <i class="fas fa-heart-broken"></i>
            <h2>No Favorites Yet</h2>
            <p>Start adding movies to your favorites to see them here!</p>
            <a href="dashboard" class="browse-btn">
                <i class="fas fa-search"></i> Browse Movies
            </a>
        </div>
    `;
}

// Load favorites for dashboard (just IDs for button states)
async function loadDashboardFavorites() {
    try {
        const response = await fetch('favorites');
        const data = await response.json();
        
        if (data.success) {
            const favIds = (data.data && Array.isArray(data.data.favorites)) ? data.data.favorites : (data.favorites || []);
            // Update favorite button states on dashboard
            favIds.forEach(movieId => {
                const buttons = document.querySelectorAll(`[data-movie-id="${movieId}"] .favorite-btn, #fav-${movieId}, #hero-fav-${movieId}`);
                buttons.forEach(btn => btn.classList.add('active'));
            });
            
            console.log(`Loaded ${favIds.length} favorites for dashboard`);
        }
    } catch (error) {
        console.error('Error loading dashboard favorites:', error);
    }
}

// Clear all favorites
async function clearAllFavorites() {
    if (!confirm('Are you sure you want to remove all favorites? This cannot be undone.')) {
        return;
    }
    
    try {
        // Get current favorites
        const response = await fetch('favorites');
        const data = await response.json();
        
        if (data.success && data.favorites) {
            // Remove each favorite
            const removePromises = data.favorites.map(movieId => 
                fetch('favorites', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                    body: `action=remove&movieId=${movieId}`
                })
            );
            
            await Promise.all(removePromises);
            
            showNotification('All favorites cleared successfully!', 'success');
            loadFavoritesPage(); // Reload the page
        }
    } catch (error) {
        console.error('Error clearing favorites:', error);
        showNotification('Failed to clear favorites', 'error');
    }
}

// Helper functions
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(timestamp) {
    const date = new Date(timestamp);
    const now = new Date();
    const diffTime = Math.abs(now - date);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    
    if (diffDays === 1) return 'Yesterday';
    if (diffDays < 7) return `${diffDays} days ago`;
    if (diffDays < 30) return `${Math.ceil(diffDays / 7)} weeks ago`;
    return date.toLocaleDateString();
}

// Initialize favorites based on current page
document.addEventListener('DOMContentLoaded', () => {
    const currentPath = window.location.pathname;
    
    if (currentPath.includes('favorites')) {
        // We're on the favorites page
        loadFavoritesPage();
    } else {
        // We're on dashboard or other page
        loadDashboardFavorites();
    }
});

// Enhanced toggle function that works with the display
async function toggleFavorite(movieId, title, posterPath, button) {
    if (!movieId || !title) {
        showNotification('Missing movie information', 'error');
        return;
    }
    
    const isFavorite = button.classList.contains('active');
    const action = isFavorite ? 'remove' : 'add';
    
    // Add loading state to button
    const originalContent = button.innerHTML;
    button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    button.disabled = true;
    
    try {
        const response = await fetch('favorites', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/x-www-form-urlencoded',
                'Accept': 'application/json'
            },
            body: `action=${action}&movieId=${movieId}&title=${encodeURIComponent(title)}&posterPath=${encodeURIComponent(posterPath || '')}`
        });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        
        if (data.success) {
            // Update button state
            button.classList.toggle('active');
            showNotification(data.message, 'success');
            
            // If we're on the favorites page and removed an item, remove the card
            if (action === 'remove' && window.location.pathname.includes('favorites')) {
                const card = button.closest('.favorite-card');
                if (card) {
                    card.style.animation = 'fadeOut 0.3s ease';
                    setTimeout(() => {
                        card.remove();
                        // Check if no more favorites
                        if (document.querySelectorAll('.favorite-card').length === 0) {
                            loadFavoritesPage();
                        }
                    }, 300);
                }
            }
            
            // Update all buttons with the same movie ID
            updateAllFavoriteButtons(movieId, !isFavorite);
            
        } else {
            throw new Error(data.message || 'Operation failed');
        }
        
    } catch (error) {
        console.error('Favorite error:', error);
        showNotification(error.message || 'Failed to update favorites', 'error');
    } finally {
        // Restore button content and enable
        button.innerHTML = originalContent;
        button.disabled = false;
    }
}

// Update all favorite buttons for a specific movie
function updateAllFavoriteButtons(movieId, isFavorite) {
    const buttons = document.querySelectorAll(
        `[data-movie-id="${movieId}"] .favorite-btn, ` +
        `#fav-${movieId}, ` +
        `#hero-fav-${movieId}, ` +
        `button[onclick*="toggleFavorite(${movieId}"]`
    );
    
    buttons.forEach(btn => {
        if (isFavorite) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}