package ru.clonebox.filesystem;

import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;

public class FileOperations {

    public static void main(String[] args) {
        new FileOperations();
    }

    public FileOperations() {
        Path file = Paths.get("Y:\\client");
        Path newName = Paths.get("Y:\\client\\bbb\\11");
        Path abcdefg = Paths.get("abcdefg");
        Path result = file.relativize(newName);
//        deleteFile(file);
//    renameFile(file,newName);
//    createFolder(abcdefg);
        System.out.println(result);
    }

    public static void deleteFile(Path path) {
        try {
            Files.walk(path, FileVisitOption.FOLLOW_LINKS).
                    sorted(Comparator.reverseOrder()).
                    forEachOrdered(fileOrDir -> {
                        try {
                            Files.deleteIfExists(fileOrDir);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }

       /* if(Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)){
            try {
            Files.list(file).
            sorted((path1,path2) ->{if(path2.toFile().isDirectory()) return -1; else  return 1;}).
            peek(path -> deleteFile(path)).
            forEachOrdered(path -> path.toFile().delete()); // forEachOrdered(path -> deleteFile(path));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

    }

    public static void renameFile(Path oldPath, Path path) {
        try {
            Files.move(oldPath, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createFolder(Path path) {
        try {
            if (Files.notExists(path)) Files.createDirectories(path);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
