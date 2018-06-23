package ru.clonebox.server.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import ru.clonebox.entity.UsersEntity;

public final class HibernateSessionFactoryUtil {
    static Session session;
    private static SessionFactory sessionFactory;
//    private static final Logger log = Logger.getLogger(HibernateSessionFactoryUtil.class);

    public static synchronized Session createHibernateSession() {
        if (sessionFactory == null) {
            Configuration configuration = new Configuration();//.configure();
            configuration.addAnnotatedClass(UsersEntity.class);
            StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
            sessionFactory = configuration.buildSessionFactory(builder.build());
        }
        session = sessionFactory.openSession();
        return session;
    }

    public synchronized void close() {
        session.close();
        sessionFactory.close();
    }
}
