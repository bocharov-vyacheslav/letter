package data_helpers;

import data.models.Letter;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Namer {

    public static String getReceiptTypeName(byte receiptType) {
        switch (receiptType) {
            case 1:
                return "Спецоператор связи";
            case 2:
                return "Бумажный носитель";
            case 3:
                return "Электронная почта";
            default:
                return "";
        }
    }

    public static String getResponsiblePostPrefixName(int responsiblePostPrefix, String responsiblePost) {
        switch (responsiblePostPrefix) {
            case 1:
                return "Руководитель";
            case 2:
                return "Главный бухгалтер";
            case 3:
                return "Заместитель главного бухгалтера";
            case 4:
                return "Бухгалтер";
            case 5:
                return "Исполнитель";
            case 6:
                return "Конкурсный управляющий";
            case 7:
                return "Ликвидатор";
            case 8:
                return responsiblePost;
            default:
                return "";
        }
    }

    public static String getLetterTypeName(byte letterType) {
        switch (letterType) {
            case 1:
                return "Об отсутствии деятельности";
            case 2:
                return "Об отсутствии явления";
            case 3:
                return "Нулевой отчёт";
            case 4:
                return "В стадии ликвидации";
            case 5:
                return "Произведена ликвидация";
            default:
                return "";
        }
    }

    public static String getPeriodName(Letter letter) {
        
        String periodNumberName = "";
        String periodNumber = letter.getPeriodNumber();
        switch (periodNumber) {

            case "0401":
                periodNumberName = "1й квартал";
                break;
            case "0402":
                periodNumberName = "2й квартал";
                break;
            case "0403":
                periodNumberName = "3й квартал";
                break;
            case "0404":
                periodNumberName = "4й квартал";
                break;
          
            case "1201":
                periodNumberName = "Январь";
                break;
            case "1202":
                periodNumberName = "Февраль";
                break;
            case "1203":
                periodNumberName = "Март";
                break;
            case "1204":
                periodNumberName = "Апрель";
                break;
            case "1205":
                periodNumberName = "Май";
                break;
            case "1206":
                periodNumberName = "Июнь";
                break;
            case "1207":
                periodNumberName = "Июль";
                break;
            case "1208":
                periodNumberName = "Август";
                break;
            case "1209":
                periodNumberName = "Сентябрь";
                break;
            case "1210":
                periodNumberName = "Октябрь";
                break;
            case "1211":
                periodNumberName = "Ноябрь";
                break;
            case "1212":
                periodNumberName = "Декабрь";
                break;

            case "0201":
                periodNumberName = "1 полугодие";
                break;
            case "0202":
                periodNumberName = "2 полугодие";
                break;

            case "0000":
            case "0001":
            case "0101":
                return letter.getPeriodYear() + " г.";
        }
        
        return periodNumberName + " " + letter.getPeriodYear() + " г.";
    }

    public static String getTippredName(byte tippred) throws Exception {
        switch (tippred) {
            case 0:
                return "";
            case 1:
                return "Малое предприятие";
            case 2:
                return "Крупное предприятие";
            case 3:
                return "Среднее предприятие";
            case 4:
                return "Микропредприятие";
            case 8:
                return "Данные о типе предприятия отсутствуют";
            case 9:
                return "Некоммерческая организация";
        }

        throw new Exception("Тип предприятия не определён");
    }

    public static String getMonthName(Date sortDate) throws Exception {
        LocalDate localDate = sortDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        int month = localDate.getMonthValue();
        switch (month) {
            case 1:
                return "Январь";
            case 2:
                return "Февраль";
            case 3:
                return "Март";
            case 4:
                return "Апрель";
            case 5:
                return "Май";
            case 6:
                return "Июнь";
            case 7:
                return "Июль";
            case 8:
                return "Август";
            case 9:
                return "Сентябрь";
            case 10:
                return "Октябрь";
            case 11:
                return "Ноябрь";
            case 12:
                return "Декабрь";
        }

        throw new Exception("Номер месяца не определён");
    }

}
