package com.matriz.systemtests.test.project;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.matriz.systemtests.test.AbstractBaseTest;

import static com.matriz.systemtests.rest.DemoUser.ANA;
import static com.matriz.systemtests.rest.DemoUser.BRUNO;
import static com.matriz.systemtests.rest.DemoUser.CARLA;
import static com.matriz.systemtests.rest.RestHelper.given;
import static com.matriz.systemtests.rest.StatusCode.CREATED;
import static com.matriz.systemtests.rest.StatusCode.FORBIDDEN;
import static com.matriz.systemtests.rest.StatusCode.NOT_FOUND;
import static com.matriz.systemtests.rest.StatusCode.NO_CONTENT;
import static com.matriz.systemtests.rest.StatusCode.OK;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Project-level authorisation between demo users: Ana is Manager, Bruno a member, Carla not a member (IDOR). */
class ProjectAuthorizationTest extends AbstractBaseTest {

    private UUID projectId;
    private UUID taskId;
    private String projectUrl;
    private String taskUrl;

    private void givenProjectWithTask() {
        projectId = environmentHelper.createProject(ANA, "Website 2027");
        environmentHelper.addMember(projectId, ANA, BRUNO);
        taskId = environmentHelper.createTask(projectId, ANA, "Review proposal");
        projectUrl = "/projects/" + projectId;
        taskUrl = projectUrl + "/tasks/" + taskId;
    }

    @DisplayName("US-03 AC3 / EC-05: a non-member does not see the project and gets 404 by direct id")
    @Test
    void testNonMemberCannotSeeProject() {
        givenProjectWithTask();

        given(CARLA).get("/projects").then().statusCode(OK).body("id", not(hasItem(projectId.toString())));
        given(CARLA).get(projectUrl).then().statusCode(NOT_FOUND);
        given(CARLA).get(projectUrl + "/progress").then().statusCode(NOT_FOUND);
        given(CARLA).get(projectUrl + "/members").then().statusCode(NOT_FOUND);
    }

    @DisplayName("A non-member cannot edit or delete the project and nothing changes")
    @Test
    void testNonMemberCannotChangeProject() {
        givenProjectWithTask();

        given(CARLA).body(Map.of("name", "Hacked")).patch(projectUrl).then().statusCode(NOT_FOUND);
        given(CARLA).delete(projectUrl).then().statusCode(NOT_FOUND);

        assertEquals("Website 2027", environmentHelper.database.projectName(projectId));
        assertEquals(1, environmentHelper.database.countProjects(projectId));
    }

    @DisplayName("US-04 AC5: a non-member cannot add or remove members")
    @Test
    void testNonMemberCannotManageMembers() {
        givenProjectWithTask();
        var carlaId = environmentHelper.userId(CARLA);

        given(CARLA).body(Map.of("userId", carlaId)).post(projectUrl + "/members").then().statusCode(NOT_FOUND);
        given(CARLA).delete(projectUrl + "/members/" + environmentHelper.userId(BRUNO)).then()
                .statusCode(NOT_FOUND);

        assertEquals(2, environmentHelper.database.countMembers(projectId));
    }

    @DisplayName("A non-member cannot comment on or assign people to a task")
    @Test
    void testNonMemberCannotCommentOrAssign() {
        givenProjectWithTask();

        given(CARLA).get(taskUrl + "/comments").then().statusCode(NOT_FOUND);
        given(CARLA).body(Map.of("content", "Intruder")).post(taskUrl + "/comments").then().statusCode(NOT_FOUND);
        given(CARLA).body(Map.of("userId", environmentHelper.userId(CARLA))).post(taskUrl + "/assignees")
                .then().statusCode(NOT_FOUND);

        assertEquals(0, environmentHelper.database.countComments(taskId));
        assertEquals(0, environmentHelper.database.countAssignees(taskId));
    }

    @DisplayName("US-04 AC5: a member who is not the Manager cannot edit or delete the project or manage members (403)")
    @Test
    void testMemberCannotManageProject() {
        givenProjectWithTask();

        given(BRUNO).body(Map.of("name", "Renamed")).patch(projectUrl).then().statusCode(FORBIDDEN);
        given(BRUNO).delete(projectUrl).then().statusCode(FORBIDDEN);
        given(BRUNO).body(Map.of("userId", environmentHelper.userId(CARLA))).post(projectUrl + "/members")
                .then().statusCode(FORBIDDEN);
        given(BRUNO).delete(projectUrl + "/members/" + environmentHelper.userId(ANA)).then()
                .statusCode(FORBIDDEN);

        assertEquals("Website 2027", environmentHelper.database.projectName(projectId));
        assertEquals(2, environmentHelper.database.countMembers(projectId));
    }

    @DisplayName("US-13: a member can comment on a task of the project")
    @Test
    void testMemberCanComment() {
        givenProjectWithTask();

        given(BRUNO).body(Map.of("content", "Looks good")).post(taskUrl + "/comments").then().statusCode(CREATED);

        assertEquals(1, environmentHelper.database.countComments(taskId));
    }

    @DisplayName("US-04 AC1: after the Manager adds Carla, she sees the project; she loses access when removed")
    @Test
    void testAccessFollowsMembership() {
        givenProjectWithTask();
        environmentHelper.addMember(projectId, ANA, CARLA);

        given(CARLA).get(projectUrl).then().statusCode(OK);

        given(ANA).delete(projectUrl + "/members/" + environmentHelper.userId(CARLA)).then().statusCode(NO_CONTENT);

        given(CARLA).get(projectUrl).then().statusCode(NOT_FOUND);
    }
}
