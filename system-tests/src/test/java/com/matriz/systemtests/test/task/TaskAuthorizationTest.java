package com.matriz.systemtests.test.task;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.matriz.systemtests.test.AbstractBaseTest;

import static com.matriz.systemtests.rest.DemoUser.ANA;
import static com.matriz.systemtests.rest.DemoUser.BRUNO;
import static com.matriz.systemtests.rest.DemoUser.CARLA;
import static com.matriz.systemtests.rest.RestHelper.given;
import static com.matriz.systemtests.rest.StatusCode.FORBIDDEN;
import static com.matriz.systemtests.rest.StatusCode.NOT_FOUND;
import static com.matriz.systemtests.rest.StatusCode.NO_CONTENT;
import static com.matriz.systemtests.rest.StatusCode.OK;
import static com.matriz.systemtests.rest.StatusCode.UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Authorisation over tasks between demo users (Broken Access Control / IDOR): Ana manages the project, Bruno is a
 * member and Carla is not a member.
 */
class TaskAuthorizationTest extends AbstractBaseTest {

    private UUID projectId;
    private UUID taskId;
    private String taskUrl;

    private void givenProjectWithTask() {
        projectId = environmentHelper.createProject(ANA, "Website 2027");
        environmentHelper.addMember(projectId, ANA, BRUNO);
        taskId = environmentHelper.createTask(projectId, ANA, "Review proposal");
        taskUrl = "/projects/" + projectId + "/tasks/" + taskId;
    }

    @DisplayName("EC-05: a non-member cannot read the tasks of the project, even knowing the ids")
    @Test
    void testNonMemberCannotReadTasks() {
        givenProjectWithTask();

        given(CARLA).get("/projects/" + projectId + "/tasks").then().statusCode(NOT_FOUND);
        given(CARLA).get(taskUrl).then().statusCode(NOT_FOUND);
    }

    @DisplayName("US-05: a non-member cannot create a task and nothing is saved")
    @Test
    void testNonMemberCannotCreateTask() {
        givenProjectWithTask();

        given(CARLA).body(Map.of("title", "Intruder task"))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(NOT_FOUND);

        assertEquals(1, environmentHelper.database.countTasks(projectId));
    }

    @DisplayName("US-06 AC3: a non-member cannot change the status of a task and the task is unchanged")
    @Test
    void testNonMemberCannotChangeStatus() {
        givenProjectWithTask();

        given(CARLA).body(Map.of("status", "DONE")).patch(taskUrl).then().statusCode(NOT_FOUND);

        assertEquals("TODO", environmentHelper.database.findTask(taskId).orElseThrow().status());
    }

    @DisplayName("US-08 AC3: a non-member cannot delete a task")
    @Test
    void testNonMemberCannotDeleteTask() {
        givenProjectWithTask();

        given(CARLA).delete(taskUrl).then().statusCode(NOT_FOUND);

        assertTrue(environmentHelper.database.findTask(taskId).isPresent());
    }

    @DisplayName("IDOR: Carla cannot reach Ana's task through her own project id")
    @Test
    void testTaskIsNotReachableThroughForeignProjectId() {
        givenProjectWithTask();
        UUID carlaProject = environmentHelper.createProject(CARLA, "Campanha de Natal");

        given(CARLA).get("/projects/" + carlaProject + "/tasks/" + taskId).then().statusCode(NOT_FOUND);
        given(CARLA).body(Map.of("status", "DONE")).patch("/projects/" + carlaProject + "/tasks/" + taskId)
                .then().statusCode(NOT_FOUND);
        given(CARLA).delete("/projects/" + carlaProject + "/tasks/" + taskId).then().statusCode(NOT_FOUND);

        assertEquals("TODO", environmentHelper.database.findTask(taskId).orElseThrow().status());
    }

    @DisplayName("US-06 AC1: a member can change the status of a task")
    @Test
    void testMemberCanChangeStatus() {
        givenProjectWithTask();

        given(BRUNO).body(Map.of("status", "IN_PROGRESS")).patch(taskUrl).then().statusCode(OK);

        assertEquals("IN_PROGRESS", environmentHelper.database.findTask(taskId).orElseThrow().status());
    }

    @DisplayName("US-08 AC3: a member who is not the Manager cannot delete a task (403)")
    @Test
    void testMemberCannotDeleteTask() {
        givenProjectWithTask();

        given(BRUNO).delete(taskUrl).then().statusCode(FORBIDDEN);

        assertTrue(environmentHelper.database.findTask(taskId).isPresent());
    }

    @DisplayName("US-08 AC1: the Manager can delete a task")
    @Test
    void testManagerCanDeleteTask() {
        givenProjectWithTask();

        given(ANA).delete(taskUrl).then().statusCode(NO_CONTENT);

        assertTrue(environmentHelper.database.findTask(taskId).isEmpty());
    }

    @DisplayName("US-01: requests without a token or with a malformed token are refused")
    @Test
    void testRequestsWithoutValidTokenAreRefused() {
        givenProjectWithTask();

        given().get(taskUrl).then().statusCode(UNAUTHORIZED);
        given().header("Authorization", "Bearer abc.def.ghi").get(taskUrl).then().statusCode(UNAUTHORIZED);
    }
}
