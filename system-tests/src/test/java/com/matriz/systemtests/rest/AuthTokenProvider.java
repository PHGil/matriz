package com.matriz.systemtests.rest;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;

import com.matriz.systemtests.utils.ConfigHelper;

import io.restassured.RestAssured;

/** Gets real Keycloak access tokens (password grant of the test-only client) and caches them until near expiry. */
public final class AuthTokenProvider {

    private static final String CLIENT_ID = "matriz-system-tests";
    private static final long SAFETY_MARGIN_SECONDS = 30;

    private static final Map<DemoUser, CachedToken> CACHE = new EnumMap<>(DemoUser.class);

    private AuthTokenProvider() {
    }

    public static synchronized String getToken(DemoUser user) {
        CachedToken cached = CACHE.get(user);
        if (cached == null || cached.expiresAt().isBefore(Instant.now())) {
            cached = requestToken(user);
            CACHE.put(user, cached);
        }
        return cached.value();
    }

    private static CachedToken requestToken(DemoUser user) {
        var response = RestAssured.given()
                .formParam("grant_type", "password")
                .formParam("client_id", CLIENT_ID)
                .formParam("username", user.username())
                .formParam("password", ConfigHelper.demoPassword())
                .post(ConfigHelper.keycloakTokenUrl())
                .then().statusCode(StatusCode.OK)
                .extract().jsonPath();
        long lifetime = response.getLong("expires_in");
        return new CachedToken(response.getString("access_token"),
                Instant.now().plusSeconds(Math.max(0, lifetime - SAFETY_MARGIN_SECONDS)));
    }

    private record CachedToken(String value, Instant expiresAt) {
    }
}
