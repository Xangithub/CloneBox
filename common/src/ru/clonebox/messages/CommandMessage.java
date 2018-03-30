package ru.clonebox.messages;


import java.io.File;

public class CommandMessage extends AbstractMessage {

    File path;
    File pathTarget;

    public CommandMessage(Message type, File path) {
        this.type=type;
        this.path = path;
    }
    public CommandMessage(Message type, File path, File pathTarget) {
        this.type=type;
        this.path = path;
        this.pathTarget=pathTarget;
    }

    public File getPath() {
        return path;
    }

    public File getPathTarget() {
        return pathTarget;
    }
}
