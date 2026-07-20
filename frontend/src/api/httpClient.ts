import type { ApiErrorResponse, ApiResponse, AuthSession } from '../models/api';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

let accessToken: string | null = null;
let refreshInFlight: Promise<string | null> | null = null;

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

async function refreshAccessToken(): Promise<string | null> {
  if (!refreshInFlight) {
    refreshInFlight = fetch(`${API_BASE_URL}/auth/refresh`, {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
    })
      .then(async (response) => {
        if (!response.ok) return null;
        const envelope = (await response.json()) as ApiResponse<AuthSession>;
        setAccessToken(envelope.data.accessToken);
        return envelope.data.accessToken;
      })
      .finally(() => {
        refreshInFlight = null;
      });
  }
  return refreshInFlight;
}

export async function apiRequest<T>(path: string, init: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(init.headers);
  if (!headers.has('Content-Type') && init.body) headers.set('Content-Type', 'application/json');
  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...init,
    headers,
    credentials: 'include',
  });

  if (response.status === 401 && retry && !path.startsWith('/auth/')) {
    const refreshed = await refreshAccessToken();
    if (refreshed) return apiRequest<T>(path, init, false);
  }

  if (!response.ok) {
    let error: ApiErrorResponse | undefined;
    try {
      error = (await response.json()) as ApiErrorResponse;
    } catch {
      error = undefined;
    }
    throw new ApiClientError(error?.message ?? `Error HTTP ${response.status}`, response.status, error);
  }

  const envelope = (await response.json()) as ApiResponse<T>;
  return envelope.data;
}

export class ApiClientError extends Error {
  constructor(
    message: string,
    public readonly status: number,
    public readonly detail?: ApiErrorResponse,
  ) {
    super(message);
  }
}
