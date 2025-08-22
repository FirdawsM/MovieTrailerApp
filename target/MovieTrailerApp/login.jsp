<%-- 
    Document   : login
    Created on : 4 Aug 2025, 12:58:11
    Author     : firda
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login | Movie Trailer App</title>
    
    <!-- Bootstrap CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Custom CSS with cache busting -->
<link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>

<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center mt-5">
            <div class="col-md-6 col-lg-5">
                <div class="card shadow-sm">
                    <div class="card-body p-4">
                        <div class="text-center mb-4">
                            <i class="fas fa-film fa-3x text-primary mb-3"></i>
                            <h2 class="h4">Movie Trailer App</h2>
                            <p class="text-muted">Please sign in to continue</p>
                        </div>
                        
                        <!-- Display login error if present -->
                        <c:if test="${not empty loginError}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <i class="fas fa-exclamation-circle me-2"></i>
                                ${loginError}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        
                        <!-- Registration success message -->
                        <c:if test="${not empty registrationSuccess}">
                            <div class="alert alert-success alert-dismissible fade show" role="alert">
                                <i class="fas fa-check-circle me-2"></i>
                                ${registrationSuccess}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        
                        <!-- Logout message -->
                        <c:if test="${not empty logoutMessage}">
                            <div class="alert alert-info alert-dismissible fade show" role="alert">
                                <i class="fas fa-info-circle me-2"></i>
                                ${logoutMessage}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>

                        <!-- Login Form -->
                        <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post">
                            <div class="mb-3">
                                <label for="username" class="form-label">Username</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-user"></i></span>
                                    <input type="text" 
                                           class="form-control" 
                                           id="username" 
                                           name="username" 
                                           value="${param.username}" 
                                           placeholder="Enter username" 
                                           required>
                                </div>
                            </div>
                            
                            <div class="mb-3">
                                <label for="password" class="form-label">Password</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-lock"></i></span>
                                    <input type="password" 
                                           class="form-control" 
                                           id="password" 
                                           name="password" 
                                           placeholder="Enter password" 
                                           required>
                                    <button class="btn btn-outline-secondary toggle-password" type="button">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </div>
                            </div>
                            
                            <div class="d-grid mb-3">
                                <button type="submit" class="btn btn-primary">
                                    <i class="fas fa-sign-in-alt me-2"></i> Sign In
                                </button>
                            </div>
                            
                            <div class="d-flex justify-content-between mb-3">
                                <div class="form-check">
                                    <input class="form-check-input" type="checkbox" id="rememberMe" name="rememberMe">
                                    <label class="form-check-label" for="rememberMe">Remember me</label>
                                </div>
                                <a href="${pageContext.request.contextPath}/forgot-password" class="text-decoration-none">Forgot password?</a>
                            </div>
                        </form>
                        
                        <hr class="my-4">
                        
                        <div class="text-center">
                            <p class="mb-0">Don't have an account? 
                                <a href="${pageContext.request.contextPath}/register" class="text-decoration-none">Sign up</a>
                            </p>
                        </div>
                    </div>
                </div>
                
                <div class="text-center mt-3 text-muted">
                    <small>&copy; 2025 Movie Trailer App. All rights reserved.</small>
                </div>
            </div>
        </div>
    </div>

    <!-- Bootstrap Bundle with Popper -->
   <!-- Bootstrap Bundle with Popper -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

<!-- Enhanced Auth Script -->
<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Password toggle functionality with improved accessibility
        document.querySelectorAll('.toggle-password').forEach(button => {
            button.setAttribute('aria-label', 'Toggle password visibility');
            button.addEventListener('click', function(e) {
                e.preventDefault();
                const passwordInput = this.closest('.input-group').querySelector('input');
                const icon = this.querySelector('i');
                
                if (passwordInput.type === 'password') {
                    passwordInput.type = 'text';
                    icon.classList.replace('fa-eye', 'fa-eye-slash');
                    button.setAttribute('aria-pressed', 'true');
                } else {
                    passwordInput.type = 'password';
                    icon.classList.replace('fa-eye-slash', 'fa-eye');
                    button.setAttribute('aria-pressed', 'false');
                }
                
                // Focus back on the input for better UX
                passwordInput.focus();
            });
        });

        // Form validation for both login and register forms
        const forms = document.querySelectorAll('.auth-form');
        forms.forEach(form => {
            form.addEventListener('submit', function(e) {
                if (!validateForm(this)) {
                    e.preventDefault();
                }
            });
        });

        // Toggle between login and register forms
        const authToggleLinks = document.querySelectorAll('.auth-toggle');
        authToggleLinks.forEach(link => {
            link.addEventListener('click', function(e) {
                e.preventDefault();
                document.querySelectorAll('.auth-form-container').forEach(container => {
                    container.classList.toggle('d-none');
                });
                // Reset forms when toggling
                forms.forEach(form => form.reset());
            });
        });

        // Mobile-responsive adjustments
        function handleResponsiveElements() {
            const isMobile = window.innerWidth < 768;
            const passwordInputs = document.querySelectorAll('input[type="password"], input[type="text"][name*="password"]');
            
            passwordInputs.forEach(input => {
                if (isMobile) {
                    input.setAttribute('autocomplete', 'current-password');
                    input.setAttribute('autocapitalize', 'off');
                    input.setAttribute('spellcheck', 'false');
                } else {
                    input.removeAttribute('autocomplete');
                }
            });
        }

        // Initial call and window resize listener
        handleResponsiveElements();
        window.addEventListener('resize', handleResponsiveElements);

        // Strength meter for password fields
        const passwordFields = document.querySelectorAll('input[name*="password"]');
        passwordFields.forEach(field => {
            field.addEventListener('input', function() {
                if (this.form.id === 'register-form') {
                    updatePasswordStrength(this.value);
                }
            });
        });
    });

    // Form validation function
    function validateForm(form) {
        let isValid = true;
        const inputs = form.querySelectorAll('input[required]');
        
        inputs.forEach(input => {
            const errorElement = document.getElementById(`${input.id}-error`);
            
            if (!input.value.trim()) {
                showError(input, errorElement, 'This field is required');
                isValid = false;
            } else if (input.type === 'email' && !validateEmail(input.value)) {
                showError(input, errorElement, 'Please enter a valid email address');
                isValid = false;
            } else if (input.name === 'password' && form.id === 'register-form' && input.value.length < 8) {
                showError(input, errorElement, 'Password must be at least 8 characters');
                isValid = false;
            } else if (input.name === 'confirm_password' && 
                      input.value !== form.querySelector('input[name="password"]').value) {
                showError(input, errorElement, 'Passwords do not match');
                isValid = false;
            } else {
                clearError(input, errorElement);
            }
        });
        
        return isValid;
    }

    function showError(input, errorElement, message) {
        input.classList.add('is-invalid');
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.style.display = 'block';
        }
    }

    function clearError(input, errorElement) {
        input.classList.remove('is-invalid');
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.style.display = 'none';
        }
    }

    function validateEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    function updatePasswordStrength(password) {
        const strengthMeter = document.getElementById('password-strength');
        if (!strengthMeter) return;
        
        let strength = 0;
        if (password.length >= 8) strength++;
        if (password.match(/[a-z]/) && password.match(/[A-Z]/)) strength++;
        if (password.match(/[0-9]/)) strength++;
        if (password.match(/[^a-zA-Z0-9]/)) strength++;
        
        const strengthText = ['Very Weak', 'Weak', 'Medium', 'Strong', 'Very Strong'];
        const strengthColors = ['danger', 'warning', 'info', 'primary', 'success'];
        
        strengthMeter.textContent = strengthText[strength];
        strengthMeter.className = `badge bg-${strengthColors[strength]}`;
        strengthMeter.style.display = 'inline-block';
    }
</script>
</body>
</html>