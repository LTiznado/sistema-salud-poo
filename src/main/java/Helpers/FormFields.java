package Helpers;

import javax.swing.text.*;

/** Editing and paste use the same length limits; business validation stays in services. */
public final class FormFields {
  private FormFields() {}

  public static void limit(JTextComponent field, int maximum) {
    for (java.awt.event.KeyListener listener : field.getKeyListeners())
      field.removeKeyListener(listener);
    ((AbstractDocument) field.getDocument())
        .setDocumentFilter(
            new DocumentFilter() {
              @Override
              public void insertString(
                  FilterBypass bypass, int offset, String text, AttributeSet attributes)
                  throws BadLocationException {
                replace(bypass, offset, 0, text, attributes);
              }

              @Override
              public void replace(
                  FilterBypass bypass, int offset, int length, String text, AttributeSet attributes)
                  throws BadLocationException {
                String replacement = text == null ? "" : text;
                if (bypass.getDocument().getLength() - length + replacement.length() <= maximum)
                  bypass.replace(offset, length, replacement, attributes);
              }
            });
  }
}
