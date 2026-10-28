package pt.matriz.projects;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "project_member")
public class ProjectMember extends PanacheEntityBase {

    @Embeddable
    public record Key(@Column(name = "project_id") UUID projectId, @Column(name = "user_id") UUID userId)
            implements Serializable {
    }

    @EmbeddedId
    public Key id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    public ProjectRole role;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    public OffsetDateTime addedAt;

    public static ProjectMember find(UUID projectId, UUID userId) {
        return findById(new Key(projectId, userId));
    }
}
