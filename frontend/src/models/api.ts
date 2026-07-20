export interface ApiResponse<T> {
  success: boolean;
  data: T;
  timestamp: string;
}

export interface ApiErrorResponse {
  success: false;
  code: string;
  message: string;
  status: number;
  path: string;
  timestamp: string;
  violations: Array<{ field: string; message: string; rejectedValue?: unknown }>;
}

export interface AuthSession {
  accessToken: string;
  tokenType: 'Bearer';
  expiresInSeconds: number;
  username: string;
  roles: string[];
}
