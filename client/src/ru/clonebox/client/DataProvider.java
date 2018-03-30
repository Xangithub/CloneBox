package ru.clonebox.client;

import com.google.gson.Gson;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.clonebox.client.net.ClientSocketChannel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Этот класс хранилище ссылок на объекты клиента. Его задача хранить и выдавать ссылки по запросу компонентов.
 *
 */

import static ru.clonebox.common.Constance.FILE_CONFIG;

public final class DataProvider {
    public static DataProvider dataProvider = DataProvider.getDataProvider();
    private static MessageHandler messageHandler;

    public static Preferences preferences;
    public static Stage primaryStage;
    public static Scene sceneAuthWindow;
    public static Scene sceneRegWindow;
    public static Scene scenePropertiesWindow;
    public static Scene sceneMainWindow;
    public static Client client;
    private SocketChannel socketChannel;
    public ClientSocketChannel clientSocketChannel;
    public static String login = null;
    //путь к локальной папке синхронизации
    public static String localFolder;
    private Credentials credentials;

    public static DataProvider getDataProvider() {

        DataProvider localInstance = dataProvider;
        if (localInstance == null) {
            synchronized (ClientSocketChannel.class) {
                localInstance = dataProvider;
                if (localInstance == null) dataProvider = localInstance = new DataProvider();

            }

        }
        return localInstance;
    }


    private DataProvider() {
        dataProvider = this;
        //загрузка параметров
        preferences = Preferences.loadPreferences();
        credentials = Credentials.loadCredentials();
        messageHandler = MessageHandler.getInstance();
    }

    /**
     * Загрузка настроек для клиента
     */
  /*  private void loadPreferences() {
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
        }*/

    public static MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public static void setMessageHandler(MessageHandler messageHandler) {
        DataProvider.messageHandler = messageHandler;
    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setSocketChannel(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
