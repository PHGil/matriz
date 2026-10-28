package pt.matriz.domain.project;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import pt.matriz.domain.shared.ConflictException;
import pt.matriz.domain.shared.ValidationException;

/** Plain unit tests: the domain needs no Quarkus, database or HTTP. */
class ProjectTest {

    @Test
    void createTrimsAndKeepsValidValues() {
        Project project = Project.create("  Website 2027  ", "   ");
        assertEquals("Website 2027", project.name());
        assertNull(project.description(), "blank description is stored as absent");
    }

    @Test
    void createRejectsInvalidNameAndDescription() {
        ValidationException e = assertThrows(ValidationException.class,
                () -> Project.create("ab", "d".repeat(1001)));
        assertEquals(List.of("name", "description"), e.errors().stream().map(ValidationException.FieldError::field).toList());
        assertThrows(ValidationException.class, () -> Project.create("   ", null));
        assertThrows(ValidationException.class, () -> Project.create("x".repeat(101), null));
    }

    @Test
    void changeAppliesOnlyGivenFieldsAndValidatesThem() {
        Project project = Project.create("Intranet", "Portal");

        project.change(null, Optional.empty());
        assertEquals("Intranet", project.name());
        assertNull(project.description());

        project.change(Optional.of("Intranet 2027"), null);
        assertEquals("Intranet 2027", project.name());

        assertThrows(ValidationException.class, () -> project.change(Optional.of(" "), null));
        assertThrows(ValidationException.class, () -> project.change(null, null));
        assertEquals("Intranet 2027", project.name(), "a rejected change leaves the project unchanged");
    }

    @Test
    void managerCannotBeRemovedButMemberCan() {
        UUID projectId = UUID.randomUUID();
        assertThrows(ConflictException.class, () -> ProjectMember.manager(projectId, UUID.randomUUID()).ensureRemovable());
        assertDoesNotThrow(() -> ProjectMember.member(projectId, UUID.randomUUID()).ensureRemovable());
    }
}
