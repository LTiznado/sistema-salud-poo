package Helpers;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted PBKDF2; plaintext and malformed legacy credentials are rejected. */
public final class Passwords {
  private static final int ITERATIONS = 600_000;

  private Passwords() {}

  public static String hash(String password) {
    if (password == null || password.length() < 8 || password.length() > 128)
      throw new IllegalArgumentException("La contraseña debe tener entre 8 y 128 caracteres.");
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    return "pbkdf2$"
        + ITERATIONS
        + "$"
        + Base64.getEncoder().encodeToString(salt)
        + "$"
        + Base64.getEncoder().encodeToString(derive(password, salt, ITERATIONS));
  }

  public static boolean verify(String password, String stored) {
    if (password == null || password.length() > 128 || stored == null) return false;
    try {
      String[] parts = stored.split("\\$");
      if (parts.length != 4 || !parts[0].equals("pbkdf2")) return false;
      int iterations = Integer.parseInt(parts[1]);
      if (iterations < 100_000 || iterations > 2_000_000) return false;
      byte[] salt = Base64.getDecoder().decode(parts[2]);
      byte[] expected = Base64.getDecoder().decode(parts[3]);
      if (salt.length != 16 || expected.length != 32) return false;
      return MessageDigest.isEqual(expected, derive(password, salt, iterations));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static byte[] derive(String password, byte[] salt, int iterations) {
    PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("No se pudo proteger la contraseña.", e);
    } finally {
      spec.clearPassword();
    }
  }
}
