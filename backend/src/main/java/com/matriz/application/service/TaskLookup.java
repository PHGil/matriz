package com.matriz.application.service;

import java.util.UUID;

import com.matriz.application.port.out.persistence.DomainTaskRepository;
import com.matriz.domain.exception.NotFoundException;
import com.matriz.domain.task.Task;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

/** Loads a task only if it belongs to the project in the URL; otherwise it is "not found" (EC-05). */
@ApplicationScoped
@RequiredArgsConstructor
public class TaskLookup {

    private final DomainTaskRepository taskRepository;

    public Task require(UUID projectId, UUID taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getProjectId().equals(projectId))
                .orElseThrow(NotFoundException::new);
    }
}
