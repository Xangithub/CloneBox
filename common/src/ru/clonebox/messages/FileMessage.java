package ru.clonebox.messages;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import ru.clonebox.common.Constance;
import ru.clonebox.common.Util;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

public class FileMessage extends AbstractMessage {

    public final int PRIMARY_BUFFER_CAPACITY = 1024;
//      public final int PRIMARY_BUFFER_CAPACITY = 1024*1024*64;

    transient private Path sourcePath;
    private String targetFullPathString;
    private String fileNameString;
    private File sourceFile;
    private long size;

//    Map<Path, byte[]> sendFiles = new HashMap<>(); допустим файлов много...


    public FileMessage(Message type, Path sourcePath, Path targetPath) {
        this.type = type;
        this.sourcePath = sourcePath;
        if (sourcePath == null)  this.sourceFile = sourcePath.toFile();
        if (sourcePath != null && !Files.isDirectory(sourcePath)) {
            this.targetFullPathString = targetPath.toString();
            this.fileNameString = sourcePath.getFileName().toString();

            try {
                this.size = Files.size(sourcePath);
            } catch (IOException e) {
                Util.log("Не могу определить размер файла");
            }
        }
    }

    public FileMessage(Message type, File sourceFile, Path targetPath) {
        this.type = type;
        this.sourceFile=sourceFile;
        if (sourceFile!= null && sourceFile.isFile()) {
        this.targetFullPathString = targetPath.toString();
        this.fileNameString = sourceFile.getName().toString();
        this.size = sourceFile.length();
        }
    }

    public String getFileNameString() {
        return fileNameString;
    }


    public void sendFile(SocketChannel channel, ProgressBar progressBar) {
        Path srcPath= (sourcePath!=null)? sourcePath: sourceFile.toPath();
        try {
            FileChannel inFile = FileChannel.open(srcPath);

            ByteBuffer buffer = ByteBuffer.allocate(PRIMARY_BUFFER_CAPACITY);
            int number_of_buffers = (int) (size/PRIMARY_BUFFER_CAPACITY+1);

            int noOfBytesRead = 0;
            int counter = 0;
            int counterWrites = 0;


            for (int i = 0; i < number_of_buffers ; i++) {

                noOfBytesRead = inFile.read(buffer); //читаем в буфер файл.
                //  System.out.println("Прочитано из файла байт noOfBytesRead = " + noOfBytesRead);
                if (noOfBytesRead <= 0) break; //если читать нечего выход
                counter += noOfBytesRead; // то что прочли прибавляем к счетчику
                // System.out.println("Всего прочтено байт counter = " + counter);
                buffer.flip();

                do {
                    int noOfBytesWrite = channel.write(buffer); //то что записали вычисли из того что прочитали
                    counterWrites += noOfBytesWrite;  //Сколько байт записано
                    noOfBytesRead -= noOfBytesWrite;
//                    System.out.println("sendFile Байт записано в канал  = " + noOfBytesWrite);
                } while (noOfBytesRead > 0);
                buffer.clear();
                if (progressBar!= null) progressBar.setProgress((double) i/number_of_buffers);
            }
            inFile.close();
            Util.log("sendFile Общее количество записанных в канал данных " + counterWrites + " для файла " + fileNameString);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("sendFile Провал при отсылке файла " + fileNameString);

        }
    }

    public void receiveFile(SocketChannel channel, ProgressBar progressBar ) {
        Path outFilePath = Paths.get(targetFullPathString);

        Util.log("Ожидаемый размер файла " + fileNameString + " = " + size + " это " + size / (1024 * 1024) + " Мб");
        try {

            int BUFFER_CAPACITY = PRIMARY_BUFFER_CAPACITY;
            if (size < BUFFER_CAPACITY) BUFFER_CAPACITY = (int) size;

            FileChannel outFile = FileChannel.open(outFilePath, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

            int noOfBytesReceivedFromChannel = 0; //сколько байт принято с канала
            long counter = 0; //Общее количество записанныз в файл байт  должно быть size

            do {
                buffer.clear();
                noOfBytesReceivedFromChannel = channel.read(buffer); //чтение из канала в буфер
                if (noOfBytesReceivedFromChannel == -1) break;
//                System.out.print("Байт прочитано с канала  = " + noOfBytesReceivedFromChannel + "/r");

                buffer.flip(); // переключить буфер в режим чтения
                if (noOfBytesReceivedFromChannel > 0) {
                    int bytesWritedToFile = outFile.write(buffer);
                    //if(bytesWritedToFile>0)  System.out.print("Записано в файл байт  = " + bytesWritedToFile + "/r");
                    counter += bytesWritedToFile; //counter а это сколько скинуто в файл байт

                }
                //System.out.print("Записано байт " + fileNameString+ " успешно " + counter + " это " + size/(1024*1024) + " Мб" + "\r");
                if (progressBar!= null) {
                    long finalI = counter;
                    Platform.runLater(()->progressBar.setProgress((double) finalI /size));
                }

                if (size - counter < BUFFER_CAPACITY) {
                    if (size - counter < 0) Util.log("разница меньше нуля !!! " + String.valueOf(size - counter));
                    buffer.flip();// в режиме записи
                    buffer.limit((int) (size - counter));
                    int mBytes = channel.read(buffer);
                    //  System.out.println("ПРочитано в конце " + mBytes);
                    buffer.flip(); // переключить буфер в режим чтения
                    int wBytes = outFile.write(buffer);
                    //  System.out.println("Записано в конце " + wBytes);
                    counter = counter + wBytes;
                }

            } while (counter != size); //пока кол-во скинутых байт не равно размеру перед. файла крутимся
            outFile.close();
            if (size != counter) System.out.println("Размер принятого файла ошибочный - не все байты приняты");
            Util.log("receiveFile Передача завершена !!! Записано байт " + fileNameString + " успешно " + counter + " это " + counter / (1024 * 1024) + " Мб");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("receiveFile Провал при приеме файла " + fileNameString);
        }
    }

 /***это копия метода посылки через цикл do while без прогрес бара*/
 public void sendFileZapas(SocketChannel channel) {
        Path srcPath= (sourcePath!=null)? sourcePath: sourceFile.toPath();
        try {
            FileChannel inFile = FileChannel.open(srcPath);
//            inFile.transferTo(0,size,channel); // интересный вариант

            ByteBuffer buffer = ByteBuffer.allocate(PRIMARY_BUFFER_CAPACITY);
            int noOfBytesRead = 0;
            int counter = 0;
            int counterWrites = 0;

            do {
                noOfBytesRead = inFile.read(buffer); //читаем в буфер файл.
                //  System.out.println("Прочитано из файла байт noOfBytesRead = " + noOfBytesRead);
                if (noOfBytesRead <= 0) break; //если читать нечего выход
                counter += noOfBytesRead; // то что прочли прибавляем к счетчику
                // System.out.println("Всего прочтено байт counter = " + counter);
                buffer.flip();

                do {
                    int noOfBytesWrite = channel.write(buffer); //то что записали вычисли из того что прочитали
                    counterWrites += noOfBytesWrite;  //Сколько байт записано
                    noOfBytesRead -= noOfBytesWrite;
                    System.out.println("Байт записано в канал  = " + noOfBytesWrite);
                } while (noOfBytesRead > 0);
                buffer.clear();

            } while (true);
            inFile.close();
            Util.log("Общее количество записанных в канал данных" + counterWrites + " для файла " + fileNameString);
//            channel.shutdownOutput(); //конец
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Провал при приеме файла " + fileNameString);

        }
    }
    /***это копия метода приема через цикл do while без прогрес бара*/
    public void receiveFileZapas(SocketChannel channel, ProgressBar progressBar ) {
        Path outFilePath = Paths.get(targetFullPathString);

        Util.log("Ожидаемый размер файла = " + size + " это " + size / (1024 * 1024) + " Мб");
        try {

            int BUFFER_CAPACITY = PRIMARY_BUFFER_CAPACITY;
            if (size < BUFFER_CAPACITY) BUFFER_CAPACITY = (int) size;

            FileChannel outFile = FileChannel.open(outFilePath, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_CAPACITY);

            int noOfBytesReceivedFromChannel = 0; //сколько байт принято с канала
            long counter = 0; //Общее количество записанныз в файл байт  должно быть size
            do {
                buffer.clear();
                noOfBytesReceivedFromChannel = channel.read(buffer); //чтение из канала в буфер
                if (noOfBytesReceivedFromChannel == -1) break;
//                System.out.print("Байт прочитано с канала  = " + noOfBytesReceivedFromChannel + "/r");

                buffer.flip(); // переключить буфер в режим чтения
                if (noOfBytesReceivedFromChannel > 0) {
                    int bytesWritedToFile = outFile.write(buffer);
                    //if(bytesWritedToFile>0)  System.out.print("Записано в файл байт  = " + bytesWritedToFile + "/r");
                    counter += bytesWritedToFile; //counter а это сколько скинуто в файл байт

                }
                //System.out.print("Записано байт " + fileNameString+ " успешно " + counter + " это " + size/(1024*1024) + " Мб" + "\r");

                if (size - counter < BUFFER_CAPACITY) {
                    if (size - counter < 0) Util.log("разница меньше нуля !!! " + String.valueOf(size - counter));
                    ByteBuffer addbuffer = ByteBuffer.allocate((int) (size - counter)); // передаваемый файло может быть до 2047 Мб
                    int mBytes = channel.read(addbuffer);
                    //  System.out.println("ПРочитано в конце " + mBytes);
                    addbuffer.flip(); // переключить буфер в режим чтения
                    int wBytes = outFile.write(addbuffer);
                    //  System.out.println("Записано в конце " + wBytes);
                    counter = counter + wBytes;
                }


            } while (counter != size); //пока кол-во скинутых байт не равно размеру перед. файла крутимся
            outFile.close();
            if (size != counter) System.out.println("Размер принятого файла ошибочный - не все байты приняты");
            Util.log("Передача завершена !!! Записано байт " + fileNameString + " успешно " + counter + " это " + counter / (1024 * 1024) + " Мб");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Провал при приеме файла " + fileNameString);
        }
    }

    public void sendFileOne(SocketChannel channel) {
      /*  if (type == Message.FILE_FROM_SERVER2CLIENT) {
            sourcePath = Paths.get(Constance.DIR_OUT_SERVER + fileNameString);
        }*/
        try {
            long counterWrites;
            FileChannel inFile = FileChannel.open(sourcePath);
            counterWrites = inFile.transferTo(0, size, channel);

            Util.log("Общее количество записанных в канал данных" + counterWrites + " для файла " + fileNameString);
            inFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Провал при приеме файла " + fileNameString);

        }

    }

    public void sendFileBlock(SocketChannel channel) {
        try {
//            channel.configureBlocking(true);
            FileChannel inFile = FileChannel.open(sourcePath);
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            while (inFile.read(buffer) > 0) {
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }
            inFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void receiveFileBlock(SocketChannel channel) {
        try {
            channel.configureBlocking(true);

            Path outFilePath = Paths.get(Constance.DIR_OUT_SERVER + fileNameString);

            FileChannel outFile = FileChannel.open(outFilePath, EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            //объявили буфер на 1024
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            // Reads a sequence of bytes from this channel into the given buffer.
            //Читает последовательность байт из канала в заданный буфер
            while (channel.read(buffer) > 0) { //пока что то в канале есть читать с канала и писать в буфер
                buffer.flip();
                outFile.write(buffer); // Запись данных из буфера в канал. Слово write относится к каналу.
                buffer.clear(); //по окончании слива данных с канала переводим указатель на начало
            }
            outFile.close(); //закрываем канал по окончании передачи
            channel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendFileMy(SocketChannel channel) {
        try {
            byte[] arrBytes = Files.readAllBytes(sourcePath);
            System.out.println("arrBytes.length = " + arrBytes.length);
            long noOfBytesWrite = channel.write(ByteBuffer.wrap(arrBytes));
            System.out.println("Байт записано в канал  = " + noOfBytesWrite);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
