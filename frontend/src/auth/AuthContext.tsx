import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import type { PropsWithChildren } from 'react';
import { apiRequest, setAccessToken } from '../api/httpClient';
import type { AuthSession } from '../models/api';

interface AuthContextValue {
  session: AuthSession | null;
  initializing: boolean;
  login(username: string, password: string): Promise<void>;
  logout(): Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren): JSX.Element {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [initializing, setInitializing] = useState(true);

  const applySession = useCallback((next: AuthSession | null) => {
    setSession(next);
    setAccessToken(next?.accessToken ?? null);
  }, []);

  useEffect(() => {
    apiRequest<AuthSession>('/auth/refresh', { method: 'POST' }, false)
      .then(applySession)
      .catch(() => applySession(null))
      .finally(() => setInitializing(false));
  }, [applySession]);

  const login = useCallback(async (username: string, password: string) => {
    const next = await apiRequest<AuthSession>(
      '/auth/login',
      { method: 'POST', body: JSON.stringify({ username, password }) },
      false,
    );
    applySession(next);
  }, [applySession]);

  const logout = useCallback(async () => {
    try {
      await apiRequest<void>('/auth/logout', { method: 'POST' }, false);
    } finally {
      applySession(null);
    }
  }, [applySession]);

  const value = useMemo(() => ({ session, initializing, login, logout }), [session, initializing, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe utilizarse dentro de AuthProvider');
  return context;
}
