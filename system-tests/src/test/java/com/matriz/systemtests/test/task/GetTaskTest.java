package com.matriz.systemtests.test.task;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.matriz.systemtests.test.AbstractBaseTest;

import static com.matriz.systemtests.rest.DemoUser.ANA;
import static com.matriz.systemtests.rest.DemoUser.BRUNO;
import static com.matriz.systemtests.rest.RestHelper.PROBLEM_JSON;
import static com.matriz.systemtests.rest.RestHelper.given;
import static com.matriz.systemtests.rest.StatusCode.NOT_FOUND;
import static com.matriz.systemtests.rest.StatusCode.OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

/** GET /projects/{projectId}/tasks and /{taskId}. */
class GetTaskTest extends AbstractBaseTest {

    @DisplayName("A task that does not exist answers 404 problem+json")
    @Test
    void testToGetUnknownTask() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        given(ANA).get("/projects/" + projectId + "/tasks/" + UUID.randomUUID())
                .then().statusCode(NOT_FOUND)
                .contentType(PROBLEM_JSON)
                .body("status", equalTo(NOT_FOUND));
    }

    @DisplayName("A task is not reachable through the id of another project, even by a member of both")
    @Test
    void testToGetTaskThroughAnotherProject() {
        UUID projectA = environmentHelper.createProject(ANA, "Website 2027");
        UUID projectB = environmentHelper.createProject(ANA, "Intranet");
        UUID taskId = environmentHelper.createTask(projectA, ANA, "Review proposal");

        given(ANA).get("/projects/" + projectB + "/tasks/" + taskId)
                .then().statusCode(NOT_FOUND);
    }

    @DisplayName("The task list only contains the tasks of the requested project")
    @Test
    void testToListTasksOfOneProject() {
        UUID projectA = environmentHelper.createProject(ANA, "Website 2027");
        UUID projectB = environmentHelper.createProject(ANA, "Intranet");
        UUID taskA = environmentHelper.createTask(projectA, ANA, "Review proposal");
        UUID taskB = environmentHelper.createTask(projectB, ANA, "Write manual");

        given(ANA).get("/projects/" + projectA + "/tasks")
                .then().statusCode(OK)
                .body("id", hasItem(taskA.toString()))
                .body("id", not(hasItem(taskB.toString())));
    }

    @DisplayName("A member added after the task was created can read it")
    @Test
    void testToGetTaskAsLaterMember() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");
        UUID taskId = environmentHelper.createTask(projectId, ANA, "Review proposal");
        environmentHelper.addMember(projectId, ANA, BRUNO);

        given(BRUNO).get("/projects/" + projectId + "/tasks/" + taskId)
                .then().statusCode(OK)
                .body("title", equalTo("Review proposal"));
    }
}
