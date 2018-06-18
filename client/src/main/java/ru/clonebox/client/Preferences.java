package ru.clonebox.client;


import com.google.gson.Gson;
import ru.clonebox.common.Constance;
import ru.clonebox.common.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ru.clonebox.common.Constance.FILE_CONFIG;

public final class Preferences {
    public String remoteIP = "localhost";
    public int remotePORT = 9090;
    public String pathSync = "y:/client/";
    //   transient public StringProperty loginSP = new SimpleStringProperty("");
//   transient public StringProperty passSP = new SimpleStringProperty("");
    public boolean autoSync = false;


    public void writeProperties() {
        Gson proper = new Gson();
        String properties = proper.toJson(this);

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(Constance.FILE_CONFIG))) {
            objectOutputStream.writeUTF(properties);
            objectOutputStream.flush();
        } catch (IOException e) {
            Util.log("Ошибка при записи файла конфигурации");
            throw new RuntimeException(e);
        }

    }

    static Preferences loadPreferences() {
        Preferences preferences = null;
        if (Files.exists(Paths.get(FILE_CONFIG))) {

            Gson properties = new Gson();
            try (ObjectInputStream inputConfig = new ObjectInputStream(new FileInputStream(FILE_CONFIG))) {

                String strProp = inputConfig.readUTF();
                preferences = properties.fromJson(strProp, Preferences.class);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            preferences = new Preferences();

        return preferences;
    }

    public String getRemoteIP() {
        return remoteIP;
    }

    public void setRemoteIP(String remoteIP) {
        this.remoteIP = remoteIP;
    }

    public int getRemotePORT() {
        return remotePORT;
    }

    public void setRemotePORT(int remotePORT) {
        this.remotePORT = remotePORT;
    }


    public boolean getAutoSync() {
        return autoSync;
    }

    public void setAutoSync(boolean autoSync) {
        this.autoSync = autoSync;
    }

}
