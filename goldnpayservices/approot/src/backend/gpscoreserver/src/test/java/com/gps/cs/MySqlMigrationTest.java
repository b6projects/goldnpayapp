package com.gps.cs;

import static org.assertj.core.api.Assertions.assertThat;

import com.gps.cs.entity.*;
import com.gps.cs.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Runs Flyway V1 and Hibernate's schema validation against a real MySQL 8.4.
 * Skipped automatically when Docker is not available.
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@ActiveProfiles("test")
class MySqlMigrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4");

    @DynamicPropertySource
    static void db(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }

    @MockitoBean com.gps.cs.integration.OpsApiService ops;
    @Autowired UserRepository users;

    @Test
    void schema_is_created_by_flyway_and_validated_by_hibernate() {
        User u = new User();
        u.setEmail("mysql@example.com");
        u.setPasswordHash("x");
        u.setFullName("My SQL");
        u.setRole(Role.CLIENT);
        u.setStatus(UserStatus.ACTIVE);
        users.save(u);
        assertThat(users.findByEmailIgnoreCase("MYSQL@example.com")).isPresent();
    }
}
