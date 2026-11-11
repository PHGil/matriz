package com.matriz.systemtests.rest;

/** Demo accounts of the Keycloak realm import (keycloak/matriz-realm.json). */
public enum DemoUser {

    ANA("ana"),
    BRUNO("bruno"),
    CARLA("carla");

    private final String username;

    DemoUser(String username) {
        this.username = username;
    }

    public String username() {
        return username;
    }
}
