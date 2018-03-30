package ru.clonebox.server.Server;

import ru.clonebox.common.*;
import ru.clonebox.filesystem.FileOperations;
import ru.clonebox.messages.*;
import ru.clonebox.net.NetService;
import ru.clonebox.server.DAO.DBhelper;
import ru.clonebox.server.DAO.SQLmanager;
import ru.clonebox.server.GUI.BoxServGUI;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class KeyProcessor implements Runnable {
    SelectionKey key;
    SocketChannel channel;
    ClientProcessor clientProcessor;
    Logger log = Logger.getLogger(KeyProcessor.class.getName());
    DBhelper dBhelper = SQLmanager.getInstance();

    public KeyProcessor(SelectionKey key, ClientProcessor clientProcessor) {
        if (key == null) {
            return;
        }

        this.key = key;
        this.channel = (SocketChannel) key.channel();
        this.clientProcessor = clientProcessor;
    }

    @Override
    public void run() {
//        log.info("поток обработки ключа начат " + this + " объект ключа " + key + " ключ =" + key.interestOps());
        if (key.isReadable()) {
            AbstractMessage receivedMessage = NetService.receiveObject(channel);

            //Разбор сообщений не требующих ответа

            processingEvents(receivedMessage);
        }

//        clientProcessor.queueKey.remove(key);
        if (key.isValid()) {
//            log.info("поток обработки ключа завершён " + this + " ключ =" + key.interestOps());
            key.interestOps(SelectionKey.OP_READ);
            clientProcessor.selector.wakeup();
        }
    }

    void processingEvents(AbstractMessage receivedMessage) {

        FileMessage fileMessage;
        CommandMessage commandMessage;
        Message type = receivedMessage.getType();
//        log.info("Получено сообщение типа= " + type);
        Util.log("Получено сообщение типа= " + type);
        /**
         * Имитация задержки сервера
         */
//                        try {
//                            Thread.sleep(8000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
        switch (type) {
            case AUTH:
                AuthRequest authMessage = (AuthRequest) receivedMessage;

                //Если сообщение Регистрация пользователя
                if (authMessage.isRegistrationRequest()) {
                    BoxServGUI.boxServGUI.printLogArea("\nПопытка зарегистрировать пользователя" + authMessage);
//                    log.info("Попытка зарегистрировать пользователя" + authMessage);
                    if (userRegistration(authMessage)) {

                        createUserDir(authMessage.getLogin());
                        push(new AuthAnswer(true));

                    } else push(new AuthAnswer(false));

                } else { //Если сообщение проверка пароля

                    boolean auth = dBhelper.getUser(authMessage.getLogin(), authMessage.getHashPassword());

                    if (auth) {
                        push(new AuthAnswer(true));
                        BoxServGUI.boxServGUI.printLogArea("\nпользователь авторизован. Сформирован положительный ответ клиенту");
//                        log.info("пользователь авторизован. Сформирован положительный ответ клиенту");
                    } else {
                        push(new AuthAnswer(false));
                    }

                }
                break;

            case LIST_REQUEST:
                ListMessage listMessage = (ListMessage) receivedMessage;
                File reqPath = listMessage.getRequstedFolder();
                if (reqPath == null) reqPath = new File(Constance.DIR_OUT_SERVER);
                push(send2ClientListOfFiles(reqPath));
                break;

            case ERROR:
                log.severe("Сообщение не было принято");
                BoxServGUI.boxServGUI.printLogArea("\nСообщение не было принято");
//                System.out.println(((ErrorMessage) receivedMessage).getMsgError());
                break;

            case CLOSE_CONNECTION:
                key.cancel();
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case FILE_FROM_SERVER2CLIENT:
                fileMessage = ((FileMessage) receivedMessage);
                key.interestOps(SelectionKey.OP_WRITE);
                clientProcessor.selector.wakeup();
//                log.info("Зарегистрирован запрос на файл " + fileMessage.getFileNameString());
                BoxServGUI.boxServGUI.printLogArea("\nЗарегистрирован запрос на файл " + fileMessage.getFileNameString());
                Util.log("Зарегистрирован запрос на файл " + fileMessage.getFileNameString());
                fileMessage.sendFile(channel, null);

                break;

            case FILE_FROM_CLIENT2SERVER:
                fileMessage = ((FileMessage) receivedMessage);
//                log.info("Зарегистрировано сообщение с файлом " + fileMessage.getFileNameString());
                BoxServGUI.boxServGUI.printLogArea("\nЗарегистрировано сообщение с файлом " + fileMessage.getFileNameString());
                Util.log("Зарегистрировано сообщение с файлом " + fileMessage.getFileNameString());
                fileMessage.receiveFile(channel, null);
//                log.info("блок FILE_FROM_CLIENT2SERVER обработки ключа завершён " + this + " ключ =" + key.interestOps());

                break;


            case CREATE:
                commandMessage = ((CommandMessage) receivedMessage);
                Path pathCreate = commandMessage.getPath().toPath();
                Util.log("Создаём папку  " + pathCreate.toString());
                FileOperations.createFolder(pathCreate);
                break;

            case DELETE:
                commandMessage = ((CommandMessage) receivedMessage);
                Path deletePath = commandMessage.getPath().toPath();
                FileOperations.deleteFile(deletePath);
                break;

            case RENAME:
                commandMessage = ((CommandMessage) receivedMessage);
                Path oldPath = commandMessage.getPath().toPath();
                Path newPath = commandMessage.getPathTarget().toPath();
                FileOperations.renameFile(oldPath, newPath);
                break;

        }

    }

    public <V extends AbstractMessage, T extends AbstractMessage> V push(T message) {
        V answer = null;
        assert key != null;
        key.interestOps(SelectionKey.OP_WRITE); //todo интересно что и без этого работает... засылка в канал РАБОТАЕТ!!!
        clientProcessor.selector.wakeup(); //будим селектор после смены ключа ?? надо ли..
        message.sendObject(channel);
        log.info("Запрос " + message.getType() + " выслан клиенту");
        key.interestOps(SelectionKey.OP_READ);
        clientProcessor.selector.wakeup();
        return answer;
    }

    private void createUserDir(String login) {
        try {
            Files.createDirectory(Paths.get(Constance.DIR_OUT_SERVER + login));
        } catch (FileAlreadyExistsException e) {
            log.info("Папка уже существует");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean userRegistration(AuthRequest authMessage) { //todo вынести в интерфейс
        System.out.println("begin userRegistration");
        boolean auth = false;
        try {
            if (dBhelper.isUserExist(authMessage.getLogin())) {
                System.out.println("Такой пользователь уже есть");
                return false; //если существует пользователь возвращаемся с ошибкой
            }
            System.out.println("Обращение к БД с просьбой о регистрации пользователя");
            return dBhelper.newUser(authMessage.getLogin(), authMessage.getHashPassword());

        } catch (SQLException e) {
            System.out.println("ПРоблеиы подключения к базе данных. Не удалось авторизовать");
            e.printStackTrace();

        }

        return false;
    }


    private ListMessage send2ClientListOfFiles(File file) {
        List<File> list = new ArrayList<>(Arrays.asList(file.listFiles()));
        if(file.getParent()==null)  return new ListMessage(Message.LIST_ANSWER, list);
        list.add(0, new File(".."));
        return new ListMessage(Message.LIST_ANSWER, list);
    }

}
