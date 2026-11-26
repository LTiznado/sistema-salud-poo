package Conexion;

import DAO.Jdbc;
import Helpers.Passwords;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public final class DatabaseSetup {
  private DatabaseSetup() {}

  public static void initializeDemo() throws SQLException, IOException {
    try (Connection con = new ConexionBD().getConecction()) {
      executeResource(con, "/db/schema.sql");
      try (Statement st = con.createStatement();
          ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM roles")) {
        rs.next();
        if (rs.getInt(1) > 0) return;
      }
      con.setAutoCommit(false);
      try {
        executeResource(con, "/db/catalogos.sql");
        seed(con, "00000001", "Administrador", 1);
        seed(con, "00000002", "Doctor", 2);
        seed(con, "00000003", "Paciente", 3);
        con.commit();
      } catch (SQLException | RuntimeException | IOException e) {
        con.rollback();
        throw e;
      }
    }
  }

  private static void seed(Connection con, String document, String name, int role)
      throws SQLException {
    int person =
        Jdbc.insert(
            con,
            "INSERT INTO"
                + " personas(id_tipo_documento,numero_documento,nombres,ape_paterno,ape_materno,fecha_nacimiento,email,celular)"
                + " VALUES(1,?,?,?,?,'1990-01-01',?,?)",
            document,
            name,
            "Demo",
            "Local",
            "demo@example.invalid",
            "");
    int user =
        Jdbc.insert(
            con,
            "INSERT INTO usuarios(id_persona,clave) VALUES(?,?)",
            person,
            Passwords.hash("DemoSalud2026!"));
    try (PreparedStatement ps =
        con.prepareStatement("INSERT INTO usuario_rol(id_usuario,id_rol) VALUES(?,?)")) {
      Jdbc.bind(ps, user, role);
      ps.executeUpdate();
    }
  }

  public static void executeResource(Connection con, String name) throws IOException, SQLException {
    try (InputStream input = DatabaseSetup.class.getResourceAsStream(name)) {
      if (input == null) throw new IOException("No se encontró " + name);
      String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
      try (Statement st = con.createStatement()) {
        for (String sql : script.split(";")) if (!sql.isBlank()) st.execute(sql);
      }
    }
  }
}
