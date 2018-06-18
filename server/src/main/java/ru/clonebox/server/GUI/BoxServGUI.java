package ru.clonebox.server.GUI;

import javafx.application.Application;
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
import ru.clonebox.server.Loggable;
import ru.clonebox.server.RLoader;
import ru.clonebox.server.Server.ClientAcceptor;
import ru.clonebox.server.Stoppable;

import java.nio.file.Path;
import java.util.LinkedList;

public class BoxServGUI extends Application implements Loggable {

    @FXML
    private TextField serverFolderStorage;
    @FXML
    private TextArea logArea;
    @FXML
    private Button startBtn;
    @FXML
    private Button stopBtn;
    Thread threadBoxServ;
    BoxServGUI boxServGUI;
    StringBuilder logStringBuilder = new StringBuilder();
    ;

    public Label server_status;
    //   DBhelper dBhelper = SQLmanager.getInstance();
    ClientAcceptor clientAcceptor;
    static LinkedList<Stoppable> stoppableList = new LinkedList<>();
    //    Logger log = Logger.getLogger(Class.class.getName());
    Path serverFolder;

    @Override
    public void start(Stage primaryStage) throws Exception {
//        System.out.println(getClass().getResource("server-gui.fxml"));
//        System.out.println(Thread.currentThread().getContextClassLoader().getResource("server-gui.fxml"));
//        Parent root = FXMLLoader.load(getClass().getResource("../../../server-gui.fxml"));
//        Parent root = FXMLLoader.load(getClass().getResource("../../../server-gui.fxml"));
        Parent root = FXMLLoader.load(RLoader.getResource("view/server-gui.fxml"));
        primaryStage.setTitle("Клиент CloneBox");
//        InputStream inputStream =ru.clonebox.server.RLoader.getResourceAsStream("clonebox.png");
//        primaryStage.getIcons().add(new Image(BoxServGUI.class.getResourceAsStream("clonebox.png")));
//        primaryStage.getIcons().add(new Image(ru.clonebox.server.RLoader.class.getResourceAsStream("resource/clonebox.png")));
//        System.out.println(RLoader.class.getPackage().getName());
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        primaryStage.getIcons().add(new Image(String.valueOf(classLoader.getResource("image/clonebox.png"))));
        Scene mainScene = new Scene(root);
        primaryStage.setScene(mainScene);
        boxServGUI = this;
        primaryStage.show();
    }

    @FXML
    private void handleStartServer() {
        print("\nСервер на старт");
       /* threadBoxServ = new BoxServ();
        threadBoxServ.setName("Сервер CloneBox");
        threadBoxServ.setDaemon(true);
        threadBoxServ.start();
        server_status.setText("Сервер запущен");*/

        clientAcceptor = ClientAcceptor.getClientAcceptor(this);
        server_status.setText("Сервер запущен");
        print("\nСервер стартует");
        while (!clientAcceptor.getClientAcceptorThread().isAlive()) {
            print(".");
        }
        print("\nСервер стартовал");
        startBtn.setDisable(true);
        stopBtn.setDisable(false);
    }

    @FXML
    private void handleStopServer() {
//        threadBoxServ.interrupt(); //todo при наличии подключений сервер не выключится. Надо отослать клиенту сообщние о разрыве и закрывать соединение.
//        while (threadBoxServ.isAlive()) System.out.println("Сервер останавливается");
        print("\nСервер останавливается");
        for (Stoppable stoppable : stoppableList) {
            stoppable.stop();
        }

        clientAcceptor.getClientAcceptorThread().interrupt();
        while (clientAcceptor.getClientAcceptorThread().isAlive()) {
            print(".");
        }

        server_status.setText("Сервер остановлен");
        print("\nСервер остановлен ");
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


    @Override
    public void print(String s) {
        logStringBuilder.append(s);
        logArea.setText(logStringBuilder.toString());
    }
}
