package data_helpers;

import data.models.Letter;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportCreator {

    public static File createLetterReport(List<Letter> letters) throws Exception {

        try (HSSFWorkbook book = new HSSFWorkbook()) {

            List<Integer> okpoHeaderIndexes = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19);
            String[] okpoHeaders = new String[]{"ОКПО", "Наименование предприятия", "Тип предприятия", "ОКАТО", "ОКТМО", "ОКВЭД",
                    "ОКУД", "Индекс", "Периодичность", "Отчётный год", "Отчётный период", "Регистрационный номер", "Дата поступления", "Способ поступления", "Тип информационного письма", "Руководитель", "Ответственный", "Должность ответственного", "Контактный телефон", "Электронный адрес"};

            HSSFSheet sheet = book.createSheet("Отчёт по информационным письмам");

            CellStyle headerCellStyle = getHeaderCellStyle(book, true, HSSFColor.HSSFColorPredefined.BLACK.getIndex());
            CellStyle fieldCellStyle = getFieldCellStyle(book, false, false);
            CellStyle dateCellStyle = getFieldCellStyle(book, true, false);

            int rownum = 0;
            HSSFRow row = sheet.createRow(rownum);

            HSSFCell cell;
            for (int i = 0; i < okpoHeaders.length; i++) {
                cell = row.createCell(i);
                cell.setCellStyle(headerCellStyle);
                cell.setCellValue(okpoHeaders[i]);
            }

            for (int okpoHeaderIndex : okpoHeaderIndexes)
                sheet.autoSizeColumn(okpoHeaderIndex);

            for (Letter letter : letters) {

                row = sheet.createRow(++rownum);

                cell = row.createCell(0);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getOkpo());

                cell = row.createCell(1);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getName());

                cell = row.createCell(2);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(Namer.getTippredName(letter.getTippred()));

                cell = row.createCell(3);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getOkato());

                cell = row.createCell(4);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getOktmo());

                cell = row.createCell(5);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getOkved());

                cell = row.createCell(6);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getOkud());

                cell = row.createCell(7);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getIndex());

                cell = row.createCell(8);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getPeriodicity());

                cell = row.createCell(9);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getPeriodYear() + " г.");

                cell = row.createCell(10);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(Namer.getPeriodName(letter));

                cell = row.createCell(11);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getRegNo());

                cell = row.createCell(12);
                cell.setCellStyle(dateCellStyle);
                cell.setCellValue(letter.getReceiptDate());

                cell = row.createCell(13);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(Namer.getReceiptTypeName(letter.getReceiptType()));

                cell = row.createCell(14);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(Namer.getLetterTypeName(letter.getLetterType()));

                cell = row.createCell(15);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getLeaderFio());

                cell = row.createCell(16);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getResponsibleFio());

                cell = row.createCell(17);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getResponsiblePost());

                cell = row.createCell(18);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getPhone());

                cell = row.createCell(19);
                cell.setCellStyle(fieldCellStyle);
                cell.setCellValue(letter.getEmail());
            }

            Date creatingDate = new Date();
            String creatingYear = Converter.toYear(creatingDate);
            String creatingMonth = Namer.getMonthName(creatingDate);

            String reportName = "Отчёт_по_информационным_письмам";
            String reportsFileDirName = "Отчёты по информационным письмам";
            DateFormat dirDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String creatingDateDirName = creatingYear + "\\" + creatingMonth + "\\" + dirDateFormat.format(creatingDate);
            File creatingDir = new File(reportsFileDirName + "\\" + creatingDateDirName);
            creatingDir.mkdirs();

            File letterReport = Paths.get(creatingDir.getAbsolutePath(), reportName + new SimpleDateFormat("_yyyy.MM.dd_HH.mm.ss").format(creatingDate) + ".xls").toFile();
            try (FileOutputStream fos = new FileOutputStream(letterReport, false)) {
                book.write(fos);
            }

            return letterReport;
        }
    }

    private static CellStyle getHeaderCellStyle(Workbook book, boolean isForeground, short color) {
        Font font = book.createFont();
        font.setBold(true);
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 16);
        font.setColor(color);

        CellStyle headerCellStyle = book.createCellStyle();
        headerCellStyle.setBorderTop(BorderStyle.THIN);
        headerCellStyle.setBorderRight(BorderStyle.THIN);
        headerCellStyle.setBorderBottom(BorderStyle.THIN);
        headerCellStyle.setBorderLeft(BorderStyle.THIN);
        headerCellStyle.setFont(font);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        if (isForeground) {
            headerCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }

        return headerCellStyle;
    }

    private static CellStyle getFieldCellStyle(Workbook book, boolean isDate, boolean isBold) {
        Font font = book.createFont();
        font.setFontName("Arial");
        font.setBold(isBold);
        font.setFontHeightInPoints((short) 14);

        CellStyle fieldCellStyle = book.createCellStyle();
        fieldCellStyle.setBorderTop(BorderStyle.THIN);
        fieldCellStyle.setBorderRight(BorderStyle.THIN);
        fieldCellStyle.setBorderBottom(BorderStyle.THIN);
        fieldCellStyle.setBorderLeft(BorderStyle.THIN);
        fieldCellStyle.setFont(font);
        fieldCellStyle.setAlignment(HorizontalAlignment.LEFT);

        if (isDate) {
            DataFormat format = book.createDataFormat();
            fieldCellStyle.setDataFormat(format.getFormat("yyyy-MM-dd HH:mm:ss"));
        }

        return fieldCellStyle;
    }
}
