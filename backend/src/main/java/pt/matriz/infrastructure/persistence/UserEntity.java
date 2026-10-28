package pt.matriz.infrastructure.persistence;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.matriz.domain.user.User;

@Entity
@Table(name = "app_user")
public class UserEntity {

    @Id
    UUID id;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String email;

    @Column(nullable = false)
    boolean active;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    OffsetDateTime updatedAt;

    User toDomain() {
        return new User(id, name, email, active);
    }

    void copyFrom(User user) {
        id = user.id();
        name = user.name();
        email = user.email();
        active = user.isActive();
    }
}
