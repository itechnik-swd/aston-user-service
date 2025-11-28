package ru.astondevs.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.astondevs.dao.UserDao;
import ru.astondevs.dao.UserDaoImpl;
import ru.astondevs.entity.User;
import ru.astondevs.util.HibernateUtil;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    private final UserDao userDao;
    private final Scanner scanner;

    public UserService() {
        this.userDao = new UserDaoImpl();
        this.scanner = new Scanner(System.in);
    }

    public UserService(UserDao userDao) {
        this.userDao = userDao;
        this.scanner = new Scanner(System.in);
    }

    public void createUser() {
        try {
            System.out.println("\n=== Create New User ===");
            System.out.println("Enter new user name: ");
            String name = scanner.nextLine();

            System.out.println("Enter new user email: ");
            String email = scanner.nextLine();

            System.out.println("Enter new user age: ");
            int age = Integer.parseInt(scanner.nextLine());

            Optional<User> existingUser = userDao.findByEmail(email);
            if (existingUser.isPresent()) {
                System.out.println("\nError: User with this email already exists!");
                return;
            }

            User user = new User(name, email, age);
            Long id = userDao.save(user);
            System.out.println("User created successfully with ID:" + id);

        } catch (NumberFormatException e) {
            System.out.println("Error: Age must be a valid number!");
            System.out.println("Invalid age input" + e);
        } catch (Exception e) {
            System.out.println("Error creating user:" + e.getMessage());
            System.out.println("Error in createUser" + e);
        }
    }

    public void getUserById() {
        try {
            System.out.println("\n=== Get User by ID ===");
            System.out.print("Enter user ID: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                System.out.println("\nUser found:" + user.get());
            } else {
                System.out.println("User not found with ID:" + id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a valid number!");
            System.out.println("Invalid ID input" + e);
        } catch (Exception e) {
            System.out.println("Error retrieving user:" + e.getMessage());
            System.out.println("Error in getUserById" + e);
        }
    }

    public void getAllUsers() {
        try {
            System.out.println("\n=== All Users ===");
            List<User> users = userDao.findAll();

            if (users.isEmpty()) {
                System.out.println("\nNo users found.");
            } else {
                users.forEach(System.out::println);
            }

        } catch (Exception e) {
            System.out.println("Error retrieving users:" + e.getMessage());
            System.out.println("Error in getAllUsers" + e);
        }
    }

    public void updateUser() {
        try {
            System.out.println("\n=== Update User ===");
            System.out.println("Enter user ID to update: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> userOpt = userDao.findById(id);
            if (userOpt.isEmpty()) {
                System.out.println("User not found with ID:" + id);
                return;
            }

            User user = userOpt.get();
            System.out.println("Current user:" + user);

            System.out.println("Enter new name (current: " + user.getName() + "):");
            String name = scanner.nextLine();
            if (!name.trim().isEmpty()) {
                user.setName(name);
            }

            System.out.println("Enter new email (current: " + user.getEmail() + "):");
            String email = scanner.nextLine();
            if (!email.trim().isEmpty()) {
                Optional<User> existingUser = userDao.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    System.out.println("Error: Another user with this email already exists!");
                    return;
                }
                user.setEmail(email);
            }

            System.out.print("Enter new age (current: " + user.getAge() + "): ");
            String ageInput = scanner.nextLine();
            if (!ageInput.trim().isEmpty()) {
                user.setAge(Integer.parseInt(ageInput));
            }

            userDao.update(user);
            System.out.println("\nUser updated successfully!");

        } catch (NumberFormatException e) {
            System.out.println("\nError: Age must be a valid number!");
            logger.warn("Invalid age input in update", e);
        } catch (Exception e) {
            System.out.println("\nError updating user: " + e.getMessage());
            logger.error("Error in updateUser", e);
        }
    }

    public void deleteUser() {
        try {
            System.out.println("\n=== Delete User ===");
            System.out.print("Enter user ID to delete: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> user = userDao.findById(id);
            if (user.isPresent()) {
                userDao.delete(id);
                System.out.println("User deleted successfully!");
            } else {
                System.out.println("User not found with ID: " + id);
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: ID must be a valid number!");
            logger.warn("Invalid ID input in delete", e);
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            logger.error("Error in deleteUser", e);
        }
    }

    public void close() {
        scanner.close();
        HibernateUtil.shutdown();
    }
}
