// Single place for backend and Keycloak addresses (IMP-07). In dev, /api is proxied to the Quarkus backend.
export const appSettings = {
  apiBaseUrl: '/api/v1',
  keycloak: {
    url: 'http://localhost:8180',
    realm: 'matriz',
    clientId: 'matriz-frontend',
  },
} as const;
