package ru.clonebox.common;

import java.util.logging.Logger;

public class Log {
    public static Logger loger;

    public Log(String str) {
        loger = Logger.getLogger(str);
    }
}
