import client from './client';
import type { LoginRequest, LoginResponse, SignupRequest, SignupResponse, MeResponse } from '../types/auth';

export const authApi = {
    signup: (data: SignupRequest) => client.post<SignupResponse>('/v1/auth/signup', data),
    login: (data: LoginRequest) => client.post<LoginResponse>('/v1/auth/login', data),
    logout: () => client.post('/v1/auth/logout'),
    me: () => client.get<MeResponse>('/v1/me'),
};
