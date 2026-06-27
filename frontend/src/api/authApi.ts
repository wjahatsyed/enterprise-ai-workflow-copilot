import { apiRequest } from './apiClient';

export type LoginResponse = {
  accessToken: string;
  tokenType: string;
  userId: string;
  tenantId: string;
  email: string;
  role: string;
};

export function login(email: string): Promise<LoginResponse> {
  return apiRequest<LoginResponse>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email })
  });
}
