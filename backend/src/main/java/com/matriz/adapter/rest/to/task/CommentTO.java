package com.matriz.adapter.rest.to.task;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.matriz.adapter.rest.to.user.UserSummaryTO;

public record CommentTO(UUID id, UUID taskId, UserSummaryTO author, String content, OffsetDateTime createdAt) {
}
