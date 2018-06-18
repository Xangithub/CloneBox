package ru.clonebox.server;

import javafx.scene.image.Image;

import java.io.InputStream;
import java.net.URL;

public class RLoader {
    public static InputStream getResourceAsStream(String s) {
        System.out.println(RLoader.class.getName());
        return RLoader.class.getResourceAsStream(s);
    }

    public static URL getResource(String resource) {

        URL url;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        classLoader = RLoader.class.getClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        return ClassLoader.getSystemResource(resource);
    }
}
