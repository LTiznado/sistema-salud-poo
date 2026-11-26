package Controlador;

import Helpers.Mensajes;
import Helpers.Miscelanea;
import Helpers.Validaciones;
import Modelo.*;
import Vista.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class cLogin implements ActionListener {
  Login vLogin;
  Connection con;
  Statement st;
  ResultSet rs;
  PreparedStatement ps;

  public cLogin(Login login) {
    vLogin = login;
    Helpers.FormFields.limit(vLogin.txtNumDoc, 20);
    Helpers.FormFields.limit(vLogin.txtPassword, 128);

    Miscelanea.CargarCombo_TipoUsuario(vLogin.cboTipoUsuario);
    vLogin.btnLogin.addActionListener(this);
    vLogin.btnShowRegistrar.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            cRegistrar cReg = new cRegistrar(new Registrar());
            vLogin.dispose();
          }
        });
    login.setDefaultCloseOperation(login.EXIT_ON_CLOSE);
    login.setLocationRelativeTo(null);
    login.setVisible(true);
    login.setTitle(
        "Sistema de Salud" + (Conexion.ConexionBD.isDemo() ? " · DEMOSTRACIÓN LOCAL" : ""));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == vLogin.btnLogin) {
      mRoles rol = (mRoles) vLogin.cboTipoUsuario.getSelectedItem();
      Map data_usu = new HashMap();
      data_usu.put("nombre_rol", rol.getNombreRol());
      data_usu.put("tipo_usuario", rol.getIdRol());
      data_usu.put("num_doc", vLogin.txtNumDoc.getText().trim());
      data_usu.put("password", String.valueOf(vLogin.txtPassword.getPassword()));
      Map result = Validaciones.ValidarDatos_AccesoSistema(data_usu);
      if ((boolean) result.get("success")) {
        Map tmp = (Map) result.get("data");
        mUsuarioRol usu_rol = new mUsuarioRol();
        usu_rol.setIdUsuario((int) tmp.get("id_usuario"));
        usu_rol.setIdRol((int) tmp.get("id_rol"));
        usu_rol.setEstado(1);
        cHome cHome = new cHome(new Home(), usu_rol);
        vLogin.txtPassword.setText("");
        vLogin.dispose();
      } else {
        vLogin.txtPassword.setText("");
        Mensajes.M1((String) result.get("mensaje"));
      }
    }
  }
}
