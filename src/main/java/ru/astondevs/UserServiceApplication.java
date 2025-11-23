package ru.astondevs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.astondevs.service.UserService;

import java.util.Scanner;

public class UserServiceApplication {
    private static final Logger logger = LogManager.getLogger(UserServiceApplication.class);

    public static void main(String[] args) {
        logger.info("Starting User Service application");

        UserService userService = new UserService();

        try (Scanner scanner = new Scanner(System.in)) {
            displayMenu();

            while (true) {
                System.out.print("\nChoose an option (1-6): ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        userService.createUser();
                        break;
                    case "2":
                        userService.getUserById();
                        break;
                    case "3":
                        userService.getAllUsers();
                        break;
                    case "4":
                        userService.updateUser();
                        break;
                    case "5":
                        userService.deleteUser();
                        break;
                    case "6":
                        System.out.println("Goodbye!");
                        logger.info("Application shutdown");
                        return;
                    default:
                        System.out.println("Invalid option! Please choose 1-6.");
                }

                displayMenu();
            }
        } catch (Exception e) {
            logger.error("Unexpected error in main", e);
            System.out.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            userService.close();
        }
    }

    private static void displayMenu() {
        System.out.println("\n=== User Service ===");
        System.out.println("1. Create User");
        System.out.println("2. Get User by ID");
        System.out.println("3. Get All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("6. Exit");
    }
}