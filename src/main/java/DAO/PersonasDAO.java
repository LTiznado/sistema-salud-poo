package DAO;

import Conexion.ConexionBD;
import Helpers.*;
import Modelo.mPersonas;
import java.sql.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class PersonasDAO {
  private static final String SELECT =
      "SELECT p.*,pa.nombre_parametro AS nom_tipo_doc,pa.orden FROM personas p JOIN parametros pa"
          + " ON pa.id_parametro=p.id_tipo_documento";

  public void ListarRegistros_Personas(mPersonas p, JTable table, JLabel total) {
    DefaultTableModel model =
        new DefaultTableModel(
            new String[] {
              "ID", "Tipo Doc.", "N° Doc.", "Nombres y Apellidos", "Fecha Nac.", "Email", "Celular"
            },
            0) {
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    table.setModel(model);
    table.getTableHeader().setReorderingAllowed(false);
    StringBuilder sql = new StringBuilder(SELECT + " WHERE 1=1");
    List<Object> args = new ArrayList<>();
    if (p.getIdPersona() > 0) {
      sql.append(" AND p.id_persona=?");
      args.add(p.getIdPersona());
    }
    if (p.getIdTipoDocumento() > 0) {
      sql.append(" AND p.id_tipo_documento=?");
      args.add(p.getIdTipoDocumento());
    }
    String[] columns = {
      "numero_documento", "nombres", "ape_paterno", "ape_materno", "email", "celular"
    };
    String[] values = {
      p.getNumDocumento(),
      p.getNombres(),
      p.getApePaterno(),
      p.getApeMaterno(),
      p.getEmail(),
      p.getCelular()
    };
    for (int i = 0; i < columns.length; i++)
      if (values[i] != null && !values[i].isBlank()) {
        sql.append(" AND p.").append(columns[i]).append(" LIKE ?");
        args.add("%" + values[i] + "%");
      }
    if (p.getFechaNac() != null && !p.getFechaNac().isBlank()) {
      sql.append(" AND p.fecha_nacimiento=?");
      args.add(p.getFechaNac());
    }
    sql.append(" ORDER BY p.id_persona");
    try {
      for (Map<String, Object> row : Jdbc.query(sql.toString(), args.toArray()))
        model.addRow(
            new Object[] {
              Jdbc.text(row.get("id_persona")),
              row.get("nom_tipo_doc"),
              row.get("numero_documento"),
              row.get("nombres") + " " + row.get("ape_paterno") + " " + row.get("ape_materno"),
              row.get("fecha_nacimiento"),
              row.get("email"),
              row.get("celular")
            });
    } catch (SQLException e) {
      Mensajes.M1("No se pudo consultar personas.");
    }
    total.setText(String.valueOf(model.getRowCount()));
  }

  public mPersonas CargarDatos_Persona(int id) {
    mPersonas p = new mPersonas();
    try {
      List<Map<String, Object>> rows = Jdbc.query(SELECT + " WHERE p.id_persona=?", id);
      if (rows.isEmpty()) return p;
      Map<String, Object> row = rows.get(0);
      p.setIdPersona(id);
      p.setIdTipoDocumento(Jdbc.number(row.get("id_tipo_documento")));
      p.setOrden(Jdbc.number(row.get("orden")));
      p.setNumDocumento(Jdbc.text(row.get("numero_documento")));
      p.setNombres(Jdbc.text(row.get("nombres")));
      p.setApePaterno(Jdbc.text(row.get("ape_paterno")));
      p.setApeMaterno(Jdbc.text(row.get("ape_materno")));
      p.setFechaNac(Jdbc.text(row.get("fecha_nacimiento")));
      p.setEmail(Jdbc.text(row.get("email")));
      p.setCelular(Jdbc.text(row.get("celular")));
    } catch (SQLException e) {
      Mensajes.M1("No se pudo cargar la persona.");
    }
    return p;
  }

  public static int insert(Connection con, mPersonas p) throws SQLException {
    Validaciones.validatePerson(p);
    return Jdbc.insert(
        con,
        "INSERT INTO"
            + " personas(id_tipo_documento,numero_documento,nombres,ape_paterno,ape_materno,fecha_nacimiento,email,celular)"
            + " VALUES(?,?,?,?,?,?,?,?)",
        p.getIdTipoDocumento(),
        p.getNumDocumento(),
        p.getNombres(),
        p.getApePaterno(),
        p.getApeMaterno(),
        p.getFechaNac(),
        p.getEmail(),
        p.getCelular());
  }

  public int InsertarRegistro_Personas(mPersonas p) {
    try (Connection con = new ConexionBD().getConecction()) {
      return insert(con, p);
    } catch (SQLException | IllegalArgumentException e) {
      Mensajes.M1("No se pudo guardar la persona. Comprueba el documento y los datos.");
      return 0;
    }
  }

  public boolean ActualizarRegistro_Personas(mPersonas p) {
    try {
      Validaciones.validatePerson(p);
      return Jdbc.update(
              "UPDATE personas SET"
                  + " id_tipo_documento=?,numero_documento=?,nombres=?,ape_paterno=?,ape_materno=?,fecha_nacimiento=?,email=?,celular=?"
                  + " WHERE id_persona=?",
              p.getIdTipoDocumento(),
              p.getNumDocumento(),
              p.getNombres(),
              p.getApePaterno(),
              p.getApeMaterno(),
              p.getFechaNac(),
              p.getEmail(),
              p.getCelular(),
              p.getIdPersona())
          == 1;
    } catch (SQLException | IllegalArgumentException e) {
      Mensajes.M1("No se pudo actualizar la persona. Comprueba el documento y los datos.");
      return false;
    }
  }
}
