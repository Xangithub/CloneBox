package ru.clonebox.common;

public interface Constance {
    public static final String REGISTRATION_FXML = "reg/registration.fxml";
    public static final String PREF_FXML = "preferences/pref.fxml";
    public static final String CLIENT_FXML = "client/client.fxml";

    int PORT = 9090;
    //    String FILE_CONFIG = "config.ini";
    String FILE_CONFIG = "config.json";
    String FILE_CRED = "cred.json";
    String LOCAL_ADDRESS = "localhost";
    String REMOTE_ADDRESS = "localhost";

//    String DIR_OUT_CLIENT = "y:/client/";
    String DIR_OUT_SERVER = "x:/serv/";
//String DIR_OUT_CLIENT ="d:/client/";
//    String DIR_OUT_SERVER ="d:/serv/";

    int CAPACITY_SEND__MSG_BUFFER = 800;
    int CAPACITY_RECEIVE_MSG_BUFFER = 800;


    int WAIT_TIME = 0;
}
