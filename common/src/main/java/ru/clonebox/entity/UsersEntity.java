package ru.clonebox.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.sql.Timestamp;
import java.util.Date;

@Entity(name = "users")
public class UsersEntity extends AbstractEntity {

    @Column(name = "login", unique = true, nullable = false)
    private String login;

    @Column(name = "createdate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdate;

    @Column(name = "timestamp")
    private Timestamp lastlogin;

    @Column
    private String password;

    public UsersEntity() {
    }

    public UsersEntity(String login, String password) {
        this.createdate = new Date();
//        this.lastlogin=new Timestamp(System.nanoTime());
    }


    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Date getCreatedate() {
        return createdate;
    }

    public void setCreatedate(Date createdate) {
        this.createdate = createdate;
    }

    public Timestamp getLastlogin() {
        return lastlogin;
    }

    public void setLastlogin(Timestamp lastlogin) {
        this.lastlogin = lastlogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Имя " + login + " Пароль " + password + " Дата создания " + createdate;
    }
}
