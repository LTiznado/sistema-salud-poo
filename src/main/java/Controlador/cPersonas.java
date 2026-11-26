package Controlador;

import DAO.*;
import Helpers.Mensajes;
import Helpers.Miscelanea;
import Helpers.Validaciones;
import Modelo.*;
import Vista.*;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;

public class cPersonas implements ActionListener {
  Connection con;
  Statement st;
  ResultSet rs;
  PreparedStatement ps;
  GestionPersonas vPer;
  mPersonas mPer;
  PersonasDAO perDAO;

  public cPersonas(GestionPersonas vista) {
    vPer = vista;
    Helpers.FormFields.limit(vPer.txtNumDoc, 20);
    Helpers.FormFields.limit(vPer.txtNombres, 255);
    Helpers.FormFields.limit(vPer.txtApePaterno, 255);
    Helpers.FormFields.limit(vPer.txtApeMaterno, 255);
    Helpers.FormFields.limit(vPer.txtEmail, 100);
    Helpers.FormFields.limit(vPer.txtCelular, 15);

    Miscelanea.CargarCombo_TipoDocumento(vPer.cboTipoDocumento);
    vPer.btnLimpiarDatos.addActionListener(this);
    vPer.btnConsultar.addActionListener(this);
    vPer.btnRegistrar.addActionListener(this);
    vPer.btnActualizar.addActionListener(this);
    vPer.tblPersonas.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent Mouse_evt) {
            JTable table = (JTable) Mouse_evt.getSource();
            Point point = Mouse_evt.getPoint();
            int row = table.rowAtPoint(point);
            if (Mouse_evt.getClickCount() == 2 && row >= 0) {
              table.setRowSelectionInterval(row, row);
              int id_persona =
                  Integer.valueOf(
                      (String) vPer.tblPersonas.getValueAt(vPer.tblPersonas.getSelectedRow(), 0));
              mPersonas modelo = perDAO.CargarDatos_Persona(id_persona);
              SetearDatos_Persona(modelo);
            }
          }
        });
    vPer.txtIdPersona.setVisible(false);
    mPersonas datos = CapturarDatos_GestionPersonas();
    perDAO = new PersonasDAO();
    perDAO.ListarRegistros_Personas(datos, vPer.tblPersonas, vPer.txtTotal);
    vPer.setTitle("Mantenimiento de Personas");
    vPer.setVisible(true);
  }

  final mPersonas CapturarDatos_GestionPersonas() {
    mPersonas modelo = new mPersonas();
    mParametros param = (mParametros) vPer.cboTipoDocumento.getSelectedItem();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    String id_persona = vPer.txtIdPersona.getText().trim();
    int tipo_doc = param.getIdParametro();
    String num_doc = vPer.txtNumDoc.getText().trim();
    String nombres = vPer.txtNombres.getText().trim();
    String ape_paterno = vPer.txtApePaterno.getText().trim();
    String ape_materno = vPer.txtApeMaterno.getText().trim();
    String fecha_nac =
        vPer.txtFechaNac.getDate() == null ? "" : sdf.format(vPer.txtFechaNac.getDate());
    String email = vPer.txtEmail.getText().trim();
    String celular = vPer.txtCelular.getText().trim();
    modelo.setIdPersona("".equals(id_persona) ? 0 : Integer.parseInt(id_persona));
    modelo.setIdTipoDocumento(tipo_doc);
    modelo.setNumDocumento(num_doc);
    modelo.setNombres(nombres);
    modelo.setApePaterno(ape_paterno);
    modelo.setApeMaterno(ape_materno);
    modelo.setFechaNac(fecha_nac);
    modelo.setEmail(email);
    modelo.setCelular(celular);
    return modelo;
  }

  final void SetearDatos_Persona(mPersonas modelo) {
    try {
      vPer.txtIdPersona.setText(
          modelo.getIdPersona() == 0 ? "" : String.valueOf(modelo.getIdPersona()));
      vPer.cboTipoDocumento.setSelectedIndex(modelo.getOrden());
      vPer.txtNumDoc.setText(modelo.getNumDocumento());
      vPer.txtNombres.setText(modelo.getNombres());
      vPer.txtApePaterno.setText(modelo.getApePaterno());
      vPer.txtApeMaterno.setText(modelo.getApeMaterno());
      vPer.txtEmail.setText(modelo.getEmail());
      vPer.txtCelular.setText(modelo.getCelular());
      if ("".equals(modelo.getFechaNac())) {
        vPer.txtFechaNac.setDate(null);
      } else {
        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(modelo.getFechaNac());
        vPer.txtFechaNac.setDate(date);
      }
    } catch (ParseException ex) {
      Logger.getLogger(cPersonas.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  final void LimpiarCampos_Persona() {
    mPersonas modelo = new mPersonas();
    modelo.setIdPersona(0);
    modelo.setOrden(0);
    modelo.setNumDocumento("");
    modelo.setNombres("");
    modelo.setApePaterno("");
    modelo.setApeMaterno("");
    modelo.setFechaNac("");
    modelo.setEmail("");
    modelo.setCelular("");
    SetearDatos_Persona(modelo);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    mPersonas datos = CapturarDatos_GestionPersonas();
    perDAO = new PersonasDAO();
    if (e.getSource() == vPer.btnLimpiarDatos) {
      LimpiarCampos_Persona();
    }
    if (e.getSource() == vPer.btnConsultar) {
      perDAO.ListarRegistros_Personas(datos, vPer.tblPersonas, vPer.txtTotal);
    }
    if (e.getSource() == vPer.btnRegistrar) {
      Map data_per = new HashMap();
      data_per.put("tipo_doc", datos.getIdTipoDocumento());
      data_per.put("num_doc", datos.getNumDocumento());
      data_per.put("nombres", datos.getNombres());
      data_per.put("ape_paterno", datos.getApePaterno());
      data_per.put("ape_materno", datos.getApeMaterno());
      data_per.put("fecha_nac", datos.getFechaNac());
      data_per.put("email", datos.getEmail());
      data_per.put("celular", datos.getCelular());
      Map result = Validaciones.ValidarDatos_NuevaPersona(data_per);
      if ((boolean) result.get("success")) {
        Map tmp = (Map) result.get("data");
        int id_persona = (int) tmp.get("id_persona");
        if (id_persona == 0) {
          mPersonas per = new mPersonas();
          per.setIdTipoDocumento((int) tmp.get("id_tipo_documento"));
          per.setNumDocumento((String) tmp.get("numero_documento"));
          per.setNombres((String) tmp.get("nombres"));
          per.setApePaterno((String) tmp.get("ape_paterno"));
          per.setApeMaterno((String) tmp.get("ape_materno"));
          per.setFechaNac((String) tmp.get("fecha_nacimiento"));
          per.setEmail((String) tmp.get("email"));
          per.setCelular((String) tmp.get("celular"));
          perDAO = new PersonasDAO();
          id_persona = perDAO.InsertarRegistro_Personas(per);
          if (id_persona == 0) {
            Mensajes.M1("Ocurrió un error inesperado (1)");
          } else {
            Mensajes.M1("Persona registrada correctamente");
            LimpiarCampos_Persona();
            perDAO.ListarRegistros_Personas(datos, vPer.tblPersonas, vPer.txtTotal);
          }
        } else {
          Mensajes.M1("La persona ya está registrada (1)");
        }
      } else {
        Mensajes.M1((String) result.get("mensaje"));
      }
    }
    if (e.getSource() == vPer.btnActualizar) {
      Map data_per = new HashMap();
      data_per.put("id_persona", datos.getIdPersona());
      data_per.put("tipo_doc", datos.getIdTipoDocumento());
      data_per.put("num_doc", datos.getNumDocumento());
      data_per.put("nombres", datos.getNombres());
      data_per.put("ape_paterno", datos.getApePaterno());
      data_per.put("ape_materno", datos.getApeMaterno());
      data_per.put("fecha_nac", datos.getFechaNac());
      data_per.put("email", datos.getEmail());
      data_per.put("celular", datos.getCelular());
      Map result = Validaciones.ValidarDatos_ActualizarPersona(data_per);
      if ((boolean) result.get("success")) {
        Map tmp = (Map) result.get("data");
        int id_persona = (int) tmp.get("id_persona");
        if (id_persona >= 1) {
          mPersonas per = new mPersonas();
          per.setIdPersona((int) tmp.get("id_persona"));
          per.setIdTipoDocumento((int) tmp.get("id_tipo_documento"));
          per.setNumDocumento((String) tmp.get("numero_documento"));
          per.setNombres((String) tmp.get("nombres"));
          per.setApePaterno((String) tmp.get("ape_paterno"));
          per.setApeMaterno((String) tmp.get("ape_materno"));
          per.setFechaNac((String) tmp.get("fecha_nacimiento"));
          per.setEmail((String) tmp.get("email"));
          per.setCelular((String) tmp.get("celular"));
          perDAO = new PersonasDAO();
          boolean upt_success = perDAO.ActualizarRegistro_Personas(per);
          if (upt_success) {
            Mensajes.M1("Persona actualizada correctamente");
            LimpiarCampos_Persona();
            perDAO.ListarRegistros_Personas(datos, vPer.tblPersonas, vPer.txtTotal);
          } else {
            Mensajes.M1("Ocurrió un error inesperado (1)");
          }
        } else {
          Mensajes.M1("La persona ya está registrada (*)");
        }
      } else {
        Mensajes.M1((String) result.get("mensaje"));
      }
    }
  }
}
