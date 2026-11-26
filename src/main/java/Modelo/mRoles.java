package Modelo;

public class mRoles {
  private int idRol;
  private String NombreRol;
  private int estado;

  public void setEstado(int estado) {
    this.estado = estado;
  }

  public int getEstado() {
    return estado;
  }

  public int getIdRol() {
    return idRol;
  }

  public String getNombreRol() {
    return NombreRol;
  }

  public void setIdRol(int idRol) {
    this.idRol = idRol;
  }

  public void setNombreRol(String NombreRol) {
    this.NombreRol = NombreRol;
  }

  @Override
  public String toString() {
    return this.NombreRol;
  }
}
