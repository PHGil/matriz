package com.matriz.adapter.rest.to.user;

import java.util.UUID;

public record CurrentUserTO(UUID id, String name, String email) {
}
