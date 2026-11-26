package Servicio;

import Conexion.ConexionBD;
import DAO.Jdbc;
import Modelo.Sesion;
import java.sql.*;
import java.time.*;
import java.util.*;

public class CitasService {
  public record Opcion(int id, String nombre) {
    public String toString() {
      return nombre;
    }
  }

  public record Cita(
      int id, String paciente, String doctor, LocalDateTime fecha, String motivo, String estado) {}

  private void authorize(Connection con, Sesion session) throws SQLException {
    try (PreparedStatement ps =
        con.prepareStatement(
            "SELECT u.id_usuario FROM usuarios u JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario"
                + " JOIN roles r ON r.id_rol=ur.id_rol WHERE u.id_usuario=? AND ur.id_rol=? AND"
                + " u.estado=1 AND ur.estado=1 AND r.estado=1")) {
      Jdbc.bind(ps, session.usuario(), session.rol());
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next())
          throw new SecurityException("Tu sesión no tiene acceso. Vuelve a ingresar.");
      }
    }
  }

  public List<Opcion> opciones(Sesion session, int role) throws SQLException {
    try (Connection con = new ConexionBD().getConecction()) {
      authorize(con, session);
      // Patient identity lists are restricted to administrators.
      if (role != 2 && !(role == 3 && session.rol() == 1))
        throw new SecurityException("No tienes acceso a esa lista.");
      try (PreparedStatement ps =
          con.prepareStatement(
              "SELECT u.id_usuario,p.nombres,p.ape_paterno,p.ape_materno FROM usuarios u JOIN"
                  + " usuario_rol ur ON ur.id_usuario=u.id_usuario JOIN personas p ON"
                  + " p.id_persona=u.id_persona JOIN roles r ON r.id_rol=ur.id_rol WHERE"
                  + " ur.id_rol=? AND u.estado=1 AND ur.estado=1 AND r.estado=1 ORDER BY"
                  + " p.ape_paterno,p.nombres")) {
        ps.setInt(1, role);
        List<Opcion> options = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next())
            options.add(
                new Opcion(
                    rs.getInt(1), rs.getString(2) + " " + rs.getString(3) + " " + rs.getString(4)));
        }
        return options;
      }
    }
  }

  private void requireRole(Connection con, int user, int role) throws SQLException {
    try (PreparedStatement ps =
        con.prepareStatement(
            "SELECT u.id_usuario FROM usuarios u JOIN usuario_rol ur ON ur.id_usuario=u.id_usuario"
                + " JOIN roles r ON r.id_rol=ur.id_rol WHERE u.id_usuario=? AND ur.id_rol=? AND"
                + " u.estado=1 AND ur.estado=1 AND r.estado=1 FOR UPDATE")) {
      Jdbc.bind(ps, user, role);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) throw new IllegalArgumentException("El doctor o paciente no está activo.");
      }
    }
  }

  public int agendar(Sesion session, int patient, int doctor, LocalDateTime when, String reason)
      throws SQLException {
    if (session.rol() != 1 && session.rol() != 3)
      throw new SecurityException("Solo administradores y pacientes pueden agendar.");
    if (session.rol() == 3 && patient != session.usuario())
      throw new SecurityException("Solo puedes agendar para ti.");
    validateAppointment(patient, doctor, when, reason);
    try (Connection con = new ConexionBD().getConecction()) {
      con.setAutoCommit(false);
      try {
        authorize(con, session);
        requireRole(con, patient, 3);
        requireRole(con, doctor, 2);
        int id =
            Jdbc.insert(
                con,
                "INSERT INTO"
                    + " citas(id_paciente,id_doctor,fecha_hora,motivo,doctor_activo,paciente_activo)"
                    + " VALUES(?,?,?,?,?,?)",
                patient,
                doctor,
                Timestamp.valueOf(when),
                reason.trim(),
                doctor,
                patient);
        con.commit();
        return id;
      } catch (SQLException | RuntimeException e) {
        con.rollback();
        throw e;
      }
    }
  }

  public static void validateAppointment(
      int patient, int doctor, LocalDateTime when, String reason) {
    if (patient < 1 || doctor < 1 || patient == doctor)
      throw new IllegalArgumentException("Selecciona un paciente y un doctor diferentes.");
    if (when == null || !when.isAfter(LocalDateTime.now()))
      throw new IllegalArgumentException("La cita debe ser futura.");
    if (when.getMinute() % 30 != 0
        || when.getSecond() != 0
        || when.getNano() != 0
        || when.getHour() < 7
        || when.getHour() > 19)
      throw new IllegalArgumentException("Escoge un turno de 30 minutos, de 07:00 a 19:30.");
    if (reason == null || reason.isBlank() || reason.trim().length() > 500)
      throw new IllegalArgumentException("Ingresa el motivo (máximo 500 caracteres).");
  }

  public List<Cita> listar(Sesion session) throws SQLException {
    try (Connection con = new ConexionBD().getConecction()) {
      authorize(con, session);
      String sql =
          "SELECT c.*,p.nombres AS paciente_n,p.ape_paterno AS paciente_a,d.nombres AS"
              + " doctor_n,d.ape_paterno AS doctor_a FROM citas c JOIN usuarios up ON"
              + " up.id_usuario=c.id_paciente JOIN personas p ON p.id_persona=up.id_persona JOIN"
              + " usuarios ud ON ud.id_usuario=c.id_doctor JOIN personas d ON"
              + " d.id_persona=ud.id_persona";
      if (session.rol() == 2) sql += " WHERE c.id_doctor=?";
      if (session.rol() == 3) sql += " WHERE c.id_paciente=?";
      sql += " ORDER BY c.fecha_hora DESC,c.id_cita DESC";
      try (PreparedStatement ps = con.prepareStatement(sql)) {
        if (session.rol() != 1) ps.setInt(1, session.usuario());
        List<Cita> list = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
          while (rs.next())
            list.add(
                new Cita(
                    rs.getInt("id_cita"),
                    rs.getString("paciente_n") + " " + rs.getString("paciente_a"),
                    rs.getString("doctor_n") + " " + rs.getString("doctor_a"),
                    rs.getTimestamp("fecha_hora").toLocalDateTime(),
                    rs.getString("motivo"),
                    rs.getString("estado")));
        }
        return list;
      }
    }
  }

  public boolean cancelar(Sesion session, int id) throws SQLException {
    try (Connection con = new ConexionBD().getConecction()) {
      authorize(con, session);
      String sql =
          "UPDATE citas SET estado='CANCELADA',doctor_activo=NULL,paciente_activo=NULL WHERE"
              + " id_cita=? AND estado='PROGRAMADA' AND fecha_hora>CURRENT_TIMESTAMP";
      if (session.rol() == 2) sql += " AND id_doctor=?";
      if (session.rol() == 3) sql += " AND id_paciente=?";
      try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, id);
        if (session.rol() != 1) ps.setInt(2, session.usuario());
        return ps.executeUpdate() == 1;
      }
    }
  }

  public String comprobante(Sesion session, int id) throws SQLException {
    Cita cita =
        listar(session).stream()
            .filter(c -> c.id() == id)
            .findFirst()
            .orElseThrow(() -> new SecurityException("No tienes acceso a esa cita."));
    return "SISTEMA DE SALUD — COMPROBANTE DE CITA\n\nN.º "
        + cita.id()
        + "\nPaciente: "
        + cita.paciente()
        + "\nDoctor: "
        + cita.doctor()
        + "\nFecha: "
        + cita.fecha().toString().replace('T', ' ')
        + "\nMotivo: "
        + cita.motivo()
        + "\nEstado: "
        + cita.estado()
        + "\n\nDocumento de demostración académica. No es una boleta de pago.\n";
  }

  public int reclamar(Sesion session, String detail) throws SQLException {
    if (detail == null || detail.isBlank() || detail.trim().length() > 2000)
      throw new IllegalArgumentException("Escribe el reclamo (máximo 2000 caracteres).");
    try (Connection con = new ConexionBD().getConecction()) {
      authorize(con, session);
      return Jdbc.insert(
          con,
          "INSERT INTO reclamos(id_usuario,detalle) VALUES(?,?)",
          session.usuario(),
          detail.trim());
    }
  }
}
