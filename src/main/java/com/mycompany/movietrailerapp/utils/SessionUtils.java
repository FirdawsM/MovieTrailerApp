package com.mycompany.movietrailerapp.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Utility class for handling user sessions
 */
public class SessionUtils {
    
    private static final Logger LOGGER = Logger.getLogger(SessionUtils.class.getName());
    
    /**
     * Get the current user ID from session, or redirect to login if not logged in
     * @param request HTTP request
     * @param response HTTP response
     * @return User ID if logged in, null if redirected to login
     * @throws IOException if redirect fails
     */
    public static Integer getCurrentUserId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            LOGGER.info("No session found, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            LOGGER.warning("Session exists but userId is null, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        
        return userId;
    }
    
    /**
     * Get the current user ID from session, with fallback for demo/testing
     * @param request HTTP request
     * @return User ID if logged in, or 1 for demo purposes
     */
    public static Integer getCurrentUserIdWithFallback(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            LOGGER.info("No session found, using demo user ID 1");
            return 1;
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
        
        if (userId == null) {
            LOGGER.info("Session exists but userId is null, using demo user ID 1");
            return 1;
        }
        
        return userId;
    }
    
    /**
     * Check if user is logged in
     * @param request HTTP request
     * @return true if user is logged in with valid session
     */
    public static boolean isUserLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return false;
        }
        
        Integer userId = (Integer) session.getAttribute("userId");
        return userId != null;
    }
    
    /**
     * Get username from session
     * @param request HTTP request
     * @return username if available, null otherwise
     */
    public static String getCurrentUsername(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session == null) {
            return null;
        }
        
        return (String) session.getAttribute("username");
    }
    
    /**
     * Invalidate current session (logout)
     * @param request HTTP request
     */
    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        
        if (session != null) {
            String username = (String) session.getAttribute("username");
            session.invalidate();
            LOGGER.info("User logged out: " + username);
        }
    }
}