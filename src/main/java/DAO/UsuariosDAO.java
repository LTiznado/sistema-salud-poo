package DAO;

import Conexion.ConexionBD;
import Helpers.*;
import Modelo.mUsuarios;
import java.sql.*;

public class UsuariosDAO {
  public int InsertarRegistro_Usuario(mUsuarios user) {
    try (Connection con = new ConexionBD().getConecction()) {
      return Jdbc.insert(
          con,
          "INSERT INTO usuarios(id_persona,clave) VALUES(?,?)",
          user.getIdPersona(),
          Passwords.hash(user.getClave()));
    } catch (SQLException | IllegalArgumentException e) {
      Mensajes.M1("No se pudo registrar el usuario.");
      return 0;
    }
  }

  public boolean ActualizarRegistro_Usuarios(mUsuarios user) {
    try {
      return Jdbc.update(
              "UPDATE usuarios SET clave=? WHERE id_usuario=?",
              Passwords.hash(user.getClave()),
              user.getIdUsuario())
          == 1;
    } catch (SQLException | IllegalArgumentException e) {
      Mensajes.M1("No se pudo actualizar la contraseña.");
      return false;
    }
  }
}
