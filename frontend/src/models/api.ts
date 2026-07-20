export interface ApiEnvelope<T> {
  timestamp: string;
  data: T;
}

export interface ApiFailure {
  timestamp: string;
  code: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  userId: string;
  displayName: string;
  role: string;
}
