package ru.astondevs.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.astondevs.dao.UserDao;
import ru.astondevs.entity.User;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserDao userDao;

    @Test
    void createUser_ShouldSaveUser_WhenValidInputAndUniqueEmail() {
        // Given
        String input = "John Doe\njohn@example.com\n30\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        when(userDao.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenReturn(1L);

        // When
        userService.createUser();

        // Then
        verify(userDao).findByEmail("john@example.com");
        verify(userDao).save(any(User.class));
    }

    @Test
    void createUser_ShouldNotSaveUser_WhenDuplicateEmail() {
        // Given
        String input = "John Doe\nexisting@example.com\n30\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User existingUser = new User("Existing User", "existing@example.com", 25);
        when(userDao.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

        // When
        userService.createUser();

        // Then
        verify(userDao).findByEmail("existing@example.com");
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void createUser_ShouldHandleInvalidAgeInput() {
        // Given
        String input = "John Doe\njohn@example.com\ninvalid\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        // When
        userService.createUser();

        // Then
        // При невалидном возрасте метод должен завершиться с ошибкой ДО вызова userDao
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldFindUser_WhenUserExists() {
        // Given
        String input = "1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User expectedUser = new User("John Doe", "john@example.com", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(expectedUser));

        // When
        userService.getUserById();

        // Then
        verify(userDao).findById(1L);
    }

    @Test
    void getUserById_ShouldHandleUserNotFound() {
        // Given
        String input = "999\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // When
        userService.getUserById();

        // Then
        verify(userDao).findById(999L);
    }

    @Test
    void getUserById_ShouldRetryAfterInvalidIdInput() {
        // Given
        // Сначала невалидный ID, потом валидный
        String input = "invalid\n1";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User expectedUser = new User("John Doe", "john@example.com", 30);

        // Нужно вызвать метод дважды для тестирования повторного ввода
        // Первый вызов - с невалидным ID
        userService.getUserById();
        verify(userDao, never()).findById(anyLong());

        // Второй вызов - с валидным ID
        when(userDao.findById(1L)).thenReturn(Optional.of(expectedUser));
        userService.getUserById();

        // Then
        verify(userDao).findById(1L);
    }

    @Test
    void getAllUsers_ShouldDisplayAllUsers() {
        // Given
        Scanner scanner = new Scanner(new ByteArrayInputStream("".getBytes()));
        UserService userService = new UserService(userDao, scanner);

        List<User> expectedUsers = List.of(
                new User("User1", "user1@example.com", 20),
                new User("User2", "user2@example.com", 25)
        );

        when(userDao.findAll()).thenReturn(expectedUsers);

        // When
        userService.getAllUsers();

        // Then
        verify(userDao).findAll();
    }

    @Test
    void updateUser_ShouldUpdateUser_WhenValidInput() {
        // Given
        String input = "1\nNew Name\nnew@example.com\n35\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User existingUser = new User("Old Name", "old@example.com", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("new@example.com")).thenReturn(Optional.empty());

        // When
        userService.updateUser();

        // Then
        verify(userDao).findById(1L);
        verify(userDao).findByEmail("new@example.com");
        verify(userDao).update(existingUser);
    }

    @Test
    void updateUser_ShouldHandleUserNotFound() {
        // Given
        String input = "999\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // When
        userService.updateUser();

        // Then
        verify(userDao).findById(999L);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void updateUser_ShouldHandleDuplicateEmail() {
        // Given
        String input = "1\nNew Name\nexisting@example.com\n35\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User existingUser = new User("User1", "user1@example.com", 30);
        User anotherUser = new User("User2", "existing@example.com", 25);

        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDao.findByEmail("existing@example.com")).thenReturn(Optional.of(anotherUser));

        // When
        userService.updateUser();

        // Then
        verify(userDao).findById(1L);
        verify(userDao).findByEmail("existing@example.com");
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void updateUser_ShouldHandlePartialUpdate_OnlyAge() {
        // Given
        String input = "1\n\n\n35\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User existingUser = new User("Old Name", "old@example.com", 30);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userService.updateUser();

        // Then
        verify(userDao).findById(1L);
        verify(userDao).update(existingUser);
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Given
        String input = "1\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        User existingUser = new User("To Delete", "delete@example.com", 40);
        when(userDao.findById(1L)).thenReturn(Optional.of(existingUser));

        // When
        userService.deleteUser();

        // Then
        verify(userDao).findById(1L);
        verify(userDao).delete(1L);
    }

    @Test
    void deleteUser_ShouldHandleUserNotFound() {
        // Given
        String input = "999\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes()));
        UserService userService = new UserService(userDao, scanner);

        when(userDao.findById(999L)).thenReturn(Optional.empty());

        // When
        userService.deleteUser();

        // Then
        verify(userDao).findById(999L);
        verify(userDao, never()).delete(anyLong());
    }
}
