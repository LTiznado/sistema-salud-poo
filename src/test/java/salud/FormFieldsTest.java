package salud;

import static org.junit.jupiter.api.Assertions.*;

import Helpers.FormFields;
import javax.swing.*;
import org.junit.jupiter.api.Test;

class FormFieldsTest {
  @Test
  void supportsPassportEditingPasteAndPasswordBounds() throws Exception {
    SwingUtilities.invokeAndWait(
        () -> {
          JTextField document = new JTextField();
          FormFields.limit(document, 20);
          document.setText("PASAPORTE001");
          assertEquals("PASAPORTE001", document.getText());
          document.selectAll();
          document.replaceSelection("CE-123456789");
          assertEquals("CE-123456789", document.getText());
          document.setCaretPosition(document.getText().length());
          document.replaceSelection("X".repeat(30));
          assertEquals("CE-123456789", document.getText());
          document.setText("");
          assertEquals("", document.getText());
          JPasswordField password = new JPasswordField();
          FormFields.limit(password, 128);
          password.setText("A".repeat(128));
          assertEquals(128, password.getPassword().length);
          password.setCaretPosition(128);
          password.replaceSelection("B");
          assertEquals(128, password.getPassword().length);
        });
  }
}
