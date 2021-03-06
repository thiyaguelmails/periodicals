package com.stolser.javatraining.webproject.dao.user;

import com.stolser.javatraining.webproject.connection.pool.ConnectionPoolProvider;
import com.stolser.javatraining.webproject.dao.AbstractConnection;
import com.stolser.javatraining.webproject.dao.DaoFactory;
import com.stolser.javatraining.webproject.dao.UserDao;
import com.stolser.javatraining.webproject.model.entity.user.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.SQLException;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MysqlUserDaoTest {
    private static final int ADMIN_ID = 1;
    private static UserDao userDao;
    private static AbstractConnection conn;
    private static DaoFactory factory;
    private static User expected;

    @BeforeClass
    public static void setUp() throws Exception {
        conn = ConnectionPoolProvider.getTestPool().getConnection();
        factory = DaoFactory.getMysqlDaoFactory();
        userDao = factory.getUserDao(conn);

        User.Builder userBuilder = new User.Builder();
        userBuilder.setUserName("stolser")
                .setFirstName("Oleg")
                .setLastName("Stoliarov")
                .setEmail("stolser@gmail.com")
                .setStatus(User.Status.ACTIVE);

        expected = userBuilder.build();

    }

    @Test
    public void findOneById_Should_ReturnCorrectUser() throws Exception {

        assertUserData(userDao.findOneById(ADMIN_ID));
    }

    private void assertUserData(User actual) {
        assertEquals("UserName", expected.getUserName(), actual.getUserName());
        assertEquals("FirstName", expected.getFirstName(), actual.getFirstName());
        assertEquals("LastName", expected.getLastName(), actual.getLastName());
        assertEquals("Email", expected.getEmail(), actual.getEmail());
        assertEquals("Status", expected.getStatus(), actual.getStatus());
    }

    @Test
    public void findUserByUserName_Should_ReturnCorrectUser() {
        assertUserData(userDao.findOneByUserName("stolser"));
    }

    @Test
    public void findUserByUserName_Should_ReturnNull() {
        assertNull(userDao.findOneByUserName("stolser2"));
    }

    @Test
    public void findAll_Should_ReturnAllUsersFromDb() {
        int expectedNumber = 4;
        int actualNumber = userDao.findAll().size();

        assertEquals(expectedNumber, actualNumber);
    }


    @AfterClass
    public static void tearDown() throws SQLException {
        if (nonNull(conn)) {
            conn.close();
        }
    }

}