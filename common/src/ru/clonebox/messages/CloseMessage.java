package ru.clonebox.messages;

public class CloseMessage extends AbstractMessage {

    public CloseMessage() {
        type=Message.CLOSE_CONNECTION;
    }
    public CloseMessage(String source) {
        type=Message.CLOSE_CONNECTION;
        this.source=source;
    }

}
