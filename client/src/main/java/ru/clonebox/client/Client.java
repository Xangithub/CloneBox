package ru.clonebox.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


import java.io.IOException;


//GUI клиент
public class Client extends Application {
    private Stage primaryStage;
    private Scene sceneAuthWindow;
    DataProvider dataProvider = DataProvider.getDataProvider();

    /*public Client(String[] args) {
        System.out.println(" в конструкторе");


    }*/
//    Log  log = new Log(Client.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        DataProvider.primaryStage = primaryStage;

        primaryStage.setTitle("Клиент CloneBox");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("clonebox.png")));
//        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//        primaryStage.getIcons().add(new Image(String.valueOf(classLoader.getResource("ru/clonebox/client/image/clonebox.png"))));

        initRootLayout();

    }

    private void putLoginAndPassword() {
        if (DataProvider.preferences.getAutoSync()) {

        }
    }


    private void initRootLayout() {

        try {
            FXMLLoader loader = new FXMLLoader(); //создаем объект для загрузки интерфейса
            loader.setLocation(getClass().getResource("enter.fxml")); //Указываем на файл с ресурсами
            Pane rootLayout = loader.load(); //
            sceneAuthWindow = new Scene(rootLayout);

        } catch (IOException e) {
            System.out.println("ПРоблема с загрузкой fxml passWin");
        }

        DataProvider.sceneAuthWindow = sceneAuthWindow;
        primaryStage.setScene(sceneAuthWindow);
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);

    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public Scene getSceneAuthWindow() {
        return sceneAuthWindow;
    }

    public Scene getSceneRegWindow() {
        return sceneAuthWindow;
    }

}
