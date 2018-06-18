package ru.clonebox.server.Server;

import ru.clonebox.common.Util;
import ru.clonebox.server.GUI.BoxServGUI;
import ru.clonebox.server.Loggable;
import ru.clonebox.server.Stoppable;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

public class ClientProcessor implements Runnable, Stoppable {

    Selector selector; //Объявляем селектор для работы с каналами
    Loggable loggable;
    public static ClientProcessor clientProcessor;
    MessageHandler messageHandler;
    ExecutorService executor;

    Thread clientProcessorThread;
    private BlockingQueue<SocketChannel> queueSocketChannel = new ArrayBlockingQueue<SocketChannel>(10);
    //    int interestOps = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
    int interestOps = SelectionKey.OP_READ;

    private ClientProcessor(Loggable loggable) {
        this.loggable = loggable;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        clientProcessorThread = new Thread(this);
        clientProcessorThread.setDaemon(true);
        clientProcessorThread.setName("ClientProcessor");
        executor = Executors.newCachedThreadPool();
//        executor = Executors.newFixedThreadPool(5);

        clientProcessorThread.start();
//        getNewConnection();
//        messageHandler=MessageHandler.getInstance();


    }

    public static synchronized ClientProcessor getClientProcessor(Loggable loggable) {
        if (clientProcessor != null) return clientProcessor;
        else return new ClientProcessor(loggable);

    }

    @Override
    public void run() {
        BoxServGUI.getStoppableList().add(this);

       /* try {
            selector = Selector.open();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        //Регистрирует этот канал с заданным селектором, возвращая выбор ключ. Селектор, с которым должен регистрироваться этот канал
        // The interest set for the resulting key - Выборка для результирующего ключа
       /* synchronized (this){
            this.notify();
        }*/

        Util.log("ClientProcessor стартует ... вход в цикл обработки кдиентов");
        int countThread = 0;
        while (!clientProcessorThread.isInterrupted()) {
            int numbOper = 0;
            try {
                numbOper = selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // /**тут пока ненужно раз есть дежурный поток слежения за очередью*/
            getNewConnection();

            if (numbOper == 0) continue;
//            Util.log("ключей поймал " + numbOper);


            Set keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();
//            Util.log("Прилетело " + numbOper + "  ключей");
            //Пока есть следующий элемент. Мы его получаем, удаляем его из списка итератора.
            while (iterator.hasNext() && !clientProcessorThread.isInterrupted() && !executor.isShutdown()) {
                SelectionKey key = iterator.next();
                iterator.remove();
                if (!key.isValid()) {
                    continue;
                }
//                Util.log("До обнуления ключи " + String.valueOf(key.interestOps()));
                key.interestOps(0);
//                Util.log("После обнуления ключи " + String.valueOf(key.interestOps()));
//                if(queueKey.contains(key))  continue; else queueKey.add(key); //Если кдюч уже в обработке игнорим его
                KeyProcessor keyProcessor = new KeyProcessor(key, this);
                executor.execute(keyProcessor);
              /*  if (key.isReadable()) {

                    Util.log("прилетел ключ на чтение");
                   *//* SocketChannel channel = (SocketChannel) key.channel();

                    int n=0;
                    try {

                        do {
                            Util.log("Выборка данных с канала");
                          n = channel.read(ByteBuffer.allocate(3));
                        }while (n>0);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }*//*
                    continue;
                }

                if (key.isWritable()) {
                    Util.log("прилетел ключ на запись");
                    continue;
                }*/

            }

        }

        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Util.log("Цикл ClientProcessor окончил работу.");

    }

    private void getNewConnection() { //todo тут вот не уверен. ПОка работает код оценки размера может прилететь еще сокетЧанел в очередь.
        if (!queueSocketChannel.isEmpty()) {
            for (int i = 0; i < queueSocketChannel.size(); i++) {
                try {
                    queueSocketChannel.remove().register(selector, interestOps);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }

        }
     /*   Thread t = new Thread(() -> {
            while (true) {
                try {
//                    Thread.sleep(10000);
                    if(!queueSocketChannel.isEmpty()) Util.log("В очереди появиося сокет");
                    Util.log("Поток слежения за очередью новых подключений стартован");
                    SocketChannel socketChannel = queueSocketChannel.take();
                    selector.wakeup();
                    socketChannel.register(selector, interestOps);
                    Util.log("Новое подключение зарегистрировано ");
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Util.log("Поток слежения за очередью новых подключений завершён");
                    e.printStackTrace();
                }
            }
//            Util.log("Поток слежения за очередью новых подключений завершён");
        });
        t.setDaemon(true);
        t.start();*/

    }

    /*synchronized void newClient() {
        SocketChannel channel=ClientAcceptor.getClientAcceptor().getQueueSocketChannel().remove();
        try {
            Util.log(String.valueOf(channel.isBlocking()));
            channel.configureBlocking(false);
            Util.log(String.valueOf(channel.isBlocking()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Util.log("Канал нового подключения зарегистрирован в селекторе");
        selector.wakeup();
    }*/

    public BlockingQueue<SocketChannel> getQueueSocketChannel() {
        return queueSocketChannel;
    }

    void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdownNow();

    }

    @Override
    public void stop() {
        close();
        clientProcessorThread.interrupt();
    }
}
