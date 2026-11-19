import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const local = JSON.parse(readFileSync(new URL('./matriz-realm.json', import.meta.url), 'utf8'));
const production = JSON.parse(
  execFileSync(process.execPath, [new URL('./production-realm.mjs', import.meta.url).pathname], {
    encoding: 'utf8',
  }),
);

test('production imports only the user-sync service account and its minimum role', () => {
  assert.equal(production.sslRequired, 'all');
  assert.equal(production.users.length, 1);
  assert.equal(production.users[0].serviceAccountClientId, 'matriz-backend');
  assert.deepEqual(production.users[0].clientRoles, { 'realm-management': ['view-users'] });
  assert.ok(!JSON.stringify(production).includes('MATRIZ_DEMO_PASSWORD'));
});

test('production uses exact configurable frontend origins with PKCE and no password grants', () => {
  assert.deepEqual(
    production.clients.map((client) => client.clientId),
    ['matriz-frontend', 'matriz-backend'],
  );
  assert.ok(production.clients.every((client) => !client.directAccessGrantsEnabled));
  const frontend = production.clients[0];
  assert.equal(frontend.rootUrl, '${MATRIZ_FRONTEND_URL}');
  assert.deepEqual(frontend.redirectUris, ['${MATRIZ_FRONTEND_URL}/*']);
  assert.deepEqual(frontend.webOrigins, ['${MATRIZ_FRONTEND_URL}']);
  assert.equal(frontend.attributes['post.logout.redirect.uris'], '${MATRIZ_FRONTEND_URL}/*');
  assert.equal(frontend.attributes['pkce.code.challenge.method'], 'S256');
  assert.equal(production.clients[1].secret, '${MATRIZ_BACKEND_CLIENT_SECRET}');
});

test('cloud generation preserves approved session rules and leaves local development unchanged', () => {
  assert.equal(production.accessTokenLifespan, local.accessTokenLifespan);
  assert.equal(production.ssoSessionIdleTimeout, local.ssoSessionIdleTimeout);
  assert.equal(production.ssoSessionMaxLifespan, local.ssoSessionMaxLifespan);
  assert.equal(production.registrationAllowed, false);
  assert.equal(production.resetPasswordAllowed, false);
  assert.equal(local.clients.length, 3);
  assert.equal(local.users.length, 5);
  assert.deepEqual(local.clients[0].redirectUris, ['http://localhost:4200/*']);
});
