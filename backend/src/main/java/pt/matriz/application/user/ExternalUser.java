package pt.matriz.application.user;

import java.util.UUID;

/** Account data as provided by the identity provider. */
public record ExternalUser(UUID id, String name, String email, boolean active) {
}
