package com.matriz.systemtests.utils;

/** Environment configuration. Credentials only come from the environment (see .env.example), never from code. */
public final class ConfigHelper {

    public static final String DATABASE_NAME = "matriz_st";

    private ConfigHelper() {
    }

    public static String apiUrl() {
        return value("MATRIZ_API_URL", "http://localhost:8080/api/v1");
    }

    public static String keycloakTokenUrl() {
        return value("KEYCLOAK_URL", "http://localhost:8180") + "/realms/matriz/protocol/openid-connect/token";
    }

    public static String jdbcUrl() {
        return "jdbc:postgresql://" + value("DB_HOST", "localhost") + ":" + value("DB_PORT", "5432") + "/"
                + DATABASE_NAME;
    }

    public static String databaseUser() {
        return required("DB_USER");
    }

    public static String databasePassword() {
        return required("DB_PASSWORD");
    }

    public static String demoPassword() {
        return required("MATRIZ_DEMO_PASSWORD");
    }

    private static String value(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Environment variable " + name + " is not set (see README, \"Tests\")");
        }
        return value;
    }
}
