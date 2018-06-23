package ru.clonebox.server.server;


import ru.clonebox.messages.AbstractMessage;

import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/***
 * Серверный обработчик. Пока без дела...
 */

public final class MessageHandler implements Runnable {

    //    BlockingQueue<AbstractMessage> queueIncoming = new ArrayBlockingQueue<AbstractMessage>(10);
    LinkedList<AbstractMessage> queueToServer = new LinkedList<AbstractMessage>();
    LinkedList<AbstractMessage> queueFromServer = new LinkedList<AbstractMessage>();

    BlockingQueue<SelectionKey> queueSelectionKey = new ArrayBlockingQueue<SelectionKey>(10);


    private static MessageHandler ourInstance; // = new MessageHandler();
    private static ClientProcessor clientProcessor;
//    volatile private SelectionKey selectionKey;

    synchronized public static MessageHandler getInstance() {
        if (ourInstance == null) return new MessageHandler();
        else return ourInstance;
    }

    private MessageHandler() {
//        clientSocketChannel = ClientSocketChannel.getClientSocketChannel();
//        DataProvider.setMessageHandler(this);
        Thread msgHandler = new Thread(this);
        msgHandler.setDaemon(true);
        msgHandler.setName("MessageHandler thread");
        msgHandler.start();
    }


//    public <V extends AbstractMessage, T extends AbstractMessage> V pushThread(T message) {
//        V answer = null;
//        Task<V> task = new Task<>() {
//            @Override
//            public V call() {
//                return push(message);
//            }
//        };
//        Thread thread = new Thread(task);
//        thread.start();
//        try {
//            thread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            answer = task.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//
//        return answer;
//
//       /* V answer;
//        wait4Connection(); //ждём когда соседний поток поднимет подключение
//        assert clientSocketChannel.socketChannel != null;
//        assert selectionKey!= null;
//        selectionKey.interestOps(SelectionKey.OP_WRITE);
//        clientSocketChannel.selWakeUp();
//        message.sendObject(clientSocketChannel.socketChannel);
//        Util.log("Запрос " + message.getType() + " выслан серверу");
//        selectionKey.interestOps(SelectionKey.OP_READ);
//        clientSocketChannel.selWakeUp();
//
//        synchronized (queueFromServer) {
//            while (queueFromServer.isEmpty()) {
//                try {
//                    Util.log("Ожидание ответа");
//                    DataProvider.getMessageHandler().getQueueFromServer().wait(3000); //todo обработать ситуацию когда сервер не ответил
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            answer = (V) queueFromServer.remove();
//            Util.log("В очередь упало сообщение " + answer.getType());
//        }
//        return answer;*/
//    }

   /* public <V extends AbstractMessage, T extends AbstractMessage> V push(T message, SelectionKey selectionKey) { //todo должен ли метод возвращать сообщение?
        V answer;
        assert selectionKey != null;
        SocketChannel clientSocketChannel = (SocketChannel) selectionKey.channel();
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        clientProcessor.selector.wakeup();

        message.sendObject(clientSocketChannel);
        Util.log("Сообщение " + message.getType() + " выслано клиенту");
//        selectionKey.interestOps(SelectionKey.OP_READ); //todo включить? есть ощущение что MH не используется....
//        clientProcessor.selector.wakeup();

        //если не ловить ответ от клиента ... этот блок не нужен
        answer = (V) queueFromServer.remove();
        Util.log("В очередь упало сообщение " + answer.getType());
        return answer;
    }*/



   /* private void sendFile(Path path, SelectionKey selectionKey) throws IOException {
        FileMessage fileMessage = new FileMessage(path, Message.FILE_FROM_CLIENT2SERVER);
        FileMessage fileCheck;


        Task<FileMessage> task = new Task<>() {
            @Override
            public FileMessage call() {
                return MessageHandler.getInstance().push(fileMessage, selectionKey);
            }
        };
        Thread thread = new Thread(task);
        thread.start();
        try {
            thread.join(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            fileCheck = task.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }*/


    @Override
    public void run() {
        /*while (true) {

            synchronized (queueToServer) {
                try {
                    Util.log("Ожидание данных для сервера ");
                    queueToServer.wait();
                    Util.log("Поступили данные ");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            AbstractMessage msg = queueToServer.remove();
            FileMessage fileMessage;
            switch (msg.getType()) {
                case FILE_FROM_CLIENT2SERVER:
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    clientSocketChannel.selWakeUp();
                    fileMessage = ((FileMessage) msg);
                    Util.log("Отправка данных о файле " + fileMessage.getFileNameString());
                    msg.sendObject(clientSocketChannel.socketChannel);
                    fileMessage.sendFile(clientSocketChannel.socketChannel);
                    selectionKey.interestOps(SelectionKey.OP_READ);
                    clientSocketChannel.selWakeUp();
                    break;
                case FILE_FROM_SERVER2CLIENT:
                    selectionKey.interestOps(SelectionKey.OP_READ);
                    clientSocketChannel.selWakeUp();
                    msg.sendObject(clientSocketChannel.socketChannel);
                    fileMessage = ((FileMessage) msg);
                    fileMessage.receiveFile(clientSocketChannel.socketChannel);
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    clientSocketChannel.selWakeUp();
//                    windowClient.updateLocalPanel();
                    break;

                case CLOSE_CONNECTION:
                    msg.sendObject(clientSocketChannel.socketChannel);
                    clientSocketChannel.interrupt();
                    break;
                default:
                    selectionKey.interestOps(SelectionKey.OP_WRITE);
                    clientSocketChannel.selWakeUp();
                    msg.sendObject(clientSocketChannel.socketChannel);
                    selectionKey.interestOps(SelectionKey.OP_READ);
                    clientSocketChannel.selWakeUp();
            }

        }*/

    }

    public LinkedList<AbstractMessage> getQueueToServer() {
        return queueToServer;
    }

    public void setQueueToServer(LinkedList<AbstractMessage> queueToServer) {
        this.queueToServer = queueToServer;
    }

    public LinkedList<AbstractMessage> getQueueFromServer() {
        return queueFromServer;
    }

    public void setQueueFromServer(LinkedList<AbstractMessage> queueFromServer) {
        this.queueFromServer = queueFromServer;
    }


    void add2queueToServerAndNotify(AbstractMessage message) {
        synchronized (queueToServer) {
            queueToServer.add(message);
            queueToServer.notify();
        }
    }
}
