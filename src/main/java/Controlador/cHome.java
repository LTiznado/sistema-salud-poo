package Controlador;

import Helpers.Miscelanea;
import Modelo.*;
import Vista.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;

public class cHome implements ActionListener {
  Home vHome;
  private final Sesion session;
  private final javax.swing.JMenuItem adminCitas = new javax.swing.JMenuItem("Gestión de Citas");
  GestionPersonas ges_per;
  cPersonas cPer;
  GestionUsuarios ges_usu;
  cUsuarios cDoc;

  public cHome(Home home, mUsuarioRol usu_rol) {
    vHome = home;
    vHome.jdpnContenedor.setBackground(new java.awt.Color(233, 239, 244));
    session = new Sesion(usu_rol.getIdUsuario(), usu_rol.getIdRol());
    if (session.rol() == 1) {
      vHome.menuAdministrador.add(adminCitas);
      adminCitas.addActionListener(this);
    }
    vHome.jmiGestionPersonas.addActionListener(this);
    vHome.jmiGestionUsuarios.addActionListener(this);
    vHome.jmiGestionCitas.addActionListener(this);
    vHome.jmiAgendarCita.addActionListener(this);
    vHome.jmiConsultarCita.addActionListener(this);
    vHome.btnSalir.addActionListener(this);
    vHome.setExtendedState(JFrame.MAXIMIZED_BOTH);
    vHome.setDefaultCloseOperation(vHome.EXIT_ON_CLOSE);
    vHome.setVisible(true);
    vHome.setTitle(
        "Sistema de Salud" + (Conexion.ConexionBD.isDemo() ? " · DEMOSTRACIÓN LOCAL" : ""));
    int id_rol = usu_rol.getIdRol();
    switch (id_rol) {
      case 1: // Administrador
        vHome.menuAdministrador.setEnabled(true);
        vHome.menuDoctor.setEnabled(false);
        vHome.menuPaciente.setEnabled(false);
        break;
      case 2: // Doctor
        vHome.menuAdministrador.setEnabled(false);
        vHome.menuDoctor.setEnabled(true);
        vHome.menuPaciente.setEnabled(false);
        break;
      case 3: // Paciente
        vHome.menuAdministrador.setEnabled(false);
        vHome.menuDoctor.setEnabled(false);
        vHome.menuPaciente.setEnabled(true);
        break;
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    for (javax.swing.JInternalFrame frame : vHome.jdpnContenedor.getAllFrames()) frame.dispose();
    if (e.getSource() == vHome.jmiGestionPersonas) {
      System.out.println("Gestion Personas");
      ges_per = new GestionPersonas();
      cPer = new cPersonas(ges_per);
      vHome.jdpnContenedor.add(ges_per);
      Miscelanea.CentrarVistaInterna(vHome.jdpnContenedor, ges_per);
    }
    if (e.getSource() == vHome.jmiGestionUsuarios) {
      System.out.println("Gestion Usuarios");
      ges_usu = new GestionUsuarios();
      cDoc = new cUsuarios(ges_usu);
      // vHome.jdpnContenedor.removeAll();
      vHome.jdpnContenedor.add(ges_usu);
      // vHome.jdpnContenedor.revalidate();
      // vHome.jdpnContenedor.repaint();
      Miscelanea.CentrarVistaInterna(vHome.jdpnContenedor, ges_usu);
    }

    if (e.getSource() == adminCitas
        || e.getSource() == vHome.jmiGestionCitas
        || e.getSource() == vHome.jmiAgendarCita
        || e.getSource() == vHome.jmiConsultarCita) {
      GestionCitas citas = new GestionCitas(session);
      vHome.jdpnContenedor.add(citas);
      Miscelanea.CentrarVistaInterna(vHome.jdpnContenedor, citas);
    }
    if (e.getSource() == vHome.btnSalir) {
      new cLogin(new Login());
      vHome.dispose();
    }
    vHome.jdpnContenedor.revalidate();
    vHome.jdpnContenedor.repaint();
  }
}
