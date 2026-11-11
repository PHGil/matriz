package com.matriz.adapter.rest.to.project;

import java.time.OffsetDateTime;

import com.matriz.adapter.rest.to.user.UserSummaryTO;
import com.matriz.domain.project.ProjectRole;

public record MemberTO(UserSummaryTO user, ProjectRole role, OffsetDateTime addedAt) {
}
