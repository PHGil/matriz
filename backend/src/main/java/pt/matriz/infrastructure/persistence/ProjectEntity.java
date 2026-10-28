package pt.matriz.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.matriz.domain.project.Project;

@Entity
@Table(name = "project")
public class ProjectEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(length = 1000)
    String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    OffsetDateTime updatedAt;

    Project toDomain() {
        return new Project(id, name, description, createdAt, updatedAt);
    }

    void copyFrom(Project project) {
        id = project.id();
        name = project.name();
        description = project.description();
    }
}
