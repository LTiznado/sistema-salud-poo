package Servicio;

import Conexion.ConexionBD;
import DAO.*;
import Helpers.*;
import Modelo.mPersonas;
import java.sql.*;

public class RegistroService {
  /** Self-registration only creates a new patient; it never claims an existing identity. */
  public int registerPatient(mPersonas person, String password, String repeated)
      throws SQLException {
    Validaciones.validatePerson(person);
    Validaciones.validatePassword(password);
    if (!password.equals(repeated))
      throw new IllegalArgumentException("Las contraseñas no coinciden.");
    String hash = Passwords.hash(password);
    try (Connection con = new ConexionBD().getConecction()) {
      con.setAutoCommit(false);
      try {
        int id = PersonasDAO.insert(con, person);
        int user = Jdbc.insert(con, "INSERT INTO usuarios(id_persona,clave) VALUES(?,?)", id, hash);
        try (PreparedStatement ps =
            con.prepareStatement("INSERT INTO usuario_rol(id_usuario,id_rol) VALUES(?,3)")) {
          ps.setInt(1, user);
          ps.executeUpdate();
        }
        con.commit();
        return user;
      } catch (SQLException | RuntimeException e) {
        con.rollback();
        throw e;
      }
    }
  }

  /** First administrator for an empty installation; never overwrites an existing account. */
  public int bootstrapAdministrator(mPersonas person, String password) throws SQLException {
    Validaciones.validatePerson(person);
    String hash = Passwords.hash(password);
    try (Connection con = new ConexionBD().getConecction()) {
      con.setAutoCommit(false);
      try {
        try (PreparedStatement ps =
                con.prepareStatement(
                    "SELECT id_rol FROM roles WHERE id_rol=1 AND estado=1 FOR UPDATE");
            ResultSet rs = ps.executeQuery()) {
          if (!rs.next())
            throw new IllegalArgumentException("Instala primero el esquema y los catálogos.");
        }
        try (PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM usuarios");
            ResultSet rs = ps.executeQuery()) {
          rs.next();
          if (rs.getInt(1) != 0)
            throw new IllegalArgumentException(
                "La instalación ya tiene usuarios. Usa un administrador existente.");
        }
        int id = PersonasDAO.insert(con, person);
        int user = Jdbc.insert(con, "INSERT INTO usuarios(id_persona,clave) VALUES(?,?)", id, hash);
        try (PreparedStatement ps =
            con.prepareStatement("INSERT INTO usuario_rol(id_usuario,id_rol) VALUES(?,1)")) {
          ps.setInt(1, user);
          ps.executeUpdate();
        }
        con.commit();
        return user;
      } catch (SQLException | RuntimeException e) {
        con.rollback();
        throw e;
      }
    }
  }

  /** Atomic creation and role assignment, exposed by the administrator UI only. */
  public int assignUserRole(int person, int role, String password) throws SQLException {
    if (person < 1 || role < 1 || role > 3)
      throw new IllegalArgumentException("Selecciona una persona y un rol válidos.");
    try (Connection con = new ConexionBD().getConecction()) {
      con.setAutoCommit(false);
      try {
        int user = 0;
        try (PreparedStatement ps =
            con.prepareStatement("SELECT id_usuario FROM usuarios WHERE id_persona=?")) {
          ps.setInt(1, person);
          try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) user = rs.getInt(1);
          }
        }
        if (user == 0)
          user =
              Jdbc.insert(
                  con,
                  "INSERT INTO usuarios(id_persona,clave) VALUES(?,?)",
                  person,
                  Passwords.hash(password));
        try (PreparedStatement ps =
            con.prepareStatement("INSERT INTO usuario_rol(id_usuario,id_rol) VALUES(?,?)")) {
          Jdbc.bind(ps, user, role);
          ps.executeUpdate();
        }
        con.commit();
        return user;
      } catch (SQLException | RuntimeException e) {
        con.rollback();
        throw e;
      }
    }
  }
}
