package ru.clonebox.messages;

public class ErrorMessage extends AbstractMessage {

    String msgError;

    public ErrorMessage(String msgError, String source) {
        type = Message.ERROR;
        this.msgError = msgError;
        this.source = source;
    }

    public ErrorMessage(String msgError, String source, String dest) {
        type = Message.ERROR;
        this.msgError = msgError;
        this.source = source;
        this.destination = dest;
    }

    public String getMsgError() {
        return msgError;
    }
}
