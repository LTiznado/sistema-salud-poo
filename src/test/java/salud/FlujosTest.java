package salud;

import static org.junit.jupiter.api.Assertions.*;

import Conexion.*;
import DAO.*;
import Helpers.*;
import Modelo.*;
import Servicio.*;
import Vista.GestionCitas;
import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;
import org.junit.jupiter.api.*;

class FlujosTest {
  final Sesion admin = new Sesion(1, 1), doctor = new Sesion(2, 2), patient = new Sesion(3, 3);
  final CitasService citas = new CitasService();

  LocalDateTime tomorrow() {
    return LocalDate.now().plusDays(1).atTime(9, 0);
  }

  @BeforeEach
  void database() throws Exception {
    System.setProperty("salud.db.mode", "demo");
    System.setProperty(
        "salud.db.url",
        "jdbc:h2:mem:"
            + UUID.randomUUID()
            + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
    DatabaseSetup.initializeDemo();
  }

  @AfterEach
  void cleanup() throws Exception {
    try (Connection con = new ConexionBD().getConecction();
        Statement st = con.createStatement()) {
      st.execute("SHUTDOWN");
    }
    System.clearProperty("salud.db.url");
    System.clearProperty("salud.db.mode");
  }

  mPersonas person(String document) {
    mPersonas p = new mPersonas();
    p.setIdTipoDocumento(1);
    p.setNumDocumento(document);
    p.setNombres("Paciente");
    p.setApePaterno("Prueba");
    p.setApeMaterno("Ficticio");
    p.setFechaNac("2000-01-01");
    p.setEmail("prueba@example.invalid");
    p.setCelular("999000111");
    return p;
  }

  int count(String table) throws Exception {
    return Jdbc.number(Jdbc.query("SELECT COUNT(*) AS n FROM " + table).get(0).get("n"));
  }

  Map login(String doc, String password, int role) {
    return Validaciones.ValidarDatos_AccesoSistema(
        Map.of("tipo_usuario", role, "num_doc", doc, "password", password));
  }

  int extraPatient() throws Exception {
    return new RegistroService()
        .registerPatient(person("00000004"), "NuevaClave2026!", "NuevaClave2026!");
  }

  @Test
  void bootstrapIsIdempotentAndStoresOnlyHashes() throws Exception {
    DatabaseSetup.initializeDemo();
    assertEquals(3, count("usuarios"));
    for (Map<String, Object> r : Jdbc.query("SELECT clave FROM usuarios"))
      assertTrue(r.get("clave").toString().startsWith("pbkdf2$"));
  }

  @Test
  void loginChecksPasswordRoleAndActiveAccount() throws Exception {
    assertEquals(true, login("00000003", "DemoSalud2026!", 3).get("success"));
    assertEquals(false, login("00000003", "incorrecta", 3).get("success"));
    assertEquals(false, login("00000003", "DemoSalud2026!", 1).get("success"));
    Jdbc.update("UPDATE usuarios SET estado=0 WHERE id_usuario=3");
    assertEquals(false, login("00000003", "DemoSalud2026!", 3).get("success"));
  }

  @Test
  void loginRejectsSqlInjection() {
    assertEquals(false, login("' OR 1=1 --", "DemoSalud2026!", 1).get("success"));
  }

  @Test
  void disabledRolesCannotLogin() throws Exception {
    Jdbc.update("UPDATE roles SET estado=0 WHERE id_rol=3");
    assertEquals(false, login("00000003", "DemoSalud2026!", 3).get("success"));
  }

  @Test
  void registerCreatesCompletePatientAndCanLogin() throws Exception {
    int user = extraPatient();
    assertTrue(user > 3);
    assertEquals(4, count("personas"));
    assertEquals(4, count("usuarios"));
    assertEquals(
        3,
        Jdbc.number(
            Jdbc.query("SELECT id_rol FROM usuario_rol WHERE id_usuario=?", user)
                .get(0)
                .get("id_rol")));
    assertEquals(true, login("00000004", "NuevaClave2026!", 3).get("success"));
  }

  @Test
  void registerRejectsDuplicateIdentityWithoutClaimingIt() throws Exception {
    assertThrows(
        SQLException.class,
        () ->
            new RegistroService()
                .registerPatient(person("00000001"), "NuevaClave2026!", "NuevaClave2026!"));
    assertEquals(3, count("usuarios"));
    assertEquals(true, login("00000001", "DemoSalud2026!", 1).get("success"));
  }

  @Test
  void registrationRollsBackPersonAndAccountIfRoleCannotBeSaved() throws Exception {
    Jdbc.update("DELETE FROM usuario_rol WHERE id_rol=3");
    Jdbc.update("DELETE FROM roles WHERE id_rol=3");
    assertThrows(SQLException.class, this::extraPatient);
    assertEquals(3, count("personas"));
    assertEquals(3, count("usuarios"));
  }

  @Test
  void failedRepeatedPasswordDoesNotLeaveAnOrphan() throws Exception {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new RegistroService()
                .registerPatient(person("00000004"), "NuevaClave2026!", "Distinta2026!"));
    assertEquals(3, count("personas"));
  }

  @Test
  void adminAssignmentRollsBackNewUserIfRoleIsInactiveOrAbsent() throws Exception {
    int id;
    try (Connection con = new ConexionBD().getConecction()) {
      id = PersonasDAO.insert(con, person("00000004"));
    }
    Jdbc.update("DELETE FROM usuario_rol WHERE id_rol=2");
    Jdbc.update("DELETE FROM roles WHERE id_rol=2");
    assertThrows(
        SQLException.class, () -> new RegistroService().assignUserRole(id, 2, "NuevaClave2026!"));
    assertEquals(3, count("usuarios"));
  }

  @Test
  void passportsAndLongPasswordsWorkWithoutDocumentAmbiguity() throws Exception {
    mPersonas p = person("PASAPORTE001");
    p.setIdTipoDocumento(3);
    String password = "A".repeat(100) + "2026!";
    new RegistroService().registerPatient(p, password, password);
    assertEquals(true, login("PASAPORTE001", password, 3).get("success"));
    p.setIdTipoDocumento(2);
    assertThrows(
        SQLException.class, () -> new RegistroService().registerPatient(p, password, password));
  }

  @Test
  void bootstrapCannotOverwriteExistingUsers() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new RegistroService().bootstrapAdministrator(person("00000004"), "NuevaClave2026!"));
  }

  @Test
  void bootstrapCreatesFirstAdministratorAtomically() throws Exception {
    Jdbc.update("DELETE FROM usuario_rol");
    Jdbc.update("DELETE FROM usuarios");
    Jdbc.update("DELETE FROM personas");
    int id = new RegistroService().bootstrapAdministrator(person("00000004"), "NuevaClave2026!");
    assertTrue(id > 0);
    assertEquals(true, login("00000004", "NuevaClave2026!", 1).get("success"));
    assertEquals(1, count("usuarios"));
  }

  @Test
  void resetPasswordDoesNotReturnItToTheAdministrator() throws Exception {
    mUsuarios u = new mUsuarios();
    u.setIdUsuario(3);
    u.setClave("Cambiada2026!");
    assertTrue(new UsuariosDAO().ActualizarRegistro_Usuarios(u));
    assertEquals(true, login("00000003", "Cambiada2026!", 3).get("success"));
    assertEquals(false, login("00000003", "DemoSalud2026!", 3).get("success"));
    assertEquals("", new UsuarioRolDAO().CargarDatos_Usuarios(3, 3).get("password"));
  }

  @Test
  void peopleSupportApostrophesAndBoundFilters() throws Exception {
    mPersonas p = person("00000004");
    p.setApePaterno("O'Connor");
    int id;
    try (Connection con = new ConexionBD().getConecction()) {
      id = PersonasDAO.insert(con, p);
    }
    assertEquals("O'Connor", new PersonasDAO().CargarDatos_Persona(id).getApePaterno());
    p.setNumDocumento("' OR 1=1 --");
    p.setNombres("");
    p.setApePaterno("");
    p.setApeMaterno("");
    p.setFechaNac("");
    p.setEmail("");
    p.setCelular("");
    p.setIdTipoDocumento(0);
    JTable table = new JTable();
    new PersonasDAO().ListarRegistros_Personas(p, table, new JLabel());
    assertEquals(0, table.getRowCount());
  }

  @Test
  void invalidBirthdateEmailAndDocumentAreRejected() {
    mPersonas p = person("00000004");
    p.setFechaNac("2000-02-30");
    assertThrows(IllegalArgumentException.class, () -> Validaciones.validatePerson(p));
    p.setFechaNac("2000-01-01");
    p.setEmail("sin-arroba");
    assertThrows(IllegalArgumentException.class, () -> Validaciones.validatePerson(p));
    p.setEmail("");
    p.setNumDocumento("' OR 1=1");
    assertThrows(IllegalArgumentException.class, () -> Validaciones.validatePerson(p));
  }

  @Test
  void patientBooksViewsAndExportsOwnAppointment() throws Exception {
    int id = citas.agendar(patient, 3, 2, tomorrow(), "Control general");
    assertEquals(1, citas.listar(patient).size());
    assertEquals(1, citas.listar(doctor).size());
    assertTrue(citas.comprobante(patient, id).contains("Control general"));
  }

  @Test
  void doctorsAndPatientsCannotBookForAnotherPatient() throws Exception {
    int other = extraPatient();
    assertThrows(
        SecurityException.class, () -> citas.agendar(patient, other, 2, tomorrow(), "Control"));
    assertThrows(SecurityException.class, () -> citas.agendar(doctor, 3, 2, tomorrow(), "Control"));
  }

  @Test
  void doctorCannotBeBookedTwiceAndCancellationReleasesSlot() throws Exception {
    int other = extraPatient();
    int id = citas.agendar(patient, 3, 2, tomorrow(), "Control");
    assertThrows(SQLException.class, () -> citas.agendar(admin, other, 2, tomorrow(), "Control"));
    assertTrue(citas.cancelar(patient, id));
    assertFalse(citas.cancelar(patient, id));
    assertTrue(citas.agendar(admin, other, 2, tomorrow(), "Reutilizar turno") > 0);
  }

  @Test
  void patientCannotHaveTwoDoctorsAtSameTime() throws Exception {
    int newPerson;
    try (Connection con = new ConexionBD().getConecction()) {
      newPerson = PersonasDAO.insert(con, person("00000004"));
    }
    int newDoctor = new RegistroService().assignUserRole(newPerson, 2, "DoctorClave2026!");
    citas.agendar(patient, 3, 2, tomorrow(), "Control");
    assertThrows(
        SQLException.class,
        () -> citas.agendar(patient, 3, newDoctor, tomorrow(), "Segundo control"));
  }

  @Test
  void privacyPreventsReadingCancellingOrExportingAnotherPatientsAppointment() throws Exception {
    int other = extraPatient(), id = citas.agendar(admin, other, 2, tomorrow(), "Privado");
    assertTrue(citas.listar(patient).isEmpty());
    assertFalse(citas.cancelar(patient, id));
    assertThrows(SecurityException.class, () -> citas.comprobante(patient, id));
    assertEquals(1, citas.listar(admin).size());
    assertThrows(SecurityException.class, () -> citas.opciones(patient, 3));
  }

  @Test
  void revokedSessionAndInactiveDoctorsAreRejected() throws Exception {
    Jdbc.update("UPDATE usuarios SET estado=0 WHERE id_usuario=2");
    assertThrows(
        IllegalArgumentException.class, () -> citas.agendar(patient, 3, 2, tomorrow(), "Control"));
    Jdbc.update("UPDATE usuario_rol SET estado=0 WHERE id_usuario=3");
    assertThrows(SecurityException.class, () -> citas.listar(patient));
  }

  @Test
  void invalidTimesAndEmptyReasonsAreRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> citas.agendar(patient, 3, 2, LocalDateTime.now().minusDays(1), "Control"));
    assertThrows(
        IllegalArgumentException.class,
        () -> citas.agendar(patient, 3, 2, tomorrow().plusMinutes(5), "Control"));
    assertThrows(
        IllegalArgumentException.class, () -> citas.agendar(patient, 3, 2, tomorrow(), " "));
  }

  @Test
  void complaintPersistsTheExactTextSafely() throws Exception {
    int id = citas.reclamar(patient, "Demora en atención ' ; --");
    Map<String, Object> row = Jdbc.query("SELECT * FROM reclamos WHERE id_reclamo=?", id).get(0);
    assertEquals(3, Jdbc.number(row.get("id_usuario")));
    assertEquals("Demora en atención ' ; --", row.get("detalle"));
    assertThrows(IllegalArgumentException.class, () -> citas.reclamar(patient, "x".repeat(2001)));
  }

  @Test
  void concurrentBookingsCannotDoubleReserveADoctor() throws Exception {
    int other = extraPatient();
    ExecutorService pool = Executors.newFixedThreadPool(2);
    String url = System.getProperty("salud.db.url");
    try {
      Callable<Boolean> first =
          () -> {
            try {
              citas.agendar(admin, 3, 2, tomorrow(), "Uno");
              return true;
            } catch (SQLException e) {
              return false;
            }
          };
      Callable<Boolean> second =
          () -> {
            try {
              citas.agendar(admin, other, 2, tomorrow(), "Dos");
              return true;
            } catch (SQLException e) {
              return false;
            }
          };
      List<Future<Boolean>> results = pool.invokeAll(List.of(first, second));
      int successes = 0;
      for (Future<Boolean> result : results) if (result.get(15, TimeUnit.SECONDS)) successes++;
      assertEquals(1, successes);
      assertEquals(1, count("citas"));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void appointmentScreenConstructsForEveryRoleOnSwingThread() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          for (Sesion s : List.of(admin, doctor, patient)) {
            GestionCitas view = new GestionCitas(s);
            assertTrue(view.isVisible());
            view.dispose();
          }
        });
  }
}
