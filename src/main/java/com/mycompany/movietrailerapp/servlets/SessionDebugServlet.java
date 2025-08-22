package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import com.mycompany.movietrailerapp.models.User;

@WebServlet("/session-debug")
public class SessionDebugServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Session Debug</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println(".success { color: green; }");
            out.println(".error { color: red; }");
            out.println(".info { color: blue; }");
            out.println("table { border-collapse: collapse; width: 100%; margin: 20px 0; }");
            out.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            out.println("th { background-color: #f2f2f2; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>🔍 Session Debug Information</h1>");
            
            HttpSession session = request.getSession(false);
            
            if (session == null) {
                out.println("<div class='error'>");
                out.println("<h2>❌ No Session Found</h2>");
                out.println("<p>User is not logged in. No session exists.</p>");
                out.println("<p><a href='login'>Go to Login</a></p>");
                out.println("</div>");
            } else {
                out.println("<div class='info'>");
                out.println("<h2>📋 Session Information</h2>");
                out.println("<table>");
                out.println("<tr><th>Property</th><th>Value</th></tr>");
                out.println("<tr><td>Session ID</td><td>" + session.getId() + "</td></tr>");
                out.println("<tr><td>Creation Time</td><td>" + new java.util.Date(session.getCreationTime()) + "</td></tr>");
                out.println("<tr><td>Last Accessed</td><td>" + new java.util.Date(session.getLastAccessedTime()) + "</td></tr>");
                out.println("<tr><td>Max Inactive Interval</td><td>" + session.getMaxInactiveInterval() + " seconds</td></tr>");
                out.println("</table>");
                out.println("</div>");
                
                // Check user attributes
                Integer userId = (Integer) session.getAttribute("userId");
                String username = (String) session.getAttribute("username");
                User user = (User) session.getAttribute("user");
                
                out.println("<div class='" + (userId != null ? "success" : "error") + "'>");
                out.println("<h2>👤 User Information</h2>");
                out.println("<table>");
                out.println("<tr><th>Attribute</th><th>Value</th><th>Status</th></tr>");
                
                out.println("<tr>");
                out.println("<td>userId</td>");
                out.println("<td>" + (userId != null ? userId : "null") + "</td>");
                out.println("<td>" + (userId != null ? "✅ Present" : "❌ Missing") + "</td>");
                out.println("</tr>");
                
                out.println("<tr>");
                out.println("<td>username</td>");
                out.println("<td>" + (username != null ? username : "null") + "</td>");
                out.println("<td>" + (username != null ? "✅ Present" : "❌ Missing") + "</td>");
                out.println("</tr>");
                
                out.println("<tr>");
                out.println("<td>user object</td>");
                out.println("<td>" + (user != null ? user.getClass().getSimpleName() : "null") + "</td>");
                out.println("<td>" + (user != null ? "✅ Present" : "❌ Missing") + "</td>");
                out.println("</tr>");
                
                if (user != null) {
                    out.println("<tr>");
                    out.println("<td>user.getUserId()</td>");
                    out.println("<td>" + user.getUserId() + "</td>");
                    out.println("<td>✅ Available</td>");
                    out.println("</tr>");
                    
                    out.println("<tr>");
                    out.println("<td>user.getUsername()</td>");
                    out.println("<td>" + user.getUsername() + "</td>");
                    out.println("<td>✅ Available</td>");
                    out.println("</tr>");
                }
                
                out.println("</table>");
                out.println("</div>");
                
                // Recommendations
                if (userId == null) {
                    out.println("<div class='error'>");
                    out.println("<h2>⚠️ Issue Detected</h2>");
                    out.println("<p><strong>Problem:</strong> User session exists but userId is null.</p>");
                    out.println("<p><strong>Impact:</strong> History and favorites will not work properly.</p>");
                    out.println("<p><strong>Solution:</strong> User needs to log out and log back in.</p>");
                    out.println("<p><a href='login?logout=true'>Logout and Login Again</a></p>");
                    out.println("</div>");
                } else {
                    out.println("<div class='success'>");
                    out.println("<h2>✅ Session is Valid</h2>");
                    out.println("<p>User is properly logged in with userId: <strong>" + userId + "</strong></p>");
                    out.println("<p>History and favorites should work correctly.</p>");
                    out.println("</div>");
                }
            }
            
            out.println("<hr>");
            out.println("<h3>🔗 Quick Links</h3>");
            out.println("<p><a href='dashboard'>Dashboard</a> | ");
            out.println("<a href='login'>Login</a> | ");
            out.println("<a href='debug-favorites'>Debug Favorites</a> | ");
            out.println("<a href='debug-play'>Debug History</a></p>");
            
            out.println("</body>");
            out.println("</html>");
        }
    }
}