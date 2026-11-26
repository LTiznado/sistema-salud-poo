package Controlador;

import DAO.*;
import Helpers.*;
import Modelo.*;
import Vista.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTable;

public class cUsuarios implements ActionListener {
  GestionUsuarios vUsu;
  UsuarioRolDAO usurolDAO;

  public cUsuarios(GestionUsuarios vista) {
    vUsu = vista;
    Helpers.FormFields.limit(vUsu.txtNumDoc, 20);
    Helpers.FormFields.limit(vUsu.txtPassword, 128);

    Miscelanea.CargarCombo_TipoDocumento(vUsu.cboTipoDocumento);
    Miscelanea.CargarCombo_TipoUsuario(vUsu.cboTipoUsuario);
    vUsu.cboTipoUsuario.addActionListener(this);
    vUsu.btnLimpiarDatos.addActionListener(this);
    vUsu.btnConsultar.addActionListener(this);
    vUsu.btnRegistrar.addActionListener(this);
    vUsu.btnActualizar.addActionListener(this);
    vUsu.btnEliminar.addActionListener(this);
    vUsu.tblUsuarios.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent Mouse_evt) {
            JTable table = (JTable) Mouse_evt.getSource();
            Point point = Mouse_evt.getPoint();
            int row = table.rowAtPoint(point);
            if (Mouse_evt.getClickCount() == 2 && row >= 0) {
              table.setRowSelectionInterval(row, row);
              int id_usuario =
                  (int) vUsu.tblUsuarios.getValueAt(vUsu.tblUsuarios.getSelectedRow(), 0);
              int id_rol = (int) vUsu.tblUsuarios.getValueAt(vUsu.tblUsuarios.getSelectedRow(), 1);
              vUsu.txtIdUsuario.setText(String.valueOf(id_usuario));
              vUsu.txtIdRol.setText(String.valueOf(id_rol));
              Map datos = usurolDAO.CargarDatos_Usuarios(id_usuario, id_rol);
              SetearDatos_Usuario(datos);
            }
          }
        });
    vUsu.txtIdUsuario.setVisible(false);
    vUsu.txtIdRol.setVisible(false);
    vUsu.txtOrden.setVisible(false);
    Map datos = CapturarDatos_GestionUsuarios();

    usurolDAO = new UsuarioRolDAO();
    usurolDAO.ListarRegistros_Usuarios(datos, vUsu.tblUsuarios, vUsu.txtTotal);
    Miscelanea.OcultarColumnas(vUsu.tblUsuarios, new int[] {0, 1, 2});
    vUsu.setTitle("Mantenimiento de Usuarios");
    // FormatoForma1.Estado1(f1);
    vUsu.setVisible(true);
  }

  final Map CapturarDatos_GestionUsuarios() {
    Map result = new HashMap();
    mParametros param = (mParametros) vUsu.cboTipoDocumento.getSelectedItem();
    int id_tipo_doc = param.getIdParametro();
    String num_doc = vUsu.txtNumDoc.getText().trim();
    String password = String.valueOf(vUsu.txtPassword.getPassword());
    String id_usuario = vUsu.txtIdUsuario.getText();
    String id_rol = vUsu.txtIdRol.getText();
    String orden = vUsu.txtOrden.getText();
    result.put("id_tipo_doc", id_tipo_doc);
    result.put("num_doc", num_doc);
    result.put("password", password);
    result.put("id_usuario", id_usuario);
    result.put("id_rol", id_rol);
    result.put("orden", orden);
    return result;
  }

  final void SetearDatos_Usuario(Map datos) {
    String num_doc = (String) datos.get("num_doc");
    String password = (String) datos.get("password");
    int id_usuario =
        "".equals((String) datos.get("id_usuario"))
            ? 0
            : Integer.parseInt((String) datos.get("id_usuario"));
    int id_rol =
        "".equals((String) datos.get("id_rol"))
            ? 0
            : Integer.parseInt((String) datos.get("id_rol"));
    int orden =
        "".equals((String) datos.get("orden")) ? 0 : Integer.parseInt((String) datos.get("orden"));
    vUsu.cboTipoDocumento.setSelectedIndex(orden);
    vUsu.cboTipoUsuario.setSelectedIndex(id_rol);
    vUsu.txtNumDoc.setText(num_doc);
    vUsu.txtPassword.setText("");
    vUsu.txtPassword.setToolTipText(
        "Nueva contraseña (8 a 128 caracteres). La anterior no se muestra.");
    vUsu.txtIdUsuario.setText(String.valueOf(id_usuario));
    vUsu.txtIdRol.setText(String.valueOf(id_rol));
    vUsu.txtOrden.setText(String.valueOf(orden));
  }

  final void LimpiarCampos_Usuarios() {
    Map result = new HashMap();
    result.put("id_tipo_doc", 0);
    result.put("num_doc", "");
    result.put("password", "");
    result.put("id_usuario", "");
    result.put("id_rol", "");
    result.put("orden", "");
    SetearDatos_Usuario(result);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Map datos = CapturarDatos_GestionUsuarios();
    usurolDAO = new UsuarioRolDAO();
    if (e.getSource() == vUsu.cboTipoUsuario) {
      mRoles rol = (mRoles) vUsu.cboTipoUsuario.getSelectedItem();
      vUsu.txtIdRol.setText(String.valueOf(rol.getIdRol()));
    }
    if (e.getSource() == vUsu.btnLimpiarDatos) {
      LimpiarCampos_Usuarios();
    }
    if (e.getSource() == vUsu.btnConsultar) {
      usurolDAO.ListarRegistros_Usuarios(datos, vUsu.tblUsuarios, vUsu.txtTotal);
      Miscelanea.OcultarColumnas(vUsu.tblUsuarios, new int[] {0, 1, 2});
    }

    if (e.getSource() == vUsu.btnRegistrar) {
      int person =
          Validaciones.BuscarPersona_x_TipoNumDoc(
              (int) datos.get("id_tipo_doc"), (String) datos.get("num_doc"));
      try {
        new Servicio.RegistroService()
            .assignUserRole(
                person, DAO.Jdbc.number(datos.get("id_rol")), (String) datos.get("password"));
        Mensajes.M1("Usuario y rol registrados correctamente.");
        LimpiarCampos_Usuarios();
        usurolDAO.ListarRegistros_Usuarios(
            CapturarDatos_GestionUsuarios(), vUsu.tblUsuarios, vUsu.txtTotal);
        Miscelanea.OcultarColumnas(vUsu.tblUsuarios, new int[] {0, 1, 2});
      } catch (IllegalArgumentException ex) {
        Mensajes.M1(ex.getMessage());
      } catch (java.sql.SQLException ex) {
        Mensajes.M1(
            "No se pudo asignar el rol. Comprueba si ya existe y registra primero a la persona.");
      }
    }
    if (e.getSource() == vUsu.btnActualizar) {
      Map data_user = new HashMap();
      int id_usuario =
          "".equals((String) datos.get("id_usuario"))
              ? 0
              : Integer.parseInt((String) datos.get("id_usuario"));
      String password = (String) datos.get("password");
      data_user.put("id_usuario", id_usuario);
      data_user.put("password", password);
      Map result = Validaciones.ValidarDatos_ActualizarUsuario(data_user);
      if ((boolean) result.get("success")) {
        boolean reg_usu_rol = false;
        Map tmp = (Map) result.get("data");
        mUsuarios usu = new mUsuarios();
        usu.setIdUsuario((int) tmp.get("id_usuario"));
        usu.setClave((String) tmp.get("clave"));
        UsuariosDAO usuDAO = new UsuariosDAO();
        boolean upt_success = usuDAO.ActualizarRegistro_Usuarios(usu);
        if (upt_success) {
          Mensajes.M1("Clave del Usuario actualizada correctamente");
          LimpiarCampos_Usuarios();
          usurolDAO.ListarRegistros_Usuarios(datos, vUsu.tblUsuarios, vUsu.txtTotal);
          Miscelanea.OcultarColumnas(vUsu.tblUsuarios, new int[] {0, 1, 2});
        } else {
          Mensajes.M1("Ocurrió un error inesperado (1)");
        }
      } else {
        Mensajes.M1((String) result.get("mensaje"));
      }
    }
    if (e.getSource() == vUsu.btnEliminar) {
      int id_usuario =
          "".equals((String) datos.get("id_usuario"))
              ? 0
              : Integer.parseInt((String) datos.get("id_usuario"));
      int id_rol =
          "".equals((String) datos.get("id_rol"))
              ? 0
              : Integer.parseInt((String) datos.get("id_rol"));
      if (id_usuario == 0 || id_rol == 0) {
        Mensajes.M1("Haga Doble Click sobre el registro que quiere Eliminar");
      } else {
        boolean dlt_success = usurolDAO.EliminarRegistro_UsuarioRol(id_usuario, id_rol);
        if (dlt_success) {
          Mensajes.M1("Rol eliminado correctamente del Usuario");
          LimpiarCampos_Usuarios();
          usurolDAO.ListarRegistros_Usuarios(
              CapturarDatos_GestionUsuarios(), vUsu.tblUsuarios, vUsu.txtTotal);
          Miscelanea.OcultarColumnas(vUsu.tblUsuarios, new int[] {0, 1, 2});
        } else {
          Mensajes.M1("Ocurrió un error inesperado (1)");
        }
      }
    }
  }
}
