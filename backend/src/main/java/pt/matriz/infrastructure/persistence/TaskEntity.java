package pt.matriz.infrastructure.persistence;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import pt.matriz.domain.task.Task;
import pt.matriz.domain.task.TaskPriority;
import pt.matriz.domain.task.TaskStatus;

@Entity
@Table(name = "task")
public class TaskEntity {

    @Id
    UUID id;

    @Column(name = "project_id", nullable = false, updatable = false)
    UUID projectId;

    @Column(nullable = false, length = 150)
    String title;

    @Column(length = 2000)
    String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    TaskPriority priority;

    @Column(name = "due_date")
    LocalDate dueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    OffsetDateTime updatedAt;

    Task toDomain() {
        return new Task(id, projectId, title, description, status, priority, dueDate, createdAt, updatedAt);
    }

    void copyFrom(Task task) {
        id = task.id();
        projectId = task.projectId();
        title = task.title();
        description = task.description();
        status = task.status();
        priority = task.priority();
        dueDate = task.dueDate();
    }
}
