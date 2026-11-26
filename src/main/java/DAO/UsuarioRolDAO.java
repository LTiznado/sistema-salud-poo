package DAO;

import Helpers.Mensajes;
import Modelo.mUsuarioRol;
import java.sql.SQLException;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class UsuarioRolDAO {
  private static final String SELECT =
      "SELECT p.id_tipo_documento,param.nombre_parametro AS"
          + " nom_tipo_doc,param.orden,p.numero_documento,p.nombres,p.ape_paterno,p.ape_materno,u.id_usuario,r.id_rol,r.nombre_rol"
          + " FROM usuario_rol ur JOIN usuarios u ON u.id_usuario=ur.id_usuario JOIN roles r ON"
          + " r.id_rol=ur.id_rol JOIN personas p ON p.id_persona=u.id_persona JOIN parametros param"
          + " ON param.id_parametro=p.id_tipo_documento WHERE u.estado=1 AND r.estado=1 AND"
          + " ur.estado=1";

  public void ListarRegistros_Usuarios(Map data, JTable table, JLabel total) {
    DefaultTableModel model =
        new DefaultTableModel(
            new String[] {
              "ID Usu",
              "ID Rol",
              "Orden",
              "Tipo Doc.",
              "N° Doc.",
              "Nombres y Apellidos",
              "Rol",
              "Contraseña"
            },
            0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    table.setModel(model);
    table.getTableHeader().setReorderingAllowed(false);
    StringBuilder sql = new StringBuilder(SELECT);
    List<Object> args = new ArrayList<>();
    String[] keys = {"id_usuario", "id_rol", "id_tipo_doc"};
    String[] columns = {"u.id_usuario", "r.id_rol", "p.id_tipo_documento"};
    for (int i = 0; i < keys.length; i++) {
      int value = Jdbc.number(data.get(keys[i]));
      if (value > 0) {
        sql.append(" AND ").append(columns[i]).append("=?");
        args.add(value);
      }
    }
    if (!Jdbc.text(data.get("num_doc")).isBlank()) {
      sql.append(" AND p.numero_documento LIKE ?");
      args.add("%" + data.get("num_doc") + "%");
    }
    try {
      for (Map<String, Object> row : Jdbc.query(sql.toString(), args.toArray()))
        model.addRow(
            new Object[] {
              Jdbc.number(row.get("id_usuario")),
              Jdbc.number(row.get("id_rol")),
              Jdbc.number(row.get("orden")),
              row.get("nom_tipo_doc"),
              row.get("numero_documento"),
              row.get("nombres") + " " + row.get("ape_paterno") + " " + row.get("ape_materno"),
              row.get("nombre_rol"),
              "Protegida"
            });
    } catch (SQLException e) {
      Mensajes.M1("No se pudo consultar usuarios.");
    }
    total.setText(String.valueOf(model.getRowCount()));
  }

  public Map CargarDatos_Usuarios(int user, int role) {
    Map<String, Object> out = new HashMap<>();
    try {
      List<Map<String, Object>> rows =
          Jdbc.query(SELECT + " AND u.id_usuario=? AND r.id_rol=?", user, role);
      if (!rows.isEmpty()) {
        Map<String, Object> row = rows.get(0);
        out.put("num_doc", Jdbc.text(row.get("numero_documento")));
        out.put("id_tipo_doc", Jdbc.number(row.get("id_tipo_documento")));
        out.put("password", "");
        out.put("id_usuario", String.valueOf(user));
        out.put("id_rol", String.valueOf(role));
        out.put("orden", Jdbc.text(row.get("orden")));
      }
    } catch (SQLException e) {
      Mensajes.M1("No se pudo cargar el usuario.");
    }
    return out;
  }

  public boolean InsertarRegistro_UsuarioRol(mUsuarioRol role) {
    try {
      return Jdbc.update(
              "INSERT INTO usuario_rol(id_usuario,id_rol) VALUES(?,?)",
              role.getIdUsuario(),
              role.getIdRol())
          == 1;
    } catch (SQLException e) {
      Mensajes.M1("No se pudo asignar el rol.");
      return false;
    }
  }

  public boolean EliminarRegistro_UsuarioRol(int user, int role) {
    try {
      return Jdbc.update("DELETE FROM usuario_rol WHERE id_usuario=? AND id_rol=?", user, role)
          == 1;
    } catch (SQLException e) {
      Mensajes.M1("No se pudo retirar el rol.");
      return false;
    }
  }
}
