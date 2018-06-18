package ru.clonebox.client.client;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.clonebox.client.net.ClientSocketChannel;
import ru.clonebox.client.DataProvider;
import ru.clonebox.client.MessageHandler;
import ru.clonebox.common.*;
import ru.clonebox.filesystem.FileOperations;
import ru.clonebox.filesystem.Reaction;
import ru.clonebox.filesystem.SimpleFileTreeItem;
import ru.clonebox.filesystem.Watcher;
import ru.clonebox.messages.*;


import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ru.clonebox.messages.Message.*;

public final class WindowClient {

    @FXML
    private ComboBox<Path> rootPathsComboBox;

    @FXML
    private Button btnCreateNetFolder;
    @FXML
    private Button btnRenameNetObject;
    @FXML
    private Button btnDeleteNetObject;
    @FXML
    private TreeView rightTreeView;

    @FXML
    private TableView<Path> localTableFS;
    @FXML
    private TableColumn<Path, String> nameLocalFile;
    @FXML
    private TableColumn<Path, String> sizeLocalFile;
    @FXML
    private TableColumn<Path, String> typeLocalFile;
    @FXML
    private TableColumn<Path, String> localLastTimeModified;

    @FXML
    private TableView<File> netTableFS;
    @FXML
    private TableColumn<File, String> nameServer;
    @FXML
    private TableColumn<File, String> sizeServer;
    @FXML
    private TableColumn<File, String> serverLastTimeModified;
    @FXML
    private TableColumn<File, String> typeSeverFile;


    @FXML
    private Button btnCopy2Server;
    @FXML
    private Button btnCopy2Local;
    ClientSocketChannel clientSocketChannel;
    private String login;
    MessageHandler messageHandler;
    private Path localPath;
    ExecutorService eventExecutor = Executors.newSingleThreadExecutor();
    ExecutorService watcherExecutor = Executors.newSingleThreadExecutor();
    Watcher watcher;
    private ObservableList<Path> obsListFilesLocal = FXCollections.observableArrayList();
    private ObservableList<File> obsListFilesNet = FXCollections.observableArrayList();
    private ObservableList<Path> rootPathsList = FXCollections.observableArrayList();

    File currentServerFolder;
    Path currentLocalFolder;
    final File topFile = new File("..");
    final Path topPath = Paths.get("..");
    private Path rootPath;

    @FXML
    private void initialize() {
        Util.log("WindowClient initialize работает ");
        if (DataProvider.login != null) DataProvider.primaryStage.setTitle("CloneBox: " + DataProvider.login);
        messageHandler = MessageHandler.getInstance();
        clientSocketChannel = ClientSocketChannel.getClientSocketChannel();

        if (clientSocketChannel != null) System.out.println("clientSocketChannel!=null");
        if (DataProvider.dataProvider.getSocketChannel() != null)
            System.out.println("DataProvider.dataProvider.getSocketChannel()!= null");
        /**
         * Обработка закрытия окна
         */
        DataProvider.primaryStage.setOnCloseRequest(e -> {
            e.consume();
            CloseMessage closeMessage = new CloseMessage();
            closeMessage.sendObject(clientSocketChannel.socketChannel);
            clientSocketChannel.interrupt(); //todo вот тут надо решить как обработать прерывание сетевой части, могут быть незавершенные операции. Пока ждём окончания
            clientSocketChannel.closeConnection();
            DataProvider.primaryStage.close(); //закрытие окна
            System.exit(0);
        });

        localPath = Paths.get(DataProvider.preferences.pathSync);
        rootPath = localPath.getRoot();
        currentLocalFolder = localPath;
        assert localPath != null;
        for (Path rootPathLocal : FileSystems.getDefault().getRootDirectories()) {
            rootPathsList.add(rootPathLocal);
        }
        rootPathsComboBox.setItems(rootPathsList);
        rootPathsComboBox.getSelectionModel().select(rootPath);


        /**
         * Загрузка дерева папок
         */

        File file = localPath.toFile();
        SimpleFileTreeItem simpleFileTreeItem = new SimpleFileTreeItem(file);
        rightTreeView.setRoot(simpleFileTreeItem);

        /**
         * Загрузка локальной таблицы
         */

//        SortedList<Path> sortedListPath = new SortedList<>(obsListFilesLocal);
         /* Comparator<Path> compPath = (o1, o2) -> {
            String o1Str = o1.getFileName().toString();
            String o2Str = o2.getFileName().toString();
            if (Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
               Util.log("Сравниваются o1" + o1.toString() + "o2"+ o2.toString() + "результат = " +o1Str.compareToIgnoreCase(o2Str));
                return o1Str.compareToIgnoreCase(o2Str);
            }

            if (!Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                Util.log("Сравниваются o1" + o1.toString() + "o2"+ o2.toString() + "результат = " +o1Str.compareToIgnoreCase(o2Str));
                return o1Str.compareToIgnoreCase(o2Str);
            }

            if (Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                Util.log("Сравниваются o1" + o1.toString() + "o2"+ o2.toString() + "результат = " +o1Str.compareToIgnoreCase(o2Str));
                return -1;
            }
            if (!Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                Util.log("Сравниваются o1" + o1.toString() + "o2"+ o2.toString() + "результат = " +o1Str.compareToIgnoreCase(o2Str));
                return 1;
            }
            return -1;
        };*/
//        sortedListPath.setComparator(compPath);
//        sortedListPath.sort(compPath);
//        sortedListPath.comparatorProperty().bind(localTableFS.comparatorProperty());
        addFilesToLocalObsList();
//        obsListFilesLocal.sort(compPath);
        localTableFS.setItems(obsListFilesLocal); //наблюдаемый список Path
        localTableFS.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
      /*  localTableFS.sortPolicyProperty().set(tableView->{

        });*/

/***
 * Метод setCellValueFactory(...) определяет, какое поле внутри класса Person будут использоваться для конкретного столбца в таблице.
 * Есть вариант сделать то же самое через PropertyValueFactory, но этот способ нарушает безопасность типов).*/

        nameLocalFile.setCellValueFactory(nameLocalFile -> new SimpleStringProperty(nameLocalFile.getValue().getFileName().toString()));
//        sizeLocalFile.setCellValueFactory( sizeLocalFile -> new SimpleObjectProperty<Long>(sizeLocalFile.getValue().toFile().length()) );
        sizeLocalFile.setCellValueFactory(sizeLocalFile -> new SimpleStringProperty(Util.sizeFormatValue(sizeLocalFile.getValue().toFile().length())));
        // возвращается объект SimpleStringProperty потому что именно с Пропертями умеет работать фабрика
        //из СеллДатаФичи выдергивается объект (в данном случае Path) через getVaule() затем из объекта берётся нужный атрибут и пакуется в Проперти, возвращается фабрике
        //да по сути двойна работа выполняется, потому что фабрика снова выдирает атрибут... может даже тройная. Ради чего? Уточнить
        typeLocalFile.setCellValueFactory(typeLocalFile -> {
            Util.log("применяется setCellValueFactory  typeLocalFile");
            boolean isDirectory = false;
            isDirectory = Files.isDirectory(typeLocalFile.getValue(), LinkOption.NOFOLLOW_LINKS);
            if (isDirectory) return new SimpleStringProperty("Directory");
            return new SimpleStringProperty("File");
        });

        localLastTimeModified.setCellValueFactory(timeLocalFile -> {
            Util.log("применяется setCellValueFactory  localLastTimeModified");

            FileTime fileTime = null;
            try {
                fileTime = Files.getLastModifiedTime(timeLocalFile.getValue(), LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new SimpleStringProperty(DateUtil.format(fileTime));
        });
        /**цветное выделение папок и файлов*/
        localTableFS.setRowFactory(tableView -> {
                    Util.log("Цветные строки - применяется setRowFactory  localTableFS +++");

                    return new TableRow<Path>() {

                        @Override
                        protected void updateItem(Path path, boolean empty) {
                            super.updateItem(path, empty);
                            if (path != null) if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                                getStyleClass().add("directory");
                            } else getStyleClass().add("file");

                        }
                    };
                }
        );
        localTableFS.setOnSort(event -> {
            Util.log("ПРишло событие сортировки");
//            event.getSource().sort();
           /* if (path != null) if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                getStyleClass().add("directory");
            } else getStyleClass().add("file");*/
        });
        Util.log("Загружена локальная таблица ");

        /**
         * Запуск наблюдения за локальной папкой синхронизации
         */
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                addFilesToLocalObsList();
            }
        };

        Reaction reaction = () -> {
            Platform.runLater(() -> addFilesToLocalObsList());
        };

        try {
            watcher = new Watcher(localPath, true, reaction);
            watcherExecutor.execute(watcher);
        } catch (IOException e) {
            Util.log("Ошибка создания наблюдения за папкой " + e.getMessage());
        }

/**
 *  Right table - net
 * /
 /**Цвета для папок и файлов на сервере*/
        netTableFS.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        netTableFS.setRowFactory(param -> new TableRow<File>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                if (file != null) if (file.isDirectory()) {
                    getStyleClass().add("directory");
                } else getStyleClass().add("file");
            }
        });

        /**
         * задается контекстное меню
         */

/*        netTableFS.setRowFactory(param -> {
            final TableRow<File> row = new TableRow<>();
            final ContextMenu contextMenu = new ContextMenu();
            final MenuItem createMenuItem = new MenuItem("Create");
            final MenuItem deleteMenuItem = new MenuItem("Delete");
            final MenuItem copyMenuItem = new MenuItem("Copy");
            final MenuItem renameMenuItem = new MenuItem("Rename");

//                createMenuItem.setOnAction();
            deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    netTableFS.getItems().remove(row.getItem());
                }
            });

//                contextMenu.setOnAction();
//                renameMenuItem.setOnAction();

            contextMenu.getItems().addAll(createMenuItem, deleteMenuItem, copyMenuItem, renameMenuItem);

//                row.contextMenuProperty().bind((ObservableValue<? extends ContextMenu>) contextMenu);
            param.setContextMenu(contextMenu);
            return row;

        });*/

        /**Блок запроса списка папок с сервера
         *
         */

/** Вариант запроса 1
 *
 */
/*
        ListMessage listAnswer = null;

        Task<ListMessage> task = new Task<>() {
            @Override
            public ListMessage call() {
                return DataProvider.getMessageHandler().push(new ListMessage(Message.LIST_REQUEST));
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
            listAnswer = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        assert listAnswer!=null;
        obsListFilesNet.addAll(listAnswer.getPathList());*/

/** Вариант запроса 2
 *
 */
        Task<ListMessage> task = new Task<ListMessage>() {
            @Override
            public ListMessage call() {
                return messageHandler.push(new ListMessage(Message.LIST_REQUEST));  //todo этот запрос часто будет повторяться! имеет смысл вынести в отдельный метод
            }
        };
        task.setOnSucceeded(event -> {
            try {
                updateNetFilePanel(task.get().getPathList());
                currentServerFolder = getListFilesFromNetMessage(task.get()).get(1).toPath().getParent().toFile();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        task.setOnFailed(event -> task.getException().printStackTrace()); //todo сделать информирование о ошибке запроса с сервера
        eventExecutor.execute(task);

        /**Вариант 3 попытка использовать Service*/
       /* ServiceTask serviceTask= new ServiceTask();
        serviceTask.start();
        serviceTask.valueProperty().addListener(()->{});*/

        netTableFS.setItems(obsListFilesNet);
        nameServer.setCellValueFactory(nameFile -> new SimpleStringProperty(nameFile.getValue().getName()));
        sizeServer.setCellValueFactory(sizeFile -> new SimpleStringProperty(Util.sizeFormatValue(sizeFile.getValue().length())));
        typeSeverFile.setCellValueFactory(typeServerFile -> {
            boolean isDirectory = false;
            isDirectory = Files.isDirectory(typeServerFile.getValue().toPath(), LinkOption.NOFOLLOW_LINKS);
            if (isDirectory) return new SimpleStringProperty("Directory");
            return new SimpleStringProperty("File");
        });
        serverLastTimeModified.setCellValueFactory(modTime -> {
            FileTime fileTime = null;
            try {
                fileTime = Files.getLastModifiedTime(modTime.getValue().toPath(), LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                System.out.println("Не удалось получить временные параметры файла " + modTime.getValue());
            }

            return new SimpleStringProperty(DateUtil.format(fileTime));
        });
    }

    private void addFilesToLocalObsList() {
        Comparator<Path> compPath = (o1, o2) -> {
            String o1Str = o1.getFileName().toString();
            String o2Str = o2.getFileName().toString();
//            Util.log("применяется компаратор папок");
            if (Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return -1;
            }
            if (!Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return 1;
            }

            if (Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return o1Str.compareToIgnoreCase(o2Str);
            }

            if (!Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return o1Str.compareToIgnoreCase(o2Str);
            }

            return 0;
        };
        obsListFilesLocal.clear();
        obsListFilesLocal.addAll(prepareListFiles4Panel(currentLocalFolder));
        obsListFilesLocal.sort(compPath);
    }

    private List<Path> prepareListFiles4Panel(Path path4Prepare) {
        List<Path> temp = null;
       /* Comparator<Path> compPath = (o1, o2) -> {
            String o1Str = o1.getFileName().toString();
            String o2Str = o2.getFileName().toString();
            if (Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return o1Str.compareToIgnoreCase(o2Str);
            }

            if (!Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return o1Str.compareToIgnoreCase(o2Str);
            }

            if (Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && !Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return -1;
            }
            if (!Files.isDirectory(o1, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(o2, LinkOption.NOFOLLOW_LINKS)) {
                return 1;
            }
            return 0;
        };*/
        try {
            temp = Files.list(path4Prepare).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace(); //todo выдать алерт диалог
        }
        if (!path4Prepare.equals(rootPath)) temp.add(0, topPath);
        return temp;
    }

    private List<File> getListFilesFromNetMessage(ListMessage netPathList) {
        return netPathList.getPathList();
    }


    /**
     * реакция на нажатие - копирования файлов на сервер
     */
    public void copy2Server(ActionEvent actionEvent) {
        ObservableList<Path> selected4Copy = localTableFS.getSelectionModel().getSelectedItems();

        if (!((Button) actionEvent.getSource()).equals(btnCopy2Server)) return;
        if (selected4Copy.isEmpty()) {
            return;
        }
        Path parentPath = selected4Copy.get(0).getParent();
        System.out.println("Копируем эти файлы на сервер " + selected4Copy);

        Stage fileCopyStage = new Stage();
        Label labelNameFile = new Label();
        final ProgressBar progressBarFile = new ProgressBar(0);
        fileCopyStage.setOnCloseRequest(e -> {
            e.consume();
        });

        Task recursiveCopy2Server = new Task<Void>() {

            @Override
            public Void call() {

                for (Path path : selected4Copy) {
                    if (path.equals(topPath)) continue;
                    try {
                        Files.walk(path, FileVisitOption.FOLLOW_LINKS).
                                forEachOrdered(srcFileOrDir -> {
                                    Path temp = parentPath.relativize(srcFileOrDir);
                                    Path nameFolder = currentServerFolder.toPath().resolve(temp);
                                    if (Files.isDirectory(srcFileOrDir, LinkOption.NOFOLLOW_LINKS)) {
                                        CommandMessage createFolderMessage = new CommandMessage(Message.CREATE, nameFolder.toFile());
                                        messageHandler.push(createFolderMessage);
                                    } else {
                                        FileMessage fileMessage = new FileMessage(FILE_FROM_CLIENT2SERVER, srcFileOrDir, nameFolder);
                                        messageHandler.push(fileMessage);
//                                         Platform.runLater(()->alert.setContentText(srcFileOrDir.toString()));
                                        Platform.runLater(() -> labelNameFile.setText(srcFileOrDir.toString()));
                                        fileMessage.sendFile(clientSocketChannel.socketChannel, progressBarFile);

                                    }
                                });
                        updateNetFilePanel(requestListNetFolder(currentServerFolder));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
        recursiveCopy2Server.setOnRunning(event -> {
         /*   btnCopy2Server.setDisable(true);
            alert.initOwner(DataProvider.primaryStage);
            alert.setTitle("копирование на сервер");
            alert.setContentText("");
            alert.setResizable(true);
            alert.getDialogPane().setContent(progressBarFile);
            alert.show();*/

            btnCopy2Server.setDisable(true);
            Group root = new Group();
            Scene scene = new Scene(root, 250, 100, Color.LIGHTGREEN);
            fileCopyStage.setScene(scene);

            progressBarFile.setLayoutX(20);
            progressBarFile.setLayoutY(50);
            progressBarFile.setCursor(Cursor.TEXT);
            DropShadow effect = new DropShadow();
            effect.setOffsetX(8);
            effect.setOffsetY(8);
            progressBarFile.setEffect(effect);
            progressBarFile.setTooltip(new Tooltip("Индикатор копирования"));
            progressBarFile.setPrefSize(200, 30);
            progressBarFile.setProgress(0.0);
            root.getChildren().addAll(progressBarFile, labelNameFile);
            fileCopyStage.initOwner(DataProvider.primaryStage);
            fileCopyStage.initModality(Modality.WINDOW_MODAL);
            fileCopyStage.showAndWait();

        });
        recursiveCopy2Server.setOnSucceeded(event -> {
            btnCopy2Server.setDisable(false);
//            alert.close();
            fileCopyStage.close();

        });
        eventExecutor.execute(recursiveCopy2Server);
    }

    /**
     * реакция на нажатие - копирования файлов на локальный диск
     */
   /* public void copy2Local(ActionEvent actionEvent) {
        if (!((Button) actionEvent.getSource()).equals(btnCopy2Local)) return;
        ObservableList<File> selected4Copy = netTableFS.getSelectionModel().getSelectedItems();
        if (selected4Copy.isEmpty()) {
            return;
        }
        Path destination = localTableFS.getItems().get(1);
        //todo тут еще обаботку, что на цели есть совпадающие имена совпадение.
        System.out.println("Копируем эти файлы на ПК " + selected4Copy);


        for (File file : selected4Copy) {


         *//*   if (file.isDirectory()) {
                //создать папку у себя
                FileOperations.createFolder(file.toPath());
                //запросить содержимое папки у сервера
                requestListNetFolder(file);
            } else {
                FileMessage fileMessage = new FileMessage(FILE_FROM_CLIENT2SERVER, file.toPath(), destination);
                messageHandler.push(fileMessage);
                fileMessage.receiveFile(clientSocketChannel.socketChannel);

            }*//*



            try {
                FileMessage fileMessage = new FileMessage(FILE_FROM_SERVER2CLIENT, file.toPath(), destination); //todo разобраться с копированием на локальный диск
                Task task = new Task<Void>() {
                    @Override
                    public Void call() {
                        Util.log("отправка сообщения о запросе на  файл ");
                        messageHandler.push(fileMessage);
                        Util.log("принять файл " + file.toPath().toString());
                        fileMessage.receiveFile(clientSocketChannel.socketChannel);
                        addFilesToLocalObsList();
                        return null;
                    }
                };
                eventExecutor.execute(task);

            } catch (Exception e) {
                System.err.println("ВОзникли проблемы с чтением файла " + file + " файл невозможно отослать");
            }

        }

    }*/
    public void copy2Local(ActionEvent actionEvent) {
        if (!((Button) actionEvent.getSource()).equals(btnCopy2Local)) return;
        ObservableList<File> selected4Copy = netTableFS.getSelectionModel().getSelectedItems();
        if (selected4Copy.isEmpty()) {
            return;
        }

        //todo тут еще обаботку, что на цели есть совпадающие имена совпадение.
        System.out.println("Копируем эти файлы на ПК " + selected4Copy);
        Stage fileCopyStage = new Stage();
        Label labelNameFile = new Label();
        final ProgressBar progressBarFile = new ProgressBar(0);
        fileCopyStage.setOnCloseRequest(e -> {
            e.consume();
        });
        Task recursiveCopy2Local = new Task<Void>() {
            @Override
            public Void call() {
                copyNetTreeToLocal(selected4Copy, currentLocalFolder, progressBarFile, labelNameFile);
                return null;
            }
        };
        eventExecutor.execute(recursiveCopy2Local);
        recursiveCopy2Local.setOnRunning(event -> {
            btnCopy2Local.setDisable(true);
            Group root = new Group();
            Scene scene = new Scene(root, 250, 100, Color.LIGHTGREEN);
            fileCopyStage.setScene(scene);

            progressBarFile.setLayoutX(20);
            progressBarFile.setLayoutY(50);
            progressBarFile.setCursor(Cursor.TEXT);
            DropShadow effect = new DropShadow();
            effect.setOffsetX(8);
            effect.setOffsetY(8);
            progressBarFile.setEffect(effect);
            progressBarFile.setTooltip(new Tooltip("Индикатор копирования"));
            progressBarFile.setPrefSize(200, 30);
            progressBarFile.setProgress(0.0);
            root.getChildren().addAll(progressBarFile, labelNameFile);
            fileCopyStage.initOwner(DataProvider.primaryStage);
            fileCopyStage.initModality(Modality.WINDOW_MODAL);
            fileCopyStage.showAndWait();
        });
        recursiveCopy2Local.setOnSucceeded(event -> {
            btnCopy2Local.setDisable(false);
            fileCopyStage.close();
        });
    }

    void copyNetTreeToLocal(List<File> list, Path targetFolder, ProgressBar progressBar, Label labelNameFile) { //todo обход дерева на сервере
        Path temp;
        for (File file : list) {
            if (file.equals(topFile)) continue;
            Path destinationPath = targetFolder.resolve(file.toPath().getFileName());
            Util.log(file + " копируется в " + destinationPath);
            if (file.isDirectory()) {
                //создать папку у себя
                FileOperations.createFolder(destinationPath);
                //запросить содержимое папки у сервера
                List<File> tempList = requestListNetFolder(file);
                Util.log(" Начат обход папки " + file);
                temp = targetFolder;
                targetFolder = destinationPath;
                copyNetTreeToLocal(tempList, targetFolder, progressBar, labelNameFile);
                targetFolder = temp;
                temp = null;

            } else {
                FileMessage fileMessage = new FileMessage(FILE_FROM_SERVER2CLIENT, file, destinationPath);
                messageHandler.push(fileMessage);
                Platform.runLater(() -> labelNameFile.setText(file.toString()));
                fileMessage.receiveFile(clientSocketChannel.socketChannel, progressBar);
            }
        }

    }


    public void changeLocalFolder(MouseEvent event) { //todo два раза можно нежать любую кнопку мыши... надо только левую
        if (event.getClickCount() == 2) {
            ObservableList<Path> selectedFolder = localTableFS.getSelectionModel().getSelectedItems();
            Path selFolder = selectedFolder.get(0);
            if (Files.isDirectory(selFolder)) {
                if (selFolder.getFileName().equals(topPath)) {
                    currentLocalFolder = currentLocalFolder.getParent();
                } else
                    currentLocalFolder = selFolder;
                addFilesToLocalObsList();

            }
        }
    }

    public void changeServerFolder(MouseEvent event) {

        if (event.getClickCount() == 2) {
            ObservableList<File> selectedFolder = netTableFS.getSelectionModel().getSelectedItems();
            File reqFile = selectedFolder.get(0);
            if (reqFile.isDirectory()) {

                if ("..".equals(reqFile.getName())) {
                    reqFile = currentServerFolder.getParentFile();
                }
                updateServerPanel(reqFile);

            }
        }

    }

    private void updateServerPanel(File folder) {
        Task<List<File>> task = new Task<List<File>>() {
            @Override
            public List<File> call() {
                return requestListNetFolder(folder);
            }
        };
        task.setOnSucceeded(event -> {
            try {
                updateNetFilePanel(task.get());
                currentServerFolder = folder;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
        task.setOnFailed(event -> task.getException().printStackTrace()); //todo сделать информирование о ошибке запроса с сервера
        eventExecutor.execute(task);
    }

    private List<File> requestListNetFolder(File folder) {
        ListMessage listMessage = messageHandler.push(new ListMessage(Message.LIST_REQUEST, folder));
        return listMessage.getPathList();
    }


    public void chooseFolderInTree(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
            SimpleFileTreeItem item = (SimpleFileTreeItem) (rightTreeView.getSelectionModel().getSelectedItem());
            if (item != null) {
                File file = item.getValue();
                if (file.isDirectory()) {
                    currentLocalFolder = file.toPath();
                    addFilesToLocalObsList();
                }
            }


        }
    }

    public void deleteNetObject(ActionEvent event) {
        if (!((Button) event.getSource()).equals(btnDeleteNetObject)) return;
        ObservableList<File> selected4Delete = netTableFS.getSelectionModel().getSelectedItems();
        if (selected4Delete.isEmpty()) {
            return;
        }
        for (File path : selected4Delete) {
            if (path.equals(topFile)) continue;
            CommandMessage deleteMessage = new CommandMessage(DELETE, path);
            Task task = new Task<Void>() {
                @Override
                public Void call() {
                    Util.log("отправка сообщения об удалении файла ");
                    messageHandler.push(deleteMessage);
                    Util.log("удаление файла " + path.getName());
                    List<File> newList = requestListNetFolder(currentServerFolder);
                    updateNetFilePanel(newList);
//                    ListMessage listMessage = messageHandler.push(new ListMessage(Message.LIST_REQUEST, currentServerFolder));
                    return null;
                }
            };
            eventExecutor.execute(task);
            task.setOnRunning(event1 -> btnDeleteNetObject.setDisable(true));
            task.setOnSucceeded(event1 -> btnDeleteNetObject.setDisable(false));
        }
    }

    public void renameNetObject(ActionEvent event) {
        if (!((Button) event.getSource()).equals(btnRenameNetObject)) return;
        ObservableList<File> selected4Rename = netTableFS.getSelectionModel().getSelectedItems();
        if (selected4Rename.isEmpty() || selected4Rename.size() > 1) { /** Пока блокируем возможность переименовать много файлов, хотя возможно перемещение сделать */
            return;
        }
        btnRenameNetObject.setDisable(true);
        String targetFileName = requestStringDialog("Переименование папки", "Введите имя папки:", "Новое название папки");
        if (targetFileName == null) return;
        String targetPath = currentServerFolder.getPath() + targetFileName;
        File targetFile = new File(targetPath);
        for (File path : selected4Rename) {

            CommandMessage renameMessage = new CommandMessage(RENAME, path, targetFile);
            Task task = new Task<Void>() {
                @Override
                public Void call() {
                    Util.log("отправка сообщения об удалении файла ");
                    messageHandler.push(renameMessage);
                    Util.log("удаление файла " + path.getName());
                    List<File> newList = requestListNetFolder(currentServerFolder);
                    updateNetFilePanel(newList);
                    return null;
                }
            };
            eventExecutor.execute(task);

        }
        btnRenameNetObject.setDisable(false);
    }


    public void createNetFolder(ActionEvent event) {
        String targetFileName = requestStringDialog("Создание папки", "Введите имя папки:", "Имя создаваемой папки");
        if (targetFileName == null) return;
        String newName = currentServerFolder.getPath() + "//" + targetFileName;
        CommandMessage createFolderMessage = new CommandMessage(Message.CREATE, new File(newName));
        Task createNetFolderTask = new Task<Void>() {
            @Override
            public Void call() {
                Util.log("отправка сообщения о создании папки");
                messageHandler.push(createFolderMessage);
                Util.log("Обновление списка после создания папки");
                List<File> newList = requestListNetFolder(currentServerFolder);
                updateNetFilePanel(newList);
                return null;
            }
        };
        eventExecutor.execute(createNetFolderTask);
        createNetFolderTask.setOnRunning(event1 -> btnCreateNetFolder.setDisable(true));
        createNetFolderTask.setOnSucceeded(event1 -> btnCreateNetFolder.setDisable(false));
    }

    private void updateNetFilePanel(List list) {
        obsListFilesNet.clear();
        obsListFilesNet.addAll(list);
    }

    String requestStringDialog(String title, String headerText, String contentText) {
        final String[] answer = new String[1];
        TextInputDialog dialog = new TextInputDialog();

        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            answer[0] = name;
        });
        if (answer[0] == null) return null;
        else return answer[0];

    }


    public void changeRootDir(ActionEvent event) {
        currentLocalFolder = rootPathsComboBox.getSelectionModel().getSelectedItem();
        addFilesToLocalObsList();
    }
}
