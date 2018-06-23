package ru.clonebox.server;

import org.hibernate.Session;
import org.junit.*;
import org.junit.runners.MethodSorters;
import ru.clonebox.common.Util;
import ru.clonebox.server.DAO.HibernateSessionFactoryUtil;
import ru.clonebox.server.DAO.UserDAO;


import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestUserDAO {
    Session session;
    UserDAO userDAO;

    @Before
    public void createSession() {
        session = HibernateSessionFactoryUtil.createHibernateSession();
        userDAO = new UserDAO(session);
    }

    @Test
    public void a_LoginNotExistUser() {
        assertFalse(userDAO.isUserExist("NotExistUser"));
    }

   /* @Test
    public void a_LoginNotExistUser() {
        assertTrue(userDAO.getUser("1", Util.CreateHash("123")));
    }*/

    @Test
    public void b_NotExistUserWithPass() {
        assertFalse(userDAO.getUser("NotExistUser", "123"));
    }

    @Test
    public void c_createTestUser() {
        userDAO.newUser("NotExistUser", "newPassword");
    }

    @Test
    public void d_checkExistUser() {
        assertTrue(userDAO.isUserExist("NotExistUser"));
    }

    @Test
    public void e_checkGetUser() {
        assertTrue(userDAO.getUser("NotExistUser", "newPassword"));
    }

    @Test
    public void f_checkSetPasswordUser() {
        String newPass = "123";
        userDAO.setPassword("NotExistUser", newPass);
        assertTrue(userDAO.getUser("NotExistUser", newPass));
    }

    @Test
    public void g_checkDeleteUser() {
        assertTrue("Проверка существования логина перед удалением", userDAO.isUserExist("NotExistUser"));
        userDAO.deleteUser("NotExistUser");
        assertFalse("Проверка существования логина после удаления", userDAO.isUserExist("NotExistUser"));
    }

    @After
    public void close() {
        userDAO.dispose();
    }
}
