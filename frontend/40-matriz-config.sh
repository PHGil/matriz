#!/bin/sh
set -eu

# Only an origin is accepted, so the value can safely be embedded in JavaScript.
if [ "$(printf '%s' "$KEYCLOAK_URL" | wc -l)" -ne 0 ] ||
    ! printf '%s\n' "$KEYCLOAK_URL" | grep -Eq '^https?://[A-Za-z0-9.-]+(:[0-9]+)?$'; then
    echo 'KEYCLOAK_URL must be an HTTP(S) origin without a trailing slash' >&2
    exit 1
fi

printf 'window.matrizConfig = { keycloakUrl: "%s" };\n' "$KEYCLOAK_URL" \
    > /usr/share/nginx/html/config.js
