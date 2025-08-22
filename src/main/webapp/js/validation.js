/**
 * Movie Trailer App - Main Dashboard Script
 * Includes dashboard functionality and login/register validation
 */

// Main Dashboard Functionality
document.addEventListener('DOMContentLoaded', function() {
    // Check if we're on the dashboard page
    if (document.querySelector('.dashboard-container')) {
        initDashboard();
    }
    
    // Check if we're on the login page
    if (document.getElementById('login-form')) {
        document.getElementById('login-form').onsubmit = validateLoginForm;
    }
    
    // Check if we're on the registration page
    if (document.getElementById('register-form')) {
        document.getElementById('register-form').onsubmit = validateRegisterForm;
    }
});

/**
 * Initialize dashboard functionality
 */
function initDashboard() {
    // Sample data - in a real app, this would come from your backend API
    const movieData = {
        trending: [
            { 
                id: 1, 
                title: 'Dune: Part Two', 
                year: 2024, 
                rating: 8.8, 
                poster: 'https://via.placeholder.com/300x450?text=Dune+Part+Two', 
                trailer: 'https://www.youtube.com/embed/WarB15poINY',
                description: 'Paul Atreides unites with Chani and the Fremen while seeking revenge against the conspirators who destroyed his family.'
            },
            { 
                id: 2, 
                title: 'The Batman', 
                year: 2022, 
                rating: 7.9, 
                poster: 'https://via.placeholder.com/300x450?text=The+Batman', 
                trailer: 'https://www.youtube.com/embed/mqqft2x_Aa4',
                description: 'When a sadistic serial killer begins murdering key political figures in Gotham, Batman is forced to investigate.'
            },
            { 
                id: 3, 
                title: 'Oppenheimer', 
                year: 2023, 
                rating: 8.6, 
                poster: 'https://via.placeholder.com/300x450?text=Oppenheimer', 
                trailer: 'https://www.youtube.com/embed/uYPbbksJxIg',
                description: 'The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.'
            }
        ],
        recent: [
            { 
                id: 4, 
                title: 'Barbie', 
                year: 2023, 
                rating: 7.3, 
                poster: 'https://via.placeholder.com/300x450?text=Barbie', 
                trailer: 'https://www.youtube.com/embed/pBk4NYhWNMM',
                description: 'Barbie suffers a crisis that leads her to question her world and her existence.'
            },
            { 
                id: 5, 
                title: 'Avatar: The Way of Water', 
                year: 2022, 
                rating: 7.6, 
                poster: 'https://via.placeholder.com/300x450?text=Avatar+2', 
                trailer: 'https://www.youtube.com/embed/d9MyW72ELq0',
                description: 'Jake Sully lives with his newfound family formed on the planet of Pandora.'
            }
        ],
        watchlist: [
            { 
                id: 6, 
                title: 'Interstellar', 
                year: 2014, 
                rating: 8.6, 
                poster: 'https://via.placeholder.com/300x450?text=Interstellar', 
                trailer: 'https://www.youtube.com/embed/zSWdZVtXT7E',
                description: 'A team of explorers travel through a wormhole in space in an attempt to ensure humanity\'s survival.'
            }
        ]
    };

    // DOM Elements
    const trendingContainer = document.getElementById('trending-trailers');
    const recentContainer = document.getElementById('recent-trailers');
    const watchlistContainer = document.getElementById('watchlist-trailers');
    const modal = document.getElementById('trailer-modal');
    const closeModal = document.querySelector('.close-modal');
    const trailerIframe = document.getElementById('trailer-iframe');
    const favoritesBtn = document.getElementById('add-to-favorites');
    const favoritesCount = document.getElementById('favorites-count');
    const modalTitle = document.getElementById('modal-movie-title');
    const modalYear = document.getElementById('modal-movie-year');
    const modalRating = document.getElementById('modal-movie-rating');
    const modalDescription = document.getElementById('modal-movie-description');

    // Initialize favorites from localStorage or default to empty array
    let favorites = JSON.parse(localStorage.getItem('favorites')) || [];
    updateFavoritesCount();

    // Render movie cards
    function renderMovies(movies, container) {
        if (!container) return;
        
        container.innerHTML = movies.map(movie => `
            <div class="trailer-card" data-id="${movie.id}" data-trailer="${movie.trailer}">
                <div class="trailer-poster" style="background-image: url('${movie.poster}')">
                    <div class="trailer-rating">
                        <i class="fas fa-star"></i>
                        ${movie.rating}
                    </div>
                </div>
                <div class="trailer-info">
                    <h3>${movie.title}</h3>
                    <p>${movie.year}</p>
                </div>
            </div>
        `).join('');
    }

    // Initial render of all sections
    function renderAllSections() {
        renderMovies(movieData.trending, trendingContainer);
        renderMovies(movieData.recent, recentContainer);
        renderMovies(movieData.watchlist, watchlistContainer);
    }

    // Update favorites count display
    function updateFavoritesCount() {
        if (favoritesCount) {
            favoritesCount.textContent = favorites.length;
        }
    }

    // Open trailer modal with movie details
    function openTrailerModal(trailerUrl, movieId) {
        // Find the movie in our data
        const allMovies = [...movieData.trending, ...movieData.recent, ...movieData.watchlist];
        const movie = allMovies.find(m => m.id == movieId);
        
        if (!movie) return;
        
        // Set modal content
        trailerIframe.src = trailerUrl;
        modalTitle.textContent = movie.title;
        modalYear.textContent = movie.year;
        modalRating.innerHTML = `<i class="fas fa-star"></i> ${movie.rating}`;
        modalDescription.textContent = movie.description;
        
        // Show modal
        modal.classList.add('show');
        document.body.style.overflow = 'hidden';
        
        // Update favorite button state
        updateFavoriteButton(movieId);
    }

    // Close trailer modal
    function closeTrailerModal() {
        modal.classList.remove('show');
        trailerIframe.src = '';
        document.body.style.overflow = 'auto';
    }

    // Update favorite button appearance and behavior
    function updateFavoriteButton(movieId) {
        if (!favoritesBtn) return;
        
        const isFavorite = favorites.includes(movieId);
        
        if (isFavorite) {
            favoritesBtn.innerHTML = '<i class="fas fa-heart"></i> Remove from Favorites';
            favoritesBtn.classList.add('added');
        } else {
            favoritesBtn.innerHTML = '<i class="far fa-heart"></i> Add to Favorites';
            favoritesBtn.classList.remove('added');
        }
        
        favoritesBtn.onclick = function() {
            toggleFavorite(movieId);
        };
    }

    // Toggle movie in favorites
    function toggleFavorite(movieId) {
        const index = favorites.indexOf(movieId);
        
        if (index === -1) {
            favorites.push(movieId);
        } else {
            favorites.splice(index, 1);
        }
        
        // Save to localStorage
        localStorage.setItem('favorites', JSON.stringify(favorites));
        
        // Update UI
        updateFavoritesCount();
        updateFavoriteButton(movieId);
    }

    // Initialize event listeners
    function initEventListeners() {
        // Event delegation for movie cards
        document.addEventListener('click', function(e) {
            const card = e.target.closest('.trailer-card');
            if (card) {
                const trailerUrl = card.dataset.trailer;
                const movieId = card.dataset.id;
                openTrailerModal(trailerUrl, movieId);
            }
        });

        // Close modal events
        if (closeModal) {
            closeModal.addEventListener('click', closeTrailerModal);
        }
        
        window.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeTrailerModal();
            }
        });

        // Search form
        const searchForm = document.querySelector('.search-form');
        if (searchForm) {
            searchForm.addEventListener('submit', function(e) {
                e.preventDefault();
                const query = this.querySelector('input').value.trim();
                if (query) {
                    // In a real app, this would redirect to search results
                    alert(`Searching for: ${query}`);
                    // window.location.href = `search.jsp?query=${encodeURIComponent(query)}`;
                }
            });
        }
    }

    // Initialize dashboard
    renderAllSections();
    initEventListeners();
}

/**
 * Validate login form
 */
function validateLoginForm() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;
    const errorElement = document.getElementById('login-error');
    
    // Clear previous errors
    errorElement.textContent = '';
    
    // Validate username
    if (username === '') {
        showError(errorElement, 'Please enter your username');
        return false;
    }
    
    // Validate password
    if (password === '') {
        showError(errorElement, 'Please enter your password');
        return false;
    }
    
    if (password.length < 6) {
        showError(errorElement, 'Password must be at least 6 characters');
        return false;
    }
    
    return true;
}

/**
 * Validate registration form
 */
function validateRegisterForm() {
    const username = document.getElementById('username').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const errorElement = document.getElementById('register-error');
    
    // Clear previous errors
    errorElement.textContent = '';
    
    // Username validation
    if (username.length < 4) {
        showError(errorElement, 'Username must be at least 4 characters long');
        return false;
    }
    
    // Email validation
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
        showError(errorElement, 'Please enter a valid email address');
        return false;
    }
    
    // Password validation
    if (password.length < 8) {
        showError(errorElement, 'Password must be at least 8 characters long');
        return false;
    }
    
    if (password !== confirmPassword) {
        showError(errorElement, 'Passwords do not match');
        return false;
    }
    
    // Check password complexity (optional)
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumber = /\d/.test(password);
    
    if (!hasUpperCase || !hasLowerCase || !hasNumber) {
        showError(errorElement, 'Password must contain uppercase, lowercase letters and a number');
        return false;
    }
    
    return true;
}

/**
 * Display error message
 */
function showError(element, message) {
    if (!element) return;
    
    element.textContent = message;
    element.style.display = 'block';
    
    // Hide error after 5 seconds
    setTimeout(() => {
        element.style.display = 'none';
    }, 5000);
}

/**
 * Show loading spinner
 */
function showLoading() {
    const spinner = document.createElement('div');
    spinner.className = 'loading-spinner';
    spinner.innerHTML = '<div class="spinner"></div>';
    document.body.appendChild(spinner);
}

/**
 * Hide loading spinner
 */
function hideLoading() {
    const spinner = document.querySelector('.loading-spinner');
    if (spinner) {
        spinner.remove();
    }
}