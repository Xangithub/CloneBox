package ru.clonebox.client.reg;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import ru.clonebox.client.Client;
import ru.clonebox.client.DataProvider;
import ru.clonebox.common.*;
import ru.clonebox.messages.AuthAnswer;
import ru.clonebox.messages.AuthRequest;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class RegistrationController {
    public static final String CLIENT_FXML = "client/client.fxml";
    @FXML
    private Button sendReg;
    @FXML
    private TextField nameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField passwordField2;

    private boolean authPerm = false;
    String name;

    @FXML
    private void getRegistrationData() {
        sendReg.setDisable(true);
        name = nameField.getText();
        String p1 = passwordField.getText();
        String p2 = passwordField2.getText();
        String errorMessage = Util.isInputValid(name, p1, p2);

        if (errorMessage.length() != 0) {
            Util.showAlert("Invalid Fields", "Please correct invalid fields", DataProvider.primaryStage);

            sendReg.setDisable(false);

        } else {
            String hashPass = Util.CreateHash(p1);
            AuthRequest regMessage = new AuthRequest(name, hashPass, true);

            AuthAnswer authAnswer = null;
         /*   Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                   authAnswer= DataProvider.getMessageHandler().push(regMessage);
                }
            });

            thread.start();

            try {
                thread.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
          /*  Service<AuthAnswer> service = new Service<AuthAnswer>() {
                AuthAnswer authAnswer = null;
                @Override
                protected Task<AuthAnswer> createTask() {
                    return null;
                }
            };*/

            Task<AuthAnswer> task = new Task<>() {
                @Override
                public AuthAnswer call() {
                    return DataProvider.getMessageHandler().push(regMessage);
                }
            };
            Thread thread = new Thread(task);
            thread.start();
            try {
                thread.join(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            authAnswer=task.getValue();

            try {
                authAnswer = task.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            assert authAnswer != null;
            if(authAnswer!=null) processAuthResult(authAnswer); else Util.log("authAnswer = null");

        }



      /*  clientSocketChannel = ClientSocketChannel.getClientSocketChannel();
        //теперь нам требуется подключение к серверу
        clientSocketChannel.queueMessage2Server.add(regMessage); // отдаём сообщение в очередь
        //и ждём ответа...
        while (clientSocketChannel.queueMessageFromServer.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.print("Ждём ответа от сервера/r");
        }
        checkAuthResult();*/

//      showMainWindow();
    }

    private void processAuthResult(AuthAnswer answer) {
        if (answer.isAuthOK()) {
            DataProvider.login = name;
            showMainWindow();
        } else
            Util.showAlert("Сервер отклонил запрос", " Регистрация провалена: Выберите другое имя.\n  Авторизация: проверьте введённые данные.", DataProvider.primaryStage);


    }

    private void showMainWindow() {
        Util.log("Регистраия пройдена");
        try {
            // Загружаем fxml-файл и создаём новую сцену
            // для всплывающего диалогового окна.
            FXMLLoader loader = new FXMLLoader(); //создаем объект для загрузки интерфейса //todo 4 окна один код. Упростить - метод загрузки или класс?? Фабрика?
            loader.setLocation(Client.class.getResource(CLIENT_FXML)); //Указываем на файл с ресурсами
            Pane mainWindow = loader.load(); // Загружаем ресурс на панель
            DataProvider.sceneMainWindow = new Scene(mainWindow);

            DataProvider.primaryStage.setScene(DataProvider.sceneMainWindow);
//            ((WindowClient) loader.getController()).setLogin(DataProvider.preferences.getLogin()); //todo передать логин пользователя

//            WindowClient controller = loader.getController();

//           clientSocketChannel.setWindowClient(controller);
//            client.getPrimaryStage().setOnCloseRequest(e -> {
//                e.consume();
//                if (controller.clientSocketChannel != null) {
//                    controller.clientSocketChannel.queueMessage2Server.add(new CloseMessage());
//
//                    while (!controller.clientSocketChannel.queueMessage2Server.isEmpty())
//                        System.out.print("Оповещаем сервер о закрытии клиента/r");
////                    controller.clientSocketChannel.interrupt();
//                    while (controller.clientSocketChannel.isAlive()) System.out.print("Закрываем подключение/r");
//                }
//                client.getPrimaryStage().close(); //закрытие окна
//            });
            DataProvider.primaryStage.show();


        } catch (IOException e) {
            System.out.println("ПРоблема с загрузкой fxml " + CLIENT_FXML);
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }


    public void handleRegistrationCancel(ActionEvent actionEvent) {
        DataProvider.primaryStage.setScene(DataProvider.sceneAuthWindow);
        DataProvider.primaryStage.show();
    }

    public void setAuthPerm(boolean authPerm) {
        this.authPerm = authPerm;
    }


}
