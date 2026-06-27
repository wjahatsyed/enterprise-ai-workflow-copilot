const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

export type ApiError = {
  message?: string;
  status?: number;
};

export function getToken(): string | null {
  return localStorage.getItem('accessToken');
}

export function setToken(token: string): void {
  localStorage.setItem('accessToken', token);
}

export function clearToken(): void {
  localStorage.removeItem('accessToken');
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const headers = new Headers(options.headers);
  const token = getToken();

  if (!headers.has('Content-Type') && options.body) {
    headers.set('Content-Type', 'application/json');
  }

  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers
  });

  if (!response.ok) {
    let apiError: ApiError = {};
    try {
      apiError = await response.json();
    } catch {
      apiError = { message: response.statusText, status: response.status };
    }

    if (response.status === 401) {
      clearToken();
    }

    throw new Error(apiError.message ?? `Request failed with ${response.status}`);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  try {
    return JSON.parse(text) as T;
  } catch {
    return text as T;
  }
}
