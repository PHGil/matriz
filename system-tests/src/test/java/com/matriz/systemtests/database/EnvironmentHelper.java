package com.matriz.systemtests.database;

import java.util.Map;
import java.util.UUID;

import com.matriz.systemtests.rest.DemoUser;

import static com.matriz.systemtests.rest.RestHelper.given;
import static com.matriz.systemtests.rest.StatusCode.CREATED;
import static com.matriz.systemtests.rest.StatusCode.OK;

/** Test fixtures: resets the database and builds projects, members and tasks through the real API. */
public class EnvironmentHelper {

    public final DatabaseHelper database = new DatabaseHelper();

    /** Empties the database and mirrors the demo users again (users are created on their first authenticated call). */
    public void reset() {
        database.deleteAllData();
        for (DemoUser user : DemoUser.values()) {
            userId(user);
        }
    }

    public UUID userId(DemoUser user) {
        return UUID.fromString(given(user).get("/auth/me").then().statusCode(OK).extract().path("id"));
    }

    public UUID createProject(DemoUser manager, String name) {
        String id = given(manager).body(Map.of("name", name))
                .post("/projects").then().statusCode(CREATED).extract().path("id");
        return UUID.fromString(id);
    }

    public void addMember(UUID projectId, DemoUser manager, DemoUser member) {
        given(manager).body(Map.of("userId", userId(member)))
                .post("/projects/" + projectId + "/members").then().statusCode(CREATED);
    }

    public UUID createTask(UUID projectId, DemoUser author, String title) {
        String id = given(author).body(Map.of("title", title))
                .post("/projects/" + projectId + "/tasks").then().statusCode(CREATED).extract().path("id");
        return UUID.fromString(id);
    }
}
