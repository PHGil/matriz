package pt.matriz.projects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
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
import pt.matriz.users.UserService;

@QuarkusTest
class ProjectResourceTest {

    static final String ANA = "0f8b2c7e-3333-4000-8000-000000000011";
    static final String BRUNO = "0f8b2c7e-3333-4000-8000-000000000012";
    static final String CARLA = "0f8b2c7e-3333-4000-8000-000000000013";

    @Inject
    UserService users;

    @Inject
    ProjectService projects;

    @Inject
    MemberService members;

    @Inject
    EntityManager em;

    @BeforeEach
    void users() {
        users.upsertFromToken(UUID.fromString(ANA), "Ana Teste", "ana.projects@matriz.test");
        users.upsertFromToken(UUID.fromString(BRUNO), "Bruno Teste", "bruno.projects@matriz.test");
        users.upsertFromToken(UUID.fromString(CARLA), "Carla Teste", "carla.projects@matriz.test");
    }

    /** Project managed by Ana with Bruno as member; Carla is an outsider. */
    private UUID sharedProject(String name) {
        UUID id = projects.create(UUID.fromString(ANA), new ProjectDtos.ProjectCreate(name, null)).id();
        members.add(id, UUID.fromString(ANA), new ProjectDtos.UserRef(UUID.fromString(BRUNO)));
        return id;
    }

    @Test
    @TestSecurity(user = "ana")
    @OidcSecurity(claims = { @Claim(key = "sub", value = ANA), @Claim(key = "name", value = "Ana Teste"),
            @Claim(key = "email", value = "ana.projects@matriz.test") })
    void createThenReadBackPersistsProjectWithCreatorAsManager() {
        String id = given().contentType(ContentType.JSON)
                .body("{\"name\":\"  Website 2027  \",\"description\":\"Site\"}")
                .when().post("/api/v1/projects")
                .then().statusCode(201)
                .header("Location", startsWith("http"))
                .body("name", equalTo("Website 2027"))
                .body("myRole", equalTo("MANAGER"))
                .extract().path("id");

        given().when().get("/api/v1/projects/" + id)
                .then().statusCode(200)
                .body("name", equalTo("Website 2027"))
                .body("description", equalTo("Site"));

        given().when().get("/api/v1/projects")
                .then().statusCode(200)
                .body("find { it.id == '" + id + "' }.counts.total", equalTo(0));

        String role = QuarkusTransaction.requiringNew().call(() -> (String) em
                .createNativeQuery("select role from project_member where project_id = ?1 and user_id = ?2")
                .setParameter(1, UUID.fromString(id)).setParameter(2, UUID.fromString(ANA))
                .getSingleResult());
        assertEquals("MANAGER", role);
    }

    @Test
    @TestSecurity(user = "ana")
    @OidcSecurity(claims = { @Claim(key = "sub", value = ANA), @Claim(key = "name", value = "Ana Teste"),
            @Claim(key = "email", value = "ana.projects@matriz.test") })
    void invalidNamesAreRejectedWithFieldErrors() {
        for (String body : new String[] { "{\"name\":\"   \"}", "{\"name\":\"ab\"}", "{}",
                "{\"name\":\"" + "x".repeat(101) + "\"}" }) {
            given().contentType(ContentType.JSON).body(body)
                    .when().post("/api/v1/projects")
                    .then().statusCode(400)
                    .contentType("application/problem+json")
                    .body("errors.field", hasItem("name"));
        }
        given().contentType(ContentType.JSON)
                .body("{\"name\":\"Valid\",\"description\":\"" + "d".repeat(1001) + "\"}")
                .when().post("/api/v1/projects")
                .then().statusCode(400)
                .body("errors.field", hasItem("description"));
        for (String body : new String[] { "{\"name\":", "{\"name\":\"Valid\",\"owner\":\"x\"}" }) {
            given().contentType(ContentType.JSON).body(body)
                    .when().post("/api/v1/projects")
                    .then().statusCode(400)
                    .contentType("application/problem+json");
        }
    }

    @Test
    @TestSecurity(user = "carla")
    @OidcSecurity(claims = { @Claim(key = "sub", value = CARLA), @Claim(key = "name", value = "Carla Teste"),
            @Claim(key = "email", value = "carla.projects@matriz.test") })
    void outsiderCannotSeeOrChangeProjectEvenWithItsId() {
        UUID id = sharedProject("Privado");

        given().when().get("/api/v1/projects").then().statusCode(200)
                .body("id", not(hasItem(id.toString())));
        given().when().get("/api/v1/projects/" + id).then().statusCode(404)
                .contentType("application/problem+json");
        given().contentType(ContentType.JSON).body("{\"name\":\"Hack\"}")
                .when().patch("/api/v1/projects/" + id).then().statusCode(404);
        given().when().delete("/api/v1/projects/" + id).then().statusCode(404);
        given().when().get("/api/v1/projects/" + id + "/members").then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "bruno")
    @OidcSecurity(claims = { @Claim(key = "sub", value = BRUNO), @Claim(key = "name", value = "Bruno Teste"),
            @Claim(key = "email", value = "bruno.projects@matriz.test") })
    void memberCanReadButNotManage() {
        UUID id = sharedProject("Equipa");

        given().when().get("/api/v1/projects/" + id).then().statusCode(200)
                .body("myRole", equalTo("MEMBER"));
        given().contentType(ContentType.JSON).body("{\"name\":\"Renomeado\"}")
                .when().patch("/api/v1/projects/" + id).then().statusCode(403);
        given().when().delete("/api/v1/projects/" + id).then().statusCode(403);
        given().contentType(ContentType.JSON).body("{\"userId\":\"" + CARLA + "\"}")
                .when().post("/api/v1/projects/" + id + "/members").then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "ana")
    @OidcSecurity(claims = { @Claim(key = "sub", value = ANA), @Claim(key = "name", value = "Ana Teste"),
            @Claim(key = "email", value = "ana.projects@matriz.test") })
    void managerEditsSearchesAndManagesMembers() {
        UUID id = sharedProject("Intranet Nova");

        given().contentType(ContentType.JSON).body("{\"name\":\"Intranet 2027\",\"description\":null}")
                .when().patch("/api/v1/projects/" + id).then().statusCode(200)
                .body("name", equalTo("Intranet 2027"));
        given().queryParam("q", "intranet 20").when().get("/api/v1/projects").then().statusCode(200)
                .body("id", hasItem(id.toString()));
        given().queryParam("q", "%").when().get("/api/v1/projects").then().statusCode(200)
                .body("id", not(hasItem(id.toString())));

        given().contentType(ContentType.JSON).body("{\"userId\":\"" + BRUNO + "\"}")
                .when().post("/api/v1/projects/" + id + "/members").then().statusCode(409);
        given().when().delete("/api/v1/projects/" + id + "/members/" + ANA).then().statusCode(409);
        given().when().get("/api/v1/projects/" + id + "/members").then().statusCode(200)
                .body("user.id", contains(ANA, BRUNO));

        UUID task = insertTaskAssignedTo(id, BRUNO);
        given().when().delete("/api/v1/projects/" + id + "/members/" + BRUNO).then().statusCode(204);
        assertEquals(0L, count("select count(*) from task_assignee where task_id = ?1", task));
    }

    @Test
    @TestSecurity(user = "ana")
    @OidcSecurity(claims = { @Claim(key = "sub", value = ANA), @Claim(key = "name", value = "Ana Teste"),
            @Claim(key = "email", value = "ana.projects@matriz.test") })
    void deletingProjectRemovesDependentRows() {
        UUID id = sharedProject("Para apagar");
        UUID task = insertTaskAssignedTo(id, BRUNO);

        given().when().delete("/api/v1/projects/" + id).then().statusCode(204);

        given().when().get("/api/v1/projects/" + id).then().statusCode(404);
        assertEquals(0L, count("select count(*) from project_member where project_id = ?1", id));
        assertEquals(0L, count("select count(*) from task where id = ?1", task));
        assertEquals(0L, count("select count(*) from task_assignee where task_id = ?1", task));
    }

    private UUID insertTaskAssignedTo(UUID projectId, String userId) {
        UUID taskId = UUID.randomUUID();
        QuarkusTransaction.requiringNew().run(() -> {
            em.createNativeQuery("insert into task (id, project_id, title) values (?1, ?2, 'Tarefa')")
                    .setParameter(1, taskId).setParameter(2, projectId).executeUpdate();
            em.createNativeQuery("insert into task_assignee (task_id, user_id) values (?1, ?2)")
                    .setParameter(1, taskId).setParameter(2, UUID.fromString(userId)).executeUpdate();
        });
        return taskId;
    }

    private long count(String sql, UUID param) {
        return QuarkusTransaction.requiringNew().call(() -> ((Number) em.createNativeQuery(sql)
                .setParameter(1, param).getSingleResult()).longValue());
    }
}
