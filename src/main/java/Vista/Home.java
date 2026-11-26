package Vista;

public class Home extends javax.swing.JFrame {

    public Home() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jdpnContenedor = new javax.swing.JDesktopPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuAdministrador = new javax.swing.JMenu();
        jmiGestionPersonas = new javax.swing.JMenuItem();
        jmiGestionUsuarios = new javax.swing.JMenuItem();
        menuDoctor = new javax.swing.JMenu();
        jmiGestionCitas = new javax.swing.JMenuItem();
        menuPaciente = new javax.swing.JMenu();
        jmiAgendarCita = new javax.swing.JMenuItem();
        jmiConsultarCita = new javax.swing.JMenuItem();
        btnSalir = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jdpnContenedor.setBackground(new java.awt.Color(102, 204, 255));

        javax.swing.GroupLayout jdpnContenedorLayout = new javax.swing.GroupLayout(jdpnContenedor);
        jdpnContenedor.setLayout(jdpnContenedorLayout);
        jdpnContenedorLayout.setHorizontalGroup(
            jdpnContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 815, Short.MAX_VALUE)
        );
        jdpnContenedorLayout.setVerticalGroup(
            jdpnContenedorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 522, Short.MAX_VALUE)
        );

        menuAdministrador.setText("Administrador");

        jmiGestionPersonas.setText("Gestión de Personas");
        menuAdministrador.add(jmiGestionPersonas);

        jmiGestionUsuarios.setText("Gestión de Usuarios");
        menuAdministrador.add(jmiGestionUsuarios);

        jMenuBar1.add(menuAdministrador);

        menuDoctor.setText("Doctor");

        jmiGestionCitas.setText("Gestión de Citas");
        menuDoctor.add(jmiGestionCitas);

        jMenuBar1.add(menuDoctor);

        menuPaciente.setText("Paciente");

        jmiAgendarCita.setText("Agendar Cita");
        menuPaciente.add(jmiAgendarCita);

        jmiConsultarCita.setText("Consultar Cita");
        menuPaciente.add(jmiConsultarCita);

        jMenuBar1.add(menuPaciente);

        btnSalir.setText("Salir");
        jMenuBar1.add(btnSalir);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jdpnContenedor)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jdpnContenedor)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JMenu btnSalir;
    private javax.swing.JMenuBar jMenuBar1;
    public javax.swing.JDesktopPane jdpnContenedor;
    public javax.swing.JMenuItem jmiAgendarCita;
    public javax.swing.JMenuItem jmiConsultarCita;
    public javax.swing.JMenuItem jmiGestionCitas;
    public javax.swing.JMenuItem jmiGestionPersonas;
    public javax.swing.JMenuItem jmiGestionUsuarios;
    public javax.swing.JMenu menuAdministrador;
    public javax.swing.JMenu menuDoctor;
    public javax.swing.JMenu menuPaciente;
    // End of variables declaration//GEN-END:variables
}
