# System tests

Black-box tests of the REST API against a **running backend** (profile `st`) with real PostgreSQL and real Keycloak tokens. They follow the layout of the SEPIA system tests: scenarios in `test/`, fixtures in `database/`, HTTP in `rest/`.

```
src/test/java/com/matriz/systemtests/
├── test/                 # scenarios (*Test, run by failsafe), grouped by domain
│   ├── AbstractBaseTest  # @BeforeEach: truncates matriz_st and registers the demo users
│   ├── task/             # CreateTaskTest, GetTaskTest, TaskAuthorizationTest
│   └── project/          # ProjectAuthorizationTest
├── database/             # DatabaseHelper (JDBC: truncate + persistence asserts), EnvironmentHelper (fixtures through the API)
├── rest/                 # RestHelper (REST Assured + bearer token), AuthTokenProvider (Keycloak password grant), DemoUser, StatusCode
└── utils/                # ConfigHelper (URLs and credentials from the environment)
```

| Actor                | Responsibility                                                                                                              |
| -------------------- | --------------------------------------------------------------------------------------------------------------------------- |
| Test class           | Arrange with `EnvironmentHelper` (real API calls) → act with `given(user)` → assert on the response and on the database.   |
| `AbstractBaseTest`   | Isolation: every test starts from an empty `matriz_st` (`TRUNCATE` of the application tables) with Ana, Bruno and Carla registered. |
| `AuthTokenProvider`  | Tokens for `ana`, `bruno`, `carla` from Keycloak (client `matriz-system-tests`, password grant), cached until near expiry.   |
| `DatabaseHelper`     | Direct JDBC access to `matriz_st` only (the URL is fixed; it never touches the `matriz` development database).               |

## Prerequisites

- `docker compose up -d` and the `.env` from `.env.example`.
- Database `matriz_st` (once): `docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE matriz_st"'`.
- Keycloak client `matriz-system-tests`: included in [keycloak/matriz-realm.json](../keycloak/matriz-realm.json), imported on a fresh realm. For an existing realm, create it once without wiping data: `docker compose exec -T keycloak sh -c '/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8180 --realm master --user "$KC_BOOTSTRAP_ADMIN_USERNAME" --password "$KC_BOOTSTRAP_ADMIN_PASSWORD"'`, then `kcadm.sh create clients -r matriz -f -` fed with that client's JSON.
- Backend with profile `st` (no demo data, database `matriz_st`) on port 8080 — stop the dev backend first: `cd backend && ./mvnw quarkus:dev -Dquarkus.profile=st`.

## Run

```bash
cd backend && (set -a; . ../.env; set +a; ./mvnw -f ../system-tests/pom.xml verify)
```

The subshell loads `.env` only for this command. `MATRIZ_API_URL` (default `http://localhost:8080/api/v1`) and `KEYCLOAK_URL` (default `http://localhost:8180`) can be overridden. Add `-s <settings.xml>` if the local Maven settings require it.

## Adding a scenario

Extend `AbstractBaseTest`, build the state with `environmentHelper`, call the API with `given(DemoUser)` and assert both the HTTP result and the database. If a new application table is added, add it to `TRUNCATE_ALL` in `DatabaseHelper` (and to `resetDB` in `frontend/e2e/helpers/general.ts`).
