package ru.clonebox.server.DAO;

import java.sql.*;
import java.util.List;

public final class SQLmanager implements DBhelper {
    public static Connection conn;
    static Statement stmt;
    static SQLmanager dbHelper = getInstance();
    public final String databaseUrl = "jdbc:sqlite:base.db";
    final String createUsersTable = "CREATE TABLE IF NOT EXISTS USERS\n" +
            "(\n" +
            "    NAME        TEXT    PRIMARY KEY NOT NULL,\n" +
            "    PASSWORD    CHAR(32),\n" +
            "    CREATEDATE  INTEGER NOT NULL,\n" +
            "    LASTLOGINDATE  INTEGER NOT NULL" +
            "); ";

    public static Connection getConn() {
        return conn;
    }

    private SQLmanager() {
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.executeUpdate(createUsersTable);
        } catch (SQLException | ClassNotFoundException e) {
            System.out.println("Ошибка подключения к базе " + e.getMessage());
        }


    }

    public static synchronized SQLmanager getInstance() {
        if (null == dbHelper) return new SQLmanager();
        else return dbHelper;
    }


    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");

        return conn = DriverManager.getConnection(databaseUrl);
    }

    public void CloseConnection() {
        try {
            if (null != conn) conn.close();
        } catch (SQLException e) {
            System.out.println("Ошибка закрытия соединения");
        }
        conn = null;
    }

    @Override
    public boolean isUserExist(String login) throws SQLException //проверка существования пользователя в базе
    {
        ResultSet rs = stmt.executeQuery("SELECT count(*) FROM USERS WHERE name='" + login + "';");
        return 0 != rs.getInt(1); //если счетчик не ноль то существует.
    }

    @Override
    public void dispose() {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long findByName(String login) {
        return 0;
    }

    @Override
    public String findById(long id) {
        return null;
    }

    @Override
    public List<String> findAllUsers() {
        return null;
    }

    @Override
    public boolean newUser(String login, String password) {
        int flag = 0;
        System.out.println("login = [" + login + "], password = [" + password + "]");
        java.util.Date createDate = new java.util.Date();
        java.util.Date lastLoginDate = createDate;
        try {
            PreparedStatement psmt = conn.prepareStatement("INSERT INTO USERS (name,password,createdate,lastLoginDate) VALUES (?,?,?,?);");
            psmt.setString(1, login);
            psmt.setString(2, password);
            psmt.setLong(3, createDate.getTime());
            psmt.setLong(4, lastLoginDate.getTime());
            flag = psmt.executeUpdate();
        } catch (SQLException e) {
            //todo Передать выше сбой при создании
            System.out.println("Ошибка создания записи нового пользователя в БД flag= " + flag);
        }
        System.out.println("Обращение к бд завершено. результат " + flag);
        return flag != 0;
    }

    @Override
    public boolean changePassword(String login, String passwordOld, String passwordNew) {
        return false;
    }

    @Override
    public boolean setPassword(String login, String passwordNew) {
        return false;
    }

    @Override
    public boolean deleteUser(String login) {
        return false;
    }

    @Override
    public boolean getUser(String login, String password) {
        boolean auth = false;
        try {
            PreparedStatement psmt = conn.prepareStatement("SELECT * FROM USERS WHERE  name = ? AND password=?;");

            psmt.setString(1, login);
            psmt.setString(2, password);
            ResultSet resultSet = psmt.executeQuery();
            if (resultSet.next()) auth = true;

        } catch (SQLException e) {
            //todo Передать выше сбой при создании
            System.out.println("Ошибка получния записи пользователя в БД");
        }

        return auth;
    }


}
