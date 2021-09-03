package gui.table_models;

import data.models.Phone;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PhoneTableModel extends AbstractTableModel implements IUserTableModel<Phone> {

    public PhoneTableModel(List<Phone> phoneRecords) {
        this.phoneRecords = phoneRecords;
    }

    private List<Phone> phoneRecords;

    private final String[] COLUMN_NAMES = new String[]{
            "П/п", "Основной телефон", "Факс", "Внутренний телефон"
    };

    @Override
    public int getRowCount() {
        return phoneRecords.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 2) {
            return JLabel.class;
        }

        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Phone phone = phoneRecords.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowIndex + 1;
            case 1:
                return phone.getOsnPhone();
            case 2:
                JLabel label = new JLabel();
                label.setHorizontalTextPosition(SwingConstants.CENTER);
                label.setText("");
                label.setHorizontalAlignment(SwingConstants.CENTER);
                if (phone.isFax()) {
                    label.setIcon(new ImageIcon(getClass().getResource("/images/yes.png")));
                } else {
                    label.setIcon(new ImageIcon(getClass().getResource("/images/no.png")));
                }
                return label;
            case 3:
                return phone.getDopPhone();
            default:
                return "";
        }
    }

    public Phone getObject(int rowIndex) {
        return phoneRecords.get(rowIndex);
    }
}


