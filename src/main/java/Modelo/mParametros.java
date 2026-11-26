package Modelo;

public class mParametros {
  private int idParametro;
  private int idCatalogo;
  private String nombreParametro;
  private int estado;

  public void setEstado(int estado) {
    this.estado = estado;
  }

  public int getEstado() {
    return estado;
  }

  public int getIdParametro() {
    return idParametro;
  }

  public int getIdCatalogo() {
    return idCatalogo;
  }

  public String getNombreParametro() {
    return nombreParametro;
  }

  public void setIdParametro(int idParametro) {
    this.idParametro = idParametro;
  }

  public void setIdCatalogo(int idCatalogo) {
    this.idCatalogo = idCatalogo;
  }

  public void setNombreParametro(String nombreParametro) {
    this.nombreParametro = nombreParametro;
  }

  @Override
  public String toString() {
    return this.nombreParametro;
  }

  public void getNombreParametro(String nomTipoDocumento) {
    throw new UnsupportedOperationException(
        "Not supported yet."); // To change body of generated methods, choose Tools | Templates.
  }
}
