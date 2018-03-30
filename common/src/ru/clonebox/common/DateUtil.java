package ru.clonebox.common;

import java.nio.file.attribute.FileTime;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
        //Шаблон даты
    public static final String DATE_PATTERN = "dd.MM.yyyy";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String LOCAL_PATTERN = "HH:mm:ss dd.MM.yyyy";
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);
//    public static final DateTimeFormatter LOCAL_FORMATTER = DateTimeFormatter.ofLocalizedDateTime(LOCAL_PATTERN);
    static final DateFormat df = new SimpleDateFormat(LOCAL_PATTERN);
    public static String format(LocalDate date){
        if (date == null) {
            return null;
        }
        return DATE_FORMATTER.format(date);
    }

    public static String format(FileTime timedate){
        if (timedate == null) {
            return null;
        }

        return df.format(timedate.toMillis());
    }



    /*
     * Преобразуем строку в дату
     * */

    public static LocalDate parse(String dateString){

        try {
            return DATE_FORMATTER.parse(dateString, LocalDate::from);
        } catch (Exception e) {
            return  null;
        }
    }

    /*
     * ПРоверяет является ли строка корректной датой
     * */

    public static boolean validatDate(String dateString){
        //пробуем разобрать строку
        return DateUtil.parse(dateString)!= null;

    }
}
