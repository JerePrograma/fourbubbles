import { createContext, useContext, useMemo, useState, type ReactNode } from 'react';
import type { AuthSession } from '../models/api';
import { login as loginRequest } from '../services/apiClient';
import { tokenStore } from '../services/tokenStore';

interface AuthContextValue {
  session: AuthSession | null;
  login(email: string, password: string): Promise<void>;
  logout(): void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(() => tokenStore.read());

  const value = useMemo<AuthContextValue>(() => ({
    session,
    async login(email, password) {
      setSession(await loginRequest(email, password));
    },
    logout() {
      tokenStore.clear();
      setSession(null);
    }
  }), [session]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext);
  if (!value) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return value;
}
