import type { ApiEnvelope, ApiFailure, AuthSession } from '../models/api';
import { tokenStore } from './tokenStore';

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';
let refreshPromise: Promise<AuthSession> | null = null;

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly code?: string,
    readonly fieldErrors?: Record<string, string>
  ) {
    super(message);
  }
}

async function refreshSession(): Promise<AuthSession> {
  const current = tokenStore.read();
  if (!current) throw new ApiError('La sesión no existe', 401, 'NO_SESSION');

  const response = await fetch(`${API_BASE}/v1/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken: current.refreshToken })
  });

  if (!response.ok) {
    tokenStore.clear();
    throw new ApiError('La sesión expiró', response.status, 'SESSION_EXPIRED');
  }

  const envelope = (await response.json()) as ApiEnvelope<AuthSession>;
  tokenStore.write(envelope.data);
  return envelope.data;
}

async function authenticatedFetch(path: string, init: RequestInit, retry = true): Promise<Response> {
  const session = tokenStore.read();
  const headers = new Headers(init.headers);
  headers.set('Content-Type', 'application/json');
  if (session?.accessToken) headers.set('Authorization', `Bearer ${session.accessToken}`);

  const response = await fetch(`${API_BASE}${path}`, { ...init, headers });
  if (response.status !== 401 || !retry || !session) return response;

  refreshPromise ??= refreshSession().finally(() => {
    refreshPromise = null;
  });
  await refreshPromise;
  return authenticatedFetch(path, init, false);
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const response = await authenticatedFetch(path, init);
  if (!response.ok) {
    let failure: ApiFailure | undefined;
    try {
      failure = (await response.json()) as ApiFailure;
    } catch {
      // The proxy may have returned a non-JSON failure.
    }
    throw new ApiError(
      failure?.message ?? `Error HTTP ${response.status}`,
      response.status,
      failure?.code,
      failure?.fieldErrors
    );
  }

  if (response.status === 204) return undefined as T;
  const envelope = (await response.json()) as ApiEnvelope<T>;
  return envelope.data;
}

export async function login(email: string, password: string): Promise<AuthSession> {
  const response = await fetch(`${API_BASE}/v1/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  });
  if (!response.ok) throw new ApiError('Credenciales inválidas', response.status, 'LOGIN_FAILED');
  const envelope = (await response.json()) as ApiEnvelope<AuthSession>;
  tokenStore.write(envelope.data);
  return envelope.data;
}
