package com.mycompany.movietrailerapp.servlets;

import com.mycompany.movietrailerapp.dao.UserDAO;
import com.mycompany.movietrailerapp.models.User;
import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private UserDAO userDao;
    
    @Override
    public void init() throws ServletException {
        try {
            this.userDao = new UserDAO();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize UserDAO", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to register page
        request.getRequestDispatcher("/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        
        try {
            // Validate input
            if (username == null || username.trim().isEmpty() || 
                email == null || email.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                
                request.setAttribute("errorMessage", "All fields are required");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Validate password confirmation if provided
            if (confirmPassword != null && !password.equals(confirmPassword)) {
                request.setAttribute("errorMessage", "Passwords do not match");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Validate email format
            if (!isValidEmail(email)) {
                request.setAttribute("errorMessage", "Please enter a valid email address");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Validate password strength
            if (password.length() < 6) {
                request.setAttribute("errorMessage", "Password must be at least 6 characters long");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Check if username already exists
            if (userDao.userExists(username)) {
                request.setAttribute("errorMessage", "Username already exists. Please choose another.");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Check if email already exists
            if (userDao.emailExists(email)) {
                request.setAttribute("errorMessage", "Email already registered. Please use another email.");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
                return;
            }

            // Create new user
            User newUser = new User(username, email, password);
            boolean success = userDao.createUser(newUser);

            if (success) {
                // Registration successful - redirect to login page with success message
                request.getSession().setAttribute("successMessage", 
                    "Registration successful! Please login with your credentials.");
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            } else {
                request.setAttribute("errorMessage", "Registration failed. Please try again.");
                request.getRequestDispatcher("/register.jsp").forward(request, response);
            }

        } catch (Exception e) {
            log("Unexpected error during registration", e);
            request.setAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            request.getRequestDispatcher("/register.jsp").forward(request, response);
        }
    }

    /**
     * Simple email validation
     */
    private boolean isValidEmail(String email) {
        return email != null && 
               email.contains("@") && 
               email.contains(".") && 
               email.length() > 5 &&
               !email.startsWith("@") &&
               !email.endsWith("@") &&
               !email.startsWith(".") &&
               !email.endsWith(".");
    }

    @Override
    public void destroy() {
        // Clean up resources
        if (userDao != null) {
            try {
                userDao.close();
            } catch (Exception e) {
                log("Error closing UserDAO", e);
            }
        }
        super.destroy();
    }
}