package ru.clonebox.client;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import ru.clonebox.common.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static ru.clonebox.common.Constance.CLIENT_FXML;
import static ru.clonebox.common.Constance.PREF_FXML;
import static ru.clonebox.common.Constance.REGISTRATION_FXML;

public class EnterController {

    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nameField;
    @FXML
    private Text actiontarget;
    ClientSocketChannel clientSocketChannel;

    @FXML
    private CheckBox saveCred;

    private StringProperty login;
    private StringProperty password;
    private String name = null;
    private String p1;
    private String p2;
    boolean isChangeCredits = false;

    @FXML
    private void initialize() {
        DataProvider.primaryStage.setOnCloseRequest(e -> {
            e.consume(); //todo обработать закрытие окна. Остановить потоки, завершенить подключения. Закрыть файлы.
            System.out.println("Отрабатывается setOnClose внутри EnterController");
            /*if (clientSocketChannel != null) {
                clientSocketChannel.queueMessage2Server.add(new CloseMessage("Клиент"));

                while (!clientSocketChannel.queueMessage2Server.isEmpty())
                    System.out.println("Оповещаем сервер о закрытии клиента");
//                       controller.clientSocketChannel.interrupt();
                while (clientSocketChannel.isAlive()) System.out.println("Закрываем подключение");
            }*/
            DataProvider.primaryStage.close(); //закрытие окна
        });



       login = new SimpleStringProperty();
       password = new SimpleStringProperty();

//        login.bindBidirectional(DataProvider.preferences.loginSP);
//        password.bindBidirectional(DataProvider.preferences.passSP);
//        nameField.setText(DataProvider.preferences.loginSP.getValue());
        nameField.setText(DataProvider.preferences.name);
        if (DataProvider.preferences.getSavepass()) {
            saveCred.setSelected(true);
            passwordField.setText(DataProvider.preferences.passHash);
        } else saveCred.setSelected(false);
    }

    @FXML
    protected void handleEnterButtonAction(ActionEvent event) {
        actiontarget.setText("Нажата кнопка войти");

        String errorMessage = "";


if(DataProvider.preferences.savePass ) { //если настройки сохранены
    if(saveCred.isSelected()){ //и галка стоит
    //todo откуда брать настойки?? они могут быть сохранены, но пользователь ввёл НОВЫЕ
        name = nameField.getText().trim();

        saveCred2PrefObj(); //todo нафига пересохранять настройки ? пользователь мог отредактировать их, но! в методе пересчитывается хеш и
        // это портит хеш для сообщения
        /**Этот блок посвящен тому что поля не измениились. Может пользователь отрадактировал их?
         * */
        name = nameField.getText().trim();
        p1 = passwordField.getText().trim();
            if (name.equals(DataProvider.preferences.name) && p1.equals(DataProvider.preferences.passHash)){
                System.out.println("ПОльзователь ничего не менял.");
            }
            else {
                DataProvider.preferences.name = name;
                DataProvider.preferences.passHash = p1;
            }

        /**
         * */

    } else { //настройки сохранены, а галки нет
        name = nameField.getText().trim();
        p1 = passwordField.getText().trim();
    }

} else { //настроек нет
    if(saveCred.isSelected()){ //а галка стоит (если настроек нет, а галка поставлена, значит отработал savePref)
//        saveCred2PrefObj();
        DataProvider.preferences.savePass=true;

    } else { // не стоит
        name = nameField.getText().trim();
        p1 = passwordField.getText().trim();
    }
}
        p2 = p1;
        errorMessage = Util.isInputValid(name, p1, p2);
        if (errorMessage.length() != 0) {
            Util.showAlert("Invalid Fields", "Please correct invalid fields", DataProvider.primaryStage);
            return;
        }
        String hashPass = Util.CreateHash(p1);

        //Если с логином и паролем ОК то сохранить
        if(saveCred.isSelected())   DataProvider.preferences.writeProperties();

        AuthAnswer authAnswer = null;
        AuthRequest authMessage = new AuthRequest(name, hashPass);

        Task<AuthAnswer> task = new Task<>() {
            @Override
            public AuthAnswer call() {
                return DataProvider.getMessageHandler().push(authMessage);
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join(Constance.WAIT_TIME);
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
//            nameField.setText(DataProvider.preferences.getLogin());
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
        loader.setLocation(Client.class.getResource(PREF_FXML)); //Указываем на файл с ресурсами
        try {
            GridPane propertiesWindow = (GridPane) loader.load();

            DataProvider.scenePropertiesWindow = new Scene(propertiesWindow);
            DataProvider.primaryStage.setScene(DataProvider.scenePropertiesWindow);
//        ip.setText(Client.preferences.getRemoteIP()); //todo Почему это не работает? Объект который выполняет ЭТОТ код не имеет поля инициализированного поля ip, его имеет тот объект что загружен в этом методе
            DataProvider.primaryStage.show();

        } catch (IOException e) {
            Util.log("Ошибка загрузки pref.fxml" + e.getMessage());
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


    /**
     * Проверяет пользовательский ввод в текстовых полях.
     *
     * @return true, если пользовательский ввод корректен
     */
   /* private boolean isInputValid(String name, String p1, String p2) {
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


        if (errorMessage.length() == 0) {
            return true;
        } else {
            // Показываем сообщение об ошибке.
            Util.showAlert("Invalid Fields", "Please correct invalid fields", DataProvider.primaryStage);


            return false;
        }
    }*/

    private void processAuthResult(AuthAnswer answer) {
        if (answer.isAuthOK()) {
            //  DataProvider.login = name;
            showMainWindow();
        } else
            Util.showAlert("Сервер отклонил запрос", " Регистрация провалена: Выберите другое имя.\n  Авторизация: проверьте введённые данные.", DataProvider.primaryStage);


    }


    public void handleSaveCred(ActionEvent actionEvent) {
        saveCred2PrefObj();
    }

    private void saveCred2PrefObj() {
        name = nameField.getText().trim();
        p1 = passwordField.getText().trim();
        String winHashPass = Util.CreateHash(p1);
        if (saveCred.isSelected()) { //если галка стоит
            if (name.equals(DataProvider.preferences.name) && winHashPass.equals(DataProvider.preferences.passHash)){
                 System.out.println("сохранения нет");
                 DataProvider.preferences.setSavepass(true);
            }
            else {
                DataProvider.preferences.name = name;
                DataProvider.preferences.passHash = winHashPass;
//                DataProvider.preferences.setSavepass(false); /Галку поставил, а в настройках она снимается.. нехорошо
            }
        }

        if (!saveCred.isSelected()) { //если галка не стоит - значит ничего сохранять не надо
            DataProvider.preferences.name = "";
            DataProvider.preferences.passHash = "";
            DataProvider.preferences.setSavepass(false);
        }
    }



  /* @FXML
     private void getRegistrationData() {

         String name = nameField.getText();
         String p1 = passwordField.getText();
         String p2 = passwordField2.getText();
         if (!isInputValid(name, p1, p2)) return;

         String hashPass = Util.CreateHash(p1);

         AuthRequest regMessage = new AuthRequest(name, hashPass, true);


 //        clientSocketChannel = ClientSocketChannel.getClientSocketChannel();
         //теперь нам требуется подключение к серверу
 //        clientSocketChannel.queueMessage2Server.add(regMessage); // отдаём сообщение в очередь
         //и ждём ответа...

         checkAuthResult();
     }*/
    /* @FXML
    private void hendleCancelPropertiesWindow(ActionEvent actionEvent) {
        client.getPrimaryStage().setScene(client.getSceneAuthWindow());
        client.getPrimaryStage().show();
    }*/

    /* @FXML
     private void saveAndApplySettings(ActionEvent actionEvent) { //todo все значения из формы надо проверять на корректность.
         DataProvider.preferences.setRemoteIP(ip.getText());
         DataProvider.preferences.setRemotePORT(Integer.parseInt(port.getText()));
         DataProvider.preferences.setLogin(login.getText());
         DataProvider.preferences.setPassHash(Util.CreateHash(password.getText()));
         DataProvider.preferences.setSavepass(savePass.isSelected());
         DataProvider.preferences.writeProperties();
         hendleCancelPropertiesWindow(actionEvent);
     }*/
}
