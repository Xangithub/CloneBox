package ru.clonebox.messages;

import java.io.File;
import java.util.List;

public class ListMessage extends AbstractMessage {

    List<File> pathList;
    File requstedFolder;

    public ListMessage(Message type, List<File> pathList) {
        this.type = type;
        this.pathList = pathList;
    }

    public ListMessage(Message type) {
        this.type = type;
    }

    public ListMessage(Message type, File folder) {
        this.requstedFolder = folder;
        this.type = type;
    }

    public List<File> getPathList() {
        return pathList;
    }

    public File getRequstedFolder() {
        return requstedFolder;
    }
}
