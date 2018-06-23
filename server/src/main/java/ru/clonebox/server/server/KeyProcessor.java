package ru.clonebox.server.server;

import ru.clonebox.common.Constance;
import ru.clonebox.common.Util;
import ru.clonebox.filesystem.FileOperations;
import ru.clonebox.messages.*;
import ru.clonebox.net.NetService;
import ru.clonebox.server.DAO.DBhelper;
import ru.clonebox.server.DAO.UserDAO;
import ru.clonebox.server.Loggable;

import java.io.File;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeyProcessor implements Runnable {
    SelectionKey key;
    SocketChannel channel;
    ClientProcessor clientProcessor;
    //    Logger log = Logger.getLogger(KeyProcessor.class.getName());
//    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(KeyProcessor.class);
//    DBhelper dBhelper = SQLmanager.getInstance();
    DBhelper dBhelper = new UserDAO();

    Loggable loggable;

    /**
     * создание экземпляра класса для данного ключа
     *
     * @param key
     * @param clientProcessor
     */
    public KeyProcessor(SelectionKey key, ClientProcessor clientProcessor) {
        this.loggable = clientProcessor.loggable;
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
                System.out.println("получено сообщение с  именем " + authMessage.getLogin() + " и паролем " + authMessage.getHashPassword());
                //Если сообщение Регистрация пользователя
                if (authMessage.isRegistrationRequest()) {
                    loggable.print("\nПопытка зарегистрировать пользователя" + authMessage);
//                    log.info("Попытка зарегистрировать пользователя" + authMessage);
                    if (dBhelper.newUser(authMessage.getLogin(), authMessage.getHashPassword())) { //если регистрация в БД прошла то сделать папку пользователя и отправлять ответ, что всё хорошо

                        createUserDir(authMessage.getLogin());
                        push(new AuthAnswer(true));

                    } else
                        push(new AuthAnswer(false));// если регистрация не прошла то отправка негативного ответа клиенту

                } else { //Если сообщение проверка пароля

                    boolean auth = dBhelper.getUser(authMessage.getLogin(), authMessage.getHashPassword());
                    Util.log("ВОт что вернула проверка логина и пароля" + auth);
                    if (auth) {
                        push(new AuthAnswer(true));
                        loggable.print("\nпользователь авторизован. Сформирован положительный ответ клиенту");
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
//                log.severe("Сообщение не было принято");
                loggable.print("\nСообщение не было принято");
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
                loggable.print("\nЗарегистрирован запрос на файл " + fileMessage.getFileNameString());
                Util.log("Зарегистрирован запрос на файл " + fileMessage.getFileNameString());
                fileMessage.sendFile(channel, null);

                break;

            case FILE_FROM_CLIENT2SERVER:
                fileMessage = ((FileMessage) receivedMessage);
//                log.info("Зарегистрировано сообщение с файлом " + fileMessage.getFileNameString());
                loggable.print("\nЗарегистрировано сообщение с файлом " + fileMessage.getFileNameString());
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
        loggable.print("Запрос " + message.getType() + " выслан клиенту");
        key.interestOps(SelectionKey.OP_READ);
        clientProcessor.selector.wakeup();
        return answer;
    }

    private void createUserDir(String login) {
        try {
            Files.createDirectory(Paths.get(Constance.DIR_OUT_SERVER + login));
        } catch (FileAlreadyExistsException e) {
            loggable.print("Папка " + login + " уже существует");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private ListMessage send2ClientListOfFiles(File file) {
        List<File> list = new ArrayList<>(Arrays.asList(file.listFiles()));
        if (file.getParent() == null) return new ListMessage(Message.LIST_ANSWER, list);
        list.add(0, new File(".."));
        return new ListMessage(Message.LIST_ANSWER, list);
    }

}
