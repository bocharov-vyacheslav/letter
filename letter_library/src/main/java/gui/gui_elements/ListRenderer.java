package gui.gui_elements;

import data.models.IName;

import javax.swing.*;
import java.awt.*;

public class ListRenderer {

    public static ListCellRenderer<? super IName> createListRenderer(Font font) {
        return new DefaultListCellRenderer() {
            private Color background = new Color(0, 100, 255, 15);
            private Color defaultBackground = (Color) UIManager.get("List.background");

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (component instanceof JLabel) {
                    JLabel label = (JLabel) component;
                    label.setFont(font);
                    IName name = (IName) value;
                    label.setText(name.getName());
                    if (!isSelected) {
                        label.setBackground(index % 2 == 0 ? background : defaultBackground);
                    }
                }
                return component;
            }
        };
    }
}
