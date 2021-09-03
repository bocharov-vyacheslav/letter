package data_helpers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Converter {

    public static String toYear(Date sortDate) {
        LocalDate localDate = sortDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return String.valueOf(localDate.getYear());
    }

    public static Date toDate(LocalDate localDate) {
        return (localDate == null) ? null : Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
