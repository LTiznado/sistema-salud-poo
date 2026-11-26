package DAO;

import Conexion.ConexionBD;
import java.sql.*;
import java.util.*;

public final class Jdbc {
  private Jdbc() {}

  public static List<Map<String, Object>> query(String sql, Object... args) throws SQLException {
    try (Connection con = new ConexionBD().getConecction();
        PreparedStatement ps = con.prepareStatement(sql)) {
      bind(ps, args);
      try (ResultSet rs = ps.executeQuery()) {
        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
          Map<String, Object> row = new HashMap<>();
          for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++)
            row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(Locale.ROOT), rs.getObject(i));
          rows.add(row);
        }
        return rows;
      }
    }
  }

  public static int update(String sql, Object... args) throws SQLException {
    try (Connection con = new ConexionBD().getConecction();
        PreparedStatement ps = con.prepareStatement(sql)) {
      bind(ps, args);
      return ps.executeUpdate();
    }
  }

  public static int insert(Connection con, String sql, Object... args) throws SQLException {
    try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      bind(ps, args);
      ps.executeUpdate();
      try (ResultSet rs = ps.getGeneratedKeys()) {
        if (!rs.next()) throw new SQLException("No se obtuvo el identificador del registro.");
        return rs.getInt(1);
      }
    }
  }

  public static void bind(PreparedStatement ps, Object... args) throws SQLException {
    for (int i = 0; i < args.length; i++) ps.setObject(i + 1, args[i]);
  }

  public static int number(Object value) {
    return value == null || value.toString().isBlank() ? 0 : Integer.parseInt(value.toString());
  }

  public static String text(Object value) {
    return value == null ? "" : value.toString();
  }
}
