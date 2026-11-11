package com.matriz.adapter.persistence.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.matriz.domain.project.ProjectRole;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "project_member")
@Getter
@Setter
@NoArgsConstructor
public class ProjectMemberEntity {

    @Embeddable
    public record Key(@Column(name = "project_id") UUID projectId, @Column(name = "user_id") UUID userId)
            implements Serializable {
    }

    @EmbeddedId
    private Key id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ProjectRole role;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private OffsetDateTime addedAt;
}
