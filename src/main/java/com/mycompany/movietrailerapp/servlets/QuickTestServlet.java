package com.mycompany.movietrailerapp.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/quick-test")
public class QuickTestServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        
        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Quick History Test</title>");
            out.println("<style>");
            out.println("body { font-family: Arial, sans-serif; margin: 40px; }");
            out.println("button { padding: 10px 20px; margin: 10px; background: #e50914; color: white; border: none; border-radius: 5px; cursor: pointer; }");
            out.println("#result { margin: 20px 0; padding: 15px; border: 1px solid #ccc; border-radius: 5px; background: #f9f9f9; }");
            out.println(".success { color: green; }");
            out.println(".error { color: red; }");
            out.println("</style>");
            out.println("</head>");
            out.println("<body>");
            
            out.println("<h1>🎬 Quick History Test</h1>");
            out.println("<p>This will test if the history tracking works by simulating a 'Play Trailer' click.</p>");
            
            out.println("<button onclick='testHistory()'>🎯 Test History Tracking</button>");
            out.println("<button onclick='checkDatabase()'>📊 Check Database</button>");
            
            out.println("<div id='result'>");
            out.println("<p>Click 'Test History Tracking' to simulate clicking a play button.</p>");
            out.println("</div>");
            
            // JavaScript for testing
            out.println("<script>");
            
            // Test history function
            out.println("function testHistory() {");
            out.println("  document.getElementById('result').innerHTML = '<p>Testing... Please wait.</p>';");
            out.println("  ");
            out.println("  fetch('history', {");
            out.println("    method: 'POST',");
            out.println("    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },");
            out.println("    body: 'action=add&movieId=12345&title=Test Movie&posterPath=/test.jpg'");
            out.println("  })");
            out.println("  .then(response => {");
            out.println("    console.log('Response status:', response.status);");
            out.println("    return response.json();");
            out.println("  })");
            out.println("  .then(data => {");
            out.println("    console.log('Response data:', data);");
            out.println("    let resultHtml = '<h3>Test Result:</h3>';");
            out.println("    if (data.success) {");
            out.println("      resultHtml += '<p class=\"success\">✅ SUCCESS: ' + data.message + '</p>';");
            out.println("      resultHtml += '<p>Movie should now be in your history!</p>';");
            out.println("    } else {");
            out.println("      resultHtml += '<p class=\"error\">❌ FAILED: ' + data.message + '</p>';");
            out.println("    }");
            out.println("    resultHtml += '<pre>' + JSON.stringify(data, null, 2) + '</pre>';");
            out.println("    document.getElementById('result').innerHTML = resultHtml;");
            out.println("  })");
            out.println("  .catch(error => {");
            out.println("    console.error('Error:', error);");
            out.println("    document.getElementById('result').innerHTML = '<p class=\"error\">❌ ERROR: ' + error + '</p><p>Check browser console for details.</p>';");
            out.println("  });");
            out.println("}");
            
            // Check database function
            out.println("function checkDatabase() {");
            out.println("  window.open('test-history', '_blank');");
            out.println("}");
            
            out.println("</script>");
            
            out.println("<hr>");
            out.println("<h3>📋 Instructions:</h3>");
            out.println("<ol>");
            out.println("<li><strong>Click 'Test History Tracking'</strong> - This simulates clicking a play button</li>");
            out.println("<li><strong>Check the result</strong> - Should show SUCCESS if working</li>");
            out.println("<li><strong>Click 'Check Database'</strong> - Opens detailed database test</li>");
            out.println("<li><strong>Check your database</strong> - Run: <code>SELECT * FROM user_history;</code></li>");
            out.println("</ol>");
            
            out.println("<h3>🔍 Troubleshooting:</h3>");
            out.println("<ul>");
            out.println("<li><strong>If you see an error</strong> - Check browser console (F12)</li>");
            out.println("<li><strong>If SUCCESS but database empty</strong> - There's a database issue</li>");
            out.println("<li><strong>If no response</strong> - Check if history servlet is working</li>");
            out.println("</ul>");
            
            out.println("</body>");
            out.println("</html>");
        }
    }
}