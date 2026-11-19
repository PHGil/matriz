import { readFileSync } from 'node:fs';

const realm = JSON.parse(readFileSync(new URL('./matriz-realm.json', import.meta.url), 'utf8'));
realm.sslRequired = 'all';
realm.clients = realm.clients.filter((client) => client.clientId !== 'matriz-system-tests');
realm.users = realm.users.filter((user) => user.serviceAccountClientId === 'matriz-backend');

const frontend = realm.clients.find((client) => client.clientId === 'matriz-frontend');
frontend.rootUrl = '${MATRIZ_FRONTEND_URL}';
frontend.redirectUris = ['${MATRIZ_FRONTEND_URL}/*'];
frontend.webOrigins = ['${MATRIZ_FRONTEND_URL}'];
frontend.attributes['post.logout.redirect.uris'] = '${MATRIZ_FRONTEND_URL}/*';

process.stdout.write(JSON.stringify(realm, null, 2));
