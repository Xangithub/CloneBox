package ru.clonebox.messages;


import ru.clonebox.common.Util;
import ru.clonebox.net.NetService;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public abstract class AbstractMessage implements Serializable {
    Message type;
    String source;
    String destination;
/**Сий метод попытка сделать обмен шлю заголовок -жду подтверождения- шлю объект**/
 /*   public void sendObject(SocketChannel client, SelectionKey key) {
        ByteBuffer buffer4Object;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        int nBytesWrited2Channel = 0;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            buffer4Object = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

            //получить размер объекта
            int sizeObjectInt = buffer4Object.capacity();
            ByteBuffer sizeObjectByteBuf = ByteBuffer.allocate(4).putInt(sizeObjectInt);
            Util.log("Размер сообщения " + sizeObjectInt);
            //Выслать размер объекта получателю
            sizeObjectByteBuf.flip();
            nBytesWrited2Channel= client.write(sizeObjectByteBuf);
            *//***Теперь  принимающая сторона должна подтвердить получение размера - наверно проще вернуть размер для проверки корректности*//*
            key.interestOps(SelectionKey.OP_WRITE);
            //todo а вот тут засада... надо проинормировать селекто о котором сообщение и знать ничего не обязано



            Util.log("Записан заголовок в канал " + nBytesWrited2Channel);
            // Выслать сам объект
            nBytesWrited2Channel = client.write(buffer4Object);
        } catch (IOException e) {
            System.err.println("Ошибка отсылки данных в канал метод sendObject");
            e.printStackTrace();
//            return;
        }
        Util.log("Отослано сообщений " + this.getType() + " байт= " + nBytesWrited2Channel);

    }*/

    /***Текущий рабочий метод*/
    public void sendObject(SocketChannel client) {
        ByteBuffer buffer4Object;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        int nBytesWrited2Channel = 0;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(this);
            objectOutputStream.flush();
            buffer4Object = ByteBuffer.wrap(byteArrayOutputStream.toByteArray());

            //получить размер объекта
            int sizeObjectInt = buffer4Object.limit();
            ByteBuffer sizeObjectByteBuf = ByteBuffer.allocate(4).putInt(sizeObjectInt);
//             Util.log("Размер сообщения " + sizeObjectInt);
            //Выслать размер объекта получателю
            sizeObjectByteBuf.flip();
            nBytesWrited2Channel = client.write(sizeObjectByteBuf);
//            Util.log("Записан заголовок в канал " + nBytesWrited2Channel);
            // Выслать сам объект
            nBytesWrited2Channel = client.write(buffer4Object);
        } catch (IOException e) {
            Util.log("Ошибка отсылки данных в канал метод sendObject");
            e.printStackTrace();
//            return;
        }
//        Util.log("Отослано сообщений " + this.getType() + " байт= " + nBytesWrited2Channel);

    }

  /* static public void receiveObject(SocketChannel channel) {
       ByteBuffer bufferHead = ByteBuffer.allocate(4);
       int msgSize=NetService.receiveHeader(channel);
       if(msgSize==-1) return; //todo Может тут исключение кидать как вариант.

       ByteBuffer data = ByteBuffer.allocate(msgSize);
       int nBytesReceivedServer=0;

       try {
           nBytesReceivedServer= channel.read(data);
           ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data.array()));
           AbstractMessage msg = (AbstractMessage) objectInputStream.readObject();
           Util.log("принято сообщение " + msg.getType() + "= " + nBytesReceivedServer);
           return msg;


       } catch (IOException e) {
           Util.log("Ошибка чтения данных из канала метод receiveObject");
           e.printStackTrace();

       } catch (ClassNotFoundException e) {
           Util.log("Ошибка приведения типа сообщения");
           e.printStackTrace();
       }

       return new ErrorMessage("Ошибка принятия сообщения", "Сервер BoxServ");
    }
*/

    public Message getType() {
        return type;
    }

    public void setType(Message type) {
        this.type = type;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
