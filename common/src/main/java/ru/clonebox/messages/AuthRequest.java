package ru.clonebox.messages;

public class AuthRequest extends AbstractMessage {

    private String login;
    private String hashPassword;
    private boolean registrationRequest = false;


    public AuthRequest(String name, String hashPass) {
        this.login = name;
        this.hashPassword = hashPass;
        type = Message.AUTH;
    }

    public AuthRequest(String login, String hashPassword, boolean registrationRequest) {
        this.login = login;
        this.hashPassword = hashPassword;
        this.registrationRequest = registrationRequest;
        type = Message.AUTH;
    }

    public String getLogin() {
        return login;
    }

    public String getHashPassword() {
        return hashPassword;
    }

    public boolean isRegistrationRequest() {
        return registrationRequest;
    }

    @Override
    public String toString() {
        return "Имя " + login + " Хеш   " + hashPassword;
    }
}
