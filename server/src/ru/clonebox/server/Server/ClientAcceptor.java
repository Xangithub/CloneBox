package ru.clonebox.server.Server;


import ru.clonebox.common.*;
import ru.clonebox.server.GUI.BoxServGUI;
import ru.clonebox.server.Stoppable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;

public class ClientAcceptor implements Runnable, Stoppable {

    public static ClientAcceptor clientAcceptor;
    private ClientProcessor clientProcessor;
    ServerSocketChannel serverSocketChannel;
    Thread clientAcceptorThread;


    private ClientAcceptor() {
        clientAcceptorThread = new Thread(this);
        clientAcceptorThread.setDaemon(true);
        clientAcceptorThread.setName("ClientAcceptor");
        clientAcceptorThread.start();
        clientProcessor = ClientProcessor.getClientProcessor();
    }

    public static synchronized ClientAcceptor getClientAcceptor() {

        if (clientAcceptor != null) return clientAcceptor;
        else return new ClientAcceptor();

    }

    @Override
    public void run() {
        try {
            BoxServGUI.getStoppableList().add(this);
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            serverSocketChannel.socket().bind(new InetSocketAddress(Constance.LOCAL_ADDRESS, Constance.PORT)); // Сервер канал связывается с IP и портом для прослушивания

            Util.log("ClientAcceptor стартует ... вход в цикл обработки подключений");
            while (!clientAcceptorThread.isInterrupted()) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                Util.log("подключение принято " + socketChannel.getRemoteAddress());
                       /* socketChannel.configureBlocking(false);
                        synchronized (clientProcessor.selector){
                            socketChannel.register(clientProcessor.selector,SelectionKey.OP_READ);
                        }*/
//                       clientProcessor.newClient(socketChannel);
                socketChannel.configureBlocking(false);
                clientProcessor.getQueueSocketChannel().add(socketChannel);
                clientProcessor.selector.wakeup();
            }
//                Util.log("ClientAcceptor закончил работу ...");

        } catch (ClosedChannelException e) {
            Util.log("Операция регистрации на закрытом селекторе");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Util.log("ClientAcceptor закончил работу ...");
        }

        Util.log("ClientAcceptor мёртв ...");
    }

    public Thread getClientAcceptorThread() {
        return clientAcceptorThread;
    }

    @Override
    public void stop() {
        clientAcceptorThread.interrupt();
    }
}
