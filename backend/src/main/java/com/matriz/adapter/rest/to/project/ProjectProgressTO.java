package com.matriz.adapter.rest.to.project;

import java.util.UUID;

public record ProjectProgressTO(UUID projectId, long total, long todo, long inProgress, long done,
        Integer percentDone) {
}
