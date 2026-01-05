package org.smartsupply.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.smartsupply.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class IntegrationTestBase {

    // ========== TESTCONTAINER POSTGRESQL ==========

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("smartsupply_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true); // Réutilise le conteneur entre les tests

    // ========== CONFIGURATION DYNAMIQUE DE LA BASE DE DONNÉES ==========

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring. datasource.password", postgres::getPassword);
    }

    // ========== SPRING BOOT TEST ==========

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    // ========== MÉTHODES UTILITAIRES ==========

    // Auth helpers removed as they relied on legacy JWT implementation.
    // New integration tests should use Keycloak/OAuth2 mocks.

}
