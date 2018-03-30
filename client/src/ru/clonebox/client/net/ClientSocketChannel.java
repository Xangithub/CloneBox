package ru.clonebox.client.net;

import ru.clonebox.client.DataProvider;
import ru.clonebox.client.MessageHandler;
import ru.clonebox.client.Preferences;
import ru.clonebox.client.client.WindowClient;
import ru.clonebox.common.*;
import ru.clonebox.messages.AbstractMessage;
import ru.clonebox.messages.ErrorMessage;
import ru.clonebox.messages.Message;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class ClientSocketChannel extends Thread {

    private boolean connected = true;
    BlockingQueue<AbstractMessage> queueMessage2Server = new ArrayBlockingQueue<AbstractMessage>(10);
    public SocketChannel socketChannel;
    private static volatile ClientSocketChannel clientSocketChannel;
    static Preferences preferences;
    MessageHandler messageHandler;
    Logger log = Logger.getLogger(Class.class.getName());
    private WindowClient windowClient;

    public static ClientSocketChannel getClientSocketChannel() {

        ClientSocketChannel localInstance = clientSocketChannel;
        if (localInstance == null) {
            synchronized (ClientSocketChannel.class) {
                localInstance = clientSocketChannel;
                if (localInstance == null) clientSocketChannel = localInstance = new ClientSocketChannel();
            }

        }

        return localInstance;
    }

    public static ClientSocketChannel getClientSocketChannel(MessageHandler handler) {

        ClientSocketChannel localInstance = clientSocketChannel;
        if (localInstance == null) {
            synchronized (ClientSocketChannel.class) {
                localInstance = clientSocketChannel;
                if (localInstance == null) clientSocketChannel = localInstance = new ClientSocketChannel(handler);

            }

        }
        return localInstance;
    }

    private ClientSocketChannel(MessageHandler handler) {
        this();
        this.messageHandler=handler;
    }

    private ClientSocketChannel() {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(true);
            connected= socketChannel.connect(new InetSocketAddress(Constance.REMOTE_ADDRESS, Constance.PORT));
        } catch (IOException e) {
            e.printStackTrace();
        }


      /*  this.setName("Поток SocketChannel");
        this.setDaemon(true);
//        Log log = new Log(ClientSocketChannel.class.getName());
        this.start();*/
    }



    void processingEvents(AbstractMessage receivedMessage) { //обработка входящих событий

        Message type = receivedMessage.getType();

        switch (type) {
            case AUTH:
                break;
            case AUTH_ANS:
                synchronized ( DataProvider.getMessageHandler().getQueueFromServer()) {
                    DataProvider.getMessageHandler().getQueueFromServer().add(receivedMessage);
                    DataProvider.getMessageHandler().getQueueFromServer().notify();
                }
                log.info("AUTH_ANS положено в очередь");
                break;
            case LIST_ANSWER:
//                windowClient.updateRemotePanel(((ListMessage) receivedMessage).getPathList());
                break;
            case FILE_FROM_CLIENT2SERVER:
                break;
            case FILE_FROM_SERVER2CLIENT:

                break;
            case CLOSE_CONNECTION:
                break;
            case ERROR:
                ErrorMessage errorMessage = ((ErrorMessage) receivedMessage);
                System.out.println(errorMessage.getMsgError());
                break;
            default:
                log.warning("Не удалось определить тип сообщения " + receivedMessage.getType());
        }


    }


    @Override
    public void run() {
        log.info("Run клиентского потока стартовал");
        DataProvider.dataProvider.setSocketChannel(socketChannel);
        /*synchronized (clientSocketChannel) {
            clientSocketChannel.notify(); // надо проверить что вообще сюда всегда попадает
        }*/

        while (!isInterrupted()) {

                 /*   SocketChannel client = socketChannel = (SocketChannel) key.channel();
                                log.info("Подключение к серверу состоялось");
                                synchronized (clientSocketChannel) {
                                    clientSocketChannel.notify(); // надо проверить что вообще сюда всегда попадает
                                }
                                DataProvider.dataProvider.setSocketChannel(socketChannel);



                    if (!queueMessage2Server.isEmpty() & key.isWritable()) {
                        AbstractMessage msg = queueMessage2Server.remove();
                        FileMessage fileMessage;
                        switch (msg.getType()) {
                            case FILE_FROM_CLIENT2SERVER:
                                fileMessage = ((FileMessage) msg);
                                log.info("Отправка данных о файле " + fileMessage.getFileNameString());
                                msg.sendObject(client);
                                fileMessage.sendFile(client);
                                key.interestOps(SelectionKey.OP_READ);
                                break;
                            case FILE_FROM_SERVER2CLIENT:
                                msg.sendObject(client);
                                fileMessage = ((FileMessage) msg);
                                fileMessage.receiveFile(client);
                                windowClient.updateLocalPanel();
                                break;

                            case CLOSE_CONNECTION:
                                msg.sendObject(client);
                                interrupt();
                                break;
                            default:
                                msg.sendObject(client);
//                                key.interestOps(SelectionKey.OP_READ);
                        }
                    }

                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        AbstractMessage receivedMessage = receiveObject(channel);
                        if(receivedMessage.getType()!=Message.ERROR) messageHandler.queueFromServer.add(receivedMessage);
                        log.info("Сообщение типа " + receivedMessage.getType() +" добавлено в очередь queueFromServer");
                        synchronized ( messageHandler.queueFromServer){
                            messageHandler.queueFromServer.notify();
                        }
                        log.info(" messageHandler.queueFromServer notify");
                    }
*/

            }//while - process Message
            log.warning("Если ты это видишь то сокетЧанелу пиздец");

//            socketChannel.close();

            log.info("SocketChannel закрыт");



    }

    public  void closeConnection() {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    public void setWindowClient(WindowClient windowClient) {
        this.windowClient = windowClient;
    }
}



