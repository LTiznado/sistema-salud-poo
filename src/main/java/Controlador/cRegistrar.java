package Controlador;

import Helpers.*;
import Modelo.*;
import Vista.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class cRegistrar implements ActionListener {
  private final Registrar vRegistrar;

  public cRegistrar(Registrar view) {
    vRegistrar = view;
    Helpers.FormFields.limit(vRegistrar.txtNumDoc, 20);
    Helpers.FormFields.limit(vRegistrar.txtPassword, 128);
    Helpers.FormFields.limit(vRegistrar.txtRepeatPassword, 128);
    Helpers.FormFields.limit(vRegistrar.txtNombres, 255);
    Helpers.FormFields.limit(vRegistrar.txtApePaterno, 255);
    Helpers.FormFields.limit(vRegistrar.txtApeMaterno, 255);
    Helpers.FormFields.limit(vRegistrar.txtEmail, 100);
    Helpers.FormFields.limit(vRegistrar.txtCelular, 15);

    Miscelanea.CargarCombo_TipoDocumento(view.cboTipoDocumento);
    view.txtFechaNac.setDate(null);
    view.btnRegistrar.addActionListener(this);
    view.btnShowLogin.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent event) {
            new cLogin(new Login());
            view.dispose();
          }
        });
    view.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    view.setLocationRelativeTo(null);
    view.setTitle(
        "Registro de paciente" + (Conexion.ConexionBD.isDemo() ? " · DEMOSTRACIÓN LOCAL" : ""));
    view.setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getSource() != vRegistrar.btnRegistrar) return;
    try {
      mParametros selected = (mParametros) vRegistrar.cboTipoDocumento.getSelectedItem();
      mPersonas person = new mPersonas();
      person.setIdTipoDocumento(selected == null ? 0 : selected.getIdParametro());
      person.setNumDocumento(vRegistrar.txtNumDoc.getText().trim());
      person.setNombres(vRegistrar.txtNombres.getText().trim());
      person.setApePaterno(vRegistrar.txtApePaterno.getText().trim());
      person.setApeMaterno(vRegistrar.txtApeMaterno.getText().trim());
      person.setFechaNac(
          vRegistrar.txtFechaNac.getDate() == null
              ? ""
              : new SimpleDateFormat("yyyy-MM-dd").format(vRegistrar.txtFechaNac.getDate()));
      person.setEmail(vRegistrar.txtEmail.getText().trim());
      person.setCelular(vRegistrar.txtCelular.getText().trim());
      new Servicio.RegistroService()
          .registerPatient(
              person,
              new String(vRegistrar.txtPassword.getPassword()),
              new String(vRegistrar.txtRepeatPassword.getPassword()));
      vRegistrar.txtPassword.setText("");
      vRegistrar.txtRepeatPassword.setText("");
      Mensajes.M1("Paciente registrado correctamente.");
      new cLogin(new Login());
      vRegistrar.dispose();
    } catch (IllegalArgumentException ex) {
      Mensajes.M1(ex.getMessage());
    } catch (SQLException ex) {
      Mensajes.M1(
          "No se pudo completar el registro. Si el documento ya existe, solicita ayuda al"
              + " administrador.");
    }
  }
}
