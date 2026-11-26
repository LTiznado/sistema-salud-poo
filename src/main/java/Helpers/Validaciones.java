package Helpers;

import DAO.Jdbc;
import Modelo.mPersonas;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/** Validation facade retained for the original Swing controllers. All values are bound. */
public class Validaciones {
  public static boolean ValidarLetras(java.awt.event.KeyEvent e, boolean spaces) {
    return Character.isLetter(e.getKeyChar()) || (spaces && e.getKeyChar() == ' ');
  }

  public static boolean ValidarNumeros(java.awt.event.KeyEvent e, boolean spaces) {
    return Character.isDigit(e.getKeyChar()) || (spaces && e.getKeyChar() == ' ');
  }

  public static boolean ValidarEmail(java.awt.event.KeyEvent e, boolean spaces) {
    return !Character.isWhitespace(e.getKeyChar()) || (spaces && e.getKeyChar() == ' ');
  }

  public static boolean ValidarLength(String text, int max) {
    return text != null && text.trim().length() < max;
  }

  public static boolean do_RegEx(String pattern, String value) {
    return value != null && value.matches(pattern);
  }

  private static Map result(boolean success, String message, Map data) {
    Map<String, Object> out = new HashMap<>();
    out.put("success", success);
    out.put("mensaje", message);
    out.put("data", data);
    return out;
  }

  private static Map fail(String message) {
    return result(false, message, Collections.emptyMap());
  }

  private static String text(Map data, String key) {
    return Jdbc.text(data.get(key)).trim();
  }

  private static int number(Map data, String key) {
    return Jdbc.number(data.get(key));
  }

  public static mPersonas person(Map data) {
    mPersonas p = new mPersonas();
    p.setIdPersona(number(data, "id_persona"));
    p.setIdTipoDocumento(number(data, "tipo_doc"));
    p.setNumDocumento(text(data, "num_doc"));
    p.setNombres(text(data, "nombres"));
    p.setApePaterno(text(data, "ape_paterno"));
    p.setApeMaterno(text(data, "ape_materno"));
    p.setFechaNac(text(data, "fecha_nac"));
    p.setEmail(text(data, "email"));
    p.setCelular(text(data, "celular"));
    return p;
  }

  public static void validatePerson(mPersonas p) {
    if (p.getIdTipoDocumento() < 1 || p.getIdTipoDocumento() > 3)
      throw new IllegalArgumentException("Selecciona un tipo de documento válido.");
    if (p.getNumDocumento() == null || !p.getNumDocumento().matches("[A-Za-z0-9-]{3,20}"))
      throw new IllegalArgumentException("Documento inválido (entre 3 y 20 caracteres).");
    if (p.getIdTipoDocumento() == 1 && !p.getNumDocumento().matches("[0-9]{8}"))
      throw new IllegalArgumentException("El DNI debe tener 8 dígitos.");
    for (String name : new String[] {p.getNombres(), p.getApePaterno(), p.getApeMaterno()})
      if (name == null || name.isBlank() || name.length() > 255)
        throw new IllegalArgumentException("Completa nombres y apellidos (máximo 255 caracteres).");
    try {
      if (LocalDate.parse(p.getFechaNac()).isAfter(LocalDate.now()))
        throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura.");
    } catch (java.time.format.DateTimeParseException | NullPointerException e) {
      throw new IllegalArgumentException("Ingresa una fecha de nacimiento válida.");
    }
    if (p.getEmail() != null
        && !p.getEmail().isEmpty()
        && (p.getEmail().length() > 100 || !p.getEmail().matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")))
      throw new IllegalArgumentException("El correo no es válido.");
    if (p.getCelular() != null
        && !p.getCelular().isEmpty()
        && !p.getCelular().matches("[0-9+]{7,15}"))
      throw new IllegalArgumentException(
          "El teléfono debe tener entre 7 y 15 caracteres numéricos.");
  }

  public static void validatePassword(String password) {
    if (password == null || password.length() < 8 || password.length() > 128)
      throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres.");
  }

  private static Map personValidation(Map data, boolean update) {
    try {
      mPersonas p = person(data);
      validatePerson(p);
      if (update && p.getIdPersona() < 1) return fail("Selecciona una persona para actualizar.");
      List<Map<String, Object>> existing =
          Jdbc.query(
              "SELECT id_persona FROM personas WHERE id_tipo_documento=? AND numero_documento=?",
              p.getIdTipoDocumento(),
              p.getNumDocumento());
      int id = existing.isEmpty() ? 0 : Jdbc.number(existing.get(0).get("id_persona"));
      if (update && id > 0 && id != p.getIdPersona())
        return fail("El documento ya pertenece a otra persona.");
      Map<String, Object> out = new HashMap<>();
      out.put("id_persona", update ? p.getIdPersona() : id);
      out.put("id_tipo_documento", p.getIdTipoDocumento());
      out.put("numero_documento", p.getNumDocumento());
      out.put("nombres", p.getNombres());
      out.put("ape_paterno", p.getApePaterno());
      out.put("ape_materno", p.getApeMaterno());
      out.put("fecha_nacimiento", p.getFechaNac());
      out.put("email", p.getEmail());
      out.put("celular", p.getCelular());
      return result(true, "Datos validados", out);
    } catch (IllegalArgumentException e) {
      return fail(e.getMessage());
    } catch (SQLException e) {
      return fail("No se pudo consultar la base de datos.");
    }
  }

  public static Map ValidarDatos_NuevaPersona(Map data) {
    return personValidation(data, false);
  }

  public static Map ValidarDatos_ActualizarPersona(Map data) {
    return personValidation(data, true);
  }

  public static int BuscarPersona_x_TipoNumDoc(int type, String document) {
    try {
      List<Map<String, Object>> rows =
          Jdbc.query(
              "SELECT id_persona FROM personas WHERE id_tipo_documento=? AND numero_documento=?",
              type,
              document);
      return rows.isEmpty() ? 0 : Jdbc.number(rows.get(0).get("id_persona"));
    } catch (SQLException e) {
      Mensajes.M1("No se pudo consultar personas.");
      return 0;
    }
  }

  public static int BuscarUsuario_x_IdPersona(int person) {
    try {
      List<Map<String, Object>> rows =
          Jdbc.query("SELECT id_usuario FROM usuarios WHERE id_persona=?", person);
      return rows.isEmpty() ? 0 : Jdbc.number(rows.get(0).get("id_usuario"));
    } catch (SQLException e) {
      Mensajes.M1("No se pudo consultar usuarios.");
      return 0;
    }
  }

  public static Map ValidarDatos_NuevoUsuario(Map data) {
    try {
      validatePassword(Jdbc.text(data.get("password")));
      if (!Objects.equals(data.get("password"), data.get("r_password")))
        return fail("Las contraseñas no coinciden.");
      int id = number(data, "id_persona");
      if (id < 1) return fail("Persona no identificada.");
      if (!Jdbc.query("SELECT id_usuario FROM usuarios WHERE id_persona=?", id).isEmpty())
        return fail("La persona ya tiene un usuario.");
      return result(
          true, "Datos validados", Map.of("id_persona", id, "clave", data.get("password")));
    } catch (IllegalArgumentException e) {
      return fail(e.getMessage());
    } catch (SQLException e) {
      return fail("No se pudo validar el usuario.");
    }
  }

  public static Map ValidarDatos_ActualizarUsuario(Map data) {
    try {
      validatePassword(Jdbc.text(data.get("password")));
      int id = number(data, "id_usuario");
      if (id < 1 || Jdbc.query("SELECT id_usuario FROM usuarios WHERE id_usuario=?", id).isEmpty())
        return fail("Selecciona un usuario existente.");
      return result(
          true, "Datos validados", Map.of("id_usuario", id, "clave", data.get("password")));
    } catch (IllegalArgumentException e) {
      return fail(e.getMessage());
    } catch (SQLException e) {
      return fail("No se pudo validar el usuario.");
    }
  }

  public static Map ValidarDatos_NuevoUsuarioRol(Map data) {
    int user = number(data, "id_usuario"), role = number(data, "id_rol");
    if (user < 1 || role < 1 || role > 3) return fail("Selecciona usuario y rol válidos.");
    try {
      if (!Jdbc.query(
              "SELECT id_usuario FROM usuario_rol WHERE id_usuario=? AND id_rol=?", user, role)
          .isEmpty()) return fail("El usuario ya tiene ese rol.");
      return result(true, "Datos validados", Map.of("id_usuario", user, "id_rol", role));
    } catch (SQLException e) {
      return fail("No se pudo validar el rol.");
    }
  }

  public static Map ValidarDatos_AccesoSistema(Map data) {
    int role = number(data, "tipo_usuario");
    String document = text(data, "num_doc"), password = Jdbc.text(data.get("password"));
    if (role < 1 || document.isEmpty() || password.isEmpty())
      return fail("Selecciona un rol e ingresa documento y contraseña.");
    try {
      List<Map<String, Object>> rows =
          Jdbc.query(
              "SELECT u.id_usuario,u.clave FROM usuarios u JOIN personas p ON"
                  + " p.id_persona=u.id_persona JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario"
                  + " JOIN roles r ON r.id_rol=ur.id_rol WHERE p.numero_documento=? AND ur.id_rol=?"
                  + " AND u.estado=1 AND ur.estado=1 AND r.estado=1",
              document,
              role);
      for (Map<String, Object> row : rows)
        if (Passwords.verify(password, Jdbc.text(row.get("clave"))))
          return result(
              true,
              "Acceso correcto",
              Map.of("id_usuario", Jdbc.number(row.get("id_usuario")), "id_rol", role));
      return fail("Documento, contraseña o rol incorrectos.");
    } catch (SQLException e) {
      return fail("No se pudo conectar con la base de datos. Revisa la configuración.");
    }
  }
}
