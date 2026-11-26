package Principal;

import Conexion.*;
import Controlador.cLogin;
import Vista.Login;
import javax.swing.*;

public class Main {
  public static void configureUi() throws Exception {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    java.util.List<Object> keys = java.util.Collections.list(UIManager.getDefaults().keys());
    for (Object key : keys) {
      Object value = UIManager.get(key);
      if (value instanceof javax.swing.plaf.FontUIResource font) {
        UIManager.put(
            key, new javax.swing.plaf.FontUIResource(font.getName(), font.getStyle(), 14));
      }
    }
  }

  public static void main(String[] args) {
    try {
      if (ConexionBD.isDemo()) DatabaseSetup.initializeDemo();
      else
        try (java.sql.Connection ignored = new ConexionBD().getConecction()) {
          /* connection check only */
        }
      if (java.util.Arrays.asList(args).contains("--check")) {
        System.out.println(
            "Base de datos disponible. Modo: " + (ConexionBD.isDemo() ? "demo" : "externo"));
        return;
      }
      configureUi();
      SwingUtilities.invokeLater(() -> new cLogin(new Login()));
    } catch (Exception e) {
      String message =
          "No se pudo iniciar el sistema. Comprueba la base de datos y la configuración. Detalle: "
              + e.getClass().getSimpleName();
      if (java.awt.GraphicsEnvironment.isHeadless()) System.err.println(message);
      else
        JOptionPane.showMessageDialog(null, message, "Sistema de Salud", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }
}
