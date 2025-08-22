package com.mycompany.movietrailerapp.servlets;

import com.mycompany.movietrailerapp.dao.UserDAO;
import com.mycompany.movietrailerapp.models.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private UserDAO userDao;
    
    @Override
    public void init() throws ServletException {
        try {
            this.userDao = new UserDAO();
            LOGGER.info("LoginServlet initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize UserDAO in LoginServlet", e);
            throw new ServletException("Failed to initialize UserDAO", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Check if already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            LOGGER.info("User already logged in, redirecting to dashboard");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        // Handle logout
        String logout = request.getParameter("logout");
        if (logout != null) {
            if (session != null) {
                session.invalidate();
                LOGGER.info("User logged out successfully");
            }
            request.setAttribute("logoutMessage", "You have been successfully logged out.");
        }
        
        // Forward to login JSP (now in WEB-INF)
       request.getRequestDispatcher("/login.jsp").forward(request, response);

    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String rememberMe = request.getParameter("rememberMe");
        
        LOGGER.log(Level.INFO, "Login attempt for username: {0}", username);
        
        try {
            // Validate input
            if (username == null || username.trim().isEmpty()) {
                handleLoginError(request, response, "Username is required", username);
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                handleLoginError(request, response, "Password is required", username);
                return;
            }
            
            // Authenticate user
            User user = userDao.authenticateUser(username.trim(), password.trim());
            
            if (user != null) {
                handleSuccessfulLogin(request, response, user, rememberMe);
            } else {
                handleLoginError(request, response, "Invalid username or password", username);
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login for user: " + username, e);
            handleLoginError(request, response, "System error occurred. Please try again later.", username);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during login for user: " + username, e);
            handleLoginError(request, response, "An unexpected error occurred. Please try again.", username);
        }
    }
    
    private void handleSuccessfulLogin(HttpServletRequest request, 
                                     HttpServletResponse response, 
                                     User user, 
                                     String rememberMe) throws IOException {
        
        String username = user.getUsername();
        LOGGER.log(Level.INFO, "Authentication successful for user: {0}", username);
        
        // Invalidate old session for security
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
            LOGGER.log(Level.INFO, "Old session invalidated for user: {0}", username);
        }
        
        // Create new session
        HttpSession newSession = request.getSession(true);
        
        // Set user attributes in session
        setSessionAttributes(newSession, user);
        
        // Configure session timeout based on remember me
        configureSessionTimeout(newSession, rememberMe, username);
        
        LOGGER.log(Level.INFO, "Session created successfully for user: {0}, Session ID: {1}", 
                new Object[]{username, newSession.getId()});
        
        // Handle redirect after login
        handlePostLoginRedirect(request, response, newSession);
    }
    
    private void setSessionAttributes(HttpSession session, User user) {
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("username", user.getUsername());
        session.setAttribute("isAdmin", user.isAdmin());
        // Add any other user attributes you need
    }
    
    private void configureSessionTimeout(HttpSession session, String rememberMe, String username) {
        if (rememberMe != null && "on".equals(rememberMe)) {
            session.setMaxInactiveInterval(7 * 24 * 60 * 60); // 1 week
            LOGGER.log(Level.INFO, "Remember me enabled for user: {0}", username);
        } else {
            session.setMaxInactiveInterval(30 * 60); // 30 minutes
        }
    }
    
    private void handleLoginError(HttpServletRequest request, 
                                HttpServletResponse response, 
                                String errorMessage, 
                                String username) 
            throws ServletException, IOException {
        
        LOGGER.log(Level.WARNING, "Login failed for user {0}: {1}", 
                new Object[]{username, errorMessage});
        
        request.setAttribute("loginError", errorMessage);
        request.setAttribute("username", username);
       request.getRequestDispatcher("/login.jsp").forward(request, response);

    }
    
    private void handlePostLoginRedirect(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       HttpSession session) throws IOException {
        
        String redirectUrl = determineRedirectUrl(request, session);
        LOGGER.log(Level.INFO, "Redirecting after successful login to: {0}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
    
    private String determineRedirectUrl(HttpServletRequest request, HttpSession session) {
        // 1. Check session attribute
        String redirectUrl = (String) session.getAttribute("redirectUrl");
        
        // 2. Check request parameter
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            redirectUrl = request.getParameter("redirect");
        }
        
        // 3. Use default dashboard
        if (redirectUrl == null || redirectUrl.isEmpty()) {
            User user = (User) session.getAttribute("user");
            redirectUrl = request.getContextPath() + 
                         (user != null && user.isAdmin() ? "/admin/dashboard" : "/dashboard");
        } else {
            // Validate the redirect URL is within our application
            if (!redirectUrl.startsWith(request.getContextPath())) {
                LOGGER.log(Level.WARNING, "Invalid redirect URL detected: {0}", redirectUrl);
                redirectUrl = request.getContextPath() + "/dashboard";
            }
            session.removeAttribute("redirectUrl");
        }
        
        return redirectUrl;
    }
    
    @Override
    public void destroy() {
        if (userDao != null) {
            try {
                userDao.close();
                LOGGER.info("LoginServlet UserDAO closed successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing UserDAO in LoginServlet", e);
            }
        }
        super.destroy();
    }
}