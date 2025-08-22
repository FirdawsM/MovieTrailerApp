package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.mycompany.movietrailerapp.utils.DatabaseConnectionManager;

@WebServlet("/reset-pool")
public class ResetPoolServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Reset Connection Pool</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println("button { padding: 10px 20px; margin: 10px; background: #e50914; color: white; border: none; border-radius: 5px; cursor: pointer; }");
            out.println(".success { color: green; }");
            out.println(".error { color: red; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>🔧 Connection Pool Reset</h1>");
            
            try {
                DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance();
                
                out.println("<h2>Before Reset:</h2>");
                out.println("<p>" + connectionManager.getPoolStats() + "</p>");
                
                // Reset the pool
                connectionManager.resetPool();
                
                out.println("<h2>After Reset:</h2>");
                out.println("<p class='success'>✅ Connection pool reset successfully!</p>");
                out.println("<p>" + connectionManager.getPoolStats() + "</p>");
                
                // Test connection
                boolean testResult = connectionManager.testConnection();
                out.println("<h2>Connection Test:</h2>");
                out.println("<p class='" + (testResult ? "success" : "error") + "'>");
                out.println((testResult ? "✅ SUCCESS" : "❌ FAILED") + " - Connection test");
                out.println("</p>");
                
            } catch (Exception e) {
                out.println("<p class='error'>❌ Error resetting pool: " + e.getMessage() + "</p>");
            }
            
            out.println("<hr>");
            out.println("<p><a href='quick-test'>← Back to Quick Test</a></p>");
            out.println("<p><a href='database-status'>Database Status</a></p>");
            
            out.println("</body>");
            out.println("</html>");
        }
    }
}