package ru.clonebox.server.DAO;

import java.sql.SQLException;

public interface DBhelper {

    boolean newUser(String login, String password);
    boolean changePassword(String login, String passwordOld, String passwordNew);
    boolean deleteUser(String login);
    boolean getUser(String login, String password);
    boolean isUserExist(String login) throws SQLException;
    void dispose();


}
