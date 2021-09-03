package gui.table_models;

import data.models.Email;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class EmailTableModel extends AbstractTableModel implements IUserTableModel<Email> {

    public EmailTableModel(List<Email> emailRecords) {
        this.emailRecords = emailRecords;
    }

    private List<Email> emailRecords;

    private final String[] COLUMN_NAMES = new String[]{
            "П/п", "Электронная почта"
    };

    @Override
    public int getRowCount() {
        return emailRecords.size();
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
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Email email = emailRecords.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowIndex + 1;
            case 1:
                return email.getEmail();
            default:
                return "";
        }
    }

    public Email getObject(int rowIndex) {
        return emailRecords.get(rowIndex);
    }
}


