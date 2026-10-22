export interface PublicDemoCredentials {
  username: string;
  password: string;
}

export function publicDemoCredentials(environment: Record<string, unknown>): PublicDemoCredentials | null {
  const enabled = environment.VITE_PUBLIC_DEMO_ENABLED;
  if (enabled === undefined || enabled === '' || enabled === 'false') return null;
  if (enabled !== 'true') throw new Error('VITE_PUBLIC_DEMO_ENABLED must be true or false');

  const username = environment.VITE_PUBLIC_DEMO_USERNAME;
  const password = environment.VITE_PUBLIC_DEMO_PASSWORD;
  if (typeof username !== 'string' || !/^[A-Za-z0-9_.@-]{3,100}$/.test(username) || /admin/i.test(username)) {
    throw new Error('Public demo requires an explicit viewer username');
  }
  if (typeof password !== 'string' || password.length < 24 || password.length > 72) {
    throw new Error('Public demo requires its intentionally public viewer password');
  }
  return { username, password };
}

/** Do not install a privileged session when a public demo account is misconfigured. */
export async function readOnlyDemoSession<T extends { roles: string[] }>(
  authenticate: () => Promise<T>,
  revoke: () => Promise<unknown>,
): Promise<T> {
  const next = await authenticate();
  if (next.roles.length !== 1 || next.roles[0] !== 'REPORT_VIEWER') {
    await revoke().catch(() => undefined);
    throw new Error('El acceso de demostración no está disponible en este momento.');
  }
  return next;
}
