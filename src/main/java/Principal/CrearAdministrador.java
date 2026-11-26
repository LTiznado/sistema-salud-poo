package Principal;

import Conexion.ConexionBD;
import Modelo.mPersonas;
import Servicio.RegistroService;
import java.io.Console;
import java.util.Arrays;

public final class CrearAdministrador {
  public static void main(String[] args) {
    if (ConexionBD.isDemo()) {
      System.err.println(
          "El modo demo ya tiene cuentas ficticias. Configura SALUD_DB_MODE=mysql para una"
              + " instalación nueva.");
      System.exit(1);
    }
    Console console = System.console();
    if (console == null) {
      System.err.println("Ejecuta esta herramienta desde una terminal interactiva.");
      System.exit(1);
    }
    char[] password = null, repeat = null;
    try {
      mPersonas p = new mPersonas();
      p.setIdTipoDocumento(
          Integer.parseInt(console.readLine("Tipo de documento (1=DNI, 2=CE, 3=Pasaporte): ")));
      p.setNumDocumento(console.readLine("Documento: ").trim());
      p.setNombres(console.readLine("Nombres: ").trim());
      p.setApePaterno(console.readLine("Apellido paterno: ").trim());
      p.setApeMaterno(console.readLine("Apellido materno: ").trim());
      p.setFechaNac(console.readLine("Nacimiento (AAAA-MM-DD): ").trim());
      p.setEmail(console.readLine("Correo (opcional): ").trim());
      p.setCelular("");
      password = console.readPassword("Contraseña (8 a 128 caracteres): ");
      repeat = console.readPassword("Repetir contraseña: ");
      if (!Arrays.equals(password, repeat))
        throw new IllegalArgumentException("Las contraseñas no coinciden.");
      new RegistroService().bootstrapAdministrator(p, new String(password));
      console.printf("Administrador creado. Ya puedes iniciar la aplicación.%n");
    } catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    } catch (Exception e) {
      System.err.println(
          "No se pudo crear el administrador. Comprueba la conexión y que la base esté recién"
              + " instalada.");
      System.exit(1);
    } finally {
      if (password != null) Arrays.fill(password, '\0');
      if (repeat != null) Arrays.fill(repeat, '\0');
    }
  }
}
