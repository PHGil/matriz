package com.matriz.adapter.rest.to.user;

import java.util.UUID;

public record UserSummaryTO(UUID id, String name, boolean active) {
}
