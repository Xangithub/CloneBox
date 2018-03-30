package ru.clonebox.server.GUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.clonebox.server.Server.ClientAcceptor;
import ru.clonebox.server.Stoppable;

import java.nio.file.Path;
import java.util.LinkedList;
import java.util.logging.Logger;

public class BoxServGUI extends Application {

    public TextField serverFolderStorage;
    public TextArea logArea;
    @FXML private Button startBtn;
    @FXML private Button stopBtn;
    Thread threadBoxServ;
    public static BoxServGUI boxServGUI;
    StringBuilder logStringBuilder= new StringBuilder();;

    public Label server_status;
//   DBhelper dBhelper = SQLmanager.getInstance();
    ClientAcceptor clientAcceptor;
    static LinkedList<Stoppable> stoppableList = new LinkedList<>();
    Logger log = Logger.getLogger(Class.class.getName());
    Path serverFolder;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("server-gui.fxml"));
        primaryStage.setTitle("Клиент CloneBox");
        primaryStage.getIcons().add(new Image(BoxServGUI.class.getResourceAsStream("clonebox.png")));
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        boxServGUI=this;
        primaryStage.show();
    }

    @FXML
    private void handleStartServer() {
        printLogArea("\nСервер на старт");
       /* threadBoxServ = new BoxServ();
        threadBoxServ.setName("Сервер CloneBox");
        threadBoxServ.setDaemon(true);
        threadBoxServ.start();
        server_status.setText("Сервер запущен");*/

        clientAcceptor = ClientAcceptor.getClientAcceptor();
        server_status.setText("Сервер запущен");
        printLogArea("\nСервер стартует");
        while (!clientAcceptor.getClientAcceptorThread().isAlive())  {
            printLogArea(".");
        }
        printLogArea("\nСервер стартовал");
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
    }

    @FXML
    private void handleStopServer() {
//        threadBoxServ.interrupt(); //todo при наличии подключений сервер не выключится. Надо отослать клиенту сообщние о разрыве и закрывать соединение.
//        while (threadBoxServ.isAlive()) System.out.println("Сервер останавливается");
        printLogArea("\nСервер останавливается");
        for (Stoppable stoppable : stoppableList) {
            stoppable.stop();
        }

        clientAcceptor.getClientAcceptorThread().interrupt();
        while (clientAcceptor.getClientAcceptorThread().isAlive())  {
            printLogArea(".");
        }

        server_status.setText("Сервер остановлен");
        printLogArea("\nСервер остановлен ");
        startBtn.setDisable(false);
        stopBtn.setDisable(true);
    }

    public Label getServer_status() {
        return server_status;
    }

    public static void main(String[] args) {
//        handleStartServer();

        launch(args);
    }

    public static LinkedList<Stoppable> getStoppableList() {
        return stoppableList;
    }

    public void printLogArea(String s) {
        logStringBuilder.append(s);
        Platform.runLater(()->{logArea.setText(logStringBuilder.toString());});
    }
}
