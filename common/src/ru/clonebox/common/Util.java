package ru.clonebox.common;

import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;

public class Util {

    public static String isInputValid(String name, String p1, String p2) {
        String errorMessage = "";

        if (name == null || name.length() == 0) {
            errorMessage += "Некорректное имя пользователя!\n";
        }
        if (p1 == null || p1.length() == 0) {
            errorMessage += "Некорректный пароль!\n";
        }
        if (p2 == null || p2.length() == 0) {
            errorMessage += "Некорректный пароль!\n";
        }

        if (!p1.equals(p2)) {
            errorMessage += "Введены разные пароли. Повторите..!\n";
        }

        return errorMessage;

    }

    public static String CreateHash(String password) //возвращает хеш поступившей строки
    {
        MessageDigest md = null;
        StringBuilder hash = null;
        try {
            md = MessageDigest.getInstance("SHA");
            md.reset();
            md.update(password.getBytes());
            byte[] digest = md.digest();

            hash = new StringBuilder();

            for (int i = 0; i < digest.length; i++) {
                String hexVal = Integer.toHexString(0xFF & digest[i]);
                if (hexVal.length() == 1) {
                    hash.append("0");
                }
                hash.append(hexVal);
            }

        } catch (NoSuchAlgorithmException e) {
            System.out.println("Нет такого алгоритма");
        }
        assert hash != null;
        return hash.toString();
    }

    public static void showAlert(String title, String message, Stage stage){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(stage);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

     public static void log(String message){
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        System.out.println("["+ Thread.currentThread().getName() + "] " + "method " + stackTraceElements[2] + " " + message);
    }
    public static String sizeFormatValue(Long value) {
        DecimalFormat formatter = null;
        final long KiB = 1024;
        final long MiB = KiB * 1024;
        final long GiB = MiB * 1024;
        final long TiB = GiB * 1024;
        final long PiB = TiB * 1024;
        final long EiB = PiB * 1024;
        final long ZiB = EiB * 1024;
        final long YiB = ZiB * 1024;
        if (value != null) {
            Double d =  value.doubleValue();
            if (formatter == null)
                formatter = new DecimalFormat("###,###.#");
            if (d < KiB)
                return(formatter.format(d) + " Б");
            else if (d < MiB)
                return(formatter.format(d / KiB) + " Кб");
            else if (d < GiB)
                return(formatter.format(d / MiB) + " Мб");
            else if (d < TiB)
                return(formatter.format(d / GiB) + " Гб");
            else if (d < PiB)
                return(formatter.format(d / TiB) + " Тб");
            else if (d < EiB)
                return(formatter.format(d / PiB) + " Пб");
            else if (d < ZiB)
                return(formatter.format(d / EiB) + " EiB");
            else if (d < YiB)
                return(formatter.format(d / ZiB) + " ZiB");
            else
                return(formatter.format(d / YiB) + " YiB");
        } else
            return "";
    }
}
