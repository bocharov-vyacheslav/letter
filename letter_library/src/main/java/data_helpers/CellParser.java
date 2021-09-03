package data_helpers;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

public class CellParser {
    public static String getStringCellValue(Cell cell) throws Exception {
        CellType cellType = cell.getCellType();
        switch (cellType){
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((long)cell.getNumericCellValue());
            case BLANK:
                return "";
        }

        throw new Exception("Невозможно получить строковое значение: тип ячейки - " + cellType.toString() +
                ", строка ячейки - " + (cell.getRowIndex() + 1) + ", столбец ячейки - "  + (cell.getColumnIndex() + 1));
    }

    public static long getLongCellValue(Cell cell) throws Exception {
        CellType cellType = cell.getCellType();
        switch (cellType){
            case STRING:
                return Long.parseLong(cell.getStringCellValue().trim());
            case NUMERIC:
                return (long)cell.getNumericCellValue();
            case BLANK:
                return 0L;
        }

        throw new Exception("Невозможно получить целочисленное значение: тип ячейки - " + cellType.toString() +
                ", строка ячейки - " + (cell.getRowIndex() + 1) + ", столбец ячейки - "  + (cell.getColumnIndex() + 1));
    }
}
