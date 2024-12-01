package comp3911.cwk2;

import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@SuppressWarnings("serial")
public class AppServlet extends HttpServlet {

  private static final String CONNECTION_URL = "jdbc:sqlite:db.sqlite3";
  private static final String AUTH_QUERY = "select password from user where username = ?";
  private static final String SEARCH_QUERY = "select * from patient where surname='%s' collate nocase";

  private final Configuration fm = new Configuration(Configuration.VERSION_2_3_28);
  private Connection database;

  // UNCOMMENT TO HASH THE PLAIN TEXT PASSWORD (if hashed already, then don't)
  // After hashing, comment this function and caller function back.
  
  // private static final String GET_ALL_USERS = "select username, password from user";
  // private static final String UPDATE_PASSWORD = "update user set password = ? where username = ?";

  // private void hashExistingPasswords() throws SQLException {
  //   List<Map<String, String>> users = new ArrayList<>();
  //   try (Statement stmt = database.createStatement()) {
  //       ResultSet results = stmt.executeQuery(GET_ALL_USERS);
  //       while (results.next()) {
  //           Map<String, String> user = new HashMap<>();
  //           user.put("username", results.getString("username"));
  //           user.put("password", results.getString("password"));
  //           users.add(user);
  //       }
  //   }

  //   try (PreparedStatement updateStmt = database.prepareStatement(UPDATE_PASSWORD)) {
  //       for (Map<String, String> user : users) {
  //           String hashedPassword = hashPass(user.get("password"));
  //           updateStmt.setString(1, hashedPassword);
  //           updateStmt.setString(2, user.get("username"));
  //           updateStmt.executeUpdate();
  //       }
  //     }
  //   }

  @Override
  public void init() throws ServletException {
    configureTemplateEngine();
    connectToDatabase();
  }

  private void configureTemplateEngine() throws ServletException {
    try {
      fm.setDirectoryForTemplateLoading(new File("./templates"));
      fm.setDefaultEncoding("UTF-8");
      fm.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
      fm.setLogTemplateExceptions(false);
      fm.setWrapUncheckedExceptions(true);
    }
    catch (IOException error) {
      throw new ServletException(error.getMessage());
    }
  }

  private void connectToDatabase() throws ServletException {
    try {
      database = DriverManager.getConnection(CONNECTION_URL);
      //hashExistingPasswords();
      // uncomment to hash the password
      // comment this caller back after hashing
    }
    catch (SQLException error) {
      throw new ServletException(error.getMessage());
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
    try {
      Template template = fm.getTemplate("login.html");
      template.process(null, response.getWriter());
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (TemplateException error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
   throws ServletException, IOException {
     // Get form parameters
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    String surname = request.getParameter("surname");

    try {
      if (authenticated(username, password)) {
        // Get search results and merge with template
        Map<String, Object> model = new HashMap<>();
        model.put("records", searchResults(surname));
        Template template = fm.getTemplate("details.html");
        template.process(model, response.getWriter());
      }
      else {
        Template template = fm.getTemplate("invalid.html");
        template.process(null, response.getWriter());
      }
      response.setContentType("text/html");
      response.setStatus(HttpServletResponse.SC_OK);
    }
    catch (Exception error) {
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  private String hashPass(String plainTextPass) {
    return BCrypt.hashpw(plainTextPass, BCrypt.gensalt(12));
  }

  private boolean checkPass(String plainTextPass, String hashedPass) {
    try {
      return BCrypt.checkpw(plainTextPass, hashedPass);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private boolean authenticated(String username, String password) throws SQLException {
    try (PreparedStatement stmt = database.prepareStatement(AUTH_QUERY)) {
      stmt.setString(1, username);

      try (ResultSet results = stmt.executeQuery()) {
        if (results.next()) {
          String hashed = results.getString("password");
          return checkPass(password, hashed);
        }
      }
      return false;
    }
  }

  private List<Record> searchResults(String surname) throws SQLException {
    List<Record> records = new ArrayList<>();
    try (PreparedStatement stmt = database.prepareStatement(String.format(SEARCH_QUERY, surname))) {
      stmt.setString(1, surname);
      ResultSet results = stmt.executeQuery();
      while (results.next()) {
        Record rec = new Record();
        rec.setSurname(results.getString(2));
        rec.setForename(results.getString(3));
        rec.setAddress(results.getString(4));
        rec.setDateOfBirth(results.getString(5));
        rec.setDoctorId(results.getString(6));
        rec.setDiagnosis(results.getString(7));
        records.add(rec);
      }
    }
    return records;
  }
}
