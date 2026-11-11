// Demo accounts of the Keycloak realm import. The password only comes from the environment (.env).
export type DemoUser = 'ana' | 'bruno' | 'carla';

export const DISPLAY_NAME: Record<DemoUser, string> = {
  ana: 'Ana Silva',
  bruno: 'Bruno Costa',
  carla: 'Carla Dias',
};

export function demoPassword(): string {
  const password = process.env['MATRIZ_DEMO_PASSWORD'];
  if (!password) {
    throw new Error('MATRIZ_DEMO_PASSWORD is not set (see README, "Tests")');
  }
  return password;
}
