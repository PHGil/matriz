package com.matriz.systemtests.test.task;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.matriz.systemtests.database.DatabaseHelper.TaskRow;
import com.matriz.systemtests.test.AbstractBaseTest;

import static com.matriz.systemtests.rest.DemoUser.ANA;
import static com.matriz.systemtests.rest.DemoUser.BRUNO;
import static com.matriz.systemtests.rest.RestHelper.PROBLEM_JSON;
import static com.matriz.systemtests.rest.RestHelper.given;
import static com.matriz.systemtests.rest.StatusCode.BAD_REQUEST;
import static com.matriz.systemtests.rest.StatusCode.CREATED;
import static com.matriz.systemtests.rest.StatusCode.OK;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** US-05 (create a task): POST /projects/{projectId}/tasks and read-back through the API and the database. */
class CreateTaskTest extends AbstractBaseTest {

    @DisplayName("US-05 AC1: a member creates a task with only a title and reads it back")
    @Test
    void testToCreateTaskAndReadItBack() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");
        environmentHelper.addMember(projectId, ANA, BRUNO);

        String id = given(BRUNO).body(Map.of("title", "  Review proposal  "))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(CREATED)
                .header("Location", containsString("/projects/" + projectId + "/tasks/"))
                .body("title", equalTo("Review proposal"))
                .body("status", equalTo("TODO"))
                .body("priority", equalTo("MEDIUM"))
                .body("assignees", empty())
                .extract().path("id");
        UUID taskId = UUID.fromString(id);

        given(ANA).get("/projects/" + projectId + "/tasks/" + taskId)
                .then().statusCode(OK)
                .body("id", equalTo(id))
                .body("projectId", equalTo(projectId.toString()))
                .body("title", equalTo("Review proposal"))
                .body("status", equalTo("TODO"));
        given(ANA).get("/projects/" + projectId + "/tasks")
                .then().statusCode(OK)
                .body("id", hasItem(id));

        TaskRow row = environmentHelper.database.findTask(taskId).orElseThrow();
        assertEquals(projectId, row.projectId());
        assertEquals("Review proposal", row.title());
        assertEquals("TODO", row.status());
        assertEquals("MEDIUM", row.priority());
    }

    @DisplayName("US-05 AC2: an empty, blank or too short/long title is rejected and nothing is saved")
    @ParameterizedTest(name = "title of {0} characters")
    @ValueSource(ints = { 0, 2, 151 })
    void testToCreateTaskWithInvalidTitle(int length) {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        given(ANA).body(Map.of("title", "t".repeat(length)))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(BAD_REQUEST)
                .contentType(PROBLEM_JSON)
                .body("errors.field", hasItem("title"));

        assertEquals(0, environmentHelper.database.countTasks(projectId));
    }

    @DisplayName("US-05 AC2: a whitespace-only title is rejected and nothing is saved")
    @Test
    void testToCreateTaskWithBlankTitle() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        given(ANA).body(Map.of("title", "     "))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(BAD_REQUEST)
                .contentType(PROBLEM_JSON)
                .body("errors.field", hasItem("title"));

        assertEquals(0, environmentHelper.database.countTasks(projectId));
    }

    @DisplayName("US-05 AC2: a title with exactly 3 or 150 characters is accepted")
    @ParameterizedTest(name = "title of {0} characters")
    @ValueSource(ints = { 3, 150 })
    void testToCreateTaskWithTitleOnTheLimits(int length) {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        given(ANA).body(Map.of("title", "t".repeat(length)))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(CREATED);

        assertEquals(1, environmentHelper.database.countTasks(projectId));
    }

    @DisplayName("US-05 AC2: a description longer than 2000 characters is rejected")
    @Test
    void testToCreateTaskWithTooLongDescription() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        given(ANA).body(Map.of("title", "Review proposal", "description", "d".repeat(2001)))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(BAD_REQUEST)
                .contentType(PROBLEM_JSON)
                .body("errors.field", hasItem("description"));

        assertEquals(0, environmentHelper.database.countTasks(projectId));
    }

    @DisplayName("EC-08: a title with HTML is stored and returned as plain text")
    @Test
    void testToCreateTaskWithHtmlTitle() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        String id = given(ANA).body(Map.of("title", "<script>alert(1)</script>"))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(CREATED)
                .body("title", equalTo("<script>alert(1)</script>"))
                .extract().path("id");

        assertEquals("<script>alert(1)</script>",
                environmentHelper.database.findTask(UUID.fromString(id)).orElseThrow().title());
    }

    @DisplayName("US-05 AC4 / EC-09: a due date in the past is accepted")
    @Test
    void testToCreateTaskWithPastDueDate() {
        UUID projectId = environmentHelper.createProject(ANA, "Website 2027");

        given(ANA).body(Map.of("title", "Late task", "dueDate", "2020-01-01"))
                .post("/projects/" + projectId + "/tasks")
                .then().statusCode(CREATED)
                .body("dueDate", equalTo("2020-01-01"));
    }
}
