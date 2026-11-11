package com.matriz.adapter.rest.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.matriz.application.port.in.MemberUseCases;
import com.matriz.application.port.in.ProjectUseCases;
import com.matriz.application.port.in.TaskUseCases;
import com.matriz.application.port.in.UserUseCases;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

/** Assignees (US-12) and comments (US-13). */
@QuarkusTest
class TaskCollaborationResourceTest {

    static final String ANA = "0f8b2c7e-5555-4000-8000-000000000021";
    static final String BRUNO = "0f8b2c7e-5555-4000-8000-000000000022";
    static final String CARLA = "0f8b2c7e-5555-4000-8000-000000000023";
    static final String DIOGO = "0f8b2c7e-5555-4000-8000-000000000024";

    @Inject
    UserUseCases users;

    @Inject
    ProjectUseCases projects;

    @Inject
    MemberUseCases members;

    @Inject
    TaskUseCases tasks;

    @Inject
    EntityManager em;

    @BeforeEach
    void users() {
        users.registerLogin(UUID.fromString(ANA), "Ana Colab", "ana.collab@matriz.test");
        users.registerLogin(UUID.fromString(BRUNO), "Bruno Colab", "bruno.collab@matriz.test");
        users.registerLogin(UUID.fromString(CARLA), "Carla Colab", "carla.collab@matriz.test");
        users.registerLogin(UUID.fromString(DIOGO), "Diogo Colab", "diogo.collab@matriz.test");
    }

    /** Task in a project managed by Ana with Bruno as member; Carla and Diogo are outsiders. */
    String taskUrl() {
        UUID projectId = projects.create(UUID.fromString(ANA), "Projeto " + UUID.randomUUID(), null).project().getId();
        members.add(projectId, UUID.fromString(ANA), UUID.fromString(BRUNO));
        UUID taskId = tasks.create(projectId, UUID.fromString(ANA), "Tarefa", null, null, null).task().getId();
        return "/api/v1/projects/" + projectId + "/tasks/" + taskId;
    }

    static String userRef(String userId) {
        return "{\"userId\":\"" + userId + "\"}";
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Colab"),
            @Claim(key = "email", value = "bruno.collab@matriz.test") })
    void memberAssignsTwoMembersAndRemovesOne() {
        String url = taskUrl();

        given().contentType(ContentType.JSON).body(userRef(ANA))
                .when().post(url + "/assignees")
                .then().statusCode(201)
                .body("id", equalTo(ANA))
                .body("name", equalTo("Ana Colab"));
        given().contentType(ContentType.JSON).body(userRef(BRUNO))
                .when().post(url + "/assignees").then().statusCode(201);

        given().when().get(url).then().statusCode(200)
                .body("assignees.id", containsInAnyOrder(ANA, BRUNO));

        given().when().delete(url + "/assignees/" + ANA).then().statusCode(204);
        given().when().get(url).then().body("assignees.id", contains(BRUNO));
        given().when().delete(url + "/assignees/" + ANA).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Colab"),
            @Claim(key = "email", value = "bruno.collab@matriz.test") })
    void duplicateAssignmentIsRefusedAndNotStored() {
        String url = taskUrl();
        given().contentType(ContentType.JSON).body(userRef(BRUNO))
                .when().post(url + "/assignees").then().statusCode(201);

        given().contentType(ContentType.JSON).body(userRef(BRUNO))
                .when().post(url + "/assignees")
                .then().statusCode(409)
                .contentType("application/problem+json");

        UUID taskId = UUID.fromString(url.substring(url.lastIndexOf('/') + 1));
        long rows = QuarkusTransaction.requiringNew().call(() -> ((Number) em
                .createNativeQuery("select count(*) from task_assignee where task_id = ?1")
                .setParameter(1, taskId).getSingleResult()).longValue());
        assertEquals(1, rows);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Colab"),
            @Claim(key = "email", value = "bruno.collab@matriz.test") })
    void nonMemberUnknownOrMissingUserCannotBeAssigned() {
        String url = taskUrl();
        for (String body : new String[] { userRef(DIOGO), userRef(UUID.randomUUID().toString()), "{}" }) {
            given().contentType(ContentType.JSON).body(body)
                    .when().post(url + "/assignees")
                    .then().statusCode(400)
                    .body("errors.field", hasItem("userId"));
        }
        given().when().get(url).then().body("assignees", empty());
    }

    @Test
    @TestSecurity(user = "carla")
    @OidcSecurity(claims = { @Claim(key = "sub", value = CARLA), @Claim(key = "name", value = "Carla Colab"),
            @Claim(key = "email", value = "carla.collab@matriz.test") })
    void outsiderCannotAssignOrComment() {
        String url = taskUrl();
        given().contentType(ContentType.JSON).body(userRef(CARLA))
                .when().post(url + "/assignees").then().statusCode(404);
        given().when().delete(url + "/assignees/" + ANA).then().statusCode(404);
        given().when().get(url + "/comments").then().statusCode(404);
        given().contentType(ContentType.JSON).body("{\"content\":\"Olá\"}")
                .when().post(url + "/comments").then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Colab"),
            @Claim(key = "email", value = "bruno.collab@matriz.test") })
    void commentAuthorAndDateComeFromTheServer() {
        String url = taskUrl();

        given().contentType(ContentType.JSON)
                .body("{\"content\":\"  Primeiro comentário  \"}")
                .when().post(url + "/comments")
                .then().statusCode(201)
                .body("content", equalTo("Primeiro comentário"))
                .body("author.id", equalTo(BRUNO))
                .body("author.name", equalTo("Bruno Colab"))
                .body("createdAt", notNullValue());
        given().contentType(ContentType.JSON).body("{\"content\":\"Segundo\",\"authorId\":\"" + ANA + "\"}")
                .when().post(url + "/comments").then().statusCode(400);

        given().when().get(url + "/comments").then().statusCode(200)
                .body("content", contains("Primeiro comentário"))
                .body("author.id", contains(BRUNO));
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Colab"),
            @Claim(key = "email", value = "bruno.collab@matriz.test") })
    void invalidCommentsAreRejected() {
        String url = taskUrl();
        for (String body : new String[] { "{}", "{\"content\":\"\"}", "{\"content\":\"   \"}",
                "{\"content\":\"" + "c".repeat(2001) + "\"}" }) {
            given().contentType(ContentType.JSON).body(body)
                    .when().post(url + "/comments")
                    .then().statusCode(400)
                    .contentType("application/problem+json");
        }
        given().when().get(url + "/comments").then().body("", empty());
    }
}
