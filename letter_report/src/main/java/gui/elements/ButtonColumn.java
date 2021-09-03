package gui.elements;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class ButtonColumn extends AbstractCellEditor implements TableCellRenderer, TableCellEditor
{
    private Font font = new Font("sans-serif", Font.BOLD, 16);

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null)
            return null;

        JButton button = (JButton) value;
        button.setFont(font);
        return button;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (value == null)
            return null;

        JButton button = (JButton) value;
        button.setFont(font);
        return (JButton) value;
    }

    @Override
    public Object getCellEditorValue()
    {
        return null;
    }

}