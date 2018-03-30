package ru.clonebox.client;

import ru.clonebox.client.net.ClientSocketChannel;
import ru.clonebox.common.*;
import ru.clonebox.messages.AbstractMessage;
import ru.clonebox.messages.Message;
import ru.clonebox.net.NetService;

import java.util.LinkedList;

import static ru.clonebox.messages.Message.*;


public final class MessageHandler  {

    //    BlockingQueue<AbstractMessage> queueIncoming = new ArrayBlockingQueue<AbstractMessage>(10);
    LinkedList<AbstractMessage> queueToServer = new LinkedList<AbstractMessage>();
    LinkedList<AbstractMessage> queueFromServer = new LinkedList<AbstractMessage>();

    private static MessageHandler ourInstance; // = new MessageHandler();
    private static ClientSocketChannel clientSocketChannel;

    synchronized public static MessageHandler getInstance() {
        if (ourInstance == null) return new MessageHandler();
        else return ourInstance;
    }


    public LinkedList<AbstractMessage> getQueueFromServer() {
        return queueFromServer;
    }



    public <V extends AbstractMessage, T extends AbstractMessage> V push(T message) {
        V answer;
        clientSocketChannel = ClientSocketChannel.getClientSocketChannel(this); //ждём когда соседний поток поднимет подключение
        assert clientSocketChannel.socketChannel != null;
        message.sendObject(clientSocketChannel.socketChannel);
        Util.log( "push "+ "Запрос " + message.getType() + " выслан серверу");
        Message type= message.getType();
       if(type==FILE_FROM_CLIENT2SERVER |type == FILE_FROM_SERVER2CLIENT | type==CREATE | type==DELETE | type==RENAME )  return  null;
       answer = (V) NetService.receiveObject(clientSocketChannel.socketChannel);
        return answer;

    }


    public <V extends AbstractMessage, T extends AbstractMessage> V pop() {
        V answer;
        clientSocketChannel = ClientSocketChannel.getClientSocketChannel(this); //ждём когда соседний поток поднимет подключение
        assert clientSocketChannel.socketChannel != null;
        answer = (V) NetService.receiveObject(clientSocketChannel.socketChannel);
//        answer = (V) queueFromServer.remove();
        Util.log("В очередь упало сообщение " + answer.getType());
        return answer;
    }

    void add2queueToServerAndNotify(AbstractMessage message) {
        synchronized (queueToServer) {
            queueToServer.add(message);
            queueToServer.notify();
        }
    }

    private void wait4Connection() {
        clientSocketChannel = ClientSocketChannel.getClientSocketChannel(this);
        if (clientSocketChannel.socketChannel != null) return; //может еще SelectionKey проверять на нуль?
        synchronized (clientSocketChannel) {
            try {
                Util.log("Ожидание подъема сокета");
                clientSocketChannel.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Util.log("Подключение произошло " + clientSocketChannel.socketChannel);
    }

}
