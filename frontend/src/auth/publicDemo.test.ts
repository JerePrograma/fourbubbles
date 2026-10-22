import { describe, expect, it, vi } from 'vitest';
import { publicDemoCredentials, readOnlyDemoSession } from './publicDemo';

const publicEnvironment = {
  VITE_PUBLIC_DEMO_ENABLED: 'true',
  VITE_PUBLIC_DEMO_USERNAME: 'demo',
  VITE_PUBLIC_DEMO_PASSWORD: 'intentionally-public-viewer-test-password',
};

describe('explicit public demo build', () => {
  it('keeps ordinary builds and explicitly disabled builds without demo access', () => {
    expect(publicDemoCredentials({})).toBeNull();
    expect(publicDemoCredentials({ ...publicEnvironment, VITE_PUBLIC_DEMO_ENABLED: 'false' })).toBeNull();
  });

  it('requires the explicit flag and valid non-administrator viewer credentials', () => {
    expect(publicDemoCredentials(publicEnvironment)?.username).toBe('demo');
    expect(() => publicDemoCredentials({ ...publicEnvironment, VITE_PUBLIC_DEMO_ENABLED: 'yes' })).toThrow();
    expect(() => publicDemoCredentials({ ...publicEnvironment, VITE_PUBLIC_DEMO_USERNAME: 'demo-admin' })).toThrow();
    expect(() => publicDemoCredentials({ ...publicEnvironment, VITE_PUBLIC_DEMO_PASSWORD: 'short' })).toThrow();
  });
});

describe('public demo session boundary', () => {
  it('accepts only the report viewer role without revoking a valid session', async () => {
    const session = { roles: ['REPORT_VIEWER'], accessToken: 'synthetic-viewer-token' };
    const revoke = vi.fn();
    await expect(readOnlyDemoSession(async () => session, revoke)).resolves.toBe(session);
    expect(revoke).not.toHaveBeenCalled();
  });

  it.each([['ADMIN'], ['OPERATOR'], ['REPORT_VIEWER', 'ADMIN'], []])(
    'revokes and rejects an unexpected or privileged session %j', async (...roles) => {
      const revoke = vi.fn().mockResolvedValue(undefined);
      await expect(readOnlyDemoSession(async () => ({ roles }), revoke)).rejects.toThrow('demostración');
      expect(revoke).toHaveBeenCalledOnce();
    },
  );
});
