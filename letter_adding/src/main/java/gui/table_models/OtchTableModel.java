package gui.table_models;

import data.models.Otch;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class OtchTableModel extends AbstractTableModel implements IUserTableModel<Otch> {

    public OtchTableModel(List<Otch> otchRecords) {
        this.otchRecords = otchRecords;
    }

    private List<Otch> otchRecords;

    private final String[] COLUMN_NAMES = new String[]{
            "П/п", "ОКПО", "ОКУД", "Отчётный год", "Периодичность", "Отчётный период"
    };

    @Override
    public int getRowCount() {
        return otchRecords.size();
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
        Otch otch = otchRecords.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowIndex + 1;
            case 1:
                return otch.getOkpo();
            case 2:
                return otch.getOkud();
            case 3:
                return otch.getPeriodYear();
            case 4:
                return otch.getPeriodicity();
            case 5:
                return otch.getPeriod();
            default:
                return "";
        }
    }

    public Otch getObject(int rowIndex) {
        return otchRecords.get(rowIndex);
    }
}

