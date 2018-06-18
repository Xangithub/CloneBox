package ru.clonebox.client;

import java.net.URL;

public class RLoader {

    public Class rootClassRet() {

        return this.getClass();
    }

    public static URL getResource(String resource) {

        URL url;

        //Try with the Thread Context Loader.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        //Let now try with the classloader that loaded this class.
        classLoader = RLoader.class.getClassLoader();
        if (classLoader != null) {
            url = classLoader.getResource(resource);
            if (url != null) {
                return url;
            }
        }

        //Last ditch attempt. Get the resource from the classpath.
        return ClassLoader.getSystemResource(resource);
    }
}
