declare global {
  interface Window {
    matrizConfig?: { keycloakUrl: string };
  }
}

// Local default also supports unit tests, which do not load index.html.
export const appSettings = {
  apiBaseUrl: '/api/v1',
  keycloak: {
    url: window.matrizConfig?.keycloakUrl ?? 'http://localhost:8180',
    realm: 'matriz',
    clientId: 'matriz-frontend',
  },
} as const;
