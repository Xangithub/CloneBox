package ru.clonebox.server.DAO;

import org.hibernate.Session;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import ru.clonebox.entity.*;


public class UserDAO implements DBhelper {
    private Session session;

    public UserDAO() {
        this.session = HibernateSessionFactoryUtil.createHibernateSession();
    }

    public UserDAO(Session session) {
        this.session = session;
    }

    @Override
    public boolean newUser(String login, String password) {
        UsersEntity user = new UsersEntity();
        user.setLogin(login);
        user.setPassword(password);
        user.setCreatedate(new Date());
        user.setLastlogin(new Timestamp(System.nanoTime()));

        session.beginTransaction();
        session.save(user);
        session.getTransaction().commit();
        return true;
    }

    @Override
    public boolean changePassword(String login, String passwordOld, String passwordNew) {
        try {
            session.beginTransaction();
            UsersEntity usersEntity = session.createQuery("SELECT a FROM ru.clonebox.entity.UsersEntity a WHERE a.login=:login and a.password=:password", UsersEntity.class).setParameter("login", login).setParameter("password", passwordOld).getSingleResult();
            usersEntity.setPassword(passwordNew);
            session.getTransaction().commit();
        } catch (javax.persistence.NoResultException e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean setPassword(String login, String passwordNew) {
        try {
            session.beginTransaction();
            UsersEntity usersEntity = session.createQuery("SELECT a FROM ru.clonebox.entity.UsersEntity a WHERE a.login=:login", UsersEntity.class).setParameter("login", login).getSingleResult();
            usersEntity.setPassword(passwordNew);
            session.getTransaction().commit();
        } catch (javax.persistence.NoResultException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean deleteUser(String login) {
        try {
            session.beginTransaction();
            UsersEntity usersEntity = session.createQuery("SELECT a FROM ru.clonebox.entity.UsersEntity a WHERE a.login=:login", UsersEntity.class).setParameter("login", login).getSingleResult();
            session.delete(usersEntity);
            session.getTransaction().commit();
        } catch (javax.persistence.NoResultException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean getUser(String login, String password) {
        try {
            UsersEntity usersEntity = session.createQuery("SELECT a FROM ru.clonebox.entity.UsersEntity a WHERE a.login=:login and a.password=:password", UsersEntity.class).setParameter("login", login).setParameter("password", password).getSingleResult();
        } catch (javax.persistence.NoResultException e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isUserExist(String login) {
        try {
            UsersEntity usersEntity = session.createQuery("SELECT a FROM ru.clonebox.entity.UsersEntity a WHERE a.login=:login", UsersEntity.class).setParameter("login", login).getSingleResult();

        } catch (javax.persistence.NoResultException e) {
            return false;
        }
        return true;
    }

    @Override
    public void dispose() {

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
//        session.beginTransaction();
//        List<String>  listUserEntity=(List<String>) session.createQuery("SELECT a FROM UsersEntiry a").list();
//        session.getTransaction().commit();
//        return listUserEntity;
        return null;
    }

    private void close() {
        session.close();
    }

}
