package ru.astondevs.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.astondevs.config.TestDatabaseConfig;
import ru.astondevs.entity.User;
import ru.astondevs.util.HibernateUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDaoImplIntegrationTest {

    private SessionFactory sessionFactory;
    private UserDao userDao;

    @BeforeAll
    void setUp() {
        sessionFactory = TestDatabaseConfig.createTestSessionFactory();
        userDao = new UserDaoImpl();

        setTestSessionFactory(sessionFactory);
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
        TestDatabaseConfig.stopContainer();
    }

    @BeforeEach
    void clearDatabase() {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            session.getTransaction().commit();
        }
    }

    private void setTestSessionFactory(SessionFactory testSessionFactory) {
        try {
            java.lang.reflect.Field field = HibernateUtil.class.getDeclaredField("sessionFactory");
            field.setAccessible(true);
            field.set(null, testSessionFactory);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set test session factory", e);
        }
    }

    @Test
    void save_ShouldSaveUser_WhenValidUser() {
        // Given
        User user = new User("John Doe", "john@example.com", 30);

        // When
        Long userId = userDao.save(user);

        // Then
        assertThat(userId).isNotNull();

        Optional<User> savedUser = userDao.findById(userId);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("John Doe");
        assertThat(savedUser.get().getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.get().getAge()).isEqualTo(30);
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Given
        User user = new User("Jane Doe", "jane@example.com", 25);
        Long userId = userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findById(userId);

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getName()).isEqualTo("Jane Doe");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenUserNotExists() {
        // When
        Optional<User> foundUser = userDao.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Given
        userDao.save(new User("User1", "user1@example.com", 20));
        userDao.save(new User("User2", "user2@example.com", 25));
        userDao.save(new User("User3", "user3@example.com", 30));

        // When
        List<User> users = userDao.findAll();

        // Then
        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName).containsExactlyInAnyOrder("User1", "User2", "User3");
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoUsers() {
        // When
        List<User> users = userDao.findAll();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    void update_ShouldUpdateUser_WhenUserExists() {
        // Given
        User user = new User("Old Name", "old@example.com", 30);
        Long userId = userDao.save(user);

        User userToUpdate = userDao.findById(userId).get();
        userToUpdate.setName("New Name");
        userToUpdate.setEmail("new@example.com");
        userToUpdate.setAge(35);

        // When
        userDao.update(userToUpdate);

        // Then
        Optional<User> updatedUser = userDao.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getName()).isEqualTo("New Name");
        assertThat(updatedUser.get().getEmail()).isEqualTo("new@example.com");
        assertThat(updatedUser.get().getAge()).isEqualTo(35);
    }

    @Test
    void delete_ShouldDeleteUser_WhenUserExists() {
        // Given
        User user = new User("To Delete", "delete@example.com", 40);
        Long userId = userDao.save(user);

        // When
        userDao.delete(userId);

        // Then
        Optional<User> deletedUser = userDao.findById(userId);
        assertThat(deletedUser).isEmpty();
    }

    @Test
    void delete_ShouldNotThrow_WhenUserNotExists() {
        // When & Then
        Assertions.assertDoesNotThrow(() -> userDao.delete(999L));
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Given
        User user = new User("Email User", "email@example.com", 28);
        userDao.save(user);

        // When
        Optional<User> foundUser = userDao.findByEmail("email@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("email@example.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        // When
        Optional<User> foundUser = userDao.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void save_ShouldThrowException_WhenDuplicateEmail() {
        // Given
        User user1 = new User("User1", "duplicate@example.com", 25);
        userDao.save(user1);

        User user2 = new User("User2", "duplicate@example.com", 30);

        // When & Then
        Assertions.assertThrows(RuntimeException.class, () -> userDao.save(user2));
    }
}
