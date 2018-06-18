package ru.clonebox.client;


import com.google.gson.Gson;
import ru.clonebox.common.Constance;
import ru.clonebox.common.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static ru.clonebox.common.Constance.FILE_CRED;

public final class Credentials {
    //   transient public StringProperty loginSP = new SimpleStringProperty("");
//   transient public StringProperty passSP = new SimpleStringProperty("");
    public String name = "";
    public String passHash = "";
    public boolean savePass = false;


    public void writeCredentials() {
        Gson proper = new Gson();
        String properties = proper.toJson(this);

        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(Constance.FILE_CRED))) {
            objectOutputStream.writeUTF(properties);
            objectOutputStream.flush();
        } catch (IOException e) {
            Util.log("Ошибка при записи файла безопасности");
            throw new RuntimeException(e);
        }

    }

    static Credentials loadCredentials() {
        Credentials credentials = null;

        if (Files.exists(Paths.get(Constance.FILE_CRED))) {
            Gson properties = new Gson();
            try (ObjectInputStream inputConfig = new ObjectInputStream(new FileInputStream(FILE_CRED))) {
                String strProp = inputConfig.readUTF();
                credentials = properties.fromJson(strProp, Credentials.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else
            credentials = new Credentials();

        return credentials;
    }

    public String getPassHash() {
        return passHash;
    }

    public void setPassHash(String passHash) {
        this.passHash = passHash;
    }

    public boolean getSavepass() {
        return savePass;
    }

    public void setSavepass(boolean savePass) {
        this.savePass = savePass;
    }

    public String getName() {
        return name;
    }
}
