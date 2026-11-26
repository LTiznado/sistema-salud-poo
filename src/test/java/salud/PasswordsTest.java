package salud;

import static org.junit.jupiter.api.Assertions.*;

import Helpers.Passwords;
import org.junit.jupiter.api.Test;

class PasswordsTest {
  @Test
  void hashesUseDifferentSaltsAndValidateOnlyTheCorrectPassword() {
    String a = Passwords.hash("PruebaSegura2026!"), b = Passwords.hash("PruebaSegura2026!");
    assertNotEquals(a, b);
    assertFalse(a.contains("PruebaSegura"));
    assertTrue(Passwords.verify("PruebaSegura2026!", a));
    assertFalse(Passwords.verify("otra", a));
  }

  @Test
  void rejectsPlaintextMalformedAndExcessiveWorkFactors() {
    for (String value :
        new String[] {
          "clave-antigua", "pbkdf2$no$abc$abc", "pbkdf2$999999999$abc$abc", "pbkdf2$600000$abc$abc"
        }) assertFalse(Passwords.verify("clave-antigua", value));
    assertFalse(Passwords.verify(null, null));
  }

  @Test
  void enforcesPasswordBounds() {
    assertThrows(IllegalArgumentException.class, () -> Passwords.hash("corta"));
    assertThrows(IllegalArgumentException.class, () -> Passwords.hash("x".repeat(129)));
  }
}
