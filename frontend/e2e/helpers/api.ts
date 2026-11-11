import { expect, request } from '@playwright/test';
import { DemoUser, demoPassword } from '../test-data/users';

const API_URL = process.env['MATRIZ_API_URL'] ?? 'http://localhost:8080/api/v1';
const TOKEN_URL = `${process.env['KEYCLOAK_URL'] ?? 'http://localhost:8180'}/realms/matriz/protocol/openid-connect/token`;

async function accessToken(user: DemoUser): Promise<string> {
  const api = await request.newContext();
  const response = await api.post(TOKEN_URL, {
    form: {
      grant_type: 'password',
      client_id: 'matriz-system-tests',
      username: user,
      password: demoPassword(),
    },
  });
  expect(response.ok()).toBeTruthy();
  const { access_token } = await response.json();
  await api.dispose();
  return access_token;
}

// Creates test data through the real API so the UI test starts from a known state.
export async function createProject(user: DemoUser, name: string): Promise<string> {
  const api = await request.newContext({
    baseURL: API_URL + '/',
    extraHTTPHeaders: { Authorization: `Bearer ${await accessToken(user)}` },
  });
  const response = await api.post('projects', { data: { name } });
  expect(response.status()).toBe(201);
  const { id } = await response.json();
  await api.dispose();
  return id;
}
