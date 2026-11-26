package Modelo;

/** Logged-in user and selected role; services recheck both against the database. */
public record Sesion(int usuario, int rol) {
  public Sesion {
    if (usuario < 1 || rol < 1 || rol > 3) throw new IllegalArgumentException("Sesión inválida.");
  }
}
