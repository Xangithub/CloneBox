package ru.clonebox.client.enter;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ru.clonebox.client.Client;
import ru.clonebox.client.Credentials;
import ru.clonebox.client.Preferences;
import ru.clonebox.client.net.ClientSocketChannel;
import ru.clonebox.client.DataProvider;
import ru.clonebox.common.*;
import ru.clonebox.messages.AuthAnswer;
import ru.clonebox.messages.AuthRequest;
import ru.clonebox.messages.CloseMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import static ru.clonebox.common.Constance.*;

public class EnterController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nameField;
    @FXML
    private Text actiontarget;
    ClientSocketChannel clientSocketChannel;
    @FXML
    private Button enter;

    @FXML
    private CheckBox saveCred;

    private StringProperty login;
    private StringProperty password;
    private String name = null;
    private String p1;
    private String p2;
    Credentials credentials = DataProvider.getDataProvider().getCredentials();

    @FXML
    private void initialize() {
        DataProvider.primaryStage.setOnCloseRequest(e -> {
            e.consume();
            System.out.println("Отрабатывается setOnClose внутри EnterController");
            //todo думал просто тут окно закрыть, но нет! а если клиент уже имеет подключение придётся проверять и закрывать
            if(clientSocketChannel.socketChannel!=null){
                Task<AuthAnswer> task = new Task<>() {
                    @Override
                    public AuthAnswer call() {
                        return DataProvider.getMessageHandler().push( new CloseMessage());
                    }
                };
                Thread thread = new Thread(task);
                thread.start();
                try {
                    thread.join(500);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                clientSocketChannel.interrupt();
                clientSocketChannel.closeConnection();

            }
            DataProvider.primaryStage.close(); //закрытие окна
        });


        login = new SimpleStringProperty();
        password = new SimpleStringProperty();

        if (credentials.savePass) {
            nameField.setText(credentials.name);
            saveCred.setSelected(true);
            passwordField.setText(credentials.passHash);
        } else saveCred.setSelected(false);
    }

    @FXML
    protected void handleEnterButtonAction(ActionEvent event) {
        actiontarget.setText("Нажата кнопка войти");

        String errorMessage = "";

        enter.setDisable(true);

        name = nameField.getText().trim();
        p1 = passwordField.getText().trim();
        p2 = p1;
        if (credentials.savePass) { //если логин и пароль загружены с файла

            if (saveCred.isSelected()) { //и галка стоит
                /**Сверяем данные в полях и в загруженных настройках
                 * ведь пользователь мог поля поменять
                 * если изменений нет то ничего не делаем, всё уже готово к подготовке запроса
                 * */
                if (name.equals(credentials.name) && p1.equals(credentials.passHash)) {
                    System.out.println("ПОльзователь ничего не менял.");

                } else {
                    /** Если поля изменились
                     * присвоить объекту новые значения
                     * если изменений нет то ничего не делаем, всё уже готово к подготовке запроса
                     * */
                    /**ПРоверяем корректность полей коль скоро они изменились*/
                    errorMessage = Util.isInputValid(name, p1, p2);
                    if (errorMessage.length() != 0) {
                        Util.showAlert("Invalid Fields", "Please correct invalid fields", DataProvider.primaryStage);
                        enter.setDisable(false);
                        return;
                    }

                    credentials.name = name;
                    credentials.passHash = p1;
//                    credentials.writeCredentials();

                }


            } else { /** галку убрали значит файл кредитов*/
                try {
                    Files.delete(Paths.get(FILE_CRED));
                } catch (IOException e) {
                    Util.showAlert("Invalid Fields", "Please correct invalid fields", DataProvider.primaryStage);
                    enter.setDisable(false);
                    return;
                }
            }

        } else {  //если логин и пароль отсутствуют в файле
/**проверяем на корректность
 * передать поля в конфигурацию (Credentials)
 * сохраняем настройки в файл после положительного ответа сервера
 *
 * */
/** галки нет - проверить поля,
 передать поля в конфигурацию .
 сформировать просто запрос сервер.*/

            errorMessage = Util.isInputValid(name, p1, p2);
            if (errorMessage.length() != 0) {
                Util.showAlert("Invalid Fields", "Please correct invalid fields", DataProvider.primaryStage);
                enter.setDisable(false);
                return;
            }
            credentials.name = name;
            credentials.passHash =  Util.CreateHash(p1);
        }


        AuthAnswer authAnswer = null;

        AuthRequest authMessage = new AuthRequest(credentials.getName(), credentials.getPassHash());

        Task<AuthAnswer> task = new Task<>() {
            @Override
            public AuthAnswer call() {
                return DataProvider.getMessageHandler().push(authMessage);
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            authAnswer = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        assert authAnswer != null;
        if (authAnswer != null) processAuthResult(authAnswer);
        else Util.log("authAnswer = null");
        enter.setDisable(false);
    }


    @FXML
    private void handleRegistrationButtonAction(ActionEvent event) {
        actiontarget.setText("Нажата кнопка Регистрация");
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader(); //создаем объект для загрузки интерфейса
            loader.setLocation(Client.class.getResource(REGISTRATION_FXML)); //Указываем на файл с ресурсами
            GridPane registrationWindow = (GridPane) loader.load(); // Загружаем ресурс на панель

            DataProvider.sceneRegWindow = new Scene(registrationWindow);
            DataProvider.primaryStage.setScene(DataProvider.sceneRegWindow);
            DataProvider.primaryStage.show();


        } catch (IOException e) {
            System.out.println("ПРоблема с загрузкой fxml Registration " + e.getMessage());
            e.printStackTrace();
        }

    }


    @FXML
    private void handlePropertiesAction(ActionEvent event) {
        FXMLLoader loader = new FXMLLoader(); //создаем объект для загрузки интерфейса
        loader.setLocation(Preferences.class.getResource(PREF_FXML)); //Указываем на файл с ресурсами
        try {
            GridPane propertiesWindow = (GridPane) loader.load();

            DataProvider.scenePropertiesWindow = new Scene(propertiesWindow);
            DataProvider.primaryStage.setScene(DataProvider.scenePropertiesWindow);
//        ip.setText(Client.preferences.getRemoteIP()); //todo Почему это не работает? Объект который выполняет ЭТОТ код не имеет поля инициализированного поля ip, его имеет тот объект что загружен в этом методе
            DataProvider.primaryStage.show();

        } catch (IOException e) {
            Util.log("Ошибка загрузки preferences.fxml" + e.getMessage());
            e.printStackTrace();
        }

    }

    private void showMainWindow() {
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader(); //создаем объект для загрузки интерфейса
            loader.setLocation(Client.class.getResource(CLIENT_FXML)); //Указываем на файл с ресурсами
            VBox mainWindow = (VBox) loader.load(); // Загружаем ресурс на панель
            DataProvider.sceneMainWindow = new Scene(mainWindow);

            DataProvider.primaryStage.setScene(DataProvider.sceneMainWindow);
            DataProvider.primaryStage.show();

        } catch (IOException e) {
            System.out.println("ПРоблема с загрузкой fxml mainWindow");
        }
    }


    private void processAuthResult(AuthAnswer answer) {
        if (answer.isAuthOK()) {
            //Если с логином и паролем ОК то сохранить
            if (saveCred.isSelected()) {
                credentials.setSavepass(true);
                credentials.writeCredentials();
            }
            DataProvider.login = name;
            showMainWindow();
        } else
            Util.showAlert("Сервер отклонил запрос", " Регистрация провалена: Выберите другое имя.\n  Авторизация: проверьте введённые данные.", DataProvider.primaryStage);


    }


}
