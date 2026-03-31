package org.smartsupply.integration;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.smartsupply.dto.request.LoginRequestDto;
import org.smartsupply.dto.request.RegisterRequestDto;
import org.smartsupply.dto.response.AuthResponseDto;
import org.smartsupply.model.enums.Role;
import org.smartsupply.repository.RefreshTokenRepository;
import org.smartsupply.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest. WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class IntegrationTestBase {

    // ========== TESTCONTAINER POSTGRESQL ==========

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("smartsupply_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(false);  // Réutilise le conteneur entre les tests

    // ========== CONFIGURATION DYNAMIQUE DE LA BASE DE DONNÉES ==========

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres:: getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring. datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }


    // ========== SPRING BOOT TEST ==========

    @LocalServerPort
    protected int port;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        // Nettoyer la base de données avant chaque test
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ========== MÉTHODES UTILITAIRES ==========

    protected AuthResponseDto registerUser(String email, String password, Role role) {
        RegisterRequestDto registerDto = new RegisterRequestDto();
        registerDto.setFirstName("Test");
        registerDto.setLastName("User");
        registerDto.setEmail(email);
        registerDto.setPassword(password);
        registerDto.setRole(role);

        return given()
                .contentType("application/json")
                .body(registerDto)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201)
                .extract()
                .as(AuthResponseDto.class);
    }

    protected AuthResponseDto login(String email, String password) {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);

        return given()
                .contentType("application/json")
                .body(loginDto)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponseDto.class);
    }

    protected String getAdminToken() {
        registerUser("admin@test.com", "Admin@123", Role.ADMIN);
        return login("admin@test.com", "Admin@123").getAccessToken();
    }

    protected String getWarehouseManagerToken() {
        registerUser("warehouse@test.com", "Warehouse@123", Role.WAREHOUSE_MANAGER);
        return login("warehouse@test.com", "Warehouse@123").getAccessToken();
    }

    protected String getClientToken() {
        registerUser("client@test.com", "Client@123", Role.CLIENT);
        return login("client@test.com", "Client@123").getAccessToken();
    }



}
