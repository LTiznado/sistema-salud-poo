package Modelo;

public class mPersonas {
  private int idPersona;
  private int idTipoDocumento;
  private String NomTipoDocumento;
  private String NumDocumento;
  private String Nombres;
  private String ApePaterno;
  private String ApeMaterno;
  private String FechaNac;
  private String Email;
  private String Celular;
  private int Orden;

  public void setIdPersona(int idPersona) {
    this.idPersona = idPersona;
  }

  public void setIdTipoDocumento(int idTipoDocumento) {
    this.idTipoDocumento = idTipoDocumento;
  }

  public void setNomTipoDocumento(String NomTipoDocumento) {
    this.NomTipoDocumento = NomTipoDocumento;
  }

  public void setNumDocumento(String NumDocumento) {
    this.NumDocumento = NumDocumento;
  }

  public void setNombres(String Nombres) {
    this.Nombres = Nombres;
  }

  public void setApePaterno(String ApePaterno) {
    this.ApePaterno = ApePaterno;
  }

  public void setApeMaterno(String ApeMaterno) {
    this.ApeMaterno = ApeMaterno;
  }

  public void setFechaNac(String FechaNac) {
    this.FechaNac = FechaNac;
  }

  public void setEmail(String Email) {
    this.Email = Email;
  }

  public void setCelular(String Celular) {
    this.Celular = Celular;
  }

  public void setOrden(int Orden) {
    this.Orden = Orden;
  }

  public int getIdPersona() {
    return idPersona;
  }

  public int getIdTipoDocumento() {
    return idTipoDocumento;
  }

  public String getNomTipoDocumento() {
    return NomTipoDocumento;
  }

  public String getNumDocumento() {
    return NumDocumento;
  }

  public String getNombres() {
    return Nombres;
  }

  public String getApePaterno() {
    return ApePaterno;
  }

  public String getApeMaterno() {
    return ApeMaterno;
  }

  public String getFechaNac() {
    return FechaNac;
  }

  public String getEmail() {
    return Email;
  }

  public String getCelular() {
    return Celular;
  }

  public int getOrden() {
    return Orden;
  }
}
