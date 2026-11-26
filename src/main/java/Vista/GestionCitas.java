package Vista;

import Helpers.Mensajes;
import Modelo.Sesion;
import Servicio.CitasService;
import Servicio.CitasService.*;
import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/** Integrated appointment flow; original standalone forms remain as academic references. */
public class GestionCitas extends JInternalFrame {
  private final Sesion session;
  private final CitasService service = new CitasService();
  private final DefaultTableModel model =
      new DefaultTableModel(
          new String[] {"N.º", "Paciente", "Doctor", "Fecha y hora", "Motivo", "Estado"}, 0) {
        public boolean isCellEditable(int r, int c) {
          return false;
        }
      };
  private final JTable table = new JTable(model);
  private final JComboBox<Opcion> doctors = new JComboBox<>(), patients = new JComboBox<>();
  private final JTextField date = new JTextField(LocalDate.now().plusDays(1).toString(), 10),
      reason = new JTextField(24);
  private final JComboBox<String> hour = new JComboBox<>();
  private final JLabel status = new JLabel(" ");

  public GestionCitas(Sesion session) {
    super(session.rol() == 2 ? "Agenda del doctor" : "Gestión de citas", true, true, true, true);
    this.session = session;
    setLayout(new BorderLayout(12, 12));
    setSize(1000, 610);
    setMinimumSize(new Dimension(780, 480));
    JPanel content = new JPanel(new BorderLayout(12, 12));
    content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(content);
    JLabel title = new JLabel(session.rol() == 3 ? "Mis citas médicas" : "Agenda de atención");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 22));
    JPanel top = new JPanel(new BorderLayout(0, 12));
    top.add(title, BorderLayout.NORTH);
    JPanel form = new JPanel(new GridBagLayout());
    GridBagConstraints g = new GridBagConstraints();
    g.insets = new Insets(4, 4, 4, 4);
    g.fill = GridBagConstraints.HORIZONTAL;
    for (int h = 7; h < 20; h++)
      for (int m : new int[] {0, 30}) hour.addItem(String.format("%02d:%02d", h, m));
    hour.setSelectedItem("09:00");
    if (session.rol() != 2) {
      int row = 0;
      if (session.rol() == 1) addField(form, g, row++, "Paciente", patients);
      addField(form, g, row++, "Doctor", doctors);
      addField(form, g, row++, "Fecha (AAAA-MM-DD)", date);
      addField(form, g, row++, "Turno de 30 minutos", hour);
      addField(form, g, row++, "Motivo", reason);
      JButton book = new JButton("Agendar cita");
      book.addActionListener(e -> book());
      g.gridx = 1;
      g.gridy = row;
      form.add(book, g);
      top.add(form, BorderLayout.CENTER);
    }
    content.add(top, BorderLayout.NORTH);
    table.setRowHeight(27);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setAutoCreateRowSorter(true);
    content.add(new JScrollPane(table), BorderLayout.CENTER);
    JPanel bottom = new JPanel(new BorderLayout());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JButton reload = new JButton("Actualizar"),
        cancel = new JButton("Cancelar cita"),
        receipt = new JButton("Guardar comprobante"),
        complaint = new JButton("Registrar reclamo");
    reload.addActionListener(e -> refresh());
    cancel.addActionListener(e -> cancel());
    receipt.addActionListener(e -> receipt());
    complaint.addActionListener(e -> complaint());
    for (JButton button : new JButton[] {reload, cancel, receipt, complaint}) actions.add(button);
    bottom.add(actions, BorderLayout.NORTH);
    bottom.add(status, BorderLayout.SOUTH);
    content.add(bottom, BorderLayout.SOUTH);
    try {
      if (session.rol() != 2) {
        for (Opcion option : service.opciones(session, 2)) doctors.addItem(option);
        if (session.rol() == 1)
          for (Opcion option : service.opciones(session, 3)) patients.addItem(option);
      }
    } catch (SQLException | SecurityException e) {
      error(e);
    }
    refresh();
    setVisible(true);
  }

  private void addField(
      JPanel panel, GridBagConstraints g, int row, String label, JComponent input) {
    g.gridy = row;
    g.gridx = 0;
    g.weightx = 0;
    panel.add(new JLabel(label), g);
    g.gridx = 1;
    g.weightx = 1;
    panel.add(input, g);
  }

  private int selectedId() {
    int row = table.getSelectedRow();
    if (row < 0) throw new IllegalArgumentException("Selecciona una cita de la tabla.");
    return (int) model.getValueAt(table.convertRowIndexToModel(row), 0);
  }

  private void refresh() {
    try {
      model.setRowCount(0);
      for (Cita cita : service.listar(session))
        model.addRow(
            new Object[] {
              cita.id(),
              cita.paciente(),
              cita.doctor(),
              cita.fecha().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
              cita.motivo(),
              cita.estado()
            });
      status.setText(model.getRowCount() + " citas · Solo se muestran las permitidas para tu rol.");
    } catch (SQLException | SecurityException e) {
      error(e);
    }
  }

  private void book() {
    try {
      Opcion doctor = (Opcion) doctors.getSelectedItem(),
          patient = (Opcion) patients.getSelectedItem();
      if (doctor == null || (session.rol() == 1 && patient == null))
        throw new IllegalArgumentException("No hay doctor o paciente disponible.");
      int id =
          service.agendar(
              session,
              session.rol() == 3 ? session.usuario() : patient.id(),
              doctor.id(),
              LocalDateTime.of(
                  LocalDate.parse(date.getText().trim()),
                  LocalTime.parse((String) hour.getSelectedItem())),
              reason.getText());
      reason.setText("");
      refresh();
      Mensajes.M1("Cita N.º " + id + " registrada.");
    } catch (SQLException e) {
      Mensajes.M1(
          "No se pudo agendar. Comprueba la conexión; el doctor o paciente podrían tener otra cita"
              + " en ese turno.");
    } catch (java.time.DateTimeException | IllegalArgumentException | SecurityException e) {
      error(e);
    }
  }

  private void cancel() {
    try {
      int id = selectedId();
      if (JOptionPane.showConfirmDialog(
              this, "¿Cancelar la cita N.º " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION)
          != JOptionPane.YES_OPTION) return;
      if (!service.cancelar(session, id))
        Mensajes.M1("No se pudo cancelar: la cita ya pasó, fue cancelada o no te pertenece.");
      refresh();
    } catch (SQLException | IllegalArgumentException | SecurityException e) {
      error(e);
    }
  }

  private void receipt() {
    try {
      int id = selectedId();
      String text = service.comprobante(session, id);
      JFileChooser chooser = new JFileChooser();
      chooser.setSelectedFile(new java.io.File("cita-" + id + ".txt"));
      if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        java.nio.file.Path path = chooser.getSelectedFile().toPath();
        if (Files.exists(path)
            && JOptionPane.showConfirmDialog(
                    this,
                    "El archivo ya existe. ¿Reemplazarlo?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return;
        Files.writeString(path, text, StandardCharsets.UTF_8);
        status.setText("Comprobante guardado.");
      }
    } catch (SQLException | IOException | IllegalArgumentException | SecurityException e) {
      error(e);
    }
  }

  private void complaint() {
    JTextArea detail = new JTextArea(8, 40);
    detail.setLineWrap(true);
    detail.setWrapStyleWord(true);
    if (JOptionPane.showConfirmDialog(
            this, new JScrollPane(detail), "Registrar reclamo", JOptionPane.OK_CANCEL_OPTION)
        != JOptionPane.OK_OPTION) return;
    try {
      int id = service.reclamar(session, detail.getText());
      Mensajes.M1("Reclamo N.º " + id + " registrado.");
    } catch (SQLException | IllegalArgumentException | SecurityException e) {
      error(e);
    }
  }

  private void error(Exception e) {
    Mensajes.M1(
        e instanceof SQLException
            ? "No se pudo completar la operación. Comprueba la base de datos."
            : e.getMessage());
  }
}
