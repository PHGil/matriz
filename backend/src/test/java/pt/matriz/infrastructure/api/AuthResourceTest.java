package pt.matriz.infrastructure.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import jakarta.inject.Inject;
import pt.matriz.domain.user.User;
import pt.matriz.domain.user.UserRepository;

@QuarkusTest
class AuthResourceTest {

    private static final String ANA_ID = "0f8b2c7e-1111-4000-8000-000000000011";

    @Inject
    UserRepository users;

    @Test
    void requestWithoutTokenIsRejectedWithProblem() {
        given()
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(401)
                .contentType("application/problem+json")
                .body("status", equalTo(401));
    }

    @Test
    @TestSecurity(user = "ana@matriz.test")
    @OidcSecurity(claims = {
            @Claim(key = "sub", value = ANA_ID),
            @Claim(key = "email", value = "ana@matriz.test"),
            @Claim(key = "name", value = "Ana Silva") })
    void meReturnsTokenUserAndPersistsIt() {
        given()
                .when().get("/api/v1/auth/me")
                .then()
                .statusCode(200)
                .body("id", equalTo(ANA_ID))
                .body("name", equalTo("Ana Silva"))
                .body("email", equalTo("ana@matriz.test"));

        User stored = QuarkusTransaction.requiringNew()
                .call(() -> users.findById(UUID.fromString(ANA_ID)).orElse(null));
        assertNotNull(stored);
        assertEquals("ana@matriz.test", stored.email());
    }
}
