package ru.clonebox.client;

import javafx.beans.property.*;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;

public class FileData {
    private Path pathFile;
    private StringProperty fileName;
    private BooleanProperty isDirectory;
    private LongProperty size;
    private LongProperty dateModified;
    private final ObjectProperty<LocalDate> birthdate;



    public FileData() {

        this(null);

    }

    public FileData(Path pathFile) {
        if(pathFile!=null) {
            this.pathFile = pathFile;
            File file= pathFile.toFile();
            this.fileName = new SimpleStringProperty(file.getName());
            this.isDirectory = new SimpleBooleanProperty(file.isDirectory()) ;
            this.size = new SimpleLongProperty(file.length());
            this.dateModified = new SimpleLongProperty(file.lastModified());
        }
        birthdate = null;
    }

    public Path getPathFile() {
        return pathFile;
    }

    public String getFileName() {
        return fileName.get();
    }

    public StringProperty fileNameProperty() {
        return fileName;
    }

    public boolean isIsDirevtory() {
        return isDirectory.get();
    }

    public BooleanProperty isDirevtoryProperty() {
        return isDirectory;
    }

    public long getSize() {
        return size.get();
    }

    public LongProperty sizeProperty() {
        return size;
    }

    public long getDateModified() {
        return dateModified.get();
    }

    public LongProperty dateModifiedProperty() {
        return dateModified;
    }
}
