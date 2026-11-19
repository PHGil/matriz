# Derive the cloud realm from the local definition without importing local accounts.
FROM node:26-alpine AS realm
WORKDIR /realm
COPY matriz-realm.json production-realm.mjs ./
RUN node production-realm.mjs > production-realm.json

FROM quay.io/keycloak/keycloak:26.8.1 AS build
ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
RUN /opt/keycloak/bin/kc.sh build

FROM quay.io/keycloak/keycloak:26.8.1
COPY --from=build /opt/keycloak/ /opt/keycloak/
COPY --from=realm --chown=keycloak:keycloak /realm/production-realm.json /opt/keycloak/data/import/matriz-realm.json
ENV KC_DB=postgres
ENV KC_HEALTH_ENABLED=true
ENV KC_HTTP_ENABLED=true
ENV KC_PROXY_HEADERS=xforwarded
ENV KC_CACHE=local
EXPOSE 10000
ENTRYPOINT ["/opt/keycloak/bin/kc.sh"]
CMD ["start", "--optimized", "--import-realm", "--http-port=10000"]
