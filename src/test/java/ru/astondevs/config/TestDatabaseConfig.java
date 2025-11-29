package ru.astondevs.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatabaseConfig {
    private static PostgreSQLContainer<?> postgreSQLContainer;

    public static void startContainer() {
        if (postgreSQLContainer == null) {
            postgreSQLContainer = new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            postgreSQLContainer.start();
        }
    }

    public static void stopContainer() {
        if (postgreSQLContainer != null) {
            postgreSQLContainer.stop();
        }
    }

    public static SessionFactory createTestSessionFactory() {
        startContainer();

        Configuration configuration = new Configuration();
        configuration.configure("hibernate-test.cfg.xml");
        configuration.setProperty("hibernate.connection.url", postgreSQLContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgreSQLContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgreSQLContainer.getPassword());
        configuration.addAnnotatedClass(ru.astondevs.entity.User.class);

        return configuration.buildSessionFactory();
    }

    public static String getJdbcUrl() {
        return postgreSQLContainer.getJdbcUrl();
    }

    public static String getUsername() {
        return postgreSQLContainer.getUsername();
    }

    public static String getPassword() {
        return postgreSQLContainer.getPassword();
    }
}
