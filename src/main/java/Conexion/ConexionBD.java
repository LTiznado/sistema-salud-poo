package Conexion;

import java.sql.*;

/** One independent, caller-owned connection per operation. */
public class ConexionBD {
  public static boolean isDemo() {
    return setting("salud.db.mode", "SALUD_DB_MODE", "demo").equals("demo");
  }

  private static String setting(String property, String variable, String fallback) {
    String value = System.getProperty(property);
    if (value == null || value.isBlank()) value = System.getenv(variable);
    return value == null || value.isBlank() ? fallback : value;
  }

  public Connection getConecction() throws SQLException {
    String url =
        setting(
            "salud.db.url",
            "SALUD_DB_URL",
            isDemo() ? "jdbc:h2:./data/salud-demo;MODE=MySQL;DATABASE_TO_LOWER=TRUE" : "");
    String user = setting("salud.db.user", "SALUD_DB_USER", isDemo() ? "sa" : "");
    String pass = setting("salud.db.password", "SALUD_DB_PASSWORD", "");
    if (url.isBlank()) throw new SQLException("Falta configurar SALUD_DB_URL.");
    DriverManager.setLoginTimeout(10);
    return DriverManager.getConnection(url, user, pass);
  }
}
