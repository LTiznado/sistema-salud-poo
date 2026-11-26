package Modelo;

public class mUsuarios extends mPersonas {
  private int idUsuario;
  private String Clave;
  private int estado;

  public void setIdUsuario(int idUsuario) {
    this.idUsuario = idUsuario;
  }

  public void setClave(String Clave) {
    this.Clave = Clave;
  }

  public void setEstado(int estado) {
    this.estado = estado;
  }

  public int getIdUsuario() {
    return idUsuario;
  }

  public String getClave() {
    return Clave;
  }

  public int getEstado() {
    return estado;
  }
}
