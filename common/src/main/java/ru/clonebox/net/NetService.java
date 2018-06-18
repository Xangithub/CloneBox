package ru.clonebox.net;

import ru.clonebox.common.Util;
import ru.clonebox.messages.AbstractMessage;
import ru.clonebox.messages.ErrorMessage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NetService {

    public static int receiveHeader(SocketChannel channel) {
        ByteBuffer buffer = ByteBuffer.allocate(4); //int capacity
        int msgLength = 0;
        int nBytesReceived = 0;
        int count = 0;
        try {
            nBytesReceived = channel.read(buffer); //чтение из канала в буфер

        } catch (IOException e) {
//                Util.log("ПРинято "  + nBytesReceived + " байт заголовка");
            System.out.println("receiveHeader: чтение заголовка с канала прервано с ошибкой " + e.getMessage());
            return -1;
        }
//        Util.log("ПРинято "  + nBytesReceived + " байт заголовка");
        buffer.flip();
        msgLength = buffer.getInt();
        return msgLength;
    }


    public static AbstractMessage receiveObject(SocketChannel channel) {
        int msgSize = NetService.receiveHeader(channel);

        if (msgSize <= 0 || msgSize > 8192) {
            Util.log("прием прерван  по причине msgSize=" + msgSize);
            /*try {
                channel.close();
                Util.log("канал закрыт по причине");
            } catch (IOException e) {
                e.printStackTrace();
            }*/
            return new ErrorMessage(" Считывание заголовка сообщения провалено. Длина заголовка сообщения -1", "Сервер BoxServ");
        }


      /*  if (msgSize > 15_000 | msgSize<=0) {
            System.out.println("receiveObject: Дикий размер сообщения " + msgSize); //todo тут чтобы не ставить блок на защиту от большого сообщения, можно применить тот же метод что и при приеме файлов
            try {
                throw new IOException("receiveObject: Very big or small length msg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        ByteBuffer data = ByteBuffer.allocate(msgSize);

        int nBytesReceivedServer = 0;
        try {
            nBytesReceivedServer = channel.read(data);
            ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(data.array()));
            AbstractMessage msg = (AbstractMessage) objectInputStream.readObject();
            Util.log(" принято сообщение " + msg.getType() + "= " + nBytesReceivedServer);
            return msg;


        } catch (IOException e) {
            Util.log("Ошибка чтения данных из канала метод receiveObject");
            e.printStackTrace();
            return new ErrorMessage("Ошибка чтения данных из канала метод receiveObject", "Сервер BoxServ"); //todo Может тут исключение кидать как вариант.


        } catch (ClassNotFoundException e) {
            Util.log("Ошибка приведения типа сообщения");
            e.printStackTrace();
            return new ErrorMessage("Ошибка приведения типа сообщения", "Сервер BoxServ"); //todo Может тут исключение кидать как вариант.

        }
    }


}
