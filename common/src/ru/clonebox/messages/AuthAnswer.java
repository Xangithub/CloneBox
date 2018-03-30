package ru.clonebox.messages;

public class AuthAnswer extends AbstractMessage {

    boolean authOK=false;

    public AuthAnswer(Boolean resultAuth) {
        type=Message.AUTH_ANS;
        this.authOK=resultAuth;
    }

    public boolean isAuthOK() {
        return authOK;
    }
}
