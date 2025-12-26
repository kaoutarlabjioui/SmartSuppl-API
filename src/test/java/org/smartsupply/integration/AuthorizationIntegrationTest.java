package org.smartsupply.integration;

import org.junit.jupiter.api. DisplayName;
import org.junit.jupiter.api.Test;
import org.smartsupply.model.enums.Role;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class AuthorizationIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("✅ ADMIN peut accéder à tous les utilisateurs (PostgreSQL)")
    public void testAdminCanAccessAllUsers() {
        String adminToken = getAdminToken();

        given()
                .header("Authorization", "Bearer " + adminToken)
                .when()
                .get("/api/users/all")
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName(" CLIENT ne peut PAS accéder à tous les utilisateurs (PostgreSQL)")
    public void testClientCannotAccessAllUsers() {
        String clientToken = getClientToken();

        given()
                .header("Authorization", "Bearer " + clientToken)
                .when()
                .get("/api/users/all")
                .then()
                .statusCode(403);

    }

    @Test
    @DisplayName(" Isolation des données :  CLIENT ne peut pas voir les données d'un autre CLIENT")
    public void testClientDataIsolation() {
        // Créer deux clients
        var client1 = registerUser("client1@test.com", "Password@123", Role.CLIENT);
        var client2 = registerUser("client2@test.com", "Password@123",Role.CLIENT);

        String client2Token = client2.getAccessToken();
        Long client1Id = client1.getUser().getId();

        // Client2 tente d'accéder au profil de Client1
        given()
                .header("Authorization", "Bearer " + client2Token)
                .when()
                .get("/api/users/" + client1Id)
                .then()
                .statusCode(403);
    }
}