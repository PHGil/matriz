package pt.matriz.infrastructure.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import pt.matriz.application.project.MemberService;
import pt.matriz.application.project.ProjectService;
import pt.matriz.application.task.TaskService;
import pt.matriz.application.user.UserService;
import pt.matriz.domain.task.TaskPriority;

@QuarkusTest
class TaskResourceTest {

    static final String ANA = "0f8b2c7e-4444-4000-8000-000000000011";
    static final String BRUNO = "0f8b2c7e-4444-4000-8000-000000000012";
    static final String CARLA = "0f8b2c7e-4444-4000-8000-000000000013";

    @Inject
    UserService users;

    @Inject
    ProjectService projects;

    @Inject
    MemberService members;

    @Inject
    TaskService tasks;

    @Inject
    EntityManager em;

    @BeforeEach
    void users() {
        users.registerLogin(UUID.fromString(ANA), "Ana Tarefas", "ana.tasks@matriz.test");
        users.registerLogin(UUID.fromString(BRUNO), "Bruno Tarefas", "bruno.tasks@matriz.test");
        users.registerLogin(UUID.fromString(CARLA), "Carla Tarefas", "carla.tasks@matriz.test");
    }

    /** Project managed by Ana with Bruno as member; Carla is an outsider. */
    UUID project() {
        UUID id = projects.create(UUID.fromString(ANA), "Projeto " + UUID.randomUUID(), null).project().id();
        members.add(id, UUID.fromString(ANA), UUID.fromString(BRUNO));
        return id;
    }

    UUID task(UUID projectId, String title) {
        return tasks.create(projectId, UUID.fromString(ANA), title, null, null, null).task().id();
    }

    String tasksUrl(UUID projectId) {
        return "/api/v1/projects/" + projectId + "/tasks";
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void memberCreatesTaskAndReadsItBack() {
        UUID projectId = project();

        String id = given().contentType(ContentType.JSON).body("{\"title\":\"  Rever proposta  \"}")
                .when().post(tasksUrl(projectId))
                .then().statusCode(201)
                .body("title", equalTo("Rever proposta"))
                .body("status", equalTo("TODO"))
                .body("priority", equalTo("MEDIUM"))
                .body("assignees", empty())
                .extract().path("id");

        given().when().get(tasksUrl(projectId) + "/" + id).then().statusCode(200)
                .body("title", equalTo("Rever proposta"));
        given().when().get(tasksUrl(projectId)).then().statusCode(200)
                .body("id", hasItem(id));

        String status = QuarkusTransaction.requiringNew().call(() -> (String) em
                .createNativeQuery("select status from task where id = ?1")
                .setParameter(1, UUID.fromString(id)).getSingleResult());
        assertEquals("TODO", status);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void invalidTaskDataIsRejected() {
        UUID projectId = project();
        for (String body : new String[] { "{\"title\":\"   \"}", "{\"title\":\"ab\"}",
                "{\"title\":\"" + "x".repeat(151) + "\"}",
                "{\"title\":\"Valida\",\"description\":\"" + "d".repeat(2001) + "\"}" }) {
            given().contentType(ContentType.JSON).body(body)
                    .when().post(tasksUrl(projectId))
                    .then().statusCode(400)
                    .contentType("application/problem+json");
        }
        given().contentType(ContentType.JSON).body("{\"title\":\"Valida\",\"priority\":\"URGENT\"}")
                .when().post(tasksUrl(projectId))
                .then().statusCode(400);
        given().contentType(ContentType.JSON).body("{\"title\":\"Atrasada\",\"dueDate\":\"2020-01-01\"}")
                .when().post(tasksUrl(projectId))
                .then().statusCode(201)
                .body("dueDate", equalTo("2020-01-01"));
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void memberChangesStatusAndEditsTask() {
        UUID projectId = project();
        UUID taskId = task(projectId, "Maquetes");
        String url = tasksUrl(projectId) + "/" + taskId;
        String createdUpdatedAt = given().when().get(url).then().extract().path("updatedAt");

        given().contentType(ContentType.JSON).body("{\"status\":\"IN_PROGRESS\"}")
                .when().patch(url).then().statusCode(200)
                .body("status", equalTo("IN_PROGRESS"))
                .body("title", equalTo("Maquetes"))
                .body("updatedAt", not(equalTo(createdUpdatedAt)));

        given().contentType(ContentType.JSON).body("{\"status\":\"DONE\"}").when().patch(url).then().statusCode(200);
        given().contentType(ContentType.JSON).body("{\"status\":\"TODO\"}")
                .when().patch(url).then().statusCode(200).body("status", equalTo("TODO"));

        given().contentType(ContentType.JSON)
                .body("{\"title\":\"Maquetes v2\",\"priority\":\"HIGH\",\"dueDate\":\"2026-12-01\",\"description\":\"Rever\"}")
                .when().patch(url).then().statusCode(200)
                .body("title", equalTo("Maquetes v2"))
                .body("priority", equalTo("HIGH"))
                .body("dueDate", equalTo("2026-12-01"));
        given().contentType(ContentType.JSON).body("{\"dueDate\":null,\"description\":null}")
                .when().patch(url).then().statusCode(200)
                .body("dueDate", nullValue())
                .body("description", nullValue())
                .body("title", equalTo("Maquetes v2"));
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void invalidTaskChangesAreRejected() {
        UUID projectId = project();
        String url = tasksUrl(projectId) + "/" + task(projectId, "Tarefa");
        for (String body : new String[] { "{}", "{\"status\":null}", "{\"status\":\"CLOSED\"}",
                "{\"priority\":null}", "{\"title\":\"  \"}" }) {
            given().contentType(ContentType.JSON).body(body)
                    .when().patch(url).then().statusCode(400);
        }
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void progressReflectsStatusChanges() {
        UUID projectId = project();
        String progressUrl = "/api/v1/projects/" + projectId + "/progress";
        given().when().get(progressUrl).then().statusCode(200)
                .body("total", equalTo(0))
                .body("percentDone", nullValue());

        task(projectId, "Primeira");
        task(projectId, "Segunda");
        UUID done = task(projectId, "Terceira");
        given().contentType(ContentType.JSON).body("{\"status\":\"DONE\"}")
                .when().patch(tasksUrl(projectId) + "/" + done).then().statusCode(200);

        given().when().get(progressUrl).then().statusCode(200)
                .body("total", equalTo(3))
                .body("todo", equalTo(2))
                .body("inProgress", equalTo(0))
                .body("done", equalTo(1))
                .body("percentDone", equalTo(33));
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void filtersByStatusAndPriorityWithoutAffectingProgress() {
        UUID projectId = project();
        UUID high = tasks.create(projectId, UUID.fromString(ANA), "Alta", null,
                TaskPriority.HIGH, null).task().id();
        UUID low = tasks.create(projectId, UUID.fromString(ANA), "Baixa", null,
                TaskPriority.LOW, null).task().id();
        given().contentType(ContentType.JSON).body("{\"status\":\"DONE\"}")
                .when().patch(tasksUrl(projectId) + "/" + low).then().statusCode(200);

        given().queryParam("priority", "HIGH").when().get(tasksUrl(projectId)).then().statusCode(200)
                .body("id", contains(high.toString()));
        given().queryParam("status", "DONE").when().get(tasksUrl(projectId)).then().statusCode(200)
                .body("id", contains(low.toString()));
        given().queryParam("status", "DONE").queryParam("priority", "HIGH").when().get(tasksUrl(projectId))
                .then().statusCode(200).body("id", empty());
        given().queryParam("status", "CLOSED").when().get(tasksUrl(projectId)).then().statusCode(400)
                .body("errors.field", hasItem("status"));

        given().when().get("/api/v1/projects/" + projectId + "/progress").then()
                .body("total", equalTo(2))
                .body("done", equalTo(1));
    }

    @Test
    @TestSecurity(user = "ana")
    @OidcSecurity(claims = { @Claim(key = "sub", value = ANA), @Claim(key = "name", value = "Ana Tarefas"),
            @Claim(key = "email", value = "ana.tasks@matriz.test") })
    void managerDeletesTaskAndCountsFollow() {
        UUID projectId = project();
        task(projectId, "Fica");
        UUID taskId = task(projectId, "Sai");

        given().when().delete(tasksUrl(projectId) + "/" + taskId).then().statusCode(204);

        given().when().get(tasksUrl(projectId) + "/" + taskId).then().statusCode(404);
        given().when().get("/api/v1/projects/" + projectId + "/progress").then().body("total", equalTo(1));
        given().when().delete(tasksUrl(projectId) + "/" + taskId).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void memberCannotDeleteTask() {
        UUID projectId = project();
        UUID taskId = task(projectId, "Protegida");

        given().when().delete(tasksUrl(projectId) + "/" + taskId).then().statusCode(403);
        given().when().get(tasksUrl(projectId) + "/" + taskId).then().statusCode(200);
    }

    @Test
    @TestSecurity(user = "carla")
    @OidcSecurity(claims = { @Claim(key = "sub", value = CARLA), @Claim(key = "name", value = "Carla Tarefas"),
            @Claim(key = "email", value = "carla.tasks@matriz.test") })
    void outsiderCannotSeeProgress() {
        given().when().get("/api/v1/projects/" + project() + "/progress").then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "carla")
    @OidcSecurity(claims = { @Claim(key = "sub", value = CARLA), @Claim(key = "name", value = "Carla Tarefas"),
            @Claim(key = "email", value = "carla.tasks@matriz.test") })
    void outsiderCannotChangeTaskStatus() {
        UUID projectId = project();
        String url = tasksUrl(projectId) + "/" + task(projectId, "Tarefa");
        given().contentType(ContentType.JSON).body("{\"status\":\"DONE\"}")
                .when().patch(url).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "carla")
    @OidcSecurity(claims = { @Claim(key = "sub", value = CARLA), @Claim(key = "name", value = "Carla Tarefas"),
            @Claim(key = "email", value = "carla.tasks@matriz.test") })
    void outsiderCannotReadOrCreateTasks() {
        UUID projectId = project();
        UUID taskId = task(projectId, "Privada");

        given().when().get(tasksUrl(projectId)).then().statusCode(404);
        given().when().get(tasksUrl(projectId) + "/" + taskId).then().statusCode(404);
        given().contentType(ContentType.JSON).body("{\"title\":\"Intrusa\"}")
                .when().post(tasksUrl(projectId)).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Tarefas"),
            @Claim(key = "email", value = "bruno.tasks@matriz.test") })
    void taskIsNotFoundThroughAnotherProjectId() {
        UUID projectA = project();
        UUID projectB = project();
        UUID taskOfA = task(projectA, "Tarefa de A");

        given().when().get(tasksUrl(projectB) + "/" + taskOfA).then().statusCode(404);
    }
}
