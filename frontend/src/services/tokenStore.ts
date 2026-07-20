import type { AuthSession } from '../models/api';

const KEY = 'ropa-lista-session';

export const tokenStore = {
  read(): AuthSession | null {
    const raw = localStorage.getItem(KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as AuthSession;
    } catch {
      localStorage.removeItem(KEY);
      return null;
    }
  },
  write(session: AuthSession): void {
    localStorage.setItem(KEY, JSON.stringify(session));
  },
  clear(): void {
    localStorage.removeItem(KEY);
  }
};
