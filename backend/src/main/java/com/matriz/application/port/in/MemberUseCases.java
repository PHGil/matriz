package com.matriz.application.port.in;

import java.util.List;
import java.util.UUID;

import com.matriz.domain.project.MemberDetails;

/** Input port: project membership use cases. */
public interface MemberUseCases {

    /** Manager first, then by name. Also the list of eligible assignees (FR-24). */
    List<MemberDetails> list(UUID projectId, UUID userId);

    MemberDetails add(UUID projectId, UUID userId, UUID newMemberId);

    /** Also removes the member's task assignments in this project (EC-02, BR-06). */
    void remove(UUID projectId, UUID userId, UUID memberId);
}
