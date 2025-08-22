# MovieTrailerApp

A Java web application for browsing movie trailers, tracking watched history, and managing user favorites.

## Features

- User registration and login
- Browse movies and watch trailers
- Add/remove movies from favorites
- View watched history
- Each user’s favorites and history are kept separate
- Responsive UI with JSP and JavaScript
- MySQL database backend

## Technologies Used

- Java (Jakarta EE Servlets)
- JSP
- MySQL
- Maven
- JavaScript, HTML, CSS

## Project Structure

```
src/
  main/
    java/
      com/mycompany/movietrailerapp/
        servlets/      # Main servlet classes (FavoritesServlet, DashboardServlet, etc.)
        utils/         # Utility classes (DatabaseConnectionManager, etc.)
    webapp/
      js/              # Frontend JavaScript
      WEB-INF/         # JSP files and web.xml
database_schema.sql    # Database table definitions
pom.xml                # Maven build file
README.md              # This file
```

## Setup Instructions

1. **Clone the repository**
2. **Create the MySQL database and tables**  
   Use `database_schema.sql` if you need to set up the schema.
3. **Configure database connection**  
   Update your database credentials in `DatabaseConnectionManager.java`.
4. **Build the project**
   ```sh
   mvn clean package
   ```
5. **Deploy the WAR file**  
   Deploy to a servlet container (e.g., Tomcat).
6. **Access the app**  
   Visit `http://localhost:8080/MovieTrailerApp/`

## Troubleshooting

- If favorites/history do not work, check session handling and database connection.
- If you see database errors, verify your tables match the schema.
- For demo/testing, the app may default to user ID 1 if not logged in.

## License

MIT License

---

Enjoy exploring movie trailers and managing your