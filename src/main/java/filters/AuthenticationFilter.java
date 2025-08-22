package filters;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Authentication filter to protect secured pages
 */
@WebFilter(urlPatterns = {"/dashboard", "/dashboard/*", "/movies/*", "/favorites/*", "/profile/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String contextPath = httpRequest.getContextPath();
        
        LOGGER.info("AuthenticationFilter: Checking access to " + requestURI);
        
        // Get the session
        HttpSession session = httpRequest.getSession(false);
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);
        
        if (!isLoggedIn) {
            // User is not logged in, redirect to login page
            LOGGER.info("User not authenticated, redirecting to login from: " + requestURI);
            
            // Store the original request URL for redirect after login
            HttpSession newSession = httpRequest.getSession(true);
            newSession.setAttribute("redirectUrl", requestURI);
            
            httpResponse.sendRedirect(contextPath + "/login.jsp");
        } else {
            // User is logged in, continue with the request
            LOGGER.info("User authenticated, allowing access to: " + requestURI);
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthenticationFilter destroyed");
    }
}