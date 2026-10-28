package pt.matriz.infrastructure.persistence;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import pt.matriz.domain.project.ProjectMember;
import pt.matriz.domain.project.ProjectRole;

@Entity
@Table(name = "project_member")
public class ProjectMemberEntity {

    @Embeddable
    public record Key(@Column(name = "project_id") UUID projectId, @Column(name = "user_id") UUID userId)
            implements Serializable {
    }

    @EmbeddedId
    Key id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    ProjectRole role;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    OffsetDateTime addedAt;

    ProjectMember toDomain() {
        return new ProjectMember(id.projectId(), id.userId(), role, addedAt);
    }

    static ProjectMemberEntity from(ProjectMember member) {
        ProjectMemberEntity entity = new ProjectMemberEntity();
        entity.id = new Key(member.projectId(), member.userId());
        entity.role = member.role();
        return entity;
    }
}
