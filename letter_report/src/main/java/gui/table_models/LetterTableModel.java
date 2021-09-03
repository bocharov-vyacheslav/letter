package gui.table_models;

import data_helpers.Namer;
import data.models.LetterPagination;
import data_helpers.AppLogger;
import data_helpers.Opener;
import gui.swing_workers.FileWorker;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.util.List;

public class LetterTableModel extends AbstractTableModel {

    public LetterTableModel(JFrame frame, List<LetterPagination> letterRecords) {
        this.frame = frame;
        this.letterRecords = letterRecords;
    }

    private JFrame frame;
    private List<LetterPagination> letterRecords;

    private final String[] COLUMN_NAMES = new String[]{
            "П/п", "Регистрационный номер", "Дата поступления", "Тип информационного письма", "ОКПО", "ОКУД", "Отчётный период", ""
    };

    @Override
    public int getRowCount() {
        return letterRecords.size();
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
        if (columnIndex == 7) {
            return JButton.class;
        }

        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        return (columnIndex == 7);
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LetterPagination letter = letterRecords.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return rowIndex + 1;
            case 1:
                return letter.getRegNo();
            case 2:
                return letter.getReceiptDate().toString();
            case 3:
                return Namer.getLetterTypeName(letter.getLetterType());
            case 4:
                return letter.getOkpo();
            case 5:
                return letter.getOkud();
            case 6:
                return Namer.getPeriodName(letter);
            case 7:
                JButton button = new JButton("Файл");
                button.addActionListener(e -> {
                    try {

                        FileWorker fileWorker = new FileWorker(frame, letter);
                        fileWorker.getLetterFile();

                        File letterFile = fileWorker.get();
                        Opener.openFile(letterFile);

                    } catch (Exception ex) {
                        AppLogger.fatal(frame, ex);
                    }
                });
                return button;
            default:
                return "";
        }
    }
}

