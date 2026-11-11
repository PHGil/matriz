package com.matriz.systemtests.rest;

import com.matriz.systemtests.utils.ConfigHelper;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

/** Thin wrapper over REST Assured: JSON content type and bearer token of a demo user. Paths are relative to /api/v1. */
public final class RestHelper {

    public static final String PROBLEM_JSON = "application/problem+json";

    private RestHelper() {
    }

    public static RequestSpecification given(DemoUser user) {
        return given().header("Authorization", "Bearer " + AuthTokenProvider.getToken(user));
    }

    public static RequestSpecification given() {
        return RestAssured.given().baseUri(ConfigHelper.apiUrl()).contentType(ContentType.JSON);
    }
}
