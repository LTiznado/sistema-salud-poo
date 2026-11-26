package Helpers;

import Modelo.mParametros;
import Modelo.mRoles;
import java.awt.Dimension;
import java.sql.SQLException;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JTable;

public class Miscelanea {

  public static void CentrarVistaInterna(JDesktopPane pan, JInternalFrame inf) {
    Dimension desktopSize = pan.getSize();
    inf.setSize(
        Math.min(inf.getWidth(), Math.max(320, desktopSize.width - 24)),
        Math.min(inf.getHeight(), Math.max(300, desktopSize.height - 24)));
    Dimension jifSize = inf.getSize();
    inf.setLocation(
        (desktopSize.width - jifSize.width) / 2, (desktopSize.height - jifSize.height) / 2);
  }

  public static void setTimeout(Runnable runnable, int delay) {
    new Thread(
            () -> {
              try {
                Thread.sleep(delay);
                javax.swing.SwingUtilities.invokeLater(runnable);
              } catch (InterruptedException e) {
                Helpers.Mensajes.M1("" + e);
              }
            })
        .start();
  }

  public static void CargarCombo_TipoDocumento(JComboBox combo) {
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    mParametros empty = new mParametros();
    empty.setIdParametro(0);
    empty.setNombreParametro("Selecciona");
    model.addElement(empty);
    combo.setModel(model);
    try {
      for (java.util.Map<String, Object> row :
          DAO.Jdbc.query(
              "SELECT * FROM parametros WHERE id_catalogo=1 AND estado=1 ORDER BY orden")) {
        mParametros p = new mParametros();
        p.setIdParametro(DAO.Jdbc.number(row.get("id_parametro")));
        p.setIdCatalogo(1);
        p.setNombreParametro(DAO.Jdbc.text(row.get("nombre_parametro")));
        model.addElement(p);
      }
    } catch (SQLException e) {
      Mensajes.M1("No se pudieron cargar los tipos de documento.");
    }
  }

  public static void CargarCombo_TipoUsuario(JComboBox combo) {
    DefaultComboBoxModel model = new DefaultComboBoxModel();
    mRoles empty = new mRoles();
    empty.setIdRol(0);
    empty.setNombreRol("Selecciona");
    model.addElement(empty);
    combo.setModel(model);
    try {
      for (java.util.Map<String, Object> row :
          DAO.Jdbc.query("SELECT * FROM roles WHERE estado=1 ORDER BY id_rol")) {
        mRoles r = new mRoles();
        r.setIdRol(DAO.Jdbc.number(row.get("id_rol")));
        r.setNombreRol(DAO.Jdbc.text(row.get("nombre_rol")));
        model.addElement(r);
      }
    } catch (SQLException e) {
      Mensajes.M1("No se pudieron cargar los roles.");
    }
  }

  public static void OcultarColumnas(JTable tbl, int columna[]) {
    for (int i = 0; i < columna.length; i++) {
      tbl.getColumnModel().getColumn(columna[i]).setMaxWidth(0);
      tbl.getColumnModel().getColumn(columna[i]).setMinWidth(0);
      tbl.getTableHeader().getColumnModel().getColumn(columna[i]).setMaxWidth(0);
      tbl.getTableHeader().getColumnModel().getColumn(columna[i]).setMinWidth(0);
    }
  }
}
