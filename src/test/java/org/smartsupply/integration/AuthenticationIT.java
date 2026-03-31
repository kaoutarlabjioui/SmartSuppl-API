package org.smartsupply.integration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit. jupiter.api.Test;
import org.smartsupply.dto.request.LoginRequestDto;
import org.smartsupply.dto.response.AuthResponseDto;
import org.smartsupply. model.enums.Role;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest. Matchers.*;

public class AuthenticationIT extends IntegrationTestBase{
    @Disabled("Ignorer temporairement pour le run du projet")
    @Test
    @DisplayName(" Login valide avec PostgreSQL Testcontainer - Doit retourner des tokens")
    public void testLoginValidWithPostgres() {
        // Given - Inscription d'un utilisateur dans PostgreSQL Testcontainer
        registerUser("user@test.com", "Password@123", Role.CLIENT);

        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("user@test.com");
        loginDto.setPassword("Password@123");

        // When & Then
        given()
                .contentType("application/json")
                .body(loginDto)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user. email", equalTo("user@test.com"))
                .body("user.role", equalTo("CLIENT"))
                .body("tokenType", equalTo("Bearer"));
    }

    @Test
    @DisplayName(" Login avec email invalide - Doit retourner 401")
    public void testLoginInvalidEmail() {
        LoginRequestDto loginDto = new LoginRequestDto();
        loginDto.setEmail("nonexistent@test.com");
        loginDto.setPassword("Password@123");

        given()
                .contentType("application/json")
                .body(loginDto)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName(" Refresh token avec PostgreSQL - Doit renouveler les tokens")
    public void testRefreshTokenWithPostgres() {
        // Given
        AuthResponseDto authResponse = registerUser("user@test.com", "Password@123", Role.CLIENT);
        String refreshToken = authResponse.getRefreshToken();

        // When & Then
        given()
                .contentType("application/json")
                .body(Map.of("refreshToken", refreshToken))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("accessToken", not(equalTo(authResponse.getAccessToken())))
                .log()
                .all();
    }

    @Test
    @DisplayName(" Refresh token révoqué après logout - Doit retourner 401")
    public void testRefreshTokenRevokedAfterLogout() {
        // Given
        var authResponse = registerUser("user@test.com", "Password@123", Role.CLIENT);
        String refreshToken = authResponse.getRefreshToken();

        // Logout (révoque le token)
        given()
                .contentType("application/json")
                .body(Map. of("refreshToken", refreshToken))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(200);

        // When & Then - Tenter de réutiliser le token révoqué
        given()
                .contentType("application/json")
                .body(Map.of("refreshToken", refreshToken))
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName(" Vérifier que les tokens sont bien stockés dans PostgreSQL")
    public void testTokensStoredInPostgres() {
        // Given
        registerUser("user@test.com", "Password@123", Role.CLIENT);
        login("user@test.com", "Password@123");

        // When & Then - Vérifier dans la base
        long tokenCount = refreshTokenRepository.count();
        assert tokenCount >= 1 : "Le refresh token devrait être stocké dans PostgreSQL";
    }

}
