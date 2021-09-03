package gui.gui_elements;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class UserJPanel extends JPanel {

    UserJPanel(BorderLayout borderLayout) {
        super(borderLayout);
    }

    private JTextField textField;

    public JTextField getTextField() {
        return textField;
    }

    void setTextField(JTextField textField) {
        this.textField = textField;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserJPanel that = (UserJPanel) o;
        return Objects.equals(textField, that.textField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textField);
    }
}
