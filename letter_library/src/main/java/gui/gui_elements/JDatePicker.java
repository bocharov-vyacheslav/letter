package gui.gui_elements;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;

public class JDatePicker {

    public static DatePicker getDatePicker(Font font) {

        Locale locale = new Locale("ru");

        DatePickerSettings datePickerSettings = new DatePickerSettings(locale);
        datePickerSettings.setSizeTextFieldMinimumWidth(100);
        datePickerSettings.setSizeTextFieldMinimumWidthDefaultOverride(true);
        datePickerSettings.setFontCalendarDateLabels(font);
        datePickerSettings.setFontCalendarWeekdayLabels(font);
        datePickerSettings.setFontClearLabel(font);
        datePickerSettings.setFontMonthAndYearMenuLabels(font);
        datePickerSettings.setFontTodayLabel(font);
        datePickerSettings.setFontCalendarWeekNumberLabels(font);
        datePickerSettings.setFontInvalidDate(font);
        datePickerSettings.setFontValidDate(font);
        datePickerSettings.setFontVetoedDate(font);
        datePickerSettings.setFontMonthAndYearNavigationButtons(font);

        DatePicker datePicker = new DatePicker(datePickerSettings);
        datePicker.setDateToToday();

        return datePicker;
    }

    public static Date toDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

}
